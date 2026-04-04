package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Controller.RegistrationStatus;
import Model.ModelBean;
import Tables.Bid;
import Tables.BidId;
import Tables.Car;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import Controller.RegattaStatus;
import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocalAppExternalLinkFlowsIT {

  private static final ModelBean MODEL = new ModelBean();

  private final String baseUrl = System.getProperty(
    "topracing.baseUrl",
    "http://localhost:8080/topracingwebapp" );

  private final HttpClient client = HttpClient.newBuilder()
    .connectTimeout( Duration.ofSeconds( 30 ) )
    .followRedirects( Redirect.NEVER )
    .build();

  @Test
  void confirmMailLinkConfirmsParticipant() throws IOException,
                                                   InterruptedException {
    Participant participant = createSavedParticipant( "confirm",
                                                      "Confirm-123",
                                                      false );

    HttpResponse<String> response = get(
      "/faces/confirmusermail.xhtml?key=%27" + participant.getEmailKey() + "%27" );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "Confirmation OK" ) );

    Participant refreshed = getParticipantByEmail( participant.getEmail() );
    assertNotNull( refreshed );
    assertTrue( refreshed.isConfirmed() );
  }

  @Test
  void resetPasswordLinkChangesPasswordAndLogsTheNewPassword() throws IOException,
                                                                      InterruptedException {
    String oldPassword = "OldPw-123";
    Participant participant = createSavedParticipant( "reset",
                                                      oldPassword,
                                                      true );

    HttpResponse<String> response = get(
      "/faces/resetpassword.xhtml?key=%27" + participant.getEmailKey() + "%27" );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "Your Password has been reset." ) );
    assertTrue( response.body().contains( participant.getEmail() ) );

    Participant refreshed = getParticipantByEmail( participant.getEmail() );
    assertNotNull( refreshed );
    String newPassword = refreshed.getPassword();
    assertNotEquals( oldPassword,
                     newPassword );

    Participant oldCredentialsAttempt = new Participant();
    oldCredentialsAttempt.setEmail( participant.getEmail() );
    oldCredentialsAttempt.setPassword( oldPassword );
    assertNull( MODEL.getValidParticipant( oldCredentialsAttempt ) );

    Participant newCredentialsAttempt = new Participant();
    newCredentialsAttempt.setEmail( participant.getEmail() );
    newCredentialsAttempt.setPassword( newPassword );
    assertNotNull( MODEL.getValidParticipant( newCredentialsAttempt ) );

    assertTrue( refreshed.isConfirmed() );
    assertNotEquals( participant.getEmailKey(),
                     refreshed.getEmailKey() );
  }

  @Test
  void ownerCanReportPromoterAsDefaulterFromPositiveBalanceLink() throws IOException,
                                                                         InterruptedException {
    BalanceComplaintFixture fixture = createBalanceComplaintFixture( 10.0 );
    int promoterDefaulterBefore = getParticipantByEmail( fixture.promoter()
      .getEmail() )
        .getDefaulter();

    HttpResponse<String> response = get(
      "/faces/complaint.xhtml?mode=balance&key=%27"
      + fixture.owner().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&target="
      + fixture.promoter().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.promoter().getNamesGiven() ) );

    Participant promoterAfter = getParticipantByEmail( fixture.promoter()
      .getEmail() );
    assertEquals( promoterDefaulterBefore + 1,
                  promoterAfter.getDefaulter() );
  }

  @Test
  void promoterCanReportOwnerAsDefaulterFromNegativeBalanceLink() throws IOException,
                                                                         InterruptedException {
    BalanceComplaintFixture fixture = createBalanceComplaintFixture( -10.0 );
    int ownerDefaulterBefore = getParticipantByEmail( fixture.owner()
      .getEmail() )
        .getDefaulter();

    HttpResponse<String> response = get(
      "/faces/complaint.xhtml?mode=balance&key=%27"
      + fixture.promoter().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&target="
      + fixture.owner().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.owner().getNamesGiven() ) );

    Participant ownerAfter = getParticipantByEmail( fixture.owner()
      .getEmail() );
    assertEquals( ownerDefaulterBefore + 1,
                  ownerAfter.getDefaulter() );
  }

  @Test
  void buyerCanReportSellerAsDefaulterFromAuctionComplaintLink() throws IOException,
                                                                        InterruptedException {
    AuctionComplaintFixture fixture = createAuctionComplaintFixture();
    int sellerDefaulterBefore = getParticipantByEmail( fixture.seller()
      .getEmail() )
        .getDefaulter();

    HttpResponse<String> response = get(
      "/faces/complaint.xhtml?key=%27"
      + fixture.buyer().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&r2="
      + fixture.regatta().getId()
      + "&s="
      + fixture.seller().getId()
      + "&b="
      + fixture.buyer().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.seller().getNamesGiven() ) );

    Participant sellerAfter = getParticipantByEmail( fixture.seller()
      .getEmail() );
    assertEquals( sellerDefaulterBefore + 1,
                  sellerAfter.getDefaulter() );

    Registration refreshedRegistration = MODEL.getRegistrationById( fixture.registration()
      .getId() );
    assertEquals( RegistrationStatus.DISQUALIFIED,
                  refreshedRegistration.getStatus() );
    assertEquals( fixture.seller().getId(),
                  refreshedRegistration.getCar().getParticipant().getId() );
  }

  @Test
  void complaintBuyerPageShowsBuyerComplaintResult() throws IOException,
                                                            InterruptedException {
    AuctionComplaintFixture fixture = createAuctionComplaintFixture();

    HttpResponse<String> response = getWithRetry(
      "/faces/complaintbuyer.xhtml?key=%27"
      + fixture.buyer().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&r2="
      + fixture.regatta().getId()
      + "&s="
      + fixture.seller().getId()
      + "&b="
      + fixture.buyer().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "Confirmation key is" ) );
    assertTrue( response.body().contains( fixture.buyer().getEmailKey() ) );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.seller().getNamesGiven() ) );
  }

  @Test
  void sellerCanReportBuyerAsDefaulterFromAuctionComplaintLink() throws IOException,
                                                                        InterruptedException {
    AuctionComplaintFixture fixture = createAuctionComplaintFixture();
    int buyerDefaulterBefore = getParticipantByEmail( fixture.buyer()
      .getEmail() )
        .getDefaulter();

    HttpResponse<String> response = get(
      "/faces/complaint.xhtml?key=%27"
      + fixture.seller().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&r2="
      + fixture.regatta().getId()
      + "&s="
      + fixture.seller().getId()
      + "&b="
      + fixture.buyer().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.buyer().getNamesGiven() ) );

    Participant buyerAfter = getParticipantByEmail( fixture.buyer()
      .getEmail() );
    assertEquals( buyerDefaulterBefore + 1,
                  buyerAfter.getDefaulter() );

    List<Bid> bids = MODEL.getBids( fixture.registration() );
    assertTrue( bids.stream().anyMatch( bid
      -> bid.getParticipant().getId().equals( fixture.buyer().getId() )
         && bid.getStatus() > 0 ) );
  }

  @Test
  void complaintSellerPageShowsSellerComplaintResult() throws IOException,
                                                              InterruptedException {
    AuctionComplaintFixture fixture = createAuctionComplaintFixture();

    HttpResponse<String> response = getWithRetry(
      "/faces/complaintseller.xhtml?key=%27"
      + fixture.seller().getEmailKey()
      + "%27&r1="
      + fixture.registration().getId()
      + "&r2="
      + fixture.regatta().getId()
      + "&s="
      + fixture.seller().getId()
      + "&b="
      + fixture.buyer().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "Confirmation key is" ) );
    assertTrue( response.body().contains( fixture.seller().getEmailKey() ) );
    assertTrue( response.body().contains( "You have filed a complaint against:" ) );
    assertTrue( response.body().contains( fixture.buyer().getNamesGiven() ) );
  }

  @Test
  void invalidConfirmationKeyShowsGracefulMessage() throws IOException,
                                                           InterruptedException {
    HttpResponse<String> response = get(
      "/faces/confirmusermail.xhtml?key=%27invalid%27" );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains( "Confirmation not OK" ) );
    assertTrue( response.body().contains( "Unknown participant" ) );
  }

  @Test
  void invalidResetPasswordKeyShowsFriendlyError() throws IOException,
                                                          InterruptedException {
    HttpResponse<String> response = get(
      "/faces/resetpassword.xhtml?key=%27invalid%27" );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains(
      "Password reset request is invalid or expired." ) );
    assertTrue( !response.body().contains( "null pointer" ) );
  }

  @Test
  void invalidComplaintLinkDoesNotLeakRegistrationDetails() throws IOException,
                                                                   InterruptedException {
    BalanceComplaintFixture fixture = createBalanceComplaintFixture( 10.0 );

    HttpResponse<String> response = get(
      "/faces/complaint.xhtml?mode=balance&key=%27invalid%27&r1="
      + fixture.registration().getId()
      + "&target="
      + fixture.promoter().getId() );

    assertEquals( 200,
                  response.statusCode() );
    assertTrue( response.body().contains(
      "Complaint request is incomplete or invalid." ) );
    assertTrue( !response.body().contains( "Car:" ) );
    assertTrue( !response.body().contains(
      fixture.registration().getCar().getNickname() ) );
  }

  private BalanceComplaintFixture createBalanceComplaintFixture( double expectedBalanceSign ) {
    Participant promoter = createSavedParticipant( "promoter",
                                                   "Promoter-123",
                                                   true );
    Participant owner = createSavedParticipant( "owner",
                                                "Owner-123",
                                                true );

    Car ownerCar = MODEL.createCar( owner );
    ownerCar.setNickname( "car-" + UUID.randomUUID() );
    ownerCar.setWeight( 100.0 );
    ownerCar.setWidth( 10.0 );
    MODEL.save( ownerCar );

    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setEntryfee( 10.0 );
    regatta.setTrackrental( 0.0 );
    regatta.setPrizeEfficiency( 0.0 );
    regatta.setPrizeFinishing( expectedBalanceSign > 0 ? 20.0 : 0.0 );
    MODEL.save( regatta );

    Registration registration = MODEL.createRegistration( regatta,
                                                          owner );
    registration.setCar( MODEL.getCarById( ownerCar.getId() ) );
    registration.setParticipantByIdOwner( owner );
    registration.setParticipantByIdDriver( owner );
    registration.setParticipantByIdBuyer( owner );
    registration.setBetEfficiency( 0.0 );
    registration.setBetFinishing( 0.0 );
    registration.setPrizeEfficiency( 0.0 );
    registration.setPrizeFinishing( expectedBalanceSign > 0 ? 20.0 : 0.0 );
    registration.setBalance( 0.0 );
    registration.setStatus( RegistrationStatus.OK );
    MODEL.save( registration );

    Registration persistedRegistration = MODEL.getRegistrationById(
      registration.getId() );
    return new BalanceComplaintFixture( owner,
                                        promoter,
                                        persistedRegistration );
  }

  private AuctionComplaintFixture createAuctionComplaintFixture() {
    Participant seller = createSavedParticipant( "seller",
                                                 "Seller-123",
                                                 true );
    Participant buyer = createSavedParticipant( "buyer",
                                                "Buyer-123",
                                                true );
    Participant promoter = createSavedParticipant( "auction-promoter",
                                                   "Promoter-123",
                                                   true );

    Car car = MODEL.createCar( seller );
    car.setNickname( "auction-car-" + UUID.randomUUID() );
    car.setWeight( 100.0 );
    car.setWidth( 10.0 );
    MODEL.save( car );

    Regatta regatta = MODEL.createRegatta( promoter );
    regatta.setStatus( RegattaStatus.AUCTION );
    MODEL.save( regatta );

    Registration registration = MODEL.createRegistration( regatta,
                                                          seller );
    registration.setCar( MODEL.getCarById( car.getId() ) );
    registration.setParticipantByIdOwner( seller );
    registration.setParticipantByIdDriver( seller );
    registration.setParticipantByIdBuyer( buyer );
    registration.setStatus( RegistrationStatus.OK );
    registration.setValueAuction( 55.5 );
    MODEL.save( registration );

    Car transferredCar = MODEL.getCarById( car.getId() );
    transferredCar.setParticipant( buyer );
    MODEL.save( transferredCar );

    Registration persistedRegistration = MODEL.getRegistrationById(
      registration.getId() );
    persistedRegistration.setCar( MODEL.getCarById( transferredCar.getId() ) );
    persistedRegistration.setParticipantByIdBuyer( buyer );
    persistedRegistration.setValueAuction( 55.5 );
    MODEL.save( persistedRegistration );

    Bid bid = new Bid( new BidId( buyer.getId(),
                                  persistedRegistration.getId() ),
                       buyer,
                       persistedRegistration,
                       60.0,
                       new Date(),
                       0 );
    MODEL.save( List.of( bid ) );

    return new AuctionComplaintFixture( seller,
                                        buyer,
                                        MODEL.getRegattaById( regatta.getId() ),
                                        MODEL.getRegistrationById(
                                          persistedRegistration.getId() ) );
  }

  private Participant createSavedParticipant( String label,
                                              String password,
                                              boolean confirmed ) {
    String unique = label + "-" + UUID.randomUUID();
    Participant participant = MODEL.createParticipant();
    participant.setPassword( password );
    participant.setNamesGiven( "Codex" );
    participant.setNamesFamily( unique );
    participant.setEmail( unique + "@example.com" );
    participant.setPhone( "5555555555" );
    participant.setConfirmed( confirmed );
    participant.setDefaulter( 0 );
    return MODEL.save( participant,
                       false );
  }

  private Participant getParticipantByEmail( String email ) {
    Participant probe = new Participant();
    probe.setEmail( email );
    return MODEL.getParticipantByEmail( probe );
  }

  private HttpResponse<String> get( String path ) throws IOException,
                                                         InterruptedException {
    HttpRequest request = HttpRequest.newBuilder( URI.create( baseUrl + path ) )
      .timeout( Duration.ofSeconds( 180 ) )
      .GET()
      .build();
    return client.send( request,
                        HttpResponse.BodyHandlers.ofString() );
  }

  private HttpResponse<String> getWithRetry( String path ) throws IOException,
                                                                   InterruptedException {
    try {
      return get( path );
    } catch( IOException ex ) {
      Thread.sleep( 1000L );
      return get( path );
    }
  }

  private static final class BalanceComplaintFixture {

    private final Participant owner;
    private final Participant promoter;
    private final Registration registration;

    private BalanceComplaintFixture( Participant owner,
                                     Participant promoter,
                                     Registration registration ) {
      this.owner = owner;
      this.promoter = promoter;
      this.registration = registration;
    }

    private Participant owner() {
      return owner;
    }

    private Participant promoter() {
      return promoter;
    }

    private Registration registration() {
      return registration;
    }
  }

  private static final class AuctionComplaintFixture {

    private final Participant seller;
    private final Participant buyer;
    private final Regatta regatta;
    private final Registration registration;

    private AuctionComplaintFixture( Participant seller,
                                     Participant buyer,
                                     Regatta regatta,
                                     Registration registration ) {
      this.seller = seller;
      this.buyer = buyer;
      this.regatta = regatta;
      this.registration = registration;
    }

    private Participant seller() {
      return seller;
    }

    private Participant buyer() {
      return buyer;
    }

    private Regatta regatta() {
      return regatta;
    }

    private Registration registration() {
      return registration;
    }
  }
}
