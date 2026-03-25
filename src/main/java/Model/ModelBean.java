/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Tables.Appstats;
import Tables.Bid;
import Tables.BidId;
import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Currency;
import Tables.Participant;
import Tables.Penaltiespl;
import Tables.PenaltiesplId;
import Tables.Planetregion;
import Tables.Pointscount;
import Tables.PointscountId;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import View.MailerAgent;
import java.io.IOException;
import static java.lang.Math.signum;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.LongType;

/**
 *
 * @author Jorge
 */
@Named( value = "modelBean" )
@ApplicationScoped

public class ModelBean
  implements ModelForView {

  private long numSesionesActivas = 0;
  private long numUsuariosActivos = 0;

  private final String appURL = resolveAppURL();

  private List<Regatta> regattas;

  private boolean regattasSorted;
  private List<Penaltiespl> penaltiesplList;
  private long n;
  private final Chronometer chronometer = new Chronometer();

  private final Semaphore eMailSender = new Semaphore( 1,
                                                       true );
  private List<Model.Sesion> listaSesiones = new ArrayList<>();
  // Recalculation queue policy: 1 running job + at most 1 pending job.
  private final ExecutorService recalculationExecutor = Executors
    .newSingleThreadExecutor();
  private final Object recalculationLock = new Object();
  private boolean recalculationRunning = false;
  private boolean recalculationQueued = false;
  private ProgressBar queuedProgressBar = null;

  public void requestRecalculateRegattaPenalties( ProgressBar p ) {
    boolean submitWorker = false;
    synchronized( recalculationLock ) {
      if( recalculationRunning ) {
        recalculationQueued = true;
        queuedProgressBar = p;
        return;
      }
      recalculationRunning = true;
      queuedProgressBar = p;
      submitWorker = true;
    }

    if( submitWorker ) {
      recalculationExecutor.submit( this::runQueuedRegattaPenaltiesRecalculation );
    }
  }

  private void runQueuedRegattaPenaltiesRecalculation() {
    final ProgressBar noOpProgressBar = ( x ) -> {
    };
    while( true ) {
      ProgressBar currentProgressBar;
      synchronized( recalculationLock ) {
        currentProgressBar = queuedProgressBar;
        queuedProgressBar = null;
      }

      if( currentProgressBar == null ) {
        currentProgressBar = noOpProgressBar;
      }

      try {
        recalculateRegattaPenalties( currentProgressBar );
      } catch( RuntimeException ex ) {
        System.out.println(
          new Date() + " !!! " + "recalculateRegattaPenalties failed: " + ex
            .getMessage() );
      }

      synchronized( recalculationLock ) {
        if( !recalculationQueued ) {
          recalculationRunning = false;
          return;
        }
        recalculationQueued = false;
      }
    }
  }

  @PreDestroy
  public void destroyModelBean() {
    recalculationExecutor.shutdownNow();
    U_HibernateUtil.shutdown();
  }

  public synchronized void checkDNFs( Regatta regatta ) {

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
          + " where r.regatta.id="
          + regatta.getId() );
    List<Registration> registrations = q.list();

    registrations.sort( ( a, b )
      -> compareRegistrationRacePos( a,
                                     b ) );

    int minutes = regatta.getMinutesRace();
    Registration winner = registrations.get( 0 );
    double winnerSpeedQualifying = regatta.getVariant().getLength()
                                   / ( winner.getSecondsLap() / 60.0 / 60.0 );
    double winnerSpeedRace = regatta.getVariant().getLength()
                             * winner.getLapsRace()
                             / ( minutes / 60.0 );
    double fraction = winnerSpeedRace / winnerSpeedQualifying;

    for( Registration r
         : registrations ) {
      double speedRace = r.getLapsRace() * r.getRegatta().getVariant()
             .getLength()
                         / ( minutes / 60.0 );
      double speedQualifying = regatta.getVariant().getLength()
                               / ( r.getSecondsLap() / 60.0 / 60.0 );
      if( speedQualifying * fraction * 0.9 > speedRace ) {
        r.setStatus( RegistrationStatus.DIDNOTFINISH );
      }
    }

    t.commit();
    s.close();
  }

  public synchronized String getAppURL() {
    return appURL;
  }

  private String resolveAppURL() {
    String configured = System.getProperty( "topracing.app.url" );
    if( configured == null || configured.isBlank() ) {
      configured = System.getenv( "TOPRACING_APP_URL" );
    }
    if( configured == null || configured.isBlank() ) {
      configured = "148.206.179.34:8080/topracingwebapp/";
    }
    return configured.endsWith( "/" )
           ? configured
           : configured + "/";
  }

  public synchronized Bid getBidById( BidId _bidId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Bid as b"
          + " join fetch b.registration as r "
          + " join fetch r.participantByIdBuyer"
          + " where b.id.id_participant = " + _bidId.getIdParticipant()
          + " and b.id.id_registration = " + _bidId.getIdRegistration()
        );
    List<Bid> bidList = q.list();

    t.commit();
    s.close();

    return bidList.get( 0 );
  }

  @Override
  public synchronized List<Currency> getCurrencies() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

      Query q = s.createQuery(
          "from Tables.Currency as currency"
         );
    List<Currency> currencyList = q.list();

    t.commit();
    s.close();

    return currencyList;
  }

  public synchronized Participant getValidParticipant( Participant user ) {
    if( user == null
        || user.getId() == null
           && user.getEmail() == null ) {

      return null;
    }

    Participant u;
    u = this.getParticipantByEmail( user );
    if( u == null ) {
      u = this.getParticipantByEMailKey( user.getEmailKey() );
    }

    if( u == null
        || u.getPassword() == null
        || !u.getPassword()
        .equals( user.getPassword() )
        || u.getDefaulter() > 0 ) {
      return null;
    }

    return u;
  }

  @Override
  public synchronized List<Pointscount> getPointscounts( int periodLevel,
                                                          int tracksetLevel ) {
      SessionFactory sf = U_HibernateUtil.getSessionFactory();
      Session s = sf.openSession();
      Transaction t = null;
      try {
        t = s.beginTransaction();

        Query q = s.createQuery(
              "from Tables.Pointscount as pointscount"
              + " where pointscount.id.levelPeriod=" + periodLevel
              + " and pointscount.id.levelTrackset=" + tracksetLevel
            );
        List<Pointscount> pointscountList = q.list();

        t.commit();
        return pointscountList;
      } catch( RuntimeException ex ) {
        if( t != null && t.isActive() ) {
          t.rollback();
        }
        throw ex;
      } finally {
        s.close();
      }
  }

  @Override
  public synchronized String getVariantName( Variant variant ) {
    return variant.getVenue().getProvinceregion()
      .getProvince().getCountryregion()
      .getCountry().getPlanetregion().getName()
           + " @ " + variant.getVenue().getProvinceregion()
        .getProvince().getCountryregion()
        .getCountry().getName()
           + " @ " + variant.getVenue().getProvinceregion()
        .getProvince().getCountryregion().getName()
           + " @ " + variant.getVenue().getProvinceregion()
        .getProvince().getName()
           + " @ " + variant.getVenue().getProvinceregion()
        .getName()
           + " @ " + variant.getVenue().getName()
           + " @ " + variant.getName();
  }

  @Override
  public synchronized String getVenueName( Venue venue ) {
    return venue.getProvinceregion()
      .getProvince().getCountryregion()
      .getCountry().getPlanetregion().getName()
           + " @ " + venue.getProvinceregion()
        .getProvince().getCountryregion()
        .getCountry().getName()
           + " @ " + venue.getProvinceregion()
        .getProvince().getCountryregion().getName()
           + " @ " + venue.getProvinceregion()
        .getProvince().getName()
           + " @ " + venue.getProvinceregion().getName()
           + " @ " + venue.getName();
  }

  @Override
  public synchronized String getProvinceregionName(
    Provinceregion provinceregion ) {
    return provinceregion.getProvince()
      .getCountryregion().getCountry()
      .getPlanetregion().getName()
           + " @ " + provinceregion.getProvince()
        .getCountryregion().getCountry().getName()
           + " @ " + provinceregion.getProvince()
        .getCountryregion().getName()
           + " @ " + provinceregion.getProvince().getName()
           + " @ " + provinceregion.getName();
  }

  @Override
  public synchronized String getProvinceName(
    Province province ) {
    return province.getCountryregion()
      .getCountry().getPlanetregion().getName()
           + " @ " + province.getCountryregion()
        .getCountry().getName()
           + " @ " + province.getCountryregion().getName()
           + " @ " + province.getName();
  }

  @Override
  public synchronized String getCountryregionName(
    Countryregion countryregion ) {
    return countryregion.getCountry()
      .getPlanetregion().getName()
           + " @ " + countryregion.getCountry().getName()
           + " @ " + countryregion.getName();
  }

  @Override
  public synchronized String getCountryName(
    Country country ) {
    return country.getPlanetregion().getName()
           + " @ " + country.getName();
  }

  @Override
  public synchronized String getPlanetregionName(
    Planetregion planetregion ) {
    return planetregion.getName();
  }

  @Override
  public synchronized String getPeriodName( int level,
                                            long period ) {
    String name;

    switch( level ) {
      case LevelPeriod.CONTINUOUS:
        name = "-";
        break;

      case LevelPeriod.DECADE:
        name = ( 2000 + period * 10 ) + "'s";
        break;

      case LevelPeriod.YEAR_OF_DECADE:
        name = "y" + ( 2000 + period );
        break;

      case LevelPeriod.SEASON_OF_YEAR:
        name = ( 2000 + period / 10 )
               + "-s" + ( period % 10 );
        break;

      case LevelPeriod.MONTH_OF_SEASON:
        name = ( 2000 + period / 100 )
               + "-s" + ( ( period / 10 ) % 10 )
               + "-m" + ( period % 10 );
        break;

      case LevelPeriod.WEEK_OF_MONTH:
        name = ( 2000 + period / 1000L )
               + "-s" + ( ( period / 100 ) % 10 )
               + "-m" + ( ( period / 10 ) % 10 )
               + "-w" + ( period % 10 );
        break;
      default:
        name = ( 2000 + period / 10000L )
               + "-s" + ( ( period / 1000 ) % 10 )
               + "-m" + ( ( period / 100 ) % 10 )
               + "-w" + ( ( period / 10 ) % 10 )
               + "-d" + ( period % 10 );
    }
    return name;
  }

  @Override
  public synchronized List<Registration> getRegistrations() {

      SessionFactory sf = U_HibernateUtil.getSessionFactory();
      Session s = sf.openSession();
      Transaction t = null;
      try {
        t = s.beginTransaction();

        Query q = s.createQuery(
              "from Tables.Registration as r"
              + " join fetch r.car"
              + " join fetch r.participantByIdDriver"
              + " join fetch r.participantByIdOwner"
              + " join fetch r.participantByIdBuyer"
              + " join fetch r.regatta as regatta"
              + " join fetch regatta.participant"
              + " join fetch regatta.variant as variant"
              + " join fetch regatta.currency as currency"
              + " join fetch variant.venue as venue"
              + " join fetch venue.provinceregion as provinceregion"
              + " join fetch provinceregion.province as province"
              + " join fetch province.countryregion as countryregion"
              + " join fetch countryregion.country as country"
              + " join fetch country.planetregion"
              + " where r.status=1"
            );
        List<Registration> registrationsList = q.list();

        t.commit();
        return registrationsList;
      } catch( RuntimeException ex ) {
        if( t != null && t.isActive() ) {
          t.rollback();
        }
        throw ex;
      } finally {
        s.close();
      }
  }

  @Override
  public synchronized List<Registration> getRegattaRegistrations(
    Regatta regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
          + " join fetch r.car"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdBuyer"
          + " join fetch r.regatta as rega"
          + " join fetch rega.currency"
          + " where r.regatta.id="
          + regatta.getId()
        );
    List<Registration> registrationsList = q.list();

    t.commit();
    s.close();

    return registrationsList;
  }

  @Override
  public synchronized String getParticipantFullName( Participant participant ) {

    if( participant == null ) {
      return "-";
    }
    return ( participant.isConfirmed()
             ? ""
             : "*" )
           + participant.getNamesFamily() + ", "
           + participant.getNamesGiven();
  }

  @Override
  public synchronized String getTimeZone() {
    return "America/Mexico_City";
  }

  @Override
  public synchronized List<Participant> getParticipants() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Participant as p"
          + " join fetch p.venue"
        );
    List<Participant> participants = q.list();

    t.commit();
    s.close();

    return participants;
  }

  @Override
  public synchronized Participant getParticipantByEmail( Participant participant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Participant as p"
          + " join fetch p.venue as v"
          + " join fetch v.provinceregion as pr"
          + " join fetch pr.province as prov"
          + " join fetch prov.countryregion as cr"
          + " join fetch cr.country as c"
          + " join fetch c.planetregion"
          + " where p.email ='"
          + participant.getEmail()
          + "'"
        );
    List<Participant> participants = q.list();

    t.commit();
    s.close();

    Participant p = null;
    Crypto crypto = new Crypto();
    if( participants.size() > 0 ) {
      p = participants.get( 0 );
      p.setPassword( crypto.decryptString( p.getPassword() ) );
    }

    return p;
  }

  @Override
  public synchronized Participant getParticipantById( Participant participant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Participant as p"
          + " join fetch p.venue as v"
          + " join fetch v.provinceregion as pr"
          + " join fetch pr.province as prov"
          + " join fetch prov.countryregion as cr"
          + " join fetch cr.country as c"
          + " join fetch c.planetregion"
          + " where p.id ='"
          + participant.getId()
          + "'"
        );
    List<Participant> participants = q.list();

    t.commit();
    s.close();

    Participant p = null;
    Crypto crypto = new Crypto();
    if( participants.size() == 1 ) {
      p = participants.get( 0 );
      p.setPassword( crypto.decryptString( p.getPassword() ) );
    }

    return p;
  }

  @Override
  public synchronized Regatta getRegattaById( long regattaId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Regatta as regatta"
          + " join fetch regatta.participant"
          + " join fetch regatta.variant as variant"
          + " join fetch regatta.currency as currency"
          + " join fetch variant.venue as venue"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
          + " where regatta.id ="
          + regattaId
        );
    regattas = q.list();

    t.commit();
    s.close();

    return regattas.size() > 0
           ? regattas.get( 0 )
           : null;
  }

  @Override
  public synchronized Variant getVariantById( long variantId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Variant as va"
                             + " join fetch va.venue as ve"
                             + " join fetch ve.provinceregion as pr"
                             + " join fetch ve.participant as ow"
                             + " join fetch pr.province as p"
                             + " join fetch p.countryregion as cr"
                             + " join fetch cr.country as c"
                             + " join fetch c.planetregion"
                             + " where va.id = "
                             + variantId );
    Variant v = ( (List<Variant>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return v;
  }

  @Override
  public synchronized Venue getVenueById( long venueId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Venue as ve"
                             + " join fetch ve.provinceregion as pr"
                             + " join fetch ve.participant as ow"
                             + " join fetch pr.province as p"
                             + " join fetch p.countryregion as cr"
                             + " join fetch cr.country as c"
                             + " join fetch c.planetregion"
                             + " where ve.id = "
                             + venueId );
    Venue v = ( (List<Venue>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return v;
  }

  @Override
  public synchronized Provinceregion getProvinceregionById(
    long provinceregionId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Provinceregion as pr"
                             + " join fetch pr.province as p"
                             + " join fetch p.countryregion as cr"
                             + " join fetch cr.country as c"
                             + " join fetch c.planetregion"
                             + " where pr.id = "
                             + provinceregionId );
    Provinceregion pr = ( (List<Provinceregion>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return pr;
  }

  @Override
  public synchronized Province getProvinceById( long provinceId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Province as p"
                             + " join fetch p.countryregion as cr"
                             + " join fetch cr.country as c"
                             + " join fetch c.planetregion"
                             + " where p.id = "
                             + provinceId );
    Province p = ( (List<Province>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return p;
  }

  @Override
  public synchronized Countryregion getCountryregionById( long countryregionId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Countryregion as cr"
                             + " join fetch cr.country as c"
                             + " join fetch c.planetregion"
                             + " where cr.id = "
                             + countryregionId );
    Countryregion cr = ( (List<Countryregion>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return cr;
  }

  @Override
  public synchronized Country getCountryById( long countryId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Country as c"
                             + " join fetch c.planetregion"
                             + " where c.id = "
                             + countryId );
    Country c = ( (List<Country>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return c;
  }

  @Override
  public synchronized Planetregion getPlanetregionById( long planetregionId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Planetregion as pr"
                             + " where pr.id = "
                             + planetregionId );
    Planetregion pr = ( (List<Planetregion>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return pr;
  }

  @Override
  public synchronized Car getCarById( long carId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Car as c"
                             + " join fetch c.participant"
                             + " where c.id = "
                             + carId
        );

    Car car = ( (List<Car>) q.list() ).get( 0 );

    t.commit();
    s.close();

    return car;
  }

  @Override
  public synchronized Registration getRegistrationById( long registrationId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
          + " join fetch r.car as c"
          + " join fetch c.participant as ow"
          + " join fetch r.regatta"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdBuyer"
          + " where r.id = "
          + registrationId
        );

    List<Registration> rList = q.list();

    t.commit();
    s.close();

    return rList.size() > 0
           ? rList.get( 0 )
           : null;
  }

  public synchronized Pointscount getPointscountById(
    PointscountId pointscountId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Pointscount pointscount = (Pointscount) s.get( Pointscount.class,
                                                   pointscountId );

    t.commit();
    s.close();

    return pointscount;
  }

  @Override
  public synchronized List<Regatta> getRegattas() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Regatta as regatta"
          + " join fetch regatta.participant"
          + " join fetch regatta.variant as variant"
          + " join fetch regatta.currency as currency"
          + " join fetch variant.venue as venue"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion" );
    regattas = q.list();

    t.commit();
    s.close();

    return regattas;
  }

  public synchronized void setCarBuyerAsDefaulter( Registration r ) {
    Participant seller = r.getParticipantByIdOwner();

    // set buyer as defaulter
    Participant buyer = r.getParticipantByIdBuyer();
    if( seller.getId().longValue()
        == buyer.getId().longValue() ) {
      // avoid self defaulting by accident
      return;
    }

    buyer = getParticipantByEmail( buyer );
    buyer.setDefaulter(
      buyer.getDefaulter() + 1
    );
    save( buyer,
          false );

    BidId bidId = new BidId(
          buyer.getId(),
          r.getId() );

    // disable bid for recalculation of auction winner
    Bid bid = getBidById( bidId );
    bid.setStatus(
      bid.getStatus() + 1
    );
    save( bid );

    // assign new auction winner and efficiency positions
    this.assignRegattaEfficiencyPositions( r.getRegatta() );
    this.assignRegattaIndividualEfficiencyPrize( r.getRegatta() );

  }

  public synchronized void setCarSellerAsDefaulter( Registration r ) {

    // Disqualify registration that uses defaulter's car
    r.setStatus( RegistrationStatus.DISQUALIFIED );
    save( r );

    // set car owner as defaulter
    Participant seller = r.getParticipantByIdOwner();
    if( r.getCar().getParticipant().getId().longValue()
        == seller.getId().longValue() ) {
      // avoid self defaulting by accident
      return;
    }

    seller.setDefaulter(
      seller.getDefaulter() + 1
    );
    save( seller,
          false );

    // revert car owner change
    r.getCar().setParticipant( seller );
    save( r.getCar() );

    // recalculate efficiency positions
    this.assignRegattaEfficiencyPositions( r.getRegatta() );
    this.assignRegattaIndividualEfficiencyPrize( r.getRegatta() );
  }

  public synchronized void assignRegattaSpeedPos( long regattaId ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r where r.regatta.id="
          + regattaId );
    List<Registration> registrations = q.list();

    registrations.sort( ( a, b )
      -> compareRegistrationSecondsLap( a,
                                        b ) );
    short i = 1;
    for( Registration r
         : registrations ) {
      r.setPosSpeed( i++ );
    }

    t.commit();
    s.close();
  }

  private int compareRegistrationSecondsLap( Registration ra,
                                             Registration rb ) {
    if( ra.getStatus() != rb.getStatus() ) {
      return (int) Math.signum( ra.getStatus() - rb.getStatus() );
    }

    double laptimeA = ra.getSecondsLap();
    double laptimeB = rb.getSecondsLap();

    if( laptimeA == laptimeB ) {
      //return (int) Math.signum( ra.getPosRace() - rb.getPosRace() );
      return (int) Math.signum( ra.getId() - rb.getId() );
    }
    return (int) Math.signum( laptimeA - laptimeB );
  }

  public synchronized void assignRegattaGridPositions( long regattaId ) {

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Regatta regatta = getRegattaById( regattaId );

    Query q = s.createQuery(
          "from Tables.Registration as r where r.regatta.id="
          + regattaId );
    List<Registration> registrations = q.list();

    registrations.sort( ( a, b )
      -> compareRegistrationPointscount( a,
                                         b,
                                         regatta ) );
    int i = registrations.size();
    for( Registration r
         : registrations ) {
      r.setPosRacegrid( (short) i-- );
    }
    t.commit();
    s.close();
  }

  @Override
  public synchronized long getPeriodId( Date date,
                                        int level ) {

    Calendar calendar = new GregorianCalendar();
    calendar.setTime( date );
    int year = calendar.get( Calendar.YEAR );
    int decade = ( year / 10 ) % 100;
    int yearOfDecade = year % 10;

    int month = calendar.get( Calendar.MONTH ) + 1;
    int dayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );

    int seasonOfYear = ( month - 1 ) / 3 + 1;
    int monthOfSeason = ( month - 1 ) % 3 + 1;

    LocalDate localDate = LocalDate.of( year,
                                        month,
                                        dayOfMonth );
    int dayOfWeek = localDate.getDayOfWeek()
        .getValue();
    int shift = ( dayOfMonth - dayOfWeek + 7 ) % 7;
    if( shift == 0 ) {
      shift = 7;
    }
    int weekOfMonth = 1 + ( dayOfMonth - 1 + 7 - shift ) / 7;

    int decadeId = decade;
    int yearId = decadeId * 10 + yearOfDecade;
    int seasonId = yearId * 10 + seasonOfYear;
    int monthId = seasonId * 10 + monthOfSeason;
    int weekId = monthId * 10 + weekOfMonth;
    int dayId = weekId * 10 + dayOfWeek;

    switch( level ) {
      case LevelPeriod.CONTINUOUS:
        return 1;
      case LevelPeriod.DECADE:
        return decadeId;
      case LevelPeriod.YEAR_OF_DECADE:
        return yearId;
      case LevelPeriod.SEASON_OF_YEAR:
        return seasonId;
      case LevelPeriod.MONTH_OF_SEASON:
        return monthId;
      case LevelPeriod.WEEK_OF_MONTH:
        return weekId;
      default:
        return dayId;
    }
  }

  private long getTracksetId( Variant variant,
                              int tracksetLevel ) {
    long tracksetId = 1;

    switch( tracksetLevel ) {
      case LevelTrackset.VARIANT:
        return variant.getId();
      case LevelTrackset.VENUE:
        return variant.getVenue().getId();
      case LevelTrackset.PROVINCE_REGION:
        return variant.getVenue().getProvinceregion().getId();
      case LevelTrackset.PROVINCE:
        return variant.getVenue().getProvinceregion().getProvince().getId();
      case LevelTrackset.COUNTRY_REGION:
        return variant.getVenue().getProvinceregion().getProvince()
          .getCountryregion().getId();
      case LevelTrackset.COUNTRY:
        return variant.getVenue().getProvinceregion().getProvince()
          .getCountryregion().getCountry().getId();
      case LevelTrackset.PLANET_REGION:
        return variant.getVenue().getProvinceregion().getProvince()
          .getCountryregion().getCountry().getPlanetregion().getId();
      case LevelTrackset.PLANET:
        return 1;
    }
    return tracksetId;
  }

  private int compareRegistrationPointscount( Registration ra,
                                              Registration rb,
                                              Regatta regatta ) {

    if( ra.getStatus() != rb.getStatus() ) {
      return (int) Math.signum( ra.getStatus() - rb.getStatus() );
    }

    //
    int periodLevel = regatta.getLevelPeriod();
    long periodId = getPeriodId( regatta.getDatetime(),
                                 periodLevel );
    //
    int tracksetLevel = regatta.getLevelTrackset();
    long tracksetId = getTracksetId( regatta.getVariant(),
                                     tracksetLevel );

    PointscountId pcIdA = new PointscountId(
                  ra.getParticipantByIdDriver().getId(),
                  periodLevel,
                  periodId,
                  tracksetLevel,
                  tracksetId );
    Pointscount pcA = this.getPointscountById( pcIdA );
    double pointsA = pcA != null
                     ? pcA.getPointsSD() + pcA.getPointsRD()
                     : 0;

    PointscountId pcIdB = new PointscountId(
                  rb.getParticipantByIdDriver().getId(),
                  periodLevel,
                  periodId,
                  tracksetLevel,
                  tracksetId );
    Pointscount pcB = this.getPointscountById( pcIdB );
    double pointsB = pcB != null
                     ? pcB.getPointsSD() + pcB.getPointsRD()
                     : 0;

    return (int) Math.signum( pointsB - pointsA );
  }

  public synchronized boolean areValidRegattaSpeedResults( Regatta regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r where r.regatta.id="
          + regatta.getId() );
    List<Registration> registrations = q.list();

    t.commit();
    s.close();

    registrations.sort( ( a, b )
      -> compareRegistrationSpeedPos( a,
                                      b ) );
    int i = 0;
    for( Registration r
         : registrations ) {
      if( r.getStatus() != RegistrationStatus.OK ) {
        return i > 0; // because invalid registrations are sorted after valid ones.
      }      // If we reach this point, it means all previous registrations have passed the test
      i++;
      if( r.getSecondsLap() <= 0 ) {
        return false;
      }
    }
    return true;
  }

  private int compareRegistrationSpeedPos( Registration ra,
                                           Registration rb ) {
    if( ra.getStatus() != rb.getStatus() ) {
      return (int) Math.signum( ra.getStatus() - rb.getStatus() );
    }

    int posA = ra.getPosSpeed();
    int posB = rb.getPosSpeed();

    return posA != posB
           ? posA < posB
             ? -1
             : 1
           : 0;
  }

  public synchronized boolean areValidRegattaRaceResults( Regatta regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
            + " where r.regatta.id="
          + regatta.getId() );
    List<Registration> registrations = q.list();

    t.commit();
    s.close();

    registrations.sort( ( a, b )
      -> compareRegistrationRacePos( a,
                                     b ) );
    int i = 1;
    int prevLaps = registrations.get( 0 )
        .getLapsRace();
    for( Registration r
         : registrations ) {
      if( r.getStatus() != RegistrationStatus.OK ) {
        return true; // because invalid registrations are sorted after valid ones.
      }
      // If we reach this point, it means all previous registrations have passed the test
      if( i != r.getPosRace() ) {
        return false;
      }
      // race laps must be monotonously increasing
      if( i++ > 1 && prevLaps < r.getLapsRace() ) {
        return false;
      }
      prevLaps = r.getLapsRace();
    }
    return true;
  }

  private int compareRegistrationRacePos( Registration ra,
                                          Registration rb ) {
    if( ra.getStatus() != rb.getStatus() ) {
      return (int) Math.signum( ra.getStatus() - rb.getStatus() );
    }

    int posA = ra.getPosRace();
    int posB = rb.getPosRace();

    return posA != posB
           ? posA < posB
             ? -1
             : 1
           : 0;
  }

  public synchronized void assignRegattaIndividualFinishingPrize(
    Regatta regatta ) {

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
          + " join fetch r.car"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdBuyer"
          + " join fetch r.regatta"
            + " where r.regatta.id="
          + regatta.getId()
        );
    List<Registration> registrations = q.list();

    double totalBet = 0;
    int numOK = 0;
    for( Registration r
         : registrations ) {
      r.setPrizeFinishing( 0 );
      if( r.getStatus() == RegistrationStatus.OK ) {
        numOK++;
        totalBet += r.getBetFinishing();
      }
    }

    if( numOK > 0 ) {
      double totalWeight = 0;
      for( Registration r
           : registrations ) {
        if( r.getStatus() == RegistrationStatus.OK ) {
          totalWeight += getWeight( r );
        }
      }
      if( totalWeight > 0 ) {
        for( Registration r
             : registrations ) {
          if( r.getStatus() == RegistrationStatus.OK ) {
            r.setPrizeFinishing(
              ( regatta.getPrizeFinishing()
                + totalBet )
              * getWeight( r )
              / totalWeight );
          }
        }
      }
    }

    t.commit();
    s.close();
  }

  public synchronized double getWeight( Registration r ) {
    return r.getBetFinishing() * r.getLapsRace() / getExpectedLaps( r );
  }

  @Override
  public synchronized double getExpectedLaps( Registration r ) {
    if( r == null || r.getRegatta() == null ) {
      return 1e3;
    }
    return r.getRegatta().getMinutesRace() * 60.0 / r.getSecondsLap();
  }

  public synchronized void assignRegattaEfficiencyPositions( Regatta regatta ) {

    List<Registration> registrations = getRegattaRegistrations( regatta );

    // select auction winner for each registration
    for( Registration registration
         : registrations ) {

      // set default values for not-OK registrations
      registration.setValueAuction( 1e-10 );
      registration.setParticipantByIdBuyer(
        registration
          .getParticipantByIdOwner() );

      if( registration.getStatus() != RegistrationStatus.OK ) {
        continue;
      }

      List<Bid> bids = getBids( registration );
      bids.sort( ( a, b )
        -> compareBids( a,
                        b ) );

      // find highest and second highest bids for this registration
      Bid highestBid_1 = bids.get( 0 );
      Bid highestBid_2 = bids.get( 0 );

      for( Bid bid
           : bids ) {
        if( bid.getStatus() != 0 ) {
          continue; // discard not-OK bids
        }
        if( bid.getAmmount() >= highestBid_2.getAmmount() ) {
          if( bid.getAmmount() >= highestBid_1.getAmmount() ) {
            highestBid_2 = highestBid_1;
            highestBid_1 = bid;
          } else {
            highestBid_2 = bid;
          }
        }
      }
      // assign auction value and buyer id
      registration.setValueAuction( highestBid_2.getAmmount() );

      if( highestBid_1.getAmmount() > 0 ) {
        // set winner as buyer
        Participant winner = new Participant();
        winner.setId(
          highestBid_1
            .getId()
            .getIdParticipant()
        );
        registration
          .setParticipantByIdBuyer(
            getParticipantByEmail( winner )
          );
      }
    }

    // set efficiency positions
    regatta = getRegattaById( regatta.getId() );
    double dollarValue = regatta.getCurrency().getDollarvalue();
    boolean isMetric = regatta.getVariant().isMetric();

    // calculate slope and intercept of adjusted line in the log plot of speed( auctionValue )
    // x = speed km/hr; y = log( market_value ) in regatta currency
    int k = 1; // bycicle with
    double sumX = 32 / ( isMetric
                         ? 1
                         : 1.609 );
    double sumY = Math.log( 10000.0 / dollarValue ); // valued 10000 pesos. ln( 10000/dollar ) = 9.21
    double sumX2 = sumX * sumX;
    double sumXY = sumX * sumY;
    double x, y;

    for( Registration r
         : registrations ) {
      if( r.getStatus() == RegistrationStatus.OK ) {
        k++;
        x = regatta.getVariant().getLength() / r.getSecondsLap();
        y = Math.log( r.getValueAuction() );
        sumXY += x * y;
        sumX += x;
        sumY += y;
        sumX2 += x * x;
      }
    }
    double slope = 1;
    double intercept = 0;
    if( k > 1 ) {
      slope = ( k * sumXY - sumX * sumY ) / ( k * sumX2 - sumX * sumX );
      intercept = ( sumY - slope * sumX ) / k;
    }
    regatta.setSlope( slope );
    regatta.setIntercept( intercept );

    // efficiency is evaluated via the base cost
    for( Registration r
         : registrations ) {
      setValueBase( r,
                    regatta,
                    slope );
    }

    registrations.sort( ( a, b )
      -> compareEfficiency( a,
                            b ) );

    short i = 1;
    for( Registration r
         : registrations ) {
      if( r.getStatus() == RegistrationStatus.OK ) {
        r.setPosEfficiency( i++ );
      } else {
        r.setPosEfficiency( (short) 1000 );
      }
      save( r );
    }
    save( regatta );

  }

  private void setValueBase( Registration r,
                             Regatta regatta,
                             double slope ) {
    // x = speed; y = log( market_value )
    double x = regatta.getVariant().getLength() / r.getSecondsLap();
    double y = Math.log( r.getValueAuction() );

    // baseCost is the cost for zero speed given the current slope
    double baseValue = Math.exp( y - slope * x );
    r.setValueBase( baseValue );
  }

  private int compareBids( Bid a,
                           Bid b ) {
    if( a.getAmmount() < b.getAmmount() ) {
      return -1;
    }
    if( a.getAmmount() > b.getAmmount() ) {
      return 1;
    }
    return b.getDate()
      .compareTo( a.getDate() );
  }

  private int compareEfficiency( Registration ra,
                                 Registration rb ) {

    if( ra.getStatus() != rb.getStatus() ) {
      return (int) Math.signum( ra.getStatus() - rb.getStatus() );
    }

    double baseValueA = ra.getValueBase();
    double baseValueB = rb.getValueBase();

    int result = baseValueA != baseValueB
                 ? baseValueA > baseValueB
                   ? 1 // A worse than B
                   : -1  // A better than B
                 : (int) Math.signum( ra.getPosSpeed() - rb.getPosSpeed() );
    return result;
  }

  protected void quickSortRegatta( List<Regatta> regattas,
                                   int rIndexA,
                                   int rIndexB,
                                   int tsl,
                                   int pl ) {
    if( rIndexA >= rIndexB ) {
      return;
    }

    Regatta pivot = regattas.get( ( rIndexA + rIndexB ) / 2 );

    int left = rIndexA;
    int right = rIndexB;

    while( left < right ) {
      while( left < right
             && compareRegattaPenalty( regattas.get( left ),
                                       pivot,
                                       tsl,
                                       pl ) < 0 ) {
        left++;
      }

      while( left < right
             && compareRegattaPenalty( regattas.get( right ),
                                       pivot,
                                       tsl,
                                       pl ) > 0 ) {
        right--;
      }

      if( right > left ) {
        Regatta aux = regattas.get( left );
        regattas.set( left,
                      regattas.get( right ) );
        regattas.set( right,
                      aux );
        left++;
        right--;
      }
    }

    quickSortRegatta( regattas,
                      rIndexA,
                      right,
                      tsl,
                      pl );
    quickSortRegatta( regattas,
                      right + 1,
                      rIndexB,
                      tsl,
                      pl );
  }

  public synchronized void resetPasswordRequest( Participant _user,
                                                 long session ) {
    Participant user = this.getParticipantByEmail( _user );
    if( user == null ) {
      return;
    }

    sendEmail(
      user,
      "A password reset request was received.\n"
      + "If you want to reset your password please click the following link:\n"
      + "http://"
      + this.appURL
      // + "localhost:8080/TOPRacingWEBApp/"
      + "faces/resetpassword.xhtml?key=%27"
      + user.getEmailKey() + "%27",
      session );
  }

  public synchronized void resetPasswordConfirm( Participant _user,
                                                 long session ) {
    PasswordGenerator pwg = new PasswordGenerator( 8,
                                                   10 );
    char[] p = pwg.generatePassword();
    StringBuilder sb = new StringBuilder();
    for( char c
         : p ) {
      sb.append( c );
    }
    String password = sb.toString();
    _user.setPassword( password );
    save( _user,
          false );

    sendEmail(
      _user,
      "Your password has been reset to: "
      + password,
      session );

  }

  public synchronized void sendConfirmationRequest( Participant p,
                                                    String message,
                                                    long session ) {
    sendEmail(
      p,
      message
      + "\n"
      + "http://"
      + this.appURL
      + "faces/confirmusermail.xhtml?key=%27"
      + p.getEmailKey() + "%27",
      session
    );
  }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
  void bubbleSortRegatta( int tsl,
                          int pl ) {
    for( int i = 0;
         i < regattas.size() / 2 + 1;
         i++ ) {
      for( int j = 0;
           j < regattas.size() - 1;
           j++ ) {
        if( compareRegattaPenalty( regattas.get( j ),
                                   regattas.get( j + 1 ),
                                   tsl,
                                   pl )
            >= 0 ) {
          Regatta aux = regattas.get( j );
          regattas.set( j,
                        regattas.get( j + 1 ) );
          regattas.set( j + 1,
                        aux );
        }
      }
      for( int j = regattas.size() - 1;
           j > 0;
           j-- ) {
        if( compareRegattaPenalty( regattas.get( j ),
                                   regattas.get( j - 1 ),
                                   tsl,
                                   pl )
            < 0 ) {
          Regatta aux = regattas.get( j );
          regattas.set( j,
                        regattas.get( j - 1 ) );
          regattas.set( j - 1,
                        aux );
        }
      }
    }
  }

  void roundRobin( int tsl,
                   int pl ) {

    for( long i = 0;
         i < regattas.size();
         i++ ) {
      for( long j = i + 1;
           j < regattas.size();
           j++ ) {
        Regatta a = regattas.get( (int) i );
        Regatta b = regattas.get( (int) j );
        compareRegattaPenalty( a,
                               b,
                               tsl,
                               pl );

      }
    }
  }

  void roundRobinParallel( int tsl,
                           int pl ) {

    final int numberOfThreads = 32;

    final ExecutorService executor = Executors.newFixedThreadPool(
                          numberOfThreads );

// List to store the 'handles' (Futures) for all tasks:
    final List<Future<Integer>> futures = new ArrayList<>();

// Schedule one (parallel) task per String from "collections":
    for( long i = 0;
         i < regattas.size();
         i++ ) {
      for( long j = i + 1;
           j < regattas.size();
           j++ ) {
        Regatta a = regattas.get( (int) i );
        Regatta b = regattas.get( (int) j );
        futures.add(
          executor.submit(
            ()
            -> {
              return compareRegattaPenalty( a,
                                            b,
                                            tsl,
                                            pl );
            }
          )
        );

      }
    }

// Wait until all tasks have completed:
    futures.forEach( ( f )
      -> {
        try {
          Integer aResult = f.get(); // Will block until the result of the task is available.
          // Optionally do something with the result...
        } catch( InterruptedException |
                 ExecutionException ex ) {
          System.out.println(
            new Date() + " !!! " + "-----<<< " + ex + ">>>-----" );
        }
      } );

    executor.shutdown(); // Release the threads held by the executor.
  }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
  public synchronized void recalculateRegattaPenalties( ProgressBar p ) {
    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " -------------------- Recalculating Regatta Penalties ---------------" );

    penaltiesplList = new ArrayList<>();

    p.setProgress( 0 );

    regattas = getRegattas();
    PenaltiesplId pId;
    for( Regatta r
         : regattas ) {
      for( int periodLevel = LevelPeriod.CONTINUOUS;
           periodLevel <= LevelPeriod.WEEK_OF_MONTH;
           periodLevel++ ) {
        pId = new PenaltiesplId( r.getId(),
                                 periodLevel );
        searchPenaltiespl( pId,
                           0,
                           penaltiesplList.size() );
      }
    }
    boolean sorted;
    do {
      sorted = true;
      for( int tsl = LevelTrackset.VENUE;
           tsl >= LevelTrackset.PLANET;
           tsl-- ) {
        for( int pl = LevelPeriod.WEEK_OF_MONTH;
             pl >= LevelPeriod.CONTINUOUS;
             pl-- ) {
          do {
            regattasSorted = true;
            int tsl_ = tsl;
            int pl_ = pl;

            roundRobin( tsl,
                        pl );
            //if( !regattasSorted ) {
            //  sorted = false;
            //}
            double progress =
                   100
                   - 100
                     * ( tsl * ( LevelPeriod.WEEK_OF_MONTH + 1 ) + pl + 1 )
                     / (double) ( ( LevelPeriod.WEEK_OF_MONTH + 1 )
                                  * ( LevelTrackset.VARIANT + 1 ) );
            p.setProgress( progress );
            if( !regattasSorted ) {
              sorted = false;
            }
          } while( !regattasSorted );
        }
      }
    } while( !sorted );
    p.setProgress( 100 );

    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " ------- Finished --- Recalculating Regatta Penalties ---------------" );
    savePenalties();

    recalculatePointscounts( p );

  }

  private void savePenalties() {
    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " -------------------- Saving Penaltiespl array ---------------" );

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

      Query q = s.createQuery( "delete from Tables.Penaltiespl" );
    q.executeUpdate();

    penaltiesplList.forEach( ( p )
      -> {
        s.saveOrUpdate( p );
      } );

    t.commit();
    s.close();

    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " ------ Finished ---- Saving Penaltiespl array ---------------" );
  }

  private int comparePenaltiespl( PenaltiesplId a,
                                  PenaltiesplId b ) {
    if( a.getIdRegatta()
        < b.getIdRegatta() ) {
      return -1;
    }
    if( a.getIdRegatta()
        > b.getIdRegatta() ) {
      return 1;
    }
    if( a.getLevelPeriod()
        < b.getLevelPeriod() ) {
      return -1;
    }
    if( a.getLevelPeriod()
        > b.getLevelPeriod() ) {
      return 1;
    }
    return 0;
  }

  private Penaltiespl searchPenaltiespl( PenaltiesplId penaltiesplId,
                                         int low,
                                         int high ) {
    if( low >= high ) {
      if( low < penaltiesplList.size() ) {
        if( comparePenaltiespl(
          penaltiesplId,
          penaltiesplList.get( low ).getId() )
            == 0 ) {
          return penaltiesplList.get( low );
        }
      }

      // generate new one
      Penaltiespl p = new Penaltiespl();
      p.setId( penaltiesplId );
      penaltiesplList.add( low,
                           p );
      return p;
    }

    int middle = ( low + high ) / 2;
    Penaltiespl ppl = penaltiesplList.get( middle );

    int diff = comparePenaltiespl( ppl.getId(),
                                   penaltiesplId );
    if( diff > 0 ) {
      return searchPenaltiespl( penaltiesplId,
                                low,
                                middle );
    }
    if( diff < 0 ) {
      return searchPenaltiespl( penaltiesplId,
                                middle + 1,
                                high );
    }
    return ppl;

  }

  private void setPenaltyValue( Penaltiespl penaltiespl,
                                int tracksetLevel,
                                int value ) {

    switch( tracksetLevel ) {
      case LevelTrackset.PLANET:
        penaltiespl.setValue0( value );
        return;
      case LevelTrackset.PLANET_REGION:
        penaltiespl.setValue1( value );
        return;
      case LevelTrackset.COUNTRY:
        penaltiespl.setValue2( value );
        return;
      case LevelTrackset.COUNTRY_REGION:
        penaltiespl.setValue3( value );
        return;
      case LevelTrackset.PROVINCE:
        penaltiespl.setValue4( value );
        return;
      case LevelTrackset.PROVINCE_REGION:
        penaltiespl.setValue5( value );
        return;
      case LevelTrackset.VENUE:
        penaltiespl.setValue6( value );
        return;
      case LevelTrackset.VARIANT:
        penaltiespl.setValue7( value );
    }
  }

  private void setPenaltyToWorst( Regatta regattaA,
                                  Regatta regattaB,
                                  int tracksetLevel,
                                  int periodLevel ) {
    PenaltiesplId pIdA;
    PenaltiesplId pIdB;

    Penaltiespl pA;
    Penaltiespl pB;

    for( int tsl = tracksetLevel;
         tsl >= tracksetLevel;
         tsl-- ) {
      for( int pl = periodLevel;
           pl >= periodLevel;
           pl-- ) {
        pIdA = new PenaltiesplId( regattaA.getId(),
                                  pl );
        pIdB = new PenaltiesplId( regattaB.getId(),
                                  pl );

        pA = searchPenaltiespl( pIdA,
                                0,
                                penaltiesplList.size() );
        pB = searchPenaltiespl( pIdB,
                                0,
                                penaltiesplList.size() );

        if( getPenaltyValue( pA,
                             tsl )
            < getPenaltyValue( pB,
                               tsl ) ) {
          setPenaltyValue( pA,
                           tsl,
                           getPenaltyValue( pB,
                                            tsl ) );
          regattasSorted = false;
        }
        if( getPenaltyValue( pA,
                             tsl )
            > getPenaltyValue( pB,
                               tsl ) ) {
          setPenaltyValue( pB,
                           tsl,
                           getPenaltyValue( pA,
                                            tsl ) );
          regattasSorted = false;
        }
      }
    }
  }

  private void setPenaltyAWorseThanB( Regatta regattaA,
                                      Regatta regattaB,
                                      int tracksetLevel,
                                      int periodLevel ) {
    PenaltiesplId pIdA;
    PenaltiesplId pIdB;

    Penaltiespl pA;
    Penaltiespl pB;

    for( int tsl = tracksetLevel;
         tsl >= 0;
         tsl-- ) {
      for( int pl = periodLevel;
           pl >= 0;
           pl-- ) {
        pIdA = new PenaltiesplId( regattaA.getId(),
                                  pl );
        pIdB = new PenaltiesplId( regattaB.getId(),
                                  pl );

        pA = searchPenaltiespl( pIdA,
                                0,
                                penaltiesplList.size() );
        pB = searchPenaltiespl( pIdB,
                                0,
                                penaltiesplList.size() );
        if( getPenaltyValue( pA,
                             tsl )
            <= getPenaltyValue( pB,
                                tsl ) ) {

          setPenaltyValue( pA,
                           tsl,
                           getPenaltyValue( pB,
                                            tsl ) + 1 );
          regattasSorted = false;

        }
      }
    }
  }

  private void setPenaltyBWorseThanA( Regatta regattaA,
                                      Regatta regattaB,
                                      int tracksetLevel,
                                      int periodLevel ) {
    setPenaltyAWorseThanB( regattaB,
                           regattaA,
                           tracksetLevel,
                           periodLevel );
  }

  @Override
  public synchronized int getPenaltyValue( Penaltiespl p,
                                           int tsl ) {
    switch( tsl ) {
      case LevelTrackset.PLANET:
        return p.getValue0();
      case LevelTrackset.PLANET_REGION:
        return p.getValue1();
      case LevelTrackset.COUNTRY:
        return p.getValue2();
      case LevelTrackset.COUNTRY_REGION:
        return p.getValue3();
      case LevelTrackset.PROVINCE:
        return p.getValue4();
      case LevelTrackset.PROVINCE_REGION:
        return p.getValue5();
      case LevelTrackset.VENUE:
        return p.getValue6();
      case LevelTrackset.VARIANT:
        return p.getValue7();
      default:
        return 0;
    }
  }

  /*
   * Regresa -1 si A es mejor que B Regresa 1 si B es mejor que A Regresa 0 si A
   * es igual que B
   */
  private int compareRegattaCriteria( Regatta regattaA,
                                      Regatta regattaB,
                                      int tracksetLevel,
                                      int periodLevel ) {

    if( regattaA.getDatetime() == null ) {
      return 0;
    }

    if( regattaB.getDatetime() == null ) {
      return 0;
    }

    if( tracksetLevel > LevelTrackset.VARIANT
        || periodLevel > LevelPeriod.WEEK_OF_MONTH ) {
      return 0;
    }

    PenaltiesplId pIdA;
    PenaltiesplId pIdB;

    Penaltiespl pA;
    Penaltiespl pB;

    pIdA = new PenaltiesplId( regattaA.getId(),
                              periodLevel );
    pIdB = new PenaltiesplId( regattaB.getId(),
                              periodLevel );
    pA = searchPenaltiespl( pIdA,
                            0,
                            penaltiesplList.size() );
    pB = searchPenaltiespl( pIdB,
                            0,
                            penaltiesplList.size() );

    int result = 0;

    // si son iguales hasta aquí
    if( result == 0 ) {
      // Penalty menor es mejor
      result = (int) signum( getPenaltyValue( pA,
                                              tracksetLevel )
                             - getPenaltyValue( pB,
                                                tracksetLevel ) );
    }

    // si son iguales hasta aquí
    //if( result == 0 ) {
    // Penalty menor es mejor
    //  result = (int) signum( getPenaltyValue( pA,
    //                                          tracksetLevel + 1 )
    //                         - getPenaltyValue( pB,
    //                                            tracksetLevel + 1 ) );
    //}
    // si son iguales hasta aquí
    if( result == 0 ) {
      // Points mayor es mejor
      result = (int) signum(
      getRegattaPriorityPoints( regattaB )
      - getRegattaPriorityPoints( regattaA ) );
    } else {
      System.out.println( new Date() + " !!! " + ">>>>   result=" + result );
    }

    // si son iguales hasta aquí
    if( result == 0 ) {
      // Minutes mayor es mejor
      result = (int) signum( regattaB.getMinutesSpeed()
                             - regattaA.getMinutesSpeed() );
    }

    // si son iguales hasta aquí
    if( result == 0 ) {
      // Minutes mayor es mejor
      result = (int) signum( regattaB.getMinutesRace()
                             - regattaA.getMinutesRace() );
    }

    // si son iguales hasta aquí
    if( result == 0 ) {
      // Datetime menor es mejor
      result = (int) signum( regattaA.getDatetime()
      .compareTo( regattaB.getDatetime() ) );
    }

    // si son iguales hasta aquí
    if( result == 0 ) {
      // Id menor es mejor
      result = (int) signum( regattaA.getId() - regattaB.getId() );
    }

    return result;
  }

  private String toString( Regatta r ) {
    String s = "";
    for( int pl = LevelPeriod.CONTINUOUS;
         pl <= LevelPeriod.WEEK_OF_MONTH;
         pl++ ) {
      Penaltiespl ppl = searchPenaltiespl(
                  new PenaltiesplId(
                    r.getId(),
                    pl
                  ),
                  0,
                  penaltiesplList.size()
                );
      s += ppl.getValue0() + " ";
      s += ppl.getValue1() + " ";
      s += ppl.getValue2() + " ";
      s += ppl.getValue3() + " ";
      s += ppl.getValue4() + " ";
      s += ppl.getValue5() + " ";
      s += ppl.getValue6() + " ";
      s += ppl.getValue7() + "\t\t"
           + ppl.getId().getLevelPeriod() + " " + r.getId() + "\n";
    }
    return s;
  }

  private int compareRegattaWithEqualValidIds(
    Regatta regattaA,
    Regatta regattaB,
    int tracksetLevel,
    int periodLevel ) {

    if( tracksetLevel > LevelTrackset.VENUE ) {
      // A niel Variant todas son igualmente buenas
      return 0;
    }
    /*
     * int resultTSL = compareRegattaCriteria( regattaA, regattaB, tracksetLevel
     * + 1, periodLevel ); int resultPL = compareRegattaCriteria( regattaA,
     * regattaB, tracksetLevel, periodLevel + 1 );
     */
    int pA = getPenaltyValue(
        searchPenaltiespl(
          new PenaltiesplId(
            regattaA.getId(),
            periodLevel
          ),
          0,
          penaltiesplList.size() ),
        tracksetLevel
      );
    int pB = getPenaltyValue(
        searchPenaltiespl(
          new PenaltiesplId(
            regattaB.getId(),
            periodLevel
          ),
          0,
          penaltiesplList.size()
        ),
        tracksetLevel
      );

    int result = (int) signum( pA - pB );
    if( pA != pB ) {
      return result;
    }
    /*
     * if( 0 != resultTSL && resultTSL == resultPL ) { result = resultTSL; }
     * else if( resultTSL == 0 && resultPL != 0 ) { result = resultPL; } else
     * if( resultPL == 0 && resultTSL != 0 ) { result = resultTSL; } else {
     * result = compareRegattaCriteria_( regattaA, regattaB, tracksetLevel,
     * periodLevel ); }
     */
    result = compareRegattaCriteria( regattaA,
                                     regattaB,
                                     tracksetLevel,
                                     periodLevel );

    if( result != (int) signum( pA - pB ) ) {
      regattasSorted = false;
      switch( result ) {
        case -1:
          setPenaltyBWorseThanA( regattaA,
                                 regattaB,
                                 tracksetLevel,
                                 periodLevel );
          break;
        case 1:
          setPenaltyAWorseThanB( regattaA,
                                 regattaB,
                                 tracksetLevel,
                                 periodLevel );
          break;
      }
    }

    return result;
  }

  private int compareRegattaPenalty( Regatta regattaA,
                                     Regatta regattaB,
                                     int tsl,
                                     int pl ) {

    chronometer.reset();
    chronometer.go();
    // result == -1 means a is better than b
    if( regattaA == null ) {
      regattaA = regattaB;
    }
    if( regattaB == null ) {
      regattaB = regattaA;
    }

    if( regattaA == regattaB ) {
      return 0;
    }

    long periodIdA;
    long periodIdB;
    long periodIdA_lower;
    long periodIdB_lower;

    int result = -1; // -1 to produce more comparissons

    periodIdA = getPeriodId( regattaA.getDatetime(),
                             pl );
    periodIdB = getPeriodId( regattaB.getDatetime(),
                             pl );
    // use id in next more local level
    periodIdA_lower = getPeriodId( regattaA.getDatetime(),
                                   pl + 1 );
    periodIdB_lower = getPeriodId( regattaB.getDatetime(),
                                   pl + 1 );
    long tracksetIdA;
    long tracksetIdB;
    long tracksetIdA_lower;
    long tracksetIdB_lower;

    tracksetIdA = getIdTrackset( regattaA.getVariant(),
                                 tsl );
    tracksetIdB = getIdTrackset( regattaB.getVariant(),
                                 tsl );
    // use id in next more local level
    tracksetIdA_lower = getIdTrackset( regattaA.getVariant(),
                                       tsl + 1 );
    tracksetIdB_lower = getIdTrackset( regattaB.getVariant(),
                                       tsl + 1 );

    if( regattaA.getStatus() == RegattaStatus.CANCELLED
        && regattaB.getStatus() == RegattaStatus.CANCELLED ) {
      setPenaltyToWorst( regattaA,
                         regattaB,
                         tsl,
                         pl );
      return 0;
    }
    if( regattaA.getStatus() == RegattaStatus.CANCELLED ) {
      setPenaltyAWorseThanB( regattaA,
                             regattaB,
                             tsl,
                             pl );
      return 1;
    }
    if( regattaB.getStatus() == RegattaStatus.CANCELLED ) {
      setPenaltyBWorseThanA( regattaA,
                             regattaB,
                             tsl,
                             pl );
      return -1;
    }

    if( periodIdA_lower == periodIdB_lower
        && tracksetIdA_lower == tracksetIdB_lower ) {
      result = compareRegattaWithEqualValidIds( regattaA,
                                                regattaB,
                                                tsl,
                                                pl );
    }

    return result;
  }

  @Override
  public synchronized long getIdTrackset( Variant variant,
                                          int tracksetLevel ) {
    Venue venue = variant.getVenue();
    Provinceregion provinceregion = venue.getProvinceregion();
    Province provinceA = provinceregion.getProvince();
    Countryregion countryregion = provinceA.getCountryregion();
    Country country = countryregion.getCountry();
    Planetregion planetregion = country.getPlanetregion();

    switch( tracksetLevel ) {
      case LevelTrackset.PLANET:
        return 1;
      case LevelTrackset.PLANET_REGION:
        return planetregion.getId();
      case LevelTrackset.COUNTRY:
        return country.getId();
      case LevelTrackset.COUNTRY_REGION:
        return countryregion.getId();
      case LevelTrackset.PROVINCE:
        return provinceA.getId();
      case LevelTrackset.PROVINCE_REGION:
        return provinceregion.getId();
      case LevelTrackset.VENUE:
        return venue.getId();
      case LevelTrackset.VARIANT:
        return variant.getId();
      default:
        return 0; // n = ++n % 101;
    }
  }

  @Override
  public synchronized int getNumValidRegistrations( Regatta r ) {
    List<Registration> registrations = this.getRegattaRegistrations( r );
    int ok = 0;
    for( Registration reg
         : registrations ) {
      if( reg.getStatus() == Controller.RegistrationStatus.OK ) {
        ok++;
      }
    }
    return ok;
  }

  @Override
  public synchronized double getRegattaPriorityPoints( Regatta r ) {
    double fee;
    int numValidReg = r.getValidregistrations();
    if( numValidReg <= 0 ) {
      numValidReg = 1;
    }
    fee = r.getEntryfee()
          + r.getTrackrental() / numValidReg;
    if( fee == 0.0 ) {
      fee = 1.0;
    }

    double totalPrize = getRegattaTotalPrize( r );
    return totalPrize
           / fee;
  }

  private double getRegattaTotalPrize( Regatta r ) {
    return r.getPrizeFinishing()
           + r.getPrizeEfficiency();
  }

  private double getRegattaTotalCost( Regatta r ) {
    return r.getEntryfee()
           + r.getTrackrental() / r.getMaxQualifiers();
  }

  @Override
  public synchronized List<Penaltiespl> getPenaltiesplForPL( int periodLevel ) {

      SessionFactory sf = U_HibernateUtil.getSessionFactory();
      Session s = sf.openSession();
      Transaction t = null;
      try {
        t = s.beginTransaction();

        Query qp = s.createQuery( "from Tables.Penaltiespl as penaltiespl"
                                  + " join fetch penaltiespl.regatta as regatta"
                                  + " join fetch regatta.participant"
                                  + " join fetch regatta.variant as variant"
                                  + " join fetch regatta.currency as currency"
                                  + " join fetch variant.venue as venue"
                                  + " join fetch venue.provinceregion as provinceregion"
                                  + " join fetch provinceregion.province as province"
                                  + " join fetch province.countryregion as countryregion"
                                  + " join fetch countryregion.country as country"
                                  + " join fetch country.planetregion"
                                  + " where penaltiespl.id.levelPeriod=" + periodLevel
            );
        penaltiesplList = qp.list();

        t.commit();
        return penaltiesplList;
      } catch( RuntimeException ex ) {
        if( t != null && t.isActive() ) {
          t.rollback();
        }
        throw ex;
      } finally {
        s.close();
      }
  }

  public synchronized List<Penaltiespl> getPenaltiespl() {

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

      Query qp = s.createQuery( "from Tables.Penaltiespl as penaltiespl"
                              + " join fetch penaltiespl.regatta as regatta"
                              + " join fetch regatta.participant"
                              + " join fetch regatta.variant as variant"
                              + " join fetch regatta.currency as currency"
                              + " join fetch variant.venue as venue"
                              + " join fetch venue.provinceregion as provinceregion"
                              + " join fetch provinceregion.province as province"
                              + " join fetch province.countryregion as countryregion"
                              + " join fetch countryregion.country as country"
                              + " join fetch country.planetregion"
        );
    penaltiesplList = qp.list();

    t.commit();
    s.close();

    return penaltiesplList;
  }

  public synchronized void recalculatePointscounts( ProgressBar p ) {

    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " -------------------- Recalculting Points Counts ---------------" );

    List<Registration> registrations = getRegistrations();

    List<Pointscount> pointscount = new ArrayList<>();

//<editor-fold defaultprovince="collapsed" desc="get penalties">
    penaltiesplList = getPenaltiespl();
//</editor-fold>

//<editor-fold defaultprovince="collapsed" desc="declarations of variables">
    Regatta regatta;
    Variant variant;
    Venue venue;
    Provinceregion provinceregion;
    Province province;
    Countryregion countryregion;
    Country country;
    Planetregion planetregion;

    long periodId;
    Participant driver;
    Participant owner;
    long tracksetId;
//</editor-fold>

    long count = 0;
    p.setProgress( 100 * count / (double) ( count * registrations.size() ) );

    for( Registration registration
         : registrations ) {
      if( registration.getStatus() == RegistrationStatus.OK ) {
        regatta = registration.getRegatta();
        if( regatta.getStatus() == RegattaStatus.CANCELLED
            || regatta.getStatus() == RegattaStatus.CREATED ) {
          continue;
        }

//<editor-fold defaultprovince="collapsed" desc="get ids">
        variant = regatta.getVariant();
        venue = variant.getVenue();
        provinceregion = venue.getProvinceregion();
        province = provinceregion.getProvince();
        countryregion = province.getCountryregion();
        country = countryregion.getCountry();
        planetregion = country.getPlanetregion();

        driver = registration.getParticipantByIdDriver();
        owner = registration.getParticipantByIdOwner();
//</editor-fold>

        Pointscount st;
        PointscountId stId;

        for( byte periodLevel = LevelPeriod.WEEK_OF_MONTH;
             periodLevel >= LevelPeriod.CONTINUOUS;
             periodLevel-- ) {
          periodId = getPeriodId( regatta.getDatetime(),
                                  periodLevel );

          for( byte tracksetLevel = LevelTrackset.VARIANT;
               tracksetLevel >= LevelTrackset.PLANET;
               tracksetLevel-- ) {
            switch( tracksetLevel ) {
//<editor-fold defaultprovince="collapsed" desc="assign tracksetId">
              case LevelTrackset.VARIANT:
                tracksetId = variant.getId();
                break;
              case LevelTrackset.VENUE:
                tracksetId = venue.getId();
                break;
              case LevelTrackset.PROVINCE_REGION:
                tracksetId = provinceregion.getId();
                break;
              case LevelTrackset.PROVINCE:
                tracksetId = province.getId();
                break;
              case LevelTrackset.COUNTRY_REGION:
                tracksetId = countryregion.getId();
                break;
              case LevelTrackset.COUNTRY:
                tracksetId = country.getId();
                break;
              case LevelTrackset.PLANET_REGION:
                tracksetId = planetregion.getId();
                break;
              case LevelTrackset.PLANET:
                tracksetId = 1;
                break;
              default:
                tracksetId = -1;
//</editor-fold>
            }

            PenaltiesplId pId = new PenaltiesplId( regatta.getId(),
                                                   periodLevel );
            Penaltiespl penalty = searchPenaltiespl( pId,
                                                     0,
                                                     penaltiesplList.size() );

            // Speed, Race and Efficiency Driver
//<editor-fold defaultprovince="collapsed" desc="Driver pointscount">
            stId = new PointscountId( driver.getId(),
                                      periodLevel,
                                      periodId,
                                      tracksetLevel,
                                      tracksetId );
            st = searchPointscountId( pointscount,
                                      stId );
            if( st == null ) //
            {
              st = new Pointscount(
              stId,
              registration.getParticipantByIdDriver(),
              0,
              0,
              0,
              0,
              0,
              0 );
              pointscount.add( st );
            }

            st.setPointsSD(
              st.getPointsSD()
              + points(
                registration.getPosSpeed()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
            st.setPointsRD(
              st.getPointsRD()
              + points(
                registration.getPosRace()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
            st.setPointsED(
              st.getPointsED()
              + points(
                registration.getPosEfficiency()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
//</editor-fold>
            // Speed, Race and Efficiency Owner
//<editor-fold defaultprovince="collapsed" desc="Owner pointscount">
            stId = new PointscountId( owner.getId(),
                                      periodLevel,
                                      periodId,
                                      tracksetLevel,
                                      tracksetId );
            st = searchPointscountId( pointscount,
                                      stId );
            if( st == null ) {
              st = new Pointscount(
              stId,
              registration.getParticipantByIdOwner(),
              0,
              0,
              0,
              0,
              0,
              0 );
              pointscount.add( st );
            }

            st.setPointsSO(
              st.getPointsSO()
              + points(
                registration.getPosSpeed()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
            st.setPointsRO(
              st.getPointsRO()
              + points(
                registration.getPosRace()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
            st.setPointsEO(
              st.getPointsEO()
              + points(
                registration.getPosEfficiency()
                + getPenaltyValue( penalty,
                                   tracksetLevel )
              )
            );
//</editor-fold>
            // continue with next level
          }
        }
      }
      count++;
      p.setProgress( 100 * count / (double) ( count * registrations.size() ) );
    }
    // continue with next registration

    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " --------- Finished Recalculting Points Counts ---------------" );

    savePointscounts( pointscount );
  }

  private void savePointscounts( List<Pointscount> pointscount ) {

    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " -------------------- Saving Points Counts ---------------" );
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

      Query q = s.createQuery( "delete from Tables.Pointscount" );
    q.executeUpdate();

    pointscount.forEach( ( p )
      -> {
        s.saveOrUpdate( p );
      } );

    t.commit();
    s.close();
    System.out.println(
      new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
      + " ----------- Finished Saving Points Counts ---------------" );
  }

  @Override
  public synchronized double points( long pos ) {
    pos--;
    if( pos > 900 || pos < 0 ) {
      return 0.0f;
    }

    double p[] = { 100, 63, 40, 25, 16 };
    int index = (int) ( pos % 5 );
    int factor = (int) pos / 5;
    double sci = Math.pow( 10,
                           -factor );
    double result = p[ index ] * sci;

    return result;
  }

  long intPow( int base,
               int exp ) {
    if( exp < 0 ) {
      return 0;
    }
    if( exp == 0 ) {
      return 1;
    }
    if( exp == 1 ) {
      return base;
    }
    long r = intPow( base * base,
                     exp / 2 );
    if( exp % 2 == 0 ) {
      r *= base;
    }
    return r;
  }

  private Pointscount searchPointscountId( List<Pointscount> pointscount,
                                           PointscountId stId ) {
    for( Pointscount st
         : pointscount ) {
      if( comparePointscountsIds( st.getId(),
                                  stId ) == 0 ) {
        return st;
      }
    }
    return null;
  }

  private int comparePointscountsIds( PointscountId a,
                                      PointscountId b ) {
    int result = 0;
    if( result == 0 ) {
      result = (int) Math.signum( a.getIdParticipant() - b
      .getIdParticipant()
    );
    }
    if( result == 0 ) {
      result = (int) Math
      .signum( a.getLevelPeriod() - b.getLevelPeriod() );
    }
    if( result == 0 ) {
      result = (int) Math.signum( a.getIdPeriod() - b.getIdPeriod() );
    }
    if( result == 0 ) {
      result = (int) Math.signum( a.getLevelTrackset() - b
      .getLevelTrackset() );
    }
    if( result == 0 ) {
      result = (int) Math.signum( a.getIdTrackset() - b.getIdTrackset() );
    }
    return result;
  }

  @Override
  public synchronized List<Variant> getVariants() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Variant as variant"
          + " join fetch variant.venue as venue"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
        );
    List<Variant> variants = q.list();

    t.commit();
    s.close();

    return variants;
  }

  @Override
  public synchronized List<Venue> getVenues() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Venue as venue"
          + " join fetch venue.participant"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
        );
    List<Venue> venues = q.list();

    t.commit();
    s.close();

    return venues;
  }

  @Override
  public synchronized List<Provinceregion> getProvinceregions() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
        );
    List<Provinceregion> provinceregions = q.list();

    t.commit();
    s.close();

    return provinceregions;
  }

  @Override
  public synchronized List<Province> getProvinces() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
        );
    List<Province> provinces = q.list();

    t.commit();
    s.close();

    return provinces;
  }

  @Override
  public synchronized List<Countryregion> getCountryregions() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
        );
    List<Countryregion> countryregions = q.list();

    t.commit();
    s.close();

    return countryregions;
  }

  @Override
  public synchronized List<Country> getCountries() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Country as country"
          + " join fetch country.planetregion"
        );
    List<Country> countries = q.list();

    t.commit();
    s.close();

    return countries;
  }

  @Override
  public synchronized List<Planetregion> getPlanetregions() {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Planetregion as planetregion"
        );
    List<Planetregion> planetregions = q.list();

    t.commit();
    s.close();

    return planetregions;
  }

  @Override
  public synchronized List<Car> getCars( Participant currentParticipant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Car as car"
          + " join fetch car.participant as p"
          + " where p.id = " + currentParticipant.getId()
        );
    List<Car> cars = q.list();

    t.commit();
    s.close();

    return cars;
  }

  public synchronized void updateCarOwners( Regatta regatta ) {
    List<Registration> registrations = getRegattaRegistrations( regatta );
    registrations.sort( ( a, b )
      -> compareRegistrationRacePos( a,
                                     b ) );
    Registration winner = registrations.get( 0 );
    Car car;
    for( Registration r
         : registrations ) // for each registration of this regatta do
    {
      if( r.getStatus() == RegistrationStatus.OK ) {
        // set car owner to registration's buyerid record;
        car = r.getCar();
        car.setParticipant( r.getParticipantByIdBuyer() );
        save( car );
      }
    }
  }

  public synchronized void save( List<Bid> bids ) {
    if( bids == null ) {
      return;
    }

    for( Bid bid
         : bids ) {
      save( bid.getRegistration() );
    }
    if( bids.size() > 0
        && bids.get( 0 ).getRegistration().getRegatta().getStatus()
           <= RegattaStatus.AUCTION ) {
      for( Bid bid
           : bids ) {
        save( bid );
      }
    }
  }

  public synchronized Participant createParticipant() {

    Crypto c = new Crypto();
    Participant p = new Participant();
    p.setPassword( c.encryptString( "password" ) );
    p.setNamesGiven( "Given Names" );
    p.setNamesFamily( "Family Names" );
    p.setEmail( "example@site.com" );
    p.setPhone( "5555555555" );
    p.setVenue( this.getVenueById( 1L ) );
    p.setEmailKey( "" );
    p.setConfirmed( false );
    p.setDefaulter( 0 );

    p.setPassword( "password" );

    return p;
  }

  public synchronized Participant save( Participant user,
                                        boolean resetConfirmed ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Crypto c = new Crypto();
    String password = user.getPassword();
    user.setPassword( c.encryptString( password ) );
    user.setEmailKey(
      UUID.randomUUID()
        .toString() );
    if( resetConfirmed ) {
      user.setConfirmed( false );
    }
    try {
      s.saveOrUpdate( user );
    } catch( Exception e ) {
      user = null;
    }
    t.commit();
    s.close();

    if( user != null ) {
      user.setPassword( password ); // decoded for internal use
    }
    return user;
  }

  public synchronized Variant createVariant( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Venue venue = new Venue();
    venue.setId( 1L );
    Variant v = new Variant( venue,
                             "",
                             0,
                             0,
                             true,
                             user.getId() );
    s.saveOrUpdate( v );

    Query q = s.createSQLQuery( "SELECT max(v.id) as count FROM variant v" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    v = getVariantById( lastId );

    return v;
  }

  public synchronized void save( Variant variant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Variant DBvariant = (Variant) s.get( Variant.class,
                                         variant.getId() );
    DBvariant.setLength( variant.getLength() );
    DBvariant.setMinWidth( variant.getMinWidth() );
    DBvariant.setName( variant.getName() );
    DBvariant.setVenue( variant.getVenue() );
    DBvariant.setIdCreator( variant.getIdCreator() );
    s.save( DBvariant );

    t.commit();
    s.close();
  }

  public synchronized Venue createVenue( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Provinceregion pr = new Provinceregion();
    pr.setId( 1L );
    Venue v = new Venue( user,
                         pr,
                         "",
                         0,
                         0,
                         user.getId() );
    s.saveOrUpdate( v );

    Query q = s.createSQLQuery( "SELECT max(v.id) as count FROM venue v" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    v = getVenueById( lastId );

    return v;
  }

  public synchronized void save( Venue venue ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Venue DBvenue = (Venue) s.get( Venue.class,
                                   venue.getId() );
    DBvenue.setName( venue.getName() );
    DBvenue.setParticipant( venue.getParticipant() );
    DBvenue.setMeridian( venue.getMeridian() );
    DBvenue.setParallel( venue.getParallel() );
    DBvenue.setProvinceregion( venue.getProvinceregion() );
    DBvenue.setIdCreator( venue.getIdCreator() );
    s.save( DBvenue );

    t.commit();
    s.close();
  }

  public synchronized Provinceregion createProvinceregion( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Province p = new Province();
    p.setId( 1L );
    Provinceregion pr = new Provinceregion( p,
                                            "",
                                            user.getId() );
    s.saveOrUpdate( pr );

    Query q = s.createSQLQuery(
          "SELECT max(pr.id) as count FROM provinceregion pr" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    pr = getProvinceregionById( lastId );

    return pr;
  }

  public synchronized void save( Provinceregion provinceregion ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Provinceregion DBprovinceregion = (Provinceregion) s.get(
                   Provinceregion.class,
                   provinceregion.getId() );
    DBprovinceregion.setName( provinceregion.getName() );
    DBprovinceregion.setProvince( provinceregion.getProvince() );
    DBprovinceregion.setIdCreator( provinceregion.getIdCreator() );
    s.save( DBprovinceregion );

    t.commit();
    s.close();
  }

  public synchronized Province createProvince( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Countryregion cr = new Countryregion();
    cr.setId( 1L );
    Province p = new Province( cr,
                               "",
                               user.getId() );
    s.saveOrUpdate( p );

    Query q = s.createSQLQuery( "SELECT max(p.id) as count FROM province p" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    p = getProvinceById( lastId );

    return p;
  }

  public synchronized void save( Province province ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Province DBprovince = (Province) s.get( Province.class,
                                            province.getId() );
    DBprovince.setName( province.getName() );
    DBprovince.setCountryregion( province.getCountryregion() );
    DBprovince.setIdCreator( province.getIdCreator() );
    s.save( DBprovince );

    t.commit();
    s.close();
  }

  public synchronized Countryregion createCountryregion( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Country c = new Country();
    c.setId( 1L );
    Countryregion cr = new Countryregion( c,
                                          "",
                                          user.getId() );
    s.saveOrUpdate( cr );

    Query q = s
          .createSQLQuery(
            "SELECT max(cr.id) as count FROM countryregion cr" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    cr = getCountryregionById( lastId );

    return cr;
  }

  public synchronized void save( Countryregion countryregion ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Countryregion DBcountryregion = (Countryregion) s.get(
                  Countryregion.class,
                  countryregion.getId() );
    DBcountryregion.setName( countryregion.getName() );
    DBcountryregion.setCountry( countryregion.getCountry() );
    DBcountryregion.setIdCreator( countryregion.getIdCreator() );
    s.save( DBcountryregion );

    t.commit();
    s.close();
  }

  public synchronized Country createCountry( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Planetregion pr = new Planetregion();
    pr.setId( 1L );
    Country c = new Country( pr,
                             "",
                             user.getId() );
    s.saveOrUpdate( c );

    Query q = s.createSQLQuery( "SELECT max(c.id) as count FROM country c" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    c = getCountryById( lastId );

    return c;
  }

  public synchronized void save( Country country ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Country DBcounty = (Country) s.get( Country.class,
                                        country.getId() );
    DBcounty.setName( country.getName() );
    DBcounty.setPlanetregion( country.getPlanetregion() );
    DBcounty.setIdCreator( country.getIdCreator() );
    s.save( DBcounty );

    t.commit();
    s.close();
  }

  public synchronized Planetregion createPlanetregion( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Planetregion pr = new Planetregion( "",
                                        user.getId() );
    s.saveOrUpdate( pr );

    Query q = s.createSQLQuery(
          "SELECT max(pr.id) as count FROM planetregion pr" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    pr = getPlanetregionById( lastId );

    return pr;
  }

  public synchronized void save( Planetregion planetregion ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Planetregion DBplanetregion = (Planetregion) s.get( Planetregion.class,
                                                        planetregion
                                                          .getId() );
    DBplanetregion.setName( planetregion.getName() );
    DBplanetregion.setIdCreator( planetregion.getIdCreator() );
    s.save( DBplanetregion );

    t.commit();
    s.close();
  }

  public synchronized Car createCar( Participant user ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Car c = new Car( user,
                     "",
                     0,
                     0 );
    s.saveOrUpdate( c );

    Query q = s.createSQLQuery( "SELECT max(c.id) as count FROM car c" )
          .addScalar( "count",
                      LongType.INSTANCE );
    long lastId = ( (List<Long>) q.list() ).get( 0 );

    t.commit();
    s.close();

    c = getCarById( lastId );

    return c;
  }

  public synchronized void save( Car car ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    s.saveOrUpdate( car );

    t.commit();
    s.close();
  }

  private Car getFirstOwnerCar( Participant p ) {

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery( "from Tables.Car as c"
                             + " join fetch c.participant"
                             + " where c.participant.id = "
                             + p.getId()
        );

    Car car;
    if( q.list().size() > 0 ) {
      car = ( (List<Car>) q.list() ).get( 0 );
    } else {
      car = createCar( p );
    }

    t.commit();
    s.close();

    return car;
  }

  public synchronized Registration createRegistration( Regatta regatta,
                                                       Participant currentParticipant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Registration r = new Registration();
    r.setRegatta( regatta );
    r.setParticipantByIdDriver( currentParticipant );
    r.setParticipantByIdOwner( currentParticipant );
    r.setParticipantByIdBuyer( currentParticipant );
    r.setCar( getFirstOwnerCar( currentParticipant ) );
    r.setSecondsLap( 1e9 );
    r.setPosSpeed( (short) 1000 );
    r.setPosRacegrid( (short) 1000 );
    r.setPosRace( (short) 1000 );
    r.setValueAuction( 1e9 );
    r.setValueBase( 1e9 );
    r.setPosEfficiency( (short) 1000 );

    s.saveOrUpdate( r );

    t.commit();
    s.close();

    return r;
  }

  public synchronized void save( Registration registration ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    System.out.println(
      "saving registration: " + registration.getId()
      + "\nCar: " + registration.getCar().toString()
    );
    s.saveOrUpdate( registration );

    t.commit();
    s.close();
    System.out.println( new Date() + " !!! " + "exit" );
  }

  private void save( Bid _bid ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    s.saveOrUpdate( _bid );

    t.commit();
    s.close();
  }

  public synchronized Regatta createRegatta( Participant participant ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Date now = new Date();

    Variant v = new Variant();
    v.setId( 1L );
    Regatta r = new Regatta();

    r.setParticipant( participant );
    r.setVariant( v );
    r.setDatetime( now );
    r.setColorFg( "ffffff" );
    r.setColorBg( "000000" );
    r.setLevelPeriod( 0 );
    r.setLevelTrackset( 0 );
    Currency c = new Currency();
    c.setId( 1 );
    r.setCurrency( c );

    s.saveOrUpdate( r );

    Query q = s.createSQLQuery( "SELECT max(r.id) as count FROM regatta r" )
          .addScalar( "count",
                      LongType.INSTANCE );
    List<Long> list = q.list();
    long lastId = 0;
    if( list.size() > 0 ) {
      lastId = list.get( 0 );
    }

    t.commit();
    s.close();

    r = getRegattaById( lastId );

    return r;
  }

  public synchronized void save( Regatta regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    regatta.setValidregistrations(
      this.getNumValidRegistrations( regatta )
    );
    s.saveOrUpdate( regatta );

    t.commit();
    s.close();
  }


  /*
   * Methods previously in AppScopeBean
   */
  public synchronized long getNewSessionId() {

    this.numSesionesActivas++;
    long sessionId = System.currentTimeMillis();

    Session s = null;
    Transaction t = null;

    try {
      // Update session count in MySQL when the mapping is available.
      SessionFactory sf = U_HibernateUtil.getSessionFactory();
      s = sf.openSession();
      t = s.beginTransaction();

      System.out.println(
        new Date() + " !!! " + "-------- Obteniendo Appstats ----------" );
      Appstats appstat = (Appstats) s.get( "Appstats",
                                           1 );

      if( appstat == null ) {
        appstat = new Appstats();
        appstat.setId( 1 );
        appstat.setSessioncount( 0 );
      }

      appstat.setSessioncount(
        appstat.getSessioncount() + 1
      );
      sessionId = appstat.getSessioncount();
      s.saveOrUpdate( appstat );

      t.commit();
    } catch( Exception e ) {
      if( t != null ) {
        try {
          t.rollback();
        } catch( Exception ignored ) {
        }
      }
      System.out.println(
        new Date() + " !!! " + "---- Appstats unavailable, using local session id ----" );
      System.out.println( e );
    } finally {
      if( s != null ) {
        try {
          s.close();
        } catch( Exception ignored ) {
        }
      }
    }

    this.listaSesiones.add(
      new Sesion(
        this.getClientIpAddress(),
        new Date(),
        null,
        sessionId ) );

    System.out.println(
      " count:" + this.numSesionesActivas
      + " -------------"
    );
    for( int i = 0;
         i < this.listaSesiones.size();
         i++ ) {
      System.out.println( i + " Sesión: " + this.listaSesiones.get( i ) );
    }
    return sessionId;
  }

  private static final String[] HEADERS_TO_TRY = {
    "X-Forwarded-For",
    "Proxy-Client-IP",
    "WL-Proxy-Client-IP",
    "HTTP_X_FORWARDED_FOR",
    "HTTP_X_FORWARDED",
    "HTTP_X_CLUSTER_CLIENT_IP",
    "HTTP_CLIENT_IP",
    "HTTP_FORWARDED_FOR",
    "HTTP_FORWARDED",
    "HTTP_VIA",
    "REMOTE_ADDR" };

  public synchronized String getClientIpAddress() {
    String ip;
    try {
      HttpServletRequest request = (HttpServletRequest) FacesContext
                         .getCurrentInstance()
                         .getExternalContext()
                         .getRequest();
      for( String header
           : HEADERS_TO_TRY ) {
        ip = request.getHeader( header );
        if( ip != null
            && ip.length() != 0
            && !"unknown".equalsIgnoreCase( ip ) ) {
          //System.out.println( new Date() + " !!! " +  "ip address:" + ip );
          return ip;
        }
      }
      ip = request.getRemoteAddr();
    } catch( Exception e ) {
      //System.out.println( new Date() + " !!! " +  "getClientIpAddress failed: " + e.getMessage() );
      ip = "null";
    }
    return ip;
  }

  public synchronized void sendEmail( Participant user,
                                      String messageText,
                                      long session ) {
    logMessage( user,
                messageText,
                session );

    if( user == null ) {
      return;
    }

    try {
      MailerAgent mail;
      mail = new MailerAgent( this.eMailSender,
                              session,
                              user.getEmail(),
                              messageText
                              + "\n\nuser:" + user.getId()
                              + " " + this
                                .getParticipantFullName( user )
                              + "\n" + new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss.SSS" ).format(
                                new Date() )
                              + "\n",
                              "" );
      mail.start();

    } catch( MessagingException ex ) {
      System.out.println( new Date() + " !!! " + ex.getMessage() );
    }
  }

  private String getMonitorEmail() {
    String monitor = System.getenv( "MAIL_MONITOR_EMAIL" );
    return monitor == null || monitor.trim().isEmpty()
           ? "top.racing.org@gmail.com"
           : monitor.trim();
  }

  public synchronized void sendMonitorMail( Participant user,
                                            String messageText,
                                            long session ) {
    if( user == null ) {
      user = new Participant();
    }

    try {
      MailerAgent mail;
      mail = new MailerAgent( this.eMailSender,
                              session,
                              getMonitorEmail(),
                              messageText
                              + "\n\nsession:" + session
                              + "\n" + new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss.SSS" ).format(
                                new Date() )
                              + "\tuser:" + user.getId()
                              + "\t:" + user.getEmail()
                              + "\n" + getClientIpAddress()
                              + "\nhttps://www.iplocation.net/",
                              "" );
      mail.start();

    } catch( MessagingException ex ) {
      System.out.println( new Date() + " !!! " + ex.getMessage() );
    }

  }

  public synchronized void logMessage( Participant user,
                                       String messageText,
                                       long session ) {
    if( user == null ) {
      user = new Participant();
    }

    try {

      Path filePath = Paths.get(
           "/u_" + user.getId() + ".txt" );
      if( !Files.exists( filePath ) ) {
        Files.createFile( filePath );
      }
      System.out.println(
        "---- Escribiendo en archivo: "
        + filePath
        + " ---- "
      );
      String msg = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
             .format(
               new Date() )
                   + " session:" + session
                   + "\tu:" + user.getId()
                   + "\t" + getClientIpAddress()
                   + "\t" + messageText;

      Files.write( filePath,
                   msg.getBytes(),
                   StandardOpenOption.APPEND );
      Files
        .write( filePath,
                "\r\n".getBytes(),
                StandardOpenOption.APPEND );

    } catch( IOException ex ) {
      System.out.println( new Date() + " !!! " + ex.getMessage() );
    }
  }

  /*
   *
   */
  public synchronized Participant getParticipantByEMailKey( String key ) {
    if( null == key ) {
      return null;
    }

    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    System.out.println(
      new Date() + " !!! " + "---- getting participant by e-mail key: " + key );

    Query q = s.createQuery(
          "from Tables.Participant as user where user.emailKey = " + key );
    Participant user = (Participant) q.uniqueResult();

    if( user != null ) {
      user.setConfirmed( true );
    }

    t.commit();
    s.close();

    if( user != null ) {
      Crypto c = new Crypto();
      user.setPassword( c.decryptString( user.getPassword() ) );
    }
    return user;
  }

  @Override
  public synchronized String getParticipantFullNameById( long _userNumber ) {
    Participant u = new Participant();
    u.setId( _userNumber );
    u = getParticipantById( u );
    return getParticipantFullName( u );
  }

  @Override
  public synchronized List<Penaltiespl> getRegattaPeriodlevelPenaltiesList(
    Regatta _regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
            "from Tables.Penaltiespl as penaltiespl where"
          + " penaltiespl.id.idRegatta = " + _regatta.getId()
        );
    List<Penaltiespl> lp = q.list();

    t.commit();
    s.close();

    lp.sort( ( a, b )
      -> compareLevelPeriodPenaltiesLevel( a,
                                           b ) );

    return lp;
  }

  private int compareLevelPeriodPenaltiesLevel( Penaltiespl a,
                                                Penaltiespl b ) {
    return a.getId().getLevelPeriod() == b.getId().getLevelPeriod()
           ? 0
           : ( a.getId().getLevelPeriod() < b.getId().getLevelPeriod()
               ? -1
               : 1 );
  }

  public synchronized List<Bid> getBids( Registration _registration ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Bid as bid"
          + " join fetch bid.participant"
          + " join fetch bid.registration as r"
          + " join fetch r.car"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdBuyer"
          + " join fetch r.regatta as regatta"
          + " join fetch regatta.participant"
          + " join fetch regatta.variant as variant"
          + " join fetch regatta.currency as currency"
          + " join fetch variant.venue as venue"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
          + " where r.id="
          + _registration.getId()
        );
    List<Bid> bidList = q.list();

    t.commit();
    s.close();

    return bidList;
  }

  @Override
  public synchronized List<Bid> getBids( Participant _participant,
                                         Regatta _regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Bid as bid"
          + " join fetch bid.participant"
          + " join fetch bid.registration as r"
          + " join fetch r.car"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdBuyer"
          + " join fetch r.regatta as regatta"
          + " join fetch regatta.participant"
          + " join fetch regatta.variant as variant"
          + " join fetch regatta.currency as currency"
          + " join fetch variant.venue as venue"
          + " join fetch venue.provinceregion as provinceregion"
          + " join fetch provinceregion.province as province"
          + " join fetch province.countryregion as countryregion"
          + " join fetch countryregion.country as country"
          + " join fetch country.planetregion"
          + " where r.regatta.id="
          + _regatta.getId()
          + " and bid.id.idParticipant="
          + _participant.getId()
        );
    List<Bid> bidList = q.list();

    t.commit();
    s.close();

    return bidList;
  }

  public synchronized void assignRegattaIndividualEfficiencyPrize(
    Regatta regatta ) {
    SessionFactory sf = U_HibernateUtil.getSessionFactory();
    Session s = sf.openSession();
    Transaction t = s.beginTransaction();

    Query q = s.createQuery(
          "from Tables.Registration as r"
          + " join fetch r.car"
          + " join fetch r.participantByIdDriver"
          + " join fetch r.participantByIdOwner"
          + " join fetch r.participantByIdBuyer"
          + " join fetch r.regatta"
          + " where r.regatta.id="
          + regatta.getId()
        );
    List<Registration> registrations = q.list();

    double totalBet = 0;
    int numOK = 0;
    for( Registration r
         : registrations ) {
      r.setPrizeEfficiency( 0 );
      if( r.getStatus() == RegistrationStatus.OK ) {
        numOK++;
        totalBet += r.getBetEfficiency();
      }
    }

    if( numOK > 0 ) {
      double totalWeight = 0;
      for( Registration r
           : registrations ) {
        if( r.getStatus() == RegistrationStatus.OK ) {
          double betFraction = totalBet > 0
                               ? r.getBetEfficiency() / totalBet
                               : 1;
          double weight = betFraction * points( r.getPosEfficiency() );
          totalWeight += weight;
        }
      }
      if( totalWeight > 0 ) {
        for( Registration r
             : registrations ) {
          if( r.getStatus() == RegistrationStatus.OK ) {
            double betFraction = totalBet > 0
                                 ? r.getBetEfficiency() / totalBet
                                 : 1;
            double weight = betFraction * points( r.getPosEfficiency() );
            r.setPrizeEfficiency(
              ( regatta.getPrizeEfficiency()
                + totalBet )
              * weight
              / totalWeight );
          }
        }
      }
    }

    t.commit();
    s.close();
  }

  /*
   * e-mails for regatta status changes:
   */
  public synchronized void sendRegistrationsListMessages( Regatta _regatta ) {
// ??????????????????????
  }

  public synchronized void sendSpeedTestResultsMessages( Regatta _regatta ) {
// ??????????????????????
  }

  public synchronized void sendRaceTestResultsMessages( Regatta _regatta ) {
// ??????????????????????
  }

  public synchronized void sendAuctionResultsMessages( Regatta regatta,
                                                       String buyerMsg,
                                                       String sellerMsg ) {
    List<Registration> registrations =
                       this.getRegattaRegistrations( regatta );

    for( Registration r
         : registrations ) {
      // saved to generate a new e-mail key for complaints
      Participant p = getParticipantById( r.getParticipantByIdOwner() );
      save( p,
            false );
      p = getParticipantById( r.getParticipantByIdBuyer() );
      save( p,
            false );
    }

    for( Registration registration
         : registrations ) {
      Participant buyer = registration.getParticipantByIdBuyer();
      Participant seller = registration.getParticipantByIdOwner();
      Participant driver = registration.getParticipantByIdDriver();
      Car car = registration.getCar();

      this.sendEmail(
        buyer,
        buyerMsg
        + "\n"
        + "http://"
        + this.appURL
        + "faces/complaint.xhtml?key=%27"
        + buyer.getEmailKey() + "%27"
        + "&r1=" + registration.getId()
        + "&r2=" + regatta.getId()
        + "&s=" + seller.getId()
        + "&b=" + buyer.getId()
        + "\n\nCar: " + car.getId() + " " + car.getNickname()
        + "\nSeller: " + seller.getId()
        + " " + seller.getNamesFamily()
        + " " + seller.getNamesGiven()
        + "\nBuyer: " + buyer.getId()
        + " " + buyer.getNamesFamily()
        + " " + buyer.getNamesGiven()
        + "\nAmmount: " + registration.getValueAuction()
        + "\n",
        regatta.getId()
      );

      this.sendEmail(
        seller,
        sellerMsg
        + "\n"
        + "http://"
        + this.appURL
        + "faces/complaint.xhtml?key=%27"
        + seller.getEmailKey() + "%27"
        + "&r1=" + registration.getId()
        + "&r2=" + regatta.getId()
        + "&s=" + seller.getId()
        + "&b=" + buyer.getId()
        + "\n\nCar: " + car.getId() + " " + car.getNickname()
        + "\nSeller: " + seller.getId()
        + " " + seller.getNamesFamily()
        + " " + seller.getNamesGiven()
        + "\nBuyer: " + buyer.getId()
        + " " + buyer.getNamesFamily()
        + " " + buyer.getNamesGiven()
        + "\nAmmount: " + registration.getValueAuction()
        + "\n",
        regatta.getId()
      );
    }
  }

  public synchronized void sendBalanceMessages( Regatta regatta,
                                                String ownerMsg,
                                                String promoterMsg ) {
    List<Registration> registrations =
                       this.getRegattaRegistrations( regatta );

    Participant promoter = regatta.getParticipant();

    for( Registration registration
         : registrations ) {

      Participant owner = registration.getParticipantByIdOwner();
      Car car = registration.getCar();

      double individualRental =
             regatta.getTrackrental() / this.getNumValidRegistrations( regatta );

      registration.setBalance(
        -individualRental
        - regatta.getEntryfee()
        - registration.getBetFinishing()
        - registration.getBetEfficiency()
        + registration.getPrizeFinishing()
        + registration.getPrizeEfficiency()
      );

      this.sendEmail(
        owner,
        ownerMsg
        + "\n"
        + "\n regatta: " + regatta.getId()
        + "\n   owner: " + owner.getId()
        + "\n     Car: " + car.getId() + " " + car.getNickname()
        + "\n"
        + "\n    Track Rental: \t-" + individualRental
        + "\n       Entry fee: \t-" + regatta.getEntryfee()
        + "\n   Finishing bet: \t-" + registration.getBetFinishing()
        + "\n  Efficiency bet: \t-" + registration.getBetEfficiency()
        + "\n Finishing prize: \t+" + registration.getPrizeFinishing()
        + "\nEfficiency prize: \t+" + registration.getPrizeEfficiency()
        + "\n"
        + "\nTotal: " + registration.getBalance()
        + "\n"
        + "\npromoter: \t"
        + promoter.getNamesGiven()
        + " " + promoter.getNamesFamily()
        + " email: " + promoter.getEmail(),
        regatta.getId()
      );
      this.sendEmail(
        promoter,
        promoterMsg
        + "\n"
        + "\n regatta: " + regatta.getId()
        + "\n   owner: " + owner.getId()
        + "\n     Car: " + car.getId() + " " + car.getNickname()
        + "\n"
        + "\n    Track Rental: \t-" + individualRental
        + "\n       Entry fee: \t-" + regatta.getEntryfee()
        + "\n   Finishing bet: \t-" + registration.getBetFinishing()
        + "\n  Efficiency bet: \t-" + registration.getBetEfficiency()
        + "\n Finishing prize: \t+" + registration.getPrizeFinishing()
        + "\nEfficiency prize: \t+" + registration.getPrizeEfficiency()
        + "\n"
        + "\nTotal: " + registration.getBalance()
        + "\n"
        + "\nowner: \t"
        + owner.getNamesGiven()
        + " " + owner.getNamesFamily()
        + " email: " + owner.getEmail(),
        regatta.getId()
      );

    }
  }

  public synchronized long incNumUsuariosActivos() {
    return ++this.numUsuariosActivos;
  }

  public synchronized long decNumUsuariosActivos() {
    this.numUsuariosActivos--;
    return this.numUsuariosActivos;
  }

  public synchronized long decNumSesionesActivas( long numSesion ) {
    this.numSesionesActivas--;
    listaSesiones.forEach( ( x )
      -> {
        if( x.id == numSesion ) {
          x.fin = new Date();
        }
      } );
    System.out.println( "------ Sesión destruida: " + numSesion );
    for( int i = 0;
         i < this.listaSesiones.size();
         i++ ) {
      System.out.println( i + " Sesión: " + this.listaSesiones.get( i ) );
    }
    return this.numSesionesActivas;
  }

  @Override
  public String getTracksetName( int tsl,
                                 long idTrackset ) {
    switch( tsl ) {
      case LevelTrackset.PLANET:
        return "Earth";
      case LevelTrackset.PLANET_REGION:
        return getPlanetregionName(
          getPlanetregionById( idTrackset ) );
      case LevelTrackset.COUNTRY:
        return getCountryName(
          getCountryById( idTrackset ) );
      case LevelTrackset.COUNTRY_REGION:
        return getCountryregionName(
          getCountryregionById( idTrackset ) );
      case LevelTrackset.PROVINCE:
        return getProvinceName(
          getProvinceById( idTrackset ) );
      case LevelTrackset.PROVINCE_REGION:
        return getProvinceregionName(
          getProvinceregionById( idTrackset ) );
      case LevelTrackset.VENUE:
        return getVenueName(
          getVenueById( idTrackset ) );
      case LevelTrackset.VARIANT:
        return getVariantName(
          getVariantById( idTrackset ) );
      default:
        return "-";
    }
  }

}

