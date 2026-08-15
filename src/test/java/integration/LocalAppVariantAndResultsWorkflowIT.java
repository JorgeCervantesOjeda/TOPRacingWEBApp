package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Controller.UI;
import Model.ModelBean;
import Tables.Bid;
import Tables.BidId;
import Tables.Car;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import View.ViewForController;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppVariantAndResultsWorkflowIT {

  private static final ModelBean MODEL = new ModelBean();

  @Test
  void ownerCanSelectVariantForOwnedCreatedRegatta() throws Exception {
    Participant promoter = createSavedParticipant( "variant-owner",
                                                   "Owner-123" );
    ControllerHarness harness = createHarness( promoter );
    Regatta regatta = MODEL.createRegatta( promoter );
    Variant replacement = createSavedVariant( promoter,
                                              "Selected Variant" );

    harness.controller.clickSelectVariant( replacement,
                                           regatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( replacement.getId(),
                  refreshed.getVariant().getId() );
    assertEquals( UI.EDIT_REGATTA,
                  harness.view.lastUi );
  }

  @Test
  void nonOwnerCannotSelectVariantForForeignRegatta() throws Exception {
    Participant promoter = createSavedParticipant( "variant-promoter",
                                                   "Promoter-123" );
    Participant intruder = createSavedParticipant( "variant-intruder",
                                                   "Intruder-123" );
    ControllerHarness harness = createHarness( intruder );
    Regatta regatta = MODEL.createRegatta( promoter );
    Long originalVariantId = regatta.getVariant().getId();
    Variant replacement = createSavedVariant( intruder,
                                              "Intruder Variant" );

    harness.controller.clickSelectVariant( replacement,
                                           regatta );

    Regatta refreshed = MODEL.getRegattaById( regatta.getId() );
    assertEquals( originalVariantId,
                  refreshed.getVariant().getId() );
    assertEquals( UI.LIST_VARIANTS,
                  harness.view.lastUi );
  }

  @Test
  void participantCanSaveAuctionBidThroughRegattaResultsController() throws Exception {
    Participant promoter = createSavedParticipant( "bid-promoter",
                                                   "Promoter-123" );
    Participant bidder = createSavedParticipant( "bidder",
                                                 "Bidder-123" );
    Car car = createSavedCar( bidder,
                              "bid-car" );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.AUCTION );
    MODEL.save( regatta );
    Registration registration = MODEL.createRegistration( regatta,
                                                          bidder );
    registration.setCar( car );
    registration.setParticipantByIdOwner( bidder );
    registration.setParticipantByIdDriver( bidder );
    registration.setParticipantByIdBuyer( bidder );
    registration.setStatus( RegistrationStatus.OK );
    MODEL.save( registration );
    Registration persistedRegistration = MODEL.getRegistrationById( registration.getId() );

    ControllerHarness harness = createHarness( bidder );
    Bid bid = new Bid( new BidId( bidder.getId(),
                                  persistedRegistration.getId() ),
                       bidder,
                       persistedRegistration,
                       77.7,
                       new Date(),
                       0 );

    harness.controller.clickSaveRegattaResults( List.of( bid ) );

    List<Bid> bids = MODEL.getBids( persistedRegistration );
    assertFalse( bids.isEmpty() );
    assertTrue( bids.stream().anyMatch( saved
      -> saved.getParticipant().getId().equals( bidder.getId() )
         && Math.abs( saved.getAmmount() - 77.7 ) < 0.0001 ) );
  }

  @Test
  void promoterCanSaveSpeedTestLapTimeThroughRegattaResultsController() throws Exception {
    Participant promoter = createSavedParticipant( "speed-promoter",
                                                   "Promoter-123" );
    Participant owner = createSavedParticipant( "speed-owner",
                                                "Owner-123" );
    Car car = createSavedCar( owner,
                              "speed-car" );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.SPEED_TEST );
    MODEL.save( regatta );
    Registration registration = createOwnedRegistration( regatta,
                                                         owner,
                                                         car,
                                                         RegistrationStatus.OK );
    Registration attackRegistration = MODEL.getRegistrationById(
      registration.getId() );
    attackRegistration.setSecondsLap( 12.34 );
    Bid ownerBid = promoterBid( promoter,
                                attackRegistration );

    ControllerHarness harness = createHarness( promoter );
    harness.controller.clickSaveRegattaResults( List.of( ownerBid ) );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertEquals( 12.34,
                  refreshed.getSecondsLap(),
                  0.0001 );
  }

  @Test
  void promoterCanSaveRaceResultsThroughRegattaResultsController() throws Exception {
    Participant promoter = createSavedParticipant( "race-promoter",
                                                   "Promoter-123" );
    Participant owner = createSavedParticipant( "race-owner",
                                                "Owner-123" );
    Car car = createSavedCar( owner,
                              "race-car" );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.RACE_TEST );
    MODEL.save( regatta );
    Registration registration = createOwnedRegistration( regatta,
                                                         owner,
                                                         car,
                                                         RegistrationStatus.OK );
    Registration attackRegistration = MODEL.getRegistrationById(
      registration.getId() );
    attackRegistration.setPosRace( (short) 1 );
    attackRegistration.setLapsRace( (short) 15 );
    Bid ownerBid = promoterBid( promoter,
                                attackRegistration );

    ControllerHarness harness = createHarness( promoter );
    harness.controller.clickSaveRegattaResults( List.of( ownerBid ) );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertEquals( 1,
                  refreshed.getPosRace() );
    assertEquals( 15,
                  refreshed.getLapsRace() );
  }

  @Test
  void nonOwnerCannotOverwriteSpeedTestResultThroughRegattaResultsController() throws Exception {
    Participant promoter = createSavedParticipant( "speed-owner-negative",
                                                   "Promoter-123" );
    Participant intruder = createSavedParticipant( "speed-intruder-negative",
                                                   "Intruder-123" );
    Participant owner = createSavedParticipant( "speed-owner-driver",
                                                "Owner-123" );
    Car car = createSavedCar( owner,
                              "speed-attack-car" );
    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.SPEED_TEST );
    MODEL.save( regatta );
    Registration registration = createOwnedRegistration( regatta,
                                                         owner,
                                                         car,
                                                         RegistrationStatus.OK );
    registration.setSecondsLap( 45.67 );
    MODEL.save( registration );

    Registration attackRegistration = MODEL.getRegistrationById(
      registration.getId() );
    attackRegistration.setSecondsLap( 9.99 );
    Bid intruderBid = promoterBid( intruder,
                                   attackRegistration );
    intruderBid.setAmmount( 11.0 );

    ControllerHarness harness = createHarness( intruder );
    harness.controller.clickSaveRegattaResults( List.of( intruderBid ) );

    Registration refreshed = MODEL.getRegistrationById( registration.getId() );
    assertEquals( 45.67,
                  refreshed.getSecondsLap(),
                  0.0001 );
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

  private Car createSavedCar( Participant owner,
                              String nickname ) {
    Car car = MODEL.createCar( owner );
    car.setNickname( nickname + "-" + UUID.randomUUID() );
    car.setWeight( 100.0 );
    car.setWidth( 10.0 );
    MODEL.save( car );
    return MODEL.getCarById( car.getId() );
  }

  private Registration createOwnedRegistration( Regatta regatta,
                                                Participant owner,
                                                Car car,
                                                byte status ) {
    Registration registration = MODEL.createRegistration( regatta,
                                                          owner );
    registration.setCar( car );
    registration.setParticipantByIdOwner( owner );
    registration.setParticipantByIdDriver( owner );
    registration.setParticipantByIdBuyer( owner );
    registration.setStatus( status );
    MODEL.save( registration );
    return MODEL.getRegistrationById( registration.getId() );
  }

  private Bid promoterBid( Participant participant,
                           Registration registration ) {
    return new Bid( new BidId( participant.getId(),
                               registration.getId() ),
                    participant,
                    registration,
                    0.0,
                    new Date(),
                    0 );
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

  private ControllerHarness createHarness( Participant currentParticipant ) throws Exception {
    CapturingView view = new CapturingView();
    Controller controller = new Controller( view );

    setPrivateField( controller,
                     "modelBean",
                     MODEL );
    setPrivateField( controller,
                     "session",
                     4444L );
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
