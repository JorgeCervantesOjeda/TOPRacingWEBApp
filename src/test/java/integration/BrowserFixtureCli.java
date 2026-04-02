package integration;

import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Model.ModelBean;
import Tables.Bid;
import Tables.BidId;
import Tables.Car;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class BrowserFixtureCli {

  private static final ModelBean MODEL = new ModelBean();

  private BrowserFixtureCli() {
  }

  public static void main( String[] args ) {
    if( args.length == 0 ) {
      fail( "Missing fixture command" );
      return;
    }

    try {
      switch( args[ 0 ] ) {
        case "confirm":
          emitConfirmFixture();
          break;
        case "reset":
          emitResetFixture();
          break;
        case "balance-owner":
          emitBalanceComplaintFixture();
          break;
        case "auction-buyer":
          emitAuctionComplaintFixture();
          break;
        case "participant-by-email":
          if( args.length < 2 ) {
            fail( "Missing participant e-mail" );
            return;
          }
          emitParticipantByEmail( args[ 1 ] );
          break;
        default:
          fail( "Unknown fixture command: " + args[ 0 ] );
      }
    } catch( Exception e ) {
      fail( e.getClass()
        .getSimpleName() + ": " + e.getMessage() );
      throw e;
    }
  }

  private static void emitConfirmFixture() {
    Participant participant = createSavedParticipant( "confirm-browser",
                                                      "Confirm-123",
                                                      false );
    printCommonParticipant( participant );
    printLine( "url",
               "/faces/confirmusermail.xhtml?key=%27" + participant.getEmailKey()
               + "%27" );
  }

  private static void emitResetFixture() {
    Participant participant = createSavedParticipant( "reset-browser",
                                                      "OldPw-123",
                                                      true );
    printCommonParticipant( participant );
    printLine( "url",
               "/faces/resetpassword.xhtml?key=%27" + participant.getEmailKey()
               + "%27" );
  }

  private static void emitBalanceComplaintFixture() {
    BalanceComplaintFixture fixture = createBalanceComplaintFixture();
    printLine( "type",
               "balance-owner" );
    printLine( "url",
               "/faces/complaint.xhtml?mode=balance&key=%27"
               + fixture.owner()
                 .getEmailKey()
               + "%27&r1="
               + fixture.registration()
                 .getId()
               + "&target="
               + fixture.promoter()
                 .getId() );
    printLine( "reporterEmail",
               fixture.owner()
                 .getEmail() );
    printLine( "targetEmail",
               fixture.promoter()
                 .getEmail() );
    printLine( "targetName",
               fixture.promoter()
                 .getNamesGiven()
               + " "
               + fixture.promoter()
                 .getNamesFamily() );
  }

  private static void emitAuctionComplaintFixture() {
    AuctionComplaintFixture fixture = createAuctionComplaintFixture();
    printLine( "type",
               "auction-buyer" );
    printLine( "url",
               "/faces/complaint.xhtml?key=%27"
               + fixture.buyer()
                 .getEmailKey()
               + "%27&r1="
               + fixture.registration()
                 .getId()
               + "&r2="
               + fixture.regatta()
                 .getId()
               + "&s="
               + fixture.seller()
                 .getId()
               + "&b="
               + fixture.buyer()
                 .getId() );
    printLine( "reporterEmail",
               fixture.buyer()
                 .getEmail() );
    printLine( "targetEmail",
               fixture.seller()
                 .getEmail() );
    printLine( "targetName",
               fixture.seller()
                 .getNamesGiven()
               + " "
               + fixture.seller()
                 .getNamesFamily() );
  }

  private static void emitParticipantByEmail( String email ) {
    Participant probe = new Participant();
    probe.setEmail( email );
    Participant participant = MODEL.getParticipantByEmail( probe );
    if( participant == null ) {
      fail( "Participant not found for e-mail: " + email );
      return;
    }

    printCommonParticipant( participant );
    printLine( "confirmed",
               participant.isConfirmed() ? "true" : "false" );
    printLine( "password",
               participant.getPassword() );
  }

  private static Participant createSavedParticipant( String label,
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

  private static BalanceComplaintFixture createBalanceComplaintFixture() {
    Participant promoter = createSavedParticipant( "promoter-browser",
                                                   "Promoter-123",
                                                   true );
    Participant owner = createSavedParticipant( "owner-browser",
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
    regatta.setPrizeFinishing( 20.0 );
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
    registration.setPrizeFinishing( 20.0 );
    registration.setBalance( 0.0 );
    registration.setStatus( RegistrationStatus.OK );
    MODEL.save( registration );

    Registration persistedRegistration = MODEL.getRegistrationById(
      registration.getId() );
    return new BalanceComplaintFixture( owner,
                                        promoter,
                                        persistedRegistration );
  }

  private static AuctionComplaintFixture createAuctionComplaintFixture() {
    Participant seller = createSavedParticipant( "seller-browser",
                                                 "Seller-123",
                                                 true );
    Participant buyer = createSavedParticipant( "buyer-browser",
                                                "Buyer-123",
                                                true );
    Participant promoter = createSavedParticipant( "auction-promoter-browser",
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

  private static void printCommonParticipant( Participant participant ) {
    printLine( "type",
               "participant" );
    printLine( "email",
               participant.getEmail() );
    printLine( "familyName",
               participant.getNamesFamily() );
    printLine( "fullName",
               participant.getNamesGiven() + " " + participant.getNamesFamily() );
    printLine( "emailKey",
               participant.getEmailKey() );
  }

  private static void printLine( String key,
                                 String value ) {
    System.out.println( key + "=" + sanitize( value ) );
  }

  private static String sanitize( String value ) {
    if( value == null ) {
      return "";
    }
    return value.replace( "\r",
                          " " )
      .replace( "\n",
                " " );
  }

  private static void fail( String message ) {
    System.err.println( message );
    System.exit( 1 );
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
