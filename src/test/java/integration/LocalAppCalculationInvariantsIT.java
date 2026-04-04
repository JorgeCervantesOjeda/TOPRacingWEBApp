package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Model.LevelPeriod;
import Model.LevelTrackset;
import Model.ModelBean;
import Model.ProgressBar;
import Tables.Bid;
import Tables.BidId;
import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Participant;
import Tables.Penaltiespl;
import Tables.Planetregion;
import Tables.Pointscount;
import Tables.PointscountId;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LocalAppCalculationInvariantsIT {

  private static final ProgressBar NOOP_PROGRESS = ( value ) -> {
  };

  @Test
  void isolatedRegattaKeepsNonPlanetPenaltiesAtZeroAcrossAllPeriods() {
    ModelBean model = new ModelBean();
    Participant promoter = createSavedParticipant( model,
                                                   "penalty-promoter",
                                                   "Penalty-123" );
    Variant isolatedVariant = createIsolatedVariant( model,
                                                     promoter,
                                                     "penalty" );
    Regatta regatta = createSavedRegatta( model,
                                          promoter,
                                          isolatedVariant,
                                          RegattaStatus.SPEED_TEST,
                                          new Date( 1775260800000L ) );

    Car ownerCar = createSavedCar( model,
                                   promoter,
                                   "penalty-car" );
    Registration registration = model.createRegistration( regatta,
                                                          promoter );
    registration.setCar( ownerCar );
    registration.setParticipantByIdOwner( promoter );
    registration.setParticipantByIdDriver( promoter );
    registration.setParticipantByIdBuyer( promoter );
    registration.setStatus( RegistrationStatus.OK );
    model.save( registration );

    model.recalculateRegattaPenalties( NOOP_PROGRESS );

    Map<Integer, Penaltiespl> penaltiesByPeriod = model.getPenaltiespl()
      .stream()
      .filter( penalties
        -> penalties.getId()
          .getIdRegatta() == regatta.getId() )
      .collect( Collectors.toMap( penalties
                                  -> penalties.getId().getLevelPeriod(),
                                  penalties
                                  -> penalties ) );

    assertEquals( LevelPeriod.WEEK_OF_MONTH + 1,
                  penaltiesByPeriod.size() );

    for( int periodLevel = LevelPeriod.CONTINUOUS;
         periodLevel <= LevelPeriod.WEEK_OF_MONTH;
         periodLevel++ ) {
      Penaltiespl penalties = penaltiesByPeriod.get( periodLevel );
      assertNotNull( penalties );
      assertEquals( 0,
                    penalties.getValue1() );
      assertEquals( 0,
                    penalties.getValue2() );
      assertEquals( 0,
                    penalties.getValue3() );
      assertEquals( 0,
                    penalties.getValue4() );
      assertEquals( 0,
                    penalties.getValue5() );
      assertEquals( 0,
                    penalties.getValue6() );
      assertEquals( 0,
                    penalties.getValue7() );
    }
  }

  @Test
  void isolatedPointscountUsesExactPositionsAtVariantLevel() {
    ModelBean model = new ModelBean();
    Participant competitor = createSavedParticipant( model,
                                                     "points-competitor",
                                                     "Points-123" );
    Variant isolatedVariant = createIsolatedVariant( model,
                                                     competitor,
                                                     "points" );
    Date regattaDate = new Date( 1775347200000L );
    Regatta regatta = createSavedRegatta( model,
                                          competitor,
                                          isolatedVariant,
                                          RegattaStatus.SPEED_TEST,
                                          regattaDate );
    regatta.setMinutesRace( 10 );
    model.save( regatta );

    Car competitorCar = createSavedCar( model,
                                        competitor,
                                        "points-car" );
    Registration registration = model.createRegistration( regatta,
                                                          competitor );
    registration.setCar( competitorCar );
    registration.setParticipantByIdOwner( competitor );
    registration.setParticipantByIdDriver( competitor );
    registration.setParticipantByIdBuyer( competitor );
    registration.setSecondsLap( 60.0 );
    registration.setPosSpeed( (short) 1 );
    registration.setPosRace( (short) 2 );
    registration.setPosEfficiency( (short) 3 );
    registration.setStatus( RegistrationStatus.OK );
    model.save( registration );

    model.recalculateRegattaPenalties( NOOP_PROGRESS );

    long periodId = model.getPeriodId( regattaDate,
                                       LevelPeriod.CONTINUOUS );
    Pointscount pointscount = model.getPointscountById(
      new PointscountId( competitor.getId(),
                         LevelPeriod.CONTINUOUS,
                         periodId,
                         LevelTrackset.VARIANT,
                         isolatedVariant.getId() ) );

    assertNotNull( pointscount );
    assertEquals( 100.0,
                  pointscount.getPointsSD(),
                  0.0001 );
    assertEquals( 63.0,
                  pointscount.getPointsRD(),
                  0.0001 );
    assertEquals( 40.0,
                  pointscount.getPointsED(),
                  0.0001 );
    assertEquals( 100.0,
                  pointscount.getPointsSO(),
                  0.0001 );
    assertEquals( 63.0,
                  pointscount.getPointsRO(),
                  0.0001 );
    assertEquals( 40.0,
                  pointscount.getPointsEO(),
                  0.0001 );
  }

  @Test
  void finishingAndEfficiencyPrizesFollowTheirDocumentedWeightRules() {
    ModelBean model = new ModelBean();
    Participant promoter = createSavedParticipant( model,
                                                   "prize-promoter",
                                                   "Prize-123" );
    Participant ownerA = createSavedParticipant( model,
                                                 "prize-owner-a",
                                                 "Prize-123" );
    Participant ownerB = createSavedParticipant( model,
                                                 "prize-owner-b",
                                                 "Prize-123" );
    Variant isolatedVariant = createIsolatedVariant( model,
                                                     promoter,
                                                     "prizes" );
    Regatta regatta = createSavedRegatta( model,
                                          promoter,
                                          isolatedVariant,
                                          RegattaStatus.SPEED_TEST,
                                          new Date( 1775433600000L ) );
    regatta.setMinutesRace( 10 );
    regatta.setPrizeFinishing( 100.0 );
    regatta.setPrizeEfficiency( 200.0 );
    model.save( regatta );

    Registration registrationA = createSavedRegistration( model,
                                                          regatta,
                                                          ownerA,
                                                          createSavedCar(
                                                            model,
                                                            ownerA,
                                                            "prize-car-a" ) );
    registrationA.setSecondsLap( 60.0 );
    registrationA.setLapsRace( (short) 10 );
    registrationA.setBetFinishing( 30.0 );
    registrationA.setBetEfficiency( 20.0 );
    registrationA.setPosEfficiency( (short) 1 );
    registrationA.setStatus( RegistrationStatus.OK );
    model.save( registrationA );

    Registration registrationB = createSavedRegistration( model,
                                                          regatta,
                                                          ownerB,
                                                          createSavedCar(
                                                            model,
                                                            ownerB,
                                                            "prize-car-b" ) );
    registrationB.setSecondsLap( 60.0 );
    registrationB.setLapsRace( (short) 5 );
    registrationB.setBetFinishing( 10.0 );
    registrationB.setBetEfficiency( 10.0 );
    registrationB.setPosEfficiency( (short) 2 );
    registrationB.setStatus( RegistrationStatus.OK );
    model.save( registrationB );

    model.assignRegattaIndividualFinishingPrize( regatta );
    model.assignRegattaIndividualEfficiencyPrize( regatta );

    Registration refreshedA = model.getRegistrationById( registrationA.getId() );
    Registration refreshedB = model.getRegistrationById( registrationB.getId() );

    assertEquals( 120.0,
                  refreshedA.getPrizeFinishing(),
                  0.0001 );
    assertEquals( 20.0,
                  refreshedB.getPrizeFinishing(),
                  0.0001 );

    double expectedEfficiencyA = expectedEfficiencyPrize( model,
                                                          regatta.getPrizeEfficiency(),
                                                          List.of( refreshedA,
                                                                   refreshedB ),
                                                          refreshedA );
    double expectedEfficiencyB = expectedEfficiencyPrize( model,
                                                          regatta.getPrizeEfficiency(),
                                                          List.of( refreshedA,
                                                                   refreshedB ),
                                                          refreshedB );

    assertEquals( expectedEfficiencyA,
                  refreshedA.getPrizeEfficiency(),
                  0.0001 );
    assertEquals( expectedEfficiencyB,
                  refreshedB.getPrizeEfficiency(),
                  0.0001 );
    assertEquals( 230.0,
                  refreshedA.getPrizeEfficiency()
                  + refreshedB.getPrizeEfficiency(),
                  0.0001 );
  }

  @Test
  void balanceMessagesExposeExactTotalAndOnlyTheExpectedComplaintDirection() {
    RecordingModelBean model = new RecordingModelBean();
    Participant promoter = createSavedParticipant( model,
                                                   "balance-promoter",
                                                   "Balance-123" );
    Participant owner = createSavedParticipant( model,
                                                "balance-owner",
                                                "Balance-123" );
    Variant isolatedVariant = createIsolatedVariant( model,
                                                     promoter,
                                                     "balance" );
    Regatta regatta = createSavedRegatta( model,
                                          promoter,
                                          isolatedVariant,
                                          RegattaStatus.PUBLISHED,
                                          new Date( 1775520000000L ) );
    regatta.setTrackrental( 20.0 );
    regatta.setEntryfee( 5.0 );
    model.save( regatta );

    Registration registration = createSavedRegistration( model,
                                                         regatta,
                                                         owner,
                                                         createSavedCar( model,
                                                                         owner,
                                                                         "balance-car" ) );
    registration.setBetFinishing( 10.0 );
    registration.setBetEfficiency( 4.0 );
    registration.setPrizeFinishing( 50.0 );
    registration.setPrizeEfficiency( 6.0 );
    registration.setStatus( RegistrationStatus.OK );
    model.save( registration );

    model.sendBalanceMessages( regatta,
                               "OWNER MESSAGE",
                               "PROMOTER MESSAGE" );

    String ownerMessage = model.lastMessageFor( owner );
    String promoterMessage = model.lastMessageFor( promoter );

    assertNotNull( ownerMessage );
    assertNotNull( promoterMessage );
    assertTrue( ownerMessage.contains( "OWNER MESSAGE" ) );
    assertTrue( promoterMessage.contains( "PROMOTER MESSAGE" ) );
    assertTrue( ownerMessage.contains( "Total: 17.0" ) );
    assertTrue( promoterMessage.contains( "Total: 17.0" ) );
    assertTrue( ownerMessage.contains( "mode=balance" ) );
    assertTrue( ownerMessage.contains( "report the promoter using this link" ) );
    assertFalse( promoterMessage.contains( "report the owner using this link" ) );
  }

  @Test
  void sellerDefaulterDisqualifiesRegistrationAndRecomputesEfficiencyPrize() {
    ModelBean model = new ModelBean();
    Participant promoter = createSavedParticipant( model,
                                                   "defaulter-promoter",
                                                   "Defaulter-123" );
    Participant seller = createSavedParticipant( model,
                                                 "defaulter-seller",
                                                 "Defaulter-123" );
    Participant challenger = createSavedParticipant( model,
                                                     "defaulter-challenger",
                                                     "Defaulter-123" );
    Variant isolatedVariant = createIsolatedVariant( model,
                                                     promoter,
                                                     "defaulter" );
    Regatta regatta = createSavedRegatta( model,
                                          promoter,
                                          isolatedVariant,
                                          RegattaStatus.AUCTION,
                                          new Date( 1775606400000L ) );
    regatta.setPrizeEfficiency( 90.0 );
    model.save( regatta );

    Car sellerCar = createSavedCar( model,
                                    seller,
                                    "defaulter-car-seller" );
    Car challengerCar = createSavedCar( model,
                                        challenger,
                                        "defaulter-car-challenger" );

    Registration soldRegistration = createSavedRegistration( model,
                                                             regatta,
                                                             seller,
                                                             sellerCar );
    soldRegistration.setBetEfficiency( 20.0 );
    soldRegistration.setPosEfficiency( (short) 1 );
    soldRegistration.setStatus( RegistrationStatus.OK );
    soldRegistration.setParticipantByIdBuyer( challenger );
    model.save( soldRegistration );

    sellerCar.setParticipant( challenger );
    model.save( sellerCar );

    Registration challengerRegistration = createSavedRegistration( model,
                                                                   regatta,
                                                                   challenger,
                                                                   challengerCar );
    challengerRegistration.setBetEfficiency( 10.0 );
    challengerRegistration.setPosEfficiency( (short) 2 );
    challengerRegistration.setStatus( RegistrationStatus.OK );
    model.save( challengerRegistration );

    model.assignRegattaIndividualEfficiencyPrize( regatta );

    int defaulterBefore = getParticipantByEmail( model,
                                                 seller.getEmail() )
                                                   .getDefaulter();

    model.setCarSellerAsDefaulter( model.getRegistrationById(
      soldRegistration.getId() ) );

    Registration refreshedSold = model.getRegistrationById(
      soldRegistration.getId() );
    Registration refreshedChallenger = model.getRegistrationById(
      challengerRegistration.getId() );
    Participant refreshedSeller = getParticipantByEmail( model,
                                                         seller.getEmail() );
    Car refreshedSellerCar = model.getCarById( sellerCar.getId() );

    assertEquals( RegistrationStatus.DISQUALIFIED,
                  refreshedSold.getStatus() );
    assertEquals( defaulterBefore + 1,
                  refreshedSeller.getDefaulter() );
    assertEquals( seller.getId(),
                  refreshedSellerCar.getParticipant().getId() );
    assertEquals( 0.0,
                  refreshedSold.getPrizeEfficiency(),
                  0.0001 );
    assertEquals( 100.0,
                  refreshedChallenger.getPrizeEfficiency(),
                  0.0001 );
  }

  private static Participant createSavedParticipant( ModelBean model,
                                                     String label,
                                                     String password ) {
    String unique = label + "-" + UUID.randomUUID();
    Participant participant = model.createParticipant();
    participant.setPassword( password );
    participant.setNamesGiven( "Codex" );
    participant.setNamesFamily( unique );
    participant.setEmail( unique + "@example.com" );
    participant.setPhone( "5555555555" );
    participant.setConfirmed( true );
    participant.setDefaulter( 0 );
    return model.save( participant,
                       false );
  }

  private static Variant createIsolatedVariant( ModelBean model,
                                                Participant creator,
                                                String label ) {
    String unique = label + "-" + UUID.randomUUID();

    Planetregion planetregion = model.createPlanetregion( creator );
    planetregion.setName( "PlanetRegion-" + unique );
    model.save( planetregion );

    Country country = model.createCountry( creator );
    country.setName( "Country-" + unique );
    country.setPlanetregion( planetregion );
    model.save( country );

    Countryregion countryregion = model.createCountryregion( creator );
    countryregion.setName( "CountryRegion-" + unique );
    countryregion.setCountry( country );
    model.save( countryregion );

    Province province = model.createProvince( creator );
    province.setName( "Province-" + unique );
    province.setCountryregion( countryregion );
    model.save( province );

    Provinceregion provinceregion = model.createProvinceregion( creator );
    provinceregion.setName( "ProvinceRegion-" + unique );
    provinceregion.setProvince( province );
    model.save( provinceregion );

    Venue venue = model.createVenue( creator );
    venue.setName( "Venue-" + unique );
    venue.setProvinceregion( provinceregion );
    venue.setMeridian( -99.0 );
    venue.setParallel( 19.0 );
    model.save( venue );

    Variant variant = model.createVariant( creator );
    variant.setName( "Variant-" + unique );
    variant.setVenue( venue );
    variant.setLength( 1.5 );
    variant.setMinWidth( 0.8 );
    model.save( variant );

    return model.getVariantById( variant.getId() );
  }

  private static Regatta createSavedRegatta( ModelBean model,
                                             Participant promoter,
                                             Variant variant,
                                             byte status,
                                             Date datetime ) {
    Regatta regatta = model.createRegatta( promoter );
    regatta.setParticipant( promoter );
    regatta.setVariant( variant );
    regatta.setStatus( status );
    regatta.setDatetime( datetime );
    regatta.setMinutesSpeed( 5 );
    regatta.setMinutesRace( 10 );
    regatta.setEntryfee( 0.0 );
    regatta.setTrackrental( 0.0 );
    regatta.setPrizeFinishing( 0.0 );
    regatta.setPrizeEfficiency( 0.0 );
    regatta.setMaxQualifiers( 16 );
    regatta.setLevelPeriod( LevelPeriod.CONTINUOUS );
    regatta.setLevelTrackset( LevelTrackset.VARIANT );
    model.save( regatta );
    return model.getRegattaById( regatta.getId() );
  }

  private static Car createSavedCar( ModelBean model,
                                     Participant owner,
                                     String label ) {
    String unique = label + "-" + UUID.randomUUID();
    Car car = model.createCar( owner );
    car.setNickname( unique );
    car.setWeight( 100.0 );
    car.setWidth( 10.0 );
    model.save( car );
    return model.getCarById( car.getId() );
  }

  private static Registration createSavedRegistration( ModelBean model,
                                                       Regatta regatta,
                                                       Participant owner,
                                                       Car car ) {
    Registration registration = model.createRegistration( regatta,
                                                          owner );
    registration.setCar( car );
    registration.setParticipantByIdOwner( owner );
    registration.setParticipantByIdDriver( owner );
    registration.setParticipantByIdBuyer( owner );
    registration.setSecondsLap( 60.0 );
    registration.setLapsRace( (short) 1 );
    registration.setBetFinishing( 0.0 );
    registration.setBetEfficiency( 0.0 );
    registration.setPosSpeed( (short) 1 );
    registration.setPosRacegrid( (short) 1 );
    registration.setPosRace( (short) 1 );
    registration.setPosEfficiency( (short) 1 );
    registration.setPrizeFinishing( 0.0 );
    registration.setPrizeEfficiency( 0.0 );
    registration.setStatus( RegistrationStatus.OK );
    model.save( registration );
    return model.getRegistrationById( registration.getId() );
  }

  private static Participant getParticipantByEmail( ModelBean model,
                                                    String email ) {
    Participant probe = new Participant();
    probe.setEmail( email );
    return model.getParticipantByEmail( probe );
  }

  private static double expectedEfficiencyPrize( ModelBean model,
                                                 double regattaPrize,
                                                 List<Registration> registrations,
                                                 Registration target ) {
    double totalBet = registrations.stream()
      .filter( registration
        -> registration.getStatus() == RegistrationStatus.OK )
      .mapToDouble( Registration::getBetEfficiency )
      .sum();

    List<Double> weights = new ArrayList<>();
    for( Registration registration
         : registrations ) {
      if( registration.getStatus() != RegistrationStatus.OK ) {
        continue;
      }
      double betFraction = totalBet > 0
                           ? registration.getBetEfficiency() / totalBet
                           : 1.0;
      weights.add( betFraction * model.points( registration.getPosEfficiency() ) );
    }

    double totalWeight = weights.stream()
      .mapToDouble( Double::doubleValue )
      .sum();
    double targetBetFraction = totalBet > 0
                               ? target.getBetEfficiency() / totalBet
                               : 1.0;
    double targetWeight = targetBetFraction * model.points( target.getPosEfficiency() );

    return ( regattaPrize + totalBet ) * targetWeight / totalWeight;
  }

  private static final class RecordingModelBean
    extends ModelBean {

    private final Map<String, List<String>> messagesByEmail = new HashMap<>();

    @Override
    public synchronized void sendEmail( Participant user,
                                        String messageText,
                                        long session ) {
      if( user == null || user.getEmail() == null ) {
        return;
      }

      messagesByEmail.computeIfAbsent( user.getEmail(),
                                       key
                                       -> new ArrayList<>() )
        .add( messageText );
    }

    private String lastMessageFor( Participant participant ) {
      List<String> messages = messagesByEmail.get( participant.getEmail() );
      if( messages == null || messages.isEmpty() ) {
        return null;
      }
      return messages.get( messages.size() - 1 );
    }
  }
}
