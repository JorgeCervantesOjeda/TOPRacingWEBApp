package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Controller.UI;
import Model.ModelBean;
import Tables.Car;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import View.ViewForController;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppRegistrationWorkflowIT {

  private static final ModelBean MODEL = new ModelBean();

  @Test
  void ownerCanCreateRegistrationSelectOwnedCarAndSave() throws Exception {
    Participant promoter = createSavedParticipant( "promoter",
                                                   "Promoter-123" );
    Participant owner = createSavedParticipant( "owner",
                                                "Owner-123" );
    Car firstCar = createSavedCar( owner,
                                   "first-car" );
    Car secondCar = createSavedCar( owner,
                                    "second-car" );
    Regatta regatta = createOpenRegatta( promoter );
    ControllerHarness harness = createHarness( owner );

    harness.controller.clickAddRegistration( regatta );

    assertEquals( UI.EDIT_REGISTRATION,
                  harness.view.lastUi );
    assertNotNull( harness.view.lastRegistration );

    Registration registration = harness.view.lastRegistration;
    assertNotNull( registration.getCar() );
    assertEquals( owner.getId(),
                  registration.getParticipantByIdOwner().getId() );

    harness.controller.clickSelectCar( secondCar,
                                       registration );
    registration.setBetFinishing( 12.5 );
    registration.setBetEfficiency( 7.5 );
    harness.controller.clickSave( registration );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertNotNull( refreshed );
    assertEquals( secondCar.getId(),
                  refreshed.getCar().getId() );
    assertEquals( owner.getId(),
                  refreshed.getParticipantByIdOwner().getId() );
    assertEquals( RegistrationStatus.INCOMPLETE,
                  refreshed.getStatus() );
    assertEquals( 12.5,
                  refreshed.getBetFinishing(),
                  0.0001 );
    assertEquals( 7.5,
                  refreshed.getBetEfficiency(),
                  0.0001 );
    assertEquals( UI.EDIT_REGISTRATION,
                  harness.view.lastUi );
  }

  @Test
  void ownerCannotSelectCarOwnedBySomeoneElse() throws Exception {
    Participant promoter = createSavedParticipant( "promoter-select",
                                                   "Promoter-123" );
    Participant owner = createSavedParticipant( "owner-select",
                                                "Owner-123" );
    Participant otherOwner = createSavedParticipant( "other-owner",
                                                     "Other-123" );
    createSavedCar( owner,
                    "owner-car" );
    Car foreignCar = createSavedCar( otherOwner,
                                     "foreign-car" );
    Regatta regatta = createOpenRegatta( promoter );
    ControllerHarness harness = createHarness( owner );

    harness.controller.clickAddRegistration( regatta );
    Registration registration = harness.view.lastRegistration;
    Long originalCarId = registration.getCar().getId();

    harness.controller.clickSelectCar( foreignCar,
                                       registration );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertEquals( UI.ERROR_NOT_CAR_OWNER,
                  harness.view.lastUi );
    assertEquals( originalCarId,
                  refreshed.getCar().getId() );
  }

  private Regatta createOpenRegatta( Participant promoter ) {
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.REGISTRATIONS_OPEN );
    MODEL.save( regatta );
    return MODEL.getRegattaById( regatta.getId() );
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
    participant.setDefaulter( 0 );
    return MODEL.save( participant,
                       false );
  }

  private ControllerHarness createHarness( Participant currentParticipant ) throws Exception {
    CapturingView view = new CapturingView();
    Controller controller = new Controller( view );

    setPrivateField( controller,
                     "modelBean",
                     MODEL );
    setPrivateField( controller,
                     "session",
                     4343L );
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
    private Registration lastRegistration;

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
                        Registration registration ) {
      lastUi = ui;
      lastRegistration = registration;
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
    public String bundle( String id ) {
      return id;
    }

    @Override
    public void setProgress( double p ) {
    }
  }
}
