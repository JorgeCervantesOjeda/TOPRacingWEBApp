package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.UI;
import Model.ModelBean;
import Tables.Participant;
import Tables.Regatta;
import View.ViewForController;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppRegattaWorkflowIT {

  private static final ModelBean MODEL = new ModelBean();

  @Test
  void ownerCanAdvanceCreatedRegattaToRegistrationsOpen() throws Exception {
    Participant owner = createSavedParticipant( "advance",
                                               "Advance-123" );
    ControllerHarness harness = createHarness( owner );

    Regatta regatta = MODEL.createRegatta( owner );
    assertNotNull( regatta.getVariant() );

    harness.controller.clickSetRegattaStatusToNextStatus( regatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( RegattaStatus.REGISTRATIONS_OPEN,
                  refreshed.getStatus() );
    assertEquals( UI.REGATTA_STATUS_CHANGED,
                  harness.view.lastUi );
    assertNotNull( harness.view.lastRegatta );
    assertEquals( regatta.getId(),
                  harness.view.lastRegatta.getId() );
  }

  @Test
  void ownerCanAddTrustPromiseToFinishingPrize() throws Exception {
    Participant owner = createSavedParticipant( "finishing",
                                               "Finishing-123" );
    ControllerHarness harness = createHarness( owner );

    Regatta regatta = MODEL.createRegatta( owner );
    double initialPrize = regatta.getPrizeFinishing();

    harness.controller.clickAddToFinishingPrize( regatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( initialPrize + 100.0,
                  refreshed.getPrizeFinishing(),
                  0.0001 );
    assertEquals( UI.EDIT_REGATTA,
                  harness.view.lastUi );
  }

  @Test
  void ownerCanAddTrustPromiseToEfficiencyPrize() throws Exception {
    Participant owner = createSavedParticipant( "efficiency",
                                               "Efficiency-123" );
    ControllerHarness harness = createHarness( owner );

    Regatta regatta = MODEL.createRegatta( owner );
    double initialPrize = regatta.getPrizeEfficiency();

    harness.controller.clickAddToEfficiencyPrize( regatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( initialPrize + 100.0,
                  refreshed.getPrizeEfficiency(),
                  0.0001 );
    assertEquals( UI.EDIT_REGATTA,
                  harness.view.lastUi );
  }

  private ControllerHarness createHarness( Participant currentParticipant ) throws Exception {
    CapturingView view = new CapturingView();
    Controller controller = new Controller( view );

    setPrivateField( controller,
                     "modelBean",
                     MODEL );
    setPrivateField( controller,
                     "session",
                     4242L );
    setPrivateField( controller,
                     "currentParticipant",
                     currentParticipant );

    return new ControllerHarness( controller,
                                  view );
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
    private Regatta lastRegatta;

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
                        Regatta regatta ) {
      lastUi = ui;
      lastRegatta = regatta;
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
    public String bundle( String id ) {
      return id;
    }

    @Override
    public void setProgress( double p ) {
    }

    @Override
    public void invalidateSessionAndShowUI( int ui ) {
      lastUi = ui;
    }
  }
}
