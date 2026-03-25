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
import java.util.Date;
import java.util.List;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */
public class Controller {

  private int currentPeriodLevel;

  private int currentTracksetLevel;

  private final ViewForController theView;

  private ModelBean modelBean;
  private final PilaInteger previousUI;
  private Participant currentParticipant;
  private long session;

  public Controller( ViewForController view ) {
    this.theView = view;
    previousUI = new PilaInteger();
  }

  public void clickLanguageChange( int _languageId ) {
    theView.showUI( UI.WELCOME );
  }

  public long newSession( ModelForView modelForView ) {
    this.session = -1;
    this.modelBean = (ModelBean) modelForView;
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
    theView.showUI( UI.WELCOME );
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

  public void clickEditProfile() {
    if( !isValidParticipant() ) {
      return;
    }

    theView.showUI( UI.EDIT_USER,
                    currentParticipant );
  }

  public void clickLogout() {
    this.currentParticipant = null;
    long numUsuariosActivos = modelBean.decNumUsuariosActivos();

    System.out.println(
      "---- Quedan " + numUsuariosActivos + " usuarios activos. ----" );

    theView.showUI( UI.WELCOME,
                    this.currentParticipant );
  }

  public void clickLogin( Participant user ) {

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
                         "You have logged in to top-racing.org.",
                         this.session );

    theView.showUI( UI.WELCOME,
                    currentParticipant );
  }

  public void clickResetPasswordRequest( Participant user ) {
    modelBean.resetPasswordRequest( user,
                                    this.session );
    theView.showUI( UI.PASSWORD_RESET_REQUEST,
                    user );
  }

  public void clickResetPasswordConfirm( Participant _currentParticipant ) {
    modelBean.resetPasswordConfirm( _currentParticipant,
                                    session );
  }

  public void clickNewParticipant() {
    currentParticipant = modelBean.createParticipant();

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
    theView.showUI( UI.WELCOME,
                    currentParticipant );

  }

  public void clickViewWelcome() {
    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewWelcome() ***",
                               session );
    theView.showUI( UI.WELCOME );
  }

  public void clickViewPointscounts() {
    modelBean.sendMonitorMail( currentParticipant,
                               "*** clickViewPointscounts() ***",
                               session );
    theView.showUI( UI.LIST_POINTSCOUNTS );
  }

  public void clickViewPenalties() {

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
    regatta = modelBean.getRegattaById( regatta.getId() );

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

    modelBean.save( bids );
    modelBean.requestRecalculateRegattaPenalties(
      ( p )
      -> this.theView.setProgress( p )
    );
//    theView.showUI( UI.VIEW_EDIT_REGATTA_RESULTS );
  }

  public void clickAddToFinishingPrize( Regatta regatta ) {
    if( !isValidParticipant() ) {
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

    if( this.currentParticipant.getId().longValue()
        != registration.getCar().getParticipant().getId().longValue() ) {

      registration.setStatus( RegistrationStatus.INVALID );

      theView.showUI( UI.ERROR_NOT_CAR_OWNER );
      return;
    }

    registration.setStatus( RegistrationStatus.INCOMPLETE );

    registration.setParticipantByIdOwner(
      registration.getCar()
        .getParticipant()
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

    theView.showUI( UI.EDIT_CAR,
                    car );
  }

  public void clickSave( Car car ) {
    if( !isValidParticipant() ) {
      return;
    }

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

    theView.showUI( UI.EDIT_VARIANT,
                    variant );
  }

  public void clickSave( Variant variant ) {
    if( !isValidParticipant() ) {
      return;
    }

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
    currentParticipant = modelBean.save( currentParticipant,
                                         true );

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

    theView.showUI( UI.EDIT_VENUE,
                    venue );
  }

  public void clickSave( Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }

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
    if( currentParticipant.getId().longValue()
        != car.getParticipant().getId().longValue() ) {

      theView.showUI( UI.ERROR_NOT_CAR_OWNER );
      return;
    }

    registration.setCar( car );
    registration.setParticipantByIdBuyer( // just to have a default buyer
      car.getParticipant()
    );
    modelBean.save( registration );

    theView.showUI( UI.EDIT_REGISTRATION );
  }

  public void clickSelectDriver( Participant driver,
                                 Registration registration ) {
    if( !isValidParticipant() ) {
      return;
    }

    registration.setParticipantByIdDriver( driver );
    modelBean.save( registration );

    theView.showUI( UI.EDIT_REGISTRATION );
  }

  public void clickSelectVariant( Variant variant,
                                  Regatta regatta ) {
    if( !isValidParticipant() ) {
      return;
    }
    if( !Objects.equals( currentParticipant.getId(),
                         regatta
                           .getParticipant()
                           .getId() ) ) {
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
    if( !isValidParticipant() ) {
      return;
    }

    switch( this.previousUI.last() ) {
      case UI.EDIT_VARIANT:

        if( currentParticipant.getId() != variant.getIdCreator() ) {
          theView.showUI( UI.LIST_VENUES );
          return;
        }

        variant.setVenue( venue );
        modelBean.save( variant );

        break;

      case UI.EDIT_USER:
        currentParticipant.setVenue( venue );
        modelBean.save( currentParticipant,
                        false );
    }
    theView.showUI( this.previousUI.pop() );

  }

  public void clickSelectProvinceregion( Provinceregion provinceregion,
                                         Venue venue ) {
    if( !isValidParticipant() ) {
      return;
    }
    if( currentParticipant.getId() != venue.getIdCreator() ) {
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
    if( currentParticipant.getId() != provinceregion.getIdCreator() ) {
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
    if( currentParticipant.getId() != province.getIdCreator() ) {
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
    if( currentParticipant.getId() != countryregion.getIdCreator() ) {
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
    if( currentParticipant.getId() != country.getIdCreator() ) {
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

}

