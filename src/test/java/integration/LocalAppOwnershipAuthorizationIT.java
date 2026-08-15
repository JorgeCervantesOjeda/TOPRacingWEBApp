package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Controller.UI;
import Model.ModelBean;
import Tables.Car;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import View.ViewForController;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppOwnershipAuthorizationIT {

  private static final ModelBean MODEL = new ModelBean();

  @Test
  void foreignParticipantCannotEditOrSaveForeignRegatta() throws Exception {
    Participant promoter = createSavedParticipant( "regatta-owner",
                                                   "Owner-123" );
    Participant intruder = createSavedParticipant( "regatta-intruder",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setTrackrental( 25.0 );
    regatta.setStatus( RegattaStatus.CREATED );
    MODEL.save( regatta );

    harness.controller.clickEditRegatta( regatta,
                                         UI.LIST_PENALTIES );

    assertEquals( UI.ERROR_REGATTA_NOT_OWNED,
                  harness.view.lastUi );

    Regatta attackRegatta = MODEL.getRegattaById( regatta.getId() );
    attackRegatta.setTrackrental( 999.0 );

    harness.controller.clickSave( attackRegatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( UI.ERROR_REGATTA_NOT_OWNED,
                  harness.view.lastUi );
    assertEquals( 25.0,
                  refreshed.getTrackrental(),
                  0.0001 );
  }

  @Test
  void foreignParticipantCannotEditOrSaveForeignCar() throws Exception {
    Participant owner = createSavedParticipant( "car-owner",
                                                "Owner-123" );
    Participant intruder = createSavedParticipant( "car-intruder",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Car car = createSavedCar( owner,
                              "protected-car" );
    String originalNickname = car.getNickname();

    harness.controller.clickEditCar( car );

    assertEquals( UI.ERROR_EDIT_CAR,
                  harness.view.lastUi );

    Car attackCar = MODEL.getCarById( car.getId() );
    attackCar.setNickname( "hijacked-" + UUID.randomUUID() );

    harness.controller.clickSave( attackCar );

    Car refreshed = MODEL.getCarById( car.getId() );
    assertEquals( UI.ERROR_EDIT_CAR,
                  harness.view.lastUi );
    assertEquals( originalNickname,
                  refreshed.getNickname() );
    assertEquals( owner.getId(),
                  refreshed.getParticipant().getId() );
  }

  @Test
  void foreignParticipantCannotEditOrSaveForeignVariant() throws Exception {
    Participant owner = createSavedParticipant( "variant-owner-negative",
                                                "Owner-123" );
    Participant intruder = createSavedParticipant( "variant-intruder-negative",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Variant variant = createSavedVariant( owner,
                                          "protected-variant" );
    String originalName = variant.getName();

    harness.controller.clickEditVariant( variant );

    assertEquals( UI.ERROR_EDIT_VARIANT,
                  harness.view.lastUi );

    Variant attackVariant = MODEL.getVariantById( variant.getId() );
    attackVariant.setName( "hijacked-" + UUID.randomUUID() );

    harness.controller.clickSave( attackVariant );

    Variant refreshed = MODEL.getVariantById( variant.getId() );
    assertEquals( UI.ERROR_EDIT_VARIANT,
                  harness.view.lastUi );
    assertEquals( originalName,
                  refreshed.getName() );
    assertEquals( owner.getId().longValue(),
                  refreshed.getIdCreator() );
  }

  @Test
  void foreignParticipantCannotEditOrSaveForeignVenue() throws Exception {
    Participant owner = createSavedParticipant( "venue-owner-negative",
                                                "Owner-123" );
    Participant intruder = createSavedParticipant( "venue-intruder-negative",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Venue venue = createSavedVenue( owner,
                                    "protected-venue" );
    String originalName = venue.getName();

    harness.controller.clickEditVenue( venue );

    assertEquals( UI.ERROR_EDIT_VENUE,
                  harness.view.lastUi );

    Venue attackVenue = MODEL.getVenueById( venue.getId() );
    attackVenue.setName( "hijacked-" + UUID.randomUUID() );

    harness.controller.clickSave( attackVenue );

    Venue refreshed = MODEL.getVenueById( venue.getId() );
    assertEquals( UI.ERROR_EDIT_VENUE,
                  harness.view.lastUi );
    assertEquals( originalName,
                  refreshed.getName() );
    assertEquals( owner.getId().longValue(),
                  refreshed.getIdCreator() );
  }

  @Test
  void foreignParticipantCannotEditSelectOrSaveForeignRegistration() throws Exception {
    Participant promoter = createSavedParticipant( "registration-promoter-negative",
                                                   "Promoter-123" );
    Participant owner = createSavedParticipant( "registration-owner-negative",
                                                "Owner-123" );
    Participant intruder = createSavedParticipant( "registration-intruder-negative",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.REGISTRATIONS_OPEN );
    MODEL.save( regatta );

    Car ownerCar = createSavedCar( owner,
                                   "owner-car-negative" );
    Car intruderCar = createSavedCar( intruder,
                                      "intruder-car-negative" );
    Registration registration = MODEL.createRegistration( regatta,
                                                          owner );
    registration.setCar( ownerCar );
    registration.setParticipantByIdOwner( owner );
    registration.setParticipantByIdDriver( owner );
    registration.setParticipantByIdBuyer( owner );
    registration.setStatus( RegistrationStatus.INCOMPLETE );
    MODEL.save( registration );

    harness.controller.clickEditRegistration( registration,
                                              UI.VIEW_EDIT_REGATTA_RESULTS );

    assertEquals( UI.ERROR_EDIT_REGISTRATION_USER,
                  harness.view.lastUi );

    harness.controller.clickSelectCar( intruderCar,
                                       registration );

    Registration afterSelectAttempt = MODEL.getRegistrationById(
      registration.getId() );
    assertEquals( UI.ERROR_EDIT_REGISTRATION_USER,
                  harness.view.lastUi );
    assertEquals( ownerCar.getId(),
                  afterSelectAttempt.getCar().getId() );

    Registration attackRegistration = MODEL.getRegistrationById(
      registration.getId() );
    attackRegistration.setCar( intruderCar );
    attackRegistration.setParticipantByIdDriver( intruder );

    harness.controller.clickSave( attackRegistration );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertEquals( UI.ERROR_EDIT_REGISTRATION_USER,
                  harness.view.lastUi );
    assertEquals( ownerCar.getId(),
                  refreshed.getCar().getId() );
    assertEquals( owner.getId(),
                  refreshed.getParticipantByIdOwner().getId() );
    assertEquals( owner.getId(),
                  refreshed.getParticipantByIdDriver().getId() );
  }

  private Participant createSavedParticipant( String label,
                                              String password ) {
    String unique = label + "-" + UUID.randomUUID();
    Participant participant = MODEL.createParticipant();
    participant.setPassword( password );
    participant.setNamesGiven( "Codex" );
    participant.setNamesFamily( unique );
    participant.setEmail( unique + "@example.com" );
    participant.setPhone( "5555555555" );
    participant.setConfirmed( true );
    participant.acceptCurrentTerms();
    participant.setDefaulter( 0 );
    return MODEL.save( participant,
                       false );
  }

  private Car createSavedCar( Participant owner,
                              String nickname ) {
    Car car = MODEL.createCar( owner );
    car.setNickname( nickname + "-" + UUID.randomUUID() );
    car.setWeight( 100.0 );
    car.setWidth( 10.0 );
    MODEL.save( car );
    return MODEL.getCarById( car.getId() );
  }

  private Variant createSavedVariant( Participant creator,
                                      String name ) {
    Variant variant = MODEL.createVariant( creator );
    variant.setName( name + "-" + UUID.randomUUID() );
    variant.setLength( 1.5 );
    variant.setMinWidth( 0.5 );
    MODEL.save( variant );
    return MODEL.getVariantById( variant.getId() );
  }

  private Venue createSavedVenue( Participant creator,
                                  String name ) {
    Venue venue = MODEL.createVenue( creator );
    venue.setName( name + "-" + UUID.randomUUID() );
    venue.setMeridian( -99.1332 );
    venue.setParallel( 19.4326 );
    MODEL.save( venue );
    return MODEL.getVenueById( venue.getId() );
  }

  private ControllerHarness createHarness( Participant currentParticipant ) throws Exception {
    CapturingView view = new CapturingView();
    Controller controller = new Controller( view );

    setPrivateField( controller,
                     "modelBean",
                     MODEL );
    setPrivateField( controller,
                     "session",
                     4545L );
    setPrivateField( controller,
                     "currentParticipant",
                     currentParticipant );

    return new ControllerHarness( controller,
                                  view );
  }

  private static void setPrivateField( Object target,
                                       String fieldName,
                                       Object value ) throws Exception {
    Field field = Controller.class.getDeclaredField( fieldName );
    field.setAccessible( true );
    field.set( target,
               value );
  }

  private static final class ControllerHarness {

    private final Controller controller;
    private final CapturingView view;

    private ControllerHarness( Controller controller,
                               CapturingView view ) {
      this.controller = controller;
      this.view = view;
    }
  }

  private static final class CapturingView
    implements ViewForController {

    private int lastUi = -1;

    @Override
    public void showUI( int ui ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Participant user ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Regatta regatta ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Registration registration ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Car car ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Variant variant ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Venue venue ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Provinceregion provinceregion ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Province province ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Countryregion countryregion ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Country country ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        Tables.Planetregion planetregion ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        boolean flag ) {
      lastUi = ui;
    }

    @Override
    public void showUI( int ui,
                        int currentTracksetLevel,
                        int currentPeriodLevel ) {
      lastUi = ui;
    }

    @Override
    public String bundle( String key ) {
      return key;
    }

    @Override
    public void invalidateSessionAndShowUI( int ui ) {
      lastUi = ui;
    }

    @Override
    public void setProgress( double progress ) {
    }
  }
}
