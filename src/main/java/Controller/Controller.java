package Controller;

import Model.ModelBean;
import Model.ModelForView;
import Tables.Bid;
import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Participant;
import Tables.Planetregion;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import View.ViewForController;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */
public class Controller {

  public static final String AUTH_SESSION_KEY = "authenticatedParticipantId";

  private int currentPeriodLevel;

  private int currentTracksetLevel;

  private final ViewForController theView;

  private ModelBean modelBean;
  private final PilaInteger previousUI;
  private Participant currentParticipant;
  private long session;
  private static final Logger LOGGER = Logger.getLogger( Controller.class.getName() );

  public Controller( ViewForController view ) {
    this.theView = view;
    previousUI = new PilaInteger();
    LOGGER.log( Level.INFO,
                "Controller initialized for view {0}",
                view.getClass().getSimpleName() );
  }

  public void clickLanguageChange( int _languageId ) {
    theView.showUI( UI.WELCOME );
  }

  public long newSession( ModelForView modelForView ) {
    this.session = -1;
    this.modelBean = (ModelBean) modelForView;
    updateAuthenticatedSession( null );
    int i = 0;
    while( i < 20 && this.session == -1 ) {
      try {
        this.session = modelBean.getNewSessionId();
      } catch( Exception e ) {
        System.out.println(
          "XXXXXXXX No se pudo obtener Appstats (" + i + ")XXXXXXX" );
        System.out.println( e );
        i++;
      }
    }
    /*
     * this.modelBean.sendMonitorMail( new Participant(), theModel.bundle( "A
     * NEW SESSION HAS STARTED" ), session );
     */
    return this.session;
  }

  public void preDestroySession() {

    long numSesionesActivas = modelBean.decNumSesionesActivas( this.session );

    System.out
      .println( new Date()
                + "------ Session " + this.session
                + " destroyed ------- num sesiones activas: "
                + numSesionesActivas );

    System.out.println(
      this.currentParticipant != null
      ? this.currentParticipant.getEmail()
      : null );
    /*
     * modelBean.sendMonitorMail( currentParticipant, theModel.bundle( "A
     * SESSION WAS TERMINATED" ), session );
     */
  }

  public ModelForView getModel() {
    return this.modelBean;
  }

  private boolean isValidParticipant() {
    if( null == modelBean.getValidParticipant( currentParticipant ) ) {
      theView.showUI( UI.LOGIN );
      return false;
    } else {
      System.out.println(
        " >>>> is Valid: " + currentParticipant.getEmail() + " <<<<" );
    }
    return true;
  }

  private boolean hasCurrentParticipantLocalPromoterBlock( Regatta regatta ) {
    if( regatta == null
        || regatta.getParticipant() == null
        || currentParticipant == null ) {
      return false;
    }

    return modelBean.hasActiveLocalPromoterBlock( currentParticipant,
                                                  regatta.getParticipant() );
  }

  public void clickEditProfile() {
    if( !isValidParticipant() ) {
      return;
    }

    LOGGER.log( Level.INFO,
                "clickEditProfile for participant id {0}",
                currentParticipant.getId() );
    theView.showUI( UI.EDIT_USER,
                    currentParticipant );
  }

  public void clickLogout() {
    this.currentParticipant = null;
    updateAuthenticatedSession( null );
    long numUsuariosActivos = modelBean.decNumUsuariosActivos();

    LOGGER.info( "clickLogout, active users now: " + numUsuariosActivos );
    System.out.println(
      "---- Quedan " + numUsuariosActivos + " usuarios activos. ----" );

    theView.invalidateSessionAndShowUI( UI.WELCOME );
  }

  public void clickLogin( Participant user ) {

    LOGGER.log( Level.INFO,
                "clickLogin attempt for email={0}",
                user == null ? "null" : user.getEmail() );
    System.out.println( new Date()
                        + " clickLogin attempt for "
                        + (user == null ? "null" : user.getEmail()) );
    currentParticipant = modelBean.getValidParticipant( user );
    if( null == currentParticipant ) {
      theView.showUI( UI.ERROR_LOGIN );
      return;
    }

    long numUsuariosActivos = modelBean.incNumUsuariosActivos();
    System.out.println(
      "---- Ya hay " + numUsuariosActivos + " usuarios activos. ----" );

    // sendMonitorMail( Messages.LOGIN, userNumber );
    if( !currentParticipant.isConfirmed() ) {
      // we save the user to produce a confirmation key
      // and send a confirmation e-mail to the user
      // problems: currentParticipant = modelBean.save( currentParticipant );

      modelBean.sendConfirmationRequest(
        this.currentParticipant,
        theView.bundle(
          "PLEASE FOLLOW THIS LINK TO CONFIRM YOUR E-MAIL ADDRESS THANK YOU" ),
        this.session );

    }

    modelBean.sendEmail( currentParticipant,
                         "You have logged in to TOP Racing.",
                         this.session );

    updateAuthenticatedSession( currentParticipant );
    theView.showUI( UI.WELCOME,
                    currentParticipant );
  }

  public void clickResetPasswordRequest( Participant user ) {
    LOGGER.log( Level.INFO,
                "clickResetPasswordRequest for email={0}",
                user == null ? "null" : user.getEmail() );
    System.out.println( new Date()
                        + " clickResetPasswordRequest for "
                        + (user == null ? "null" : user.getEmail()) );
    boolean emailAccepted = modelBean.resetPasswordRequest( user,
                                                            this.session );
    theView.showUI( emailAccepted
                    ? UI.PASSWORD_RESET_REQUEST
                    : UI.ERROR_PASSWORD_RESET_REQUEST,
                    user );
  }

  public void clickResetPasswordConfirm( Participant _currentParticipant ) {
    modelBean.resetPasswordConfirm( _currentParticipant,
                                    session );
  }

  public void clickNewParticipant() {
    currentParticipant = modelBean.createParticipant();

    LOGGER.info( "clickNewParticipant created placeholder participant with venue=" +
                 (currentParticipant == null
                  ? "null"
                  : currentParticipant.getVenue() == null
                    ? "venue-null"
                    : currentParticipant.getVenue().getId()) );
    System.out.println( new Date()
                        + " clickNewParticipant created placeholder venue="
                        + (currentParticipant == null
                           ? "null"
                           : currentParticipant.getVenue() == null
                           ? "venue-null"
                           : currentParticipant.getVenue().getId()) );

    if( currentParticipant == null ) {
      theView.showUI( UI.ERROR_CREATE_REGISTRATION_DUPLICATE );
      return;
    }
    theView.showUI( UI.EDIT_USER,
                    currentParticipant );
  }

  public void clickSave( Participant user ) {

    if( user.getId() == null
        && modelBean.getParticipantByEmail( user ) != null ) {
      theView.showUI( UI.ERROR_EDIT_USER_EXISTS );
      return;
    }
    if( user.getPassword().length() < 2 ) {
      theView.showUI( UI.ERROR_EDIT_USER_PASSWORD );
      return;
    }

    currentParticipant = modelBean.save( user,
                                         true );

    modelBean.sendConfirmationRequest(
      currentParticipant,
      theView.bundle(
        "PLEASE FOLLOW THIS LINK TO CONFIRM YOUR E-MAIL ADDRESS THANK YOU" ),
      this.session );

    long numUsuariosActivos = modelBean.incNumUsuariosActivos();
    System.out.println(
      "---- Ya hay " + numUsuariosActivos + " usuarios activos. ----" );

    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW USER HAS BEEN REGISTERED" ),
      this.session
    );
    updateAuthenticatedSession( currentParticipant );
    theView.showUI( UI.WELCOME,
                    currentParticipant );

  }

  public void clickViewWelcome() {
    LOGGER.info( "clickViewWelcome requested" );
    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewWelcome() ***",
                               session );
    theView.showUI( UI.WELCOME );
  }

  public void clickViewPointscounts() {
    LOGGER.info( "clickViewPointscounts requested" );
    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewPointscounts() ***",
                               session );
    theView.showUI( UI.LIST_POINTSCOUNTS );
  }

  public void clickViewPenalties() {

    LOGGER.info( "clickViewPenalties requested" );

    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewPenalties() ***",
                               session );
    this.currentTracksetLevel = 0;
    this.currentPeriodLevel = 0;
    theView.showUI( UI.LIST_PENALTIES,
                    this.currentTracksetLevel,
                    this.currentPeriodLevel );
  }

  public void clickNewRegatta( int ui ) {
    if( !isValidParticipant() ) {
      return;
    }

    Regatta regatta = modelBean.createRegatta( currentParticipant );
    // modelBean.requestRecalculateRegattaPenalties();
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW REGATTA HAS BEEN CREATED" )
      + "\tid:" + regatta.getId(),
      this.session
    );
    this.previousUI.push( ui );
    theView.showUI( UI.EDIT_REGATTA,
                    regatta );
  }

  public void clickEditRegatta( Regatta regatta,
                                int ui ) {
    if( !isValidParticipant() ) {
      return;
    }

    regatta = getOwnedRegatta( regatta,
                               UI.ERROR_REGATTA_NOT_OWNED );
    if( regatta == null ) {
      return;
    }

    this.previousUI.push( ui );
    theView.showUI( UI.EDIT_REGATTA,
                    regatta );
  }

  public void clickSetRegattaStatusToNextStatus( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    if( regatta == null ) {
      return;
    }

    regatta = modelBean.getRegattaById( regatta.getId() );

    if( !Objects.equals( regatta.getParticipant()
      .getId(),
                         currentParticipant.getId() ) ) {
      theView.showUI( UI.ERROR_REGATTA_NOT_OWNED );
      return;
    }

    switch( regatta.getStatus() ) {
      case RegattaStatus.CREATED:
        if( regatta.getVariant() == null ) {
          theView.showUI( UI.ERROR_SAVE_REGATTA );
          return;
        }
        /*
         * if( regatta.getDatetime() .before( new Date() ) ) { theView.showUI(
         * UI.ERROR_SAVE_REGATTA ); return; }
         */
        break;

      case RegattaStatus.REGISTRATIONS_OPEN:
//        modelBean.validateRegattaRegistrations( regatta );
        modelBean.sendRegistrationsListMessages( regatta );
        break;

      case RegattaStatus.SPEED_TEST:
        if( !modelBean.areValidRegattaSpeedResults( regatta ) ) {
          theView.showUI( UI.INVALID_SPEED_RESULTS );
          return;
        }
        modelBean.assignRegattaSpeedPos( regatta.getId() );
        modelBean.recalculatePointscounts(
          ( p )
          -> this.theView.setProgress( p )
        );
        modelBean.assignRegattaGridPositions( regatta.getId() );
        modelBean.sendSpeedTestResultsMessages( regatta );
        break;

      case RegattaStatus.RACE_TEST:
        if( !modelBean.areValidRegattaRaceResults( regatta ) ) {
          theView.showUI( UI.INVALID_RACE_RESULTS );
          return;
        }
        // modelBean.checkDNFs( regatta );
        modelBean.assignRegattaIndividualFinishingPrize( regatta );
        modelBean.sendRaceTestResultsMessages( regatta );
        break;

      case RegattaStatus.AUCTION:
        modelBean.assignRegattaEfficiencyPositions( regatta );
        modelBean.assignRegattaIndividualEfficiencyPrize( regatta );
        modelBean.updateCarOwners( regatta );
        modelBean.sendAuctionResultsMessages(
          regatta,
          theView.bundle( "AUCTION BUYER EMAIL" ),
          theView.bundle( "AUCTION SELLER EMAIL" )
        );
        modelBean.sendBalanceMessages(
          regatta,
          theView.bundle( "REGATTA BALANCE OWNER" ),
          theView.bundle( "REGATTA BALANCE PROMOTER" )
        );
        break;

      case RegattaStatus.PUBLISHED:
        theView.showUI( UI.EDIT_REGATTA );
        return;
    }

    // Assign next status
    regatta.setStatus(
      (byte) ( regatta.getStatus()
               + 1 )
    );
    modelBean.save( regatta );
    modelBean.recalculatePointscounts(
      ( p )
      -> this.theView.setProgress( p )
    );

    for( Registration r
         : modelBean.getRegattaRegistrations( regatta ) ) {
      modelBean.sendEmail(
        r.getParticipantByIdOwner(),
        theView.bundle(
          "THE STATUS OF A REGATTA HAS BEEN CHANGED" )
        + "\n regattaId: " + regatta.getId()
        + "\n status: " + RegattaStatus.NAME[ regatta.getStatus() ],
        this.session
      );
    }

    theView.showUI( UI.REGATTA_STATUS_CHANGED,
                    regatta );
  }

  public void clickViewEditRegattaResults( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    theView.showUI( UI.VIEW_EDIT_REGATTA_RESULTS,
                    regatta );
  }

  public void returnFromRegattaResults( List<Bid> bids ) {
    modelBean.save( bids );
    theView.showUI( UI.EDIT_REGATTA );

  }

  public void clickSaveRegattaResults( List<Bid> bids ) {
    if( !isValidParticipant() ) {
      return;
    }

    List<Bid> sanitizedBids = sanitizeRegattaResultsEdits( bids );
    modelBean.save( sanitizedBids );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
//    theView.showUI( UI.VIEW_EDIT_REGATTA_RESULTS );
  }

  private List<Bid> sanitizeRegattaResultsEdits( List<Bid> bids ) {
    List<Bid> sanitizedBids = new ArrayList<>();
    if( bids == null ) {
      return sanitizedBids;
    }

    for( Bid incomingBid
         : bids ) {
      if( incomingBid == null
          || incomingBid.getRegistration() == null
          || incomingBid.getRegistration().getId() == null ) {
        continue;
      }

      Registration persistedRegistration = modelBean.getRegistrationById(
        incomingBid.getRegistration().getId() );
      if( persistedRegistration == null ) {
        continue;
      }

      applyAllowedRegattaResultEdits( persistedRegistration,
                                      incomingBid.getRegistration() );
      sanitizedBids.add( buildSanitizedBid( incomingBid,
                                            persistedRegistration ) );
    }

    return sanitizedBids;
  }

  private void applyAllowedRegattaResultEdits( Registration persisted,
                                               Registration incoming ) {
    if( !isCurrentParticipantRegattaOwner( persisted ) ) {
      return;
    }

    Regatta regatta = persisted.getRegatta();
    if( regatta == null || regatta.getStatus() >= RegattaStatus.PUBLISHED ) {
      return;
    }

    persisted.setStatus( incoming.getStatus() );

    if( regatta.getStatus() == RegattaStatus.SPEED_TEST
        && incoming.getStatus() == RegistrationStatus.OK ) {
      persisted.setSecondsLap( incoming.getSecondsLap() );
    }

    if( regatta.getStatus() == RegattaStatus.RACE_TEST
        && incoming.getStatus() != RegistrationStatus.INVALID ) {
      persisted.setPosRace( incoming.getPosRace() );
      persisted.setLapsRace( incoming.getLapsRace() );
    }
  }

  private Bid buildSanitizedBid( Bid incomingBid,
                                 Registration persistedRegistration ) {
    Bid sanitizedBid = getExistingBidForCurrentParticipant(
      persistedRegistration.getId() );
    boolean existingBid = sanitizedBid != null;
    if( sanitizedBid == null ) {
      sanitizedBid = new Bid();
      sanitizedBid.setStatus( 0 );
    }

    sanitizedBid.setId( new Tables.BidId( currentParticipant.getId(),
                                          persistedRegistration.getId() ) );
    sanitizedBid.setParticipant( currentParticipant );
    sanitizedBid.setRegistration( persistedRegistration );
    sanitizedBid.setDate( incomingBid.getDate() == null
                          ? new Date()
                          : incomingBid.getDate() );

    if( canCurrentParticipantEditBid( persistedRegistration ) ) {
      sanitizedBid.setAmmount( incomingBid.getAmmount() );
    } else if( !existingBid ) {
      sanitizedBid.setAmmount( 0.0 );
    }

    return sanitizedBid;
  }

  private Bid getExistingBidForCurrentParticipant( Long registrationId ) {
    try {
      return modelBean.getBidById( new Tables.BidId( currentParticipant.getId(),
                                                     registrationId ) );
    } catch( RuntimeException e ) {
      return null;
    }
  }

  private boolean isCurrentParticipantRegattaOwner( Registration registration ) {
    return registration.getRegatta() != null
           && registration.getRegatta().getParticipant() != null
           && Objects.equals( registration.getRegatta()
             .getParticipant()
             .getId(),
                              currentParticipant.getId() );
  }

  private boolean canCurrentParticipantEditBid( Registration registration ) {
    Regatta regatta = registration.getRegatta();
    return regatta != null
           && regatta.getStatus() >= RegattaStatus.SPEED_TEST
           && regatta.getStatus() <= RegattaStatus.AUCTION
           && registration.getStatus() == RegistrationStatus.OK
           && !hasCurrentParticipantLocalPromoterBlock( regatta );
  }

  public void clickAddToFinishingPrize( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    regatta = getOwnedRegatta( regatta,
                               UI.ERROR_REGATTA_NOT_OWNED );
    if( regatta == null ) {
      return;
    }

    regatta.setPrizeFinishing( regatta.getPrizeFinishing() + 100 );
    modelBean.save( regatta );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );

    theView.showUI( UI.EDIT_REGATTA );
  }

  public void clickAddToEfficiencyPrize( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    regatta = getOwnedRegatta( regatta,
                               UI.ERROR_REGATTA_NOT_OWNED );
    if( regatta == null ) {
      return;
    }

    regatta.setPrizeEfficiency( regatta.getPrizeEfficiency() + 100 );
    modelBean.save( regatta );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
    theView.showUI( UI.EDIT_REGATTA );
  }

  public void clickSave( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    Regatta persistedRegatta = getOwnedRegatta( regatta,
                                                UI.ERROR_REGATTA_NOT_OWNED );
    if( persistedRegatta == null ) {
      return;
    }

    regatta.setParticipant( persistedRegatta.getParticipant() );
    modelBean.save( regatta );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );

    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A REGATTA HAS BEEN SAVED" )
      + "\n regattaId: " + regatta.getId(),
      this.session
    );
    theView.showUI( UI.EDIT_REGATTA );
  }

  public void clickViewRegistrations() {
    LOGGER.info( "clickViewRegistrations requested" );
    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewRegistrations() ***",
                               session );
    theView.showUI( UI.LIST_REGISTRATIONS );
  }

  public void clickViewCars() {
    theView.showUI( UI.LIST_CARS );
  }

  public void clickViewDrivers() {
    theView.showUI( UI.LIST_DRIVERS );
  }

  public void clickAddRegistration( Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }

    if( hasCurrentParticipantLocalPromoterBlock( regatta ) ) {
      theView.showUI( UI.ERROR_LOCAL_PROMOTER_BLOCKED );
      return;
    }

    if( regatta.getStatus() != RegattaStatus.REGISTRATIONS_OPEN ) {
      theView.showUI( UI.ERROR_REGATTA_NOT_OPEN );
      return;
    }

    Registration r = modelBean.createRegistration( regatta,
                                                   currentParticipant );

    this.previousUI.push( UI.VIEW_EDIT_REGATTA_RESULTS );

    theView.showUI( UI.EDIT_REGISTRATION,
                    r );
    //theView.showUI( UI.ERROR_CREATE_REGISTRATION_DUPLICATE );
  }

  public void clickEditRegistration( Registration registration,
                                     int from_ui ) {
    if( !isValidParticipant() ) {
      return;
    }

    registration = getOwnedRegistration( registration,
                                         UI.ERROR_EDIT_REGISTRATION_USER );
    if( registration == null ) {
      return;
    }

    this.previousUI.push( from_ui );

    theView.showUI( UI.EDIT_REGISTRATION,
                    registration );
  }

  /*
   * public void clickAddToRegistrationPayment( Registration registration ) {
   * if( !isValidParticipant() ) { return; }
   *
   * registration.setPaidfee( registration.getPaidfee() + 100 ); modelBean.save(
   * registration );
   *
   * modelBean.sendEmail( registration.getRegatta() .getParticipant(),
   * theModel.bundle( "A NEW PAYMENT FOR A REGISTRATION HAS BEEN RECEIVED." ) +
   * "\nregattaId: " + registration.getId() .getRegattaid() + "\ndriverId: " +
   * registration.getId() .getDriverid(), this.session ); modelBean.sendEmail(
   * currentParticipant, .gettheModel.bundle( "View/BundleViewSpanish" ) "A NEW
   * PAYMENT FOR A REGISTRATION HAS BEEN RECEIVED." ) + "\nregattaId: " +
   * registration.getId() .getRegattaid() + "\ndriverId: " +
   * registration.getId() .getDriverid(), this.session );
   *
   * theView.showUI( UI.EDIT_REGISTRATION ); }
   */
  public void clickSave( Registration registration ) {
    if( !isValidParticipant() ) {
      return;
    }

    Registration persistedRegistration = getOwnedRegistration(
                   registration,
                   UI.ERROR_EDIT_REGISTRATION_USER );
    if( persistedRegistration == null ) {
      return;
    }

    if( hasCurrentParticipantLocalPromoterBlock( persistedRegistration.getRegatta() ) ) {
      theView.showUI( UI.ERROR_LOCAL_PROMOTER_BLOCKED );
      return;
    }

    if( this.currentParticipant.getId().longValue()
        != registration.getCar().getParticipant().getId().longValue() ) {

      registration.setStatus( RegistrationStatus.INVALID );

      theView.showUI( UI.ERROR_NOT_CAR_OWNER );
      return;
    }

    registration.setStatus( RegistrationStatus.INCOMPLETE );

    registration.setParticipantByIdOwner(
      persistedRegistration.getParticipantByIdOwner()
    );
    modelBean.save( registration );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A REGISTRATION HAS BEEN SAVED" )
      + "\nregattaId: " + registration.getRegatta().getId()
      + "\ndriverId: " + registration.getParticipantByIdDriver().getId()
      + "\ncarId: " + registration.getCar().getId()
      + "\nownerId: " + registration.getCar().getParticipant().getId(),
      this.session
    );
    modelBean.sendEmail(
      registration.getRegatta().getParticipant(),
      theView.bundle(
        "A REGISTRATION HAS BEEN SAVED" )
      + "\nregattaId: " + registration.getRegatta().getId()
      + "\ndriverId: " + registration.getParticipantByIdDriver().getId()
      + "\ncarId: " + registration.getCar().getId()
      + "\nownerId: " + registration.getCar().getParticipant().getId(),
      this.session
    );
    theView.showUI( UI.EDIT_REGISTRATION );
  }

  public void clickNewCar() {
    if( !isValidParticipant() ) {
      return;
    }

    Car car = modelBean.createCar( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle( "A NEW CAR HAS BEEN REGISTERED" )
      + "\nCarId: " + car.getId(),
      this.session
    );
    theView.showUI( UI.EDIT_CAR,
                    car );
  }

  public void clickEditCar( Car car ) {
    if( !isValidParticipant() ) {
      return;
    }

    car = getOwnedCar( car,
                       UI.ERROR_EDIT_CAR );
    if( car == null ) {
      return;
    }

    theView.showUI( UI.EDIT_CAR,
                    car );
  }

  public void clickSave( Car car ) {
    if( !isValidParticipant() ) {
      return;
    }

    Car persistedCar = getOwnedCar( car,
                                    UI.ERROR_EDIT_CAR );
    if( persistedCar == null ) {
      return;
    }

    car.setParticipant( persistedCar.getParticipant() );
    modelBean.save( car );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A CAR HAS BEEN SAVED" )
      + "\ncarId: " + car.getId(),
      this.session
    );
    theView.showUI( UI.EDIT_CAR );
  }

  public void clickViewVariants( Regatta r ) {

    theView.showUI( UI.LIST_VARIANTS,
                    currentParticipant == null
                    || currentParticipant.getId() == null
                    || currentParticipant.getId() <= 0
                    || !currentParticipant.getId().equals(
                      r.getParticipant().getId() )
                    || r.getStatus() != RegattaStatus.CREATED );
  }

  public void clickNewVariant() {
    if( !isValidParticipant() ) {
      return;
    }

    Variant variant = modelBean.createVariant( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW VARIANT HAS BEEN REGISTERED" )
      + "\nvariantId: " + variant.getId(),
      session
    );
    theView.showUI( UI.EDIT_VARIANT,
                    variant );
  }

  public void clickEditVariant( Variant variant ) {
    if( !isValidParticipant() ) {
      return;
    }

    variant = getOwnedVariant( variant,
                               UI.ERROR_EDIT_VARIANT );
    if( variant == null ) {
      return;
    }

    theView.showUI( UI.EDIT_VARIANT,
                    variant );
  }

  public void clickSave( Variant variant ) {
    if( !isValidParticipant() ) {
      return;
    }

    Variant persistedVariant = getOwnedVariant( variant,
                                                UI.ERROR_EDIT_VARIANT );
    if( persistedVariant == null ) {
      return;
    }

    variant.setIdCreator( persistedVariant.getIdCreator() );
    modelBean.save( variant );
    modelBean.sendEmail(
      this.currentParticipant,
      theView.bundle(
        "A VARIANT HAS BEEN SAVED" )
      + "\nvariantId: " + variant.getId(),
      session
    );
    theView.showUI( UI.EDIT_VARIANT );
  }

  public void clickViewVenues( int currentUI ) {
    this.previousUI.push( currentUI );

    theView.showUI( UI.LIST_VENUES );
  }

  public void clickNewVenue() {
    if( !isValidParticipant() ) {
      return;
    }

    Venue venue = modelBean.createVenue( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW VENUE HAS BEEN REGISTERED" )
      + "\nvenueId: " + venue.getId(),
      session
    );
    theView.showUI( UI.EDIT_VENUE,
                    venue );
  }

  public void clickEditVenue( Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }

    venue = getOwnedVenue( venue,
                           UI.ERROR_EDIT_VENUE );
    if( venue == null ) {
      return;
    }

    theView.showUI( UI.EDIT_VENUE,
                    venue );
  }

  public void clickSave( Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }

    Venue persistedVenue = getOwnedVenue( venue,
                                          UI.ERROR_EDIT_VENUE );
    if( persistedVenue == null ) {
      return;
    }

    venue.setIdCreator( persistedVenue.getIdCreator() );
    venue.setParticipant( persistedVenue.getParticipant() );
    modelBean.save( venue );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A VENUE HAS BEEN SAVED" )
      + "\nvenueId: " + venue.getId(),
      session
    );
    theView.showUI( UI.EDIT_VENUE );
  }

  public void clickViewProvinceregions() {

    theView.showUI( UI.LIST_PROVINCEREGIONS );
  }

  public void clickNewProvinceregion() {
    if( !isValidParticipant() ) {
      return;
    }

    Provinceregion provinceregion = modelBean.createProvinceregion(
                   currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW PROVINCE REGION HAS BEEN REGISTERED" )
      + "\nprovinceregionId: " + provinceregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_PROVINCEREGION,
                    provinceregion );
  }

  public void clickEditProvinceregion( Provinceregion provinceregion ) {

    theView.showUI( UI.EDIT_PROVINCEREGION,
                    provinceregion );
  }

  public void clickSave( Provinceregion provinceregion ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( provinceregion );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A PROVINCE REGION HAS BEEN SAVED" )
      + "\nprovinceregionId: " + provinceregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_PROVINCEREGION );
  }

  public void clickViewProvinces() {

    theView.showUI( UI.LIST_PROVINCES );
  }

  public void clickNewProvince() {
    if( !isValidParticipant() ) {
      return;
    }

    Province province = modelBean.createProvince( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW PROVINCE HAS BEEN REGISTERED" )
      + "\nprovinceId: " + province.getId(),
      session
    );
    theView.showUI( UI.EDIT_PROVINCE,
                    province );
  }

  public void clickEditProvince( Province province ) {

    theView.showUI( UI.EDIT_PROVINCE,
                    province );
  }

  public void clickSave( Province province ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( province );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A PROVINCE HAS BEEN SAVED" )
      + "\nprovinceId: " + province.getId(),
      session
    );
    theView.showUI( UI.EDIT_PROVINCE );
  }

  public void clickViewCountryregions() {

    theView.showUI( UI.LIST_COUNTRYREGIONS );
  }

  public void clickNewCountryregion() {
    if( !isValidParticipant() ) {
      return;
    }

    Countryregion countryregion = modelBean
                  .createCountryregion( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW COUNTRY REGION HAS BEEN REGISTERED" )
      + "\ncountryregionId: " + countryregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_COUNTRYREGION,
                    countryregion );
  }

  public void clickEditCountryregion( Countryregion countryregion ) {

    theView.showUI( UI.EDIT_COUNTRYREGION,
                    countryregion );
  }

  public void clickSave( Countryregion countryregion ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( countryregion );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A COUNTRY REGION HAS BEEN SAVED" )
      + "\ncountryregionId: " + countryregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_COUNTRYREGION );
  }

  public void clickViewCountries() {

    theView.showUI( UI.LIST_COUNTRIES );
  }

  public void clickNewCountry() {
    if( !isValidParticipant() ) {
      return;
    }

    Country country = modelBean.createCountry( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW COUNTRY HAS BEEN REGISTERED" )
      + "\ncountryId: " + country.getId(),
      session
    );
    theView.showUI( UI.EDIT_COUNTRY,
                    country );
  }

  public void clickEditCountry( Country country ) {

    theView.showUI( UI.EDIT_COUNTRY,
                    country );
  }

  public void clickSave( Country country ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( country );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A COUNTRY HAS BEEN SAVED" )
      + "\ncountryId: " + country.getId(),
      session
    );
    theView.showUI( UI.EDIT_COUNTRY );
  }

  public void clickViewPlanetregions() {

    theView.showUI( UI.LIST_PLANETREGIONS );
  }

  public void clickNewPlanetregion() {
    if( !isValidParticipant() ) {
      return;
    }

    Planetregion planetregion = modelBean
                 .createPlanetregion( currentParticipant );
    modelBean.sendMonitorMail(
      currentParticipant,
      theView.bundle(
        "A NEW PLANET REGION HAS BEEN REGISTERED" )
      + "\nplanetregionId: " + planetregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_PLANETREGION,
                    planetregion );
  }

  public void clickEditPlanetregion( Planetregion planetregion ) {

    theView.showUI( UI.EDIT_PLANETREGION,
                    planetregion );
  }

  public void clickSave( Planetregion planetregion ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( planetregion );
    modelBean.sendEmail(
      currentParticipant,
      theView.bundle(
        "A PLANET REGION HAS BEEN SAVED" )
      + "\nplanetregionId: " + planetregion.getId(),
      session
    );
    theView.showUI( UI.EDIT_PLANETREGION );
  }

  public void clickViewVenueInMap( Venue venue,
                                   int ui ) {

    this.previousUI.push( ui );
    theView.showUI( UI.EDIT_VENUE_IN_MAP,
                    venue );
  }

  public void periodLevelChanged( int _periodLevel ) {
    this.currentPeriodLevel = _periodLevel;
    this.theView.showUI( UI.LIST_PENALTIES,
                         currentTracksetLevel,
                         currentPeriodLevel );
  }

  public void saveVenue( Venue _venue ) {
    if( !isValidParticipant() ) {
      return;
    }
    if( _venue.getIdCreator() == this.currentParticipant.getId() ) {
      this.modelBean.save( _venue );
    }
  }

  public void tracksetLevelChanged( int _tracksetLevel ) {
    this.currentTracksetLevel = _tracksetLevel;
    this.theView.showUI( UI.LIST_PENALTIES,
                         currentTracksetLevel,
                         currentPeriodLevel );
  }

  public void venueCoordinatesChanged( Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }

    modelBean.save( venue );
  }

  public void clickReturn( int ui ) {

    switch( ui ) {
      case UI.LIST_POINTSCOUNTS:
      case UI.LIST_REGATTAS:
      case UI.LIST_REGISTRATIONS:
      case UI.LIST_PENALTIES:
        theView.showUI( UI.WELCOME );
        break;
      case UI.LIST_CARS:
      case UI.LIST_DRIVERS:
        theView.showUI( UI.EDIT_REGISTRATION );
        break;
      case UI.LIST_VARIANTS:
        theView.showUI( UI.EDIT_REGATTA );
        break;
      case UI.LIST_VENUES:
        theView.showUI( this.previousUI.pop() );
        break;
      case UI.LIST_PROVINCEREGIONS:
        theView.showUI( UI.EDIT_VENUE );
        break;
      case UI.LIST_PROVINCES:
        theView.showUI( UI.EDIT_PROVINCEREGION );
        break;
      case UI.LIST_COUNTRYREGIONS:
        theView.showUI( UI.EDIT_PROVINCE );
        break;
      case UI.LIST_COUNTRIES:
        theView.showUI( UI.EDIT_COUNTRYREGION );
        break;
      case UI.LIST_PLANETREGIONS:
        theView.showUI( UI.EDIT_COUNTRY );
        break;
      case UI.EDIT_VENUE_IN_MAP:
        theView.showUI( this.previousUI.pop() );
        break;
    }
  }

  public void clickEndEdit( int ui ) {

    switch( ui ) {
      case UI.EDIT_USER:
        theView.showUI( UI.WELCOME );
        break;
      case UI.EDIT_CAR:
        theView.showUI( UI.LIST_CARS );
        break;
      case UI.EDIT_REGISTRATION:
        theView.showUI( this.previousUI.pop() );
        break;
      case UI.EDIT_REGATTA:
        theView.showUI( this.previousUI.pop() );
        break;
      case UI.EDIT_VARIANT:
        theView.showUI( UI.LIST_VARIANTS );
        break;
      case UI.EDIT_VENUE:
        theView.showUI( UI.LIST_VENUES );
        break;
      case UI.EDIT_PROVINCEREGION:
        theView.showUI( UI.LIST_PROVINCEREGIONS );
        break;
      case UI.EDIT_PROVINCE:
        theView.showUI( UI.LIST_PROVINCES );
        break;
      case UI.EDIT_COUNTRYREGION:
        theView.showUI( UI.LIST_COUNTRYREGIONS );
        break;
      case UI.EDIT_COUNTRY:
        theView.showUI( UI.LIST_COUNTRIES );
        break;
      case UI.EDIT_PLANETREGION:
        theView.showUI( UI.LIST_PLANETREGIONS );
        break;
    }
  }

  public void clickSelectCar( Car car,
                              Registration registration ) {
    if( !isValidParticipant() ) {
      return;
    }
    Registration persistedRegistration = getOwnedRegistration(
                   registration,
                   UI.ERROR_EDIT_REGISTRATION_USER );
    if( persistedRegistration == null ) {
      return;
    }
    if( hasCurrentParticipantLocalPromoterBlock( persistedRegistration.getRegatta() ) ) {
      theView.showUI( UI.ERROR_LOCAL_PROMOTER_BLOCKED );
      return;
    }
    if( currentParticipant.getId().longValue()
        != car.getParticipant().getId().longValue() ) {

      theView.showUI( UI.ERROR_NOT_CAR_OWNER );
      return;
    }

    persistedRegistration.setCar( car );
    persistedRegistration.setParticipantByIdBuyer( // just to have a default buyer
      car.getParticipant()
    );
    modelBean.save( persistedRegistration );

    registration.setCar( car );
    registration.setParticipantByIdBuyer( // just to have a default buyer
      car.getParticipant()
    );

    theView.showUI( UI.EDIT_REGISTRATION );
  }

  public void clickSelectDriver( Participant driver,
                                 Registration registration ) {
    if( !isValidParticipant() ) {
      return;
    }

    Registration persistedRegistration = getOwnedRegistration(
                   registration,
                   UI.ERROR_EDIT_REGISTRATION_USER );
    if( persistedRegistration == null ) {
      return;
    }

    if( hasCurrentParticipantLocalPromoterBlock( persistedRegistration.getRegatta() )
        || modelBean.hasActiveLocalPromoterBlock(
          driver,
          persistedRegistration.getRegatta().getParticipant() ) ) {
      theView.showUI( UI.ERROR_LOCAL_PROMOTER_BLOCKED );
      return;
    }

    persistedRegistration.setParticipantByIdDriver( driver );
    modelBean.save( persistedRegistration );

    registration.setParticipantByIdDriver( driver );

    theView.showUI( UI.EDIT_REGISTRATION );
  }

  public void clickSelectVariant( Variant variant,
                                  Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }
    regatta = getOwnedRegatta( regatta,
                               UI.LIST_VARIANTS );
    if( regatta == null ) {
      theView.showUI( UI.LIST_VARIANTS );
      return;
    }

    regatta.setVariant( variant );
    modelBean.save( regatta );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );

    theView.showUI( UI.EDIT_REGATTA );
  }

  public void clickSelectVenue( Venue venue,
                                Variant variant ) {
    int previousUi = this.previousUI.last();
    if( previousUi != UI.EDIT_USER
        && !isValidParticipant() ) {
      return;
    }

    switch( previousUi ) {
      case UI.EDIT_VARIANT:
        variant = getOwnedVariant( variant,
                                   UI.LIST_VENUES );
        if( variant == null ) {
          theView.showUI( UI.LIST_VENUES );
          return;
        }

        variant.setVenue( venue );
        modelBean.save( variant );

        break;

      case UI.EDIT_USER:
        if( currentParticipant == null ) {
          theView.showUI( UI.LOGIN );
          return;
        }
        currentParticipant.setVenue( venue );
        break;
    }
    theView.showUI( this.previousUI.pop() );

  }

  public void clickSelectProvinceregion( Provinceregion provinceregion,
                                         Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }
    venue = getOwnedVenue( venue,
                           UI.LIST_PROVINCEREGIONS );
    if( venue == null ) {
      theView.showUI( UI.LIST_PROVINCEREGIONS );
      return;
    }

    venue.setProvinceregion( provinceregion );
    modelBean.save( venue );

    theView.showUI( UI.EDIT_VENUE );
  }

  public void clickSelectProvince( Province province,
                                   Provinceregion provinceregion ) {
    if( !isValidParticipant() ) {
      return;
    }
    provinceregion = getOwnedProvinceregion( provinceregion,
                                             UI.LIST_PROVINCES );
    if( provinceregion == null ) {
      theView.showUI( UI.LIST_PROVINCES );
      return;
    }

    provinceregion.setProvince( province );
    modelBean.save( provinceregion );

    theView.showUI( UI.EDIT_PROVINCEREGION );
  }

  public void clickSelectCountryregion( Countryregion countryregion,
                                        Province province ) {
    if( !isValidParticipant() ) {
      return;
    }
    province = getOwnedProvince( province,
                                 UI.LIST_COUNTRYREGIONS );
    if( province == null ) {
      theView.showUI( UI.LIST_COUNTRYREGIONS );
      return;
    }

    province.setCountryregion( countryregion );
    modelBean.save( province );

    theView.showUI( UI.EDIT_PROVINCE );
  }

  public void clickSelectCountry( Country country,
                                  Countryregion countryregion ) {
    if( !isValidParticipant() ) {
      return;
    }
    countryregion = getOwnedCountryregion( countryregion,
                                           UI.LIST_COUNTRIES );
    if( countryregion == null ) {
      theView.showUI( UI.LIST_COUNTRIES );
      return;
    }

    countryregion.setCountry( country );
    modelBean.save( countryregion );

    theView.showUI( UI.EDIT_COUNTRYREGION );
  }

  public void clickSelectPlanetregion( Planetregion planetregion,
                                       Country country ) {
    if( !isValidParticipant() ) {
      return;
    }
    country = getOwnedCountry( country,
                               UI.LIST_PLANETREGIONS );
    if( country == null ) {
      theView.showUI( UI.LIST_PLANETREGIONS );
      return;
    }

    country.setPlanetregion( planetregion );
    modelBean.save( country );

    theView.showUI( UI.EDIT_COUNTRY );
  }

  public void clickOK() {
    // TemplateBasic.xhtml has dlgInfo button OK
    // no action is necessary, only close window
  }

  private Regatta getOwnedRegatta( Regatta regatta,
                                   int errorUi ) {
    if( regatta == null || regatta.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Regatta persistedRegatta = modelBean.getRegattaById( regatta.getId() );
    if( persistedRegatta == null
        || persistedRegatta.getParticipant() == null
        || currentParticipant == null
        || !Objects.equals( persistedRegatta.getParticipant().getId(),
                            currentParticipant.getId() ) ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedRegatta;
  }

  private Registration getOwnedRegistration( Registration registration,
                                             int errorUi ) {
    if( registration == null || registration.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Registration persistedRegistration = modelBean.getRegistrationById(
                   registration.getId() );
    if( persistedRegistration == null
        || persistedRegistration.getParticipantByIdOwner() == null
        || currentParticipant == null
        || !Objects.equals(
          persistedRegistration.getParticipantByIdOwner().getId(),
          currentParticipant.getId() ) ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedRegistration;
  }

  private Car getOwnedCar( Car car,
                           int errorUi ) {
    if( car == null || car.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Car persistedCar = modelBean.getCarById( car.getId() );
    if( persistedCar == null
        || persistedCar.getParticipant() == null
        || currentParticipant == null
        || !Objects.equals( persistedCar.getParticipant().getId(),
                            currentParticipant.getId() ) ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedCar;
  }

  private Variant getOwnedVariant( Variant variant,
                                   int errorUi ) {
    if( variant == null || variant.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Variant persistedVariant = modelBean.getVariantById( variant.getId() );
    if( persistedVariant == null
        || currentParticipant == null
        || persistedVariant.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedVariant;
  }

  private Venue getOwnedVenue( Venue venue,
                               int errorUi ) {
    if( venue == null || venue.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Venue persistedVenue = modelBean.getVenueById( venue.getId() );
    if( persistedVenue == null
        || currentParticipant == null
        || persistedVenue.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedVenue;
  }

  private Provinceregion getOwnedProvinceregion( Provinceregion provinceregion,
                                                 int errorUi ) {
    if( provinceregion == null || provinceregion.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Provinceregion persistedProvinceregion = modelBean.getProvinceregionById(
      provinceregion.getId() );
    if( persistedProvinceregion == null
        || currentParticipant == null
        || persistedProvinceregion.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedProvinceregion;
  }

  private Province getOwnedProvince( Province province,
                                     int errorUi ) {
    if( province == null || province.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Province persistedProvince = modelBean.getProvinceById( province.getId() );
    if( persistedProvince == null
        || currentParticipant == null
        || persistedProvince.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedProvince;
  }

  private Countryregion getOwnedCountryregion( Countryregion countryregion,
                                               int errorUi ) {
    if( countryregion == null || countryregion.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Countryregion persistedCountryregion = modelBean.getCountryregionById(
      countryregion.getId() );
    if( persistedCountryregion == null
        || currentParticipant == null
        || persistedCountryregion.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedCountryregion;
  }

  private Country getOwnedCountry( Country country,
                                   int errorUi ) {
    if( country == null || country.getId() == null ) {
      theView.showUI( errorUi );
      return null;
    }
    Country persistedCountry = modelBean.getCountryById( country.getId() );
    if( persistedCountry == null
        || currentParticipant == null
        || persistedCountry.getIdCreator() != currentParticipant.getId() ) {
      theView.showUI( errorUi );
      return null;
    }
    return persistedCountry;
  }

  public void buyerComplaint( Registration r ) {

    modelBean.setCarSellerAsDefaulter( r );

    modelBean.sendMonitorMail(
      r.getParticipantByIdOwner(),
      theView.bundle(
        "A CAR OWNER DEFAULTER HAS BEEN REPORTED" )
      + "\nRegatta Id: " + r.getRegatta().getId()
      + "\nDriver Id: " + r.getParticipantByIdDriver().getId()
      + "\nCar Id: " + r.getCar()
      + "\nOwner Id:" + r.getParticipantByIdOwner().getId()
      + "\nByuer Id:" + r.getParticipantByIdBuyer().getId(),
      this.session
    );

  }

  public void sellerComplaint( Registration r ) {

    modelBean.setCarBuyerAsDefaulter( r );

    modelBean.sendMonitorMail(
      r.getParticipantByIdBuyer(),
      theView.bundle(
        "A BUYER DEFAULTER HAS BEEN REPORTED" )
      + "\nRegatta Id: " + r.getRegatta().getId()
      + "\nDriver Id: " + r.getParticipantByIdDriver().getId()
      + "\nCar Id:" + r.getCar().getId()
      + "\nOwner Id:" + r.getParticipantByIdOwner().getId()
      + "\nByuer Id:" + r.getParticipantByIdBuyer().getId(),
      this.session
    );

  }

  public void promoterBalanceComplaint( Registration r ) {
    modelBean.setParticipantAsLocalDefaulter(
      r.getRegatta().getParticipant(),
      r.getRegatta().getParticipant(),
      currentParticipant,
      "Promoter balance default reported for registration " + r.getId() );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
    modelBean.sendMonitorMail(
      r.getRegatta().getParticipant(),
      "A PROMOTER DEFAULTER HAS BEEN REPORTED"
      + "\nRegatta Id: " + r.getRegatta().getId()
      + "\nOwner Id: " + r.getParticipantByIdOwner().getId()
      + "\nPromoter Id: " + r.getRegatta().getParticipant().getId()
      + "\nRegistration Id: " + r.getId(),
      this.session
    );
  }

  public void ownerBalanceComplaint( Registration r ) {
    modelBean.setParticipantAsDefaulter( r.getParticipantByIdOwner() );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
    modelBean.sendMonitorMail(
      r.getParticipantByIdOwner(),
      "AN OWNER DEFAULTER HAS BEEN REPORTED"
      + "\nRegatta Id: " + r.getRegatta().getId()
      + "\nOwner Id: " + r.getParticipantByIdOwner().getId()
      + "\nPromoter Id: " + r.getRegatta().getParticipant().getId()
      + "\nRegistration Id: " + r.getId(),
      this.session
    );
  }

  public void clickViewNextRegatta( Long _id ) {

    Regatta r = this.modelBean.getRegattaById( _id + 1 );

    if( r == null ) {
      this.theView.showUI( UI.EDIT_REGATTA );
      return;
    }

    this.theView.showUI( UI.EDIT_REGATTA,
                         r );
  }

  public void clickViewPreviousRegatta( Long _id ) {

    Regatta r = modelBean.getRegattaById( _id - 1 );

    if( r == null ) {
      this.theView.showUI( UI.EDIT_REGATTA );
      return;
    }

    this.theView.showUI( UI.EDIT_REGATTA,
                         r );
  }

  public void registrationStatusChanged( Regatta _regatta ) {
    if( !isValidParticipant() ) {
      return;
    }
    this.modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
  }

  private void updateAuthenticatedSession( Participant participant ) {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if( facesContext == null ) {
      return;
    }

    ExternalContext externalContext = facesContext.getExternalContext();
    if( participant == null
        || participant.getId() == null
        || participant.getId() <= 0 ) {
      externalContext.getSessionMap().remove( AUTH_SESSION_KEY );
      return;
    }

    externalContext.getSessionMap().put( AUTH_SESSION_KEY,
                                         participant.getId() );
  }

}

