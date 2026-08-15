// src/test/java/integration/LocalAppAuthenticatedListsIT.java
// Verifies authenticated list pages through HTTP and controller-backed fixtures.
package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Model.LevelPeriod;
import Model.LevelTrackset;
import Model.ModelBean;
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
import View.ListPenaltiesBean;
import View.ListPointscountsBean;
import View.ListRegistrationsBean;
import View.ViewBean;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class LocalAppAuthenticatedListsIT {

  private static final ModelBean MODEL = new ModelBean();
  private static final Duration HTTP_TIMEOUT = Duration.ofSeconds( 60 );
  private static final Pattern VIEW_STATE_PATTERN = Pattern.compile(
    "name=\"jakarta\\.faces\\.ViewState\"[^>]*value=\"([^\"]+)\"" );

  private final String baseUrl = System.getProperty(
    "topracing.baseUrl",
    "http://localhost:8080/topracingwebapp" );

  @Test
  void authenticatedUserCanOpenCoreListPages() throws IOException,
                                                      InterruptedException {
    String password = "Pw-12345";
    Participant participant = createSavedParticipant( "http-lists",
                                                      password );
    AuthenticatedClient client = login( participant.getEmail(),
                                        password );

    HttpResponse<String> pointscounts = client.get( "/faces/listpointscounts.xhtml" );
    HttpResponse<String> penalties = client.get( "/faces/listpenalties.xhtml" );
    HttpResponse<String> registrations = client.get( "/faces/listregistrations.xhtml" );

    assertEquals( 200,
                  pointscounts.statusCode() );
    assertTrue( !pointscounts.body().contains( "participant_email" ) );
    assertTrue( !pointscounts.body().contains( "Please login or" ) );

    assertEquals( 200,
                  penalties.statusCode() );
    assertTrue( penalties.body().contains( "penaltiesList" ) );
    assertTrue( penalties.body().contains( "Create Event" ) );

    assertEquals( 200,
                  registrations.statusCode() );
    assertTrue( registrations.body().contains( "registrationsList" ) );
  }

  @Test
  void authenticatedListsExposeExpectedControlledContent() throws IOException,
                                                                   InterruptedException {
    ListContentFixture fixture = createListContentFixture();
    ViewBean viewBean = createViewBeanFixture( fixture );

    ListPenaltiesBean penaltiesBean = new ListPenaltiesBean();
    penaltiesBean.setViewBean( viewBean );
    penaltiesBean.init();

    Penaltiespl penaltyRow = penaltiesBean.getPenaltiespl()
      .stream()
      .filter( item
        -> String.valueOf( item.getRegatta().getId() ).equals(
          fixture.regattaIdText() ) )
      .findFirst()
      .orElseThrow();

    assertEquals( fixture.viewerName(),
                  penaltiesBean.getParticipantName( penaltyRow ) );
    assertTrue( penaltiesBean.getVariantName( penaltyRow.getRegatta()
      .getVariant() ).contains( fixture.variantName() ) );

    ListPointscountsBean pointscountsBean = new ListPointscountsBean();
    pointscountsBean.setViewBean( viewBean );
    pointscountsBean.init();

    Pointscount pointscountRow = pointscountsBean.getPointscount()
      .stream()
      .filter( item
        -> item.getId().getIdParticipant() == fixture.owner().getId() )
      .findFirst()
      .orElseThrow();

    assertEquals( fixture.ownerName(),
                  pointscountsBean.getParticipantName(
                    pointscountRow.getId().getIdParticipant() ) );
    assertEquals( fixture.pointsSd(),
                  formatPoints( pointscountRow.getPointsSD() ) );
    assertEquals( fixture.pointsRd(),
                  formatPoints( pointscountRow.getPointsRD() ) );
    assertEquals( fixture.pointsEd(),
                  formatPoints( pointscountRow.getPointsED() ) );
    assertEquals( fixture.pointsSo(),
                  formatPoints( pointscountRow.getPointsSO() ) );
    assertEquals( fixture.pointsRo(),
                  formatPoints( pointscountRow.getPointsRO() ) );
    assertEquals( fixture.pointsEo(),
                  formatPoints( pointscountRow.getPointsEO() ) );

    ListRegistrationsBean registrationsBean = new ListRegistrationsBean();
    registrationsBean.setViewBean( viewBean );
    registrationsBean.init();

    Registration registrationRow = registrationsBean.getRegistrations()
      .stream()
      .filter( item
        -> item.getId().equals( Long.valueOf( fixture.registrationIdText() ) ) )
      .findFirst()
      .orElseThrow();

    assertEquals( fixture.registrationStatus(),
                  registrationsBean.getStatusName( registrationRow.getStatus() ) );
    assertEquals( fixture.variantName(),
                  registrationRow.getRegatta().getVariant().getName() );
    assertEquals( fixture.venueName(),
                  registrationRow.getRegatta().getVariant().getVenue().getName() );
    assertEquals( fixture.ownerName(),
                  registrationsBean.getParticipantName(
                    registrationRow.getParticipantByIdOwner() ) );
    assertEquals( fixture.carName(),
                  registrationRow.getCar().getNickname() );
  }

  private AuthenticatedClient login( String email,
                                     String password ) throws IOException,
                                                              InterruptedException {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy( CookiePolicy.ACCEPT_ALL );

    HttpClient client = HttpClient.newBuilder()
      .cookieHandler( cookieManager )
      .connectTimeout( HTTP_TIMEOUT )
      .followRedirects( Redirect.NEVER )
      .build();

    HttpResponse<String> loginPage = send( client,
                                           HttpRequest.newBuilder( URI.create(
                                              baseUrl + "/faces/login.xhtml" ) )
                                              .timeout( HTTP_TIMEOUT )
                                             .GET()
                                             .build() );

    assertEquals( 200,
                  loginPage.statusCode() );

    String viewState = extractViewState( loginPage.body() );
    String form = "contentForm=contentForm"
                  + "&contentForm%3Aparticipant_email="
                  + encode( email )
                  + "&contentForm%3Aparticipant_password="
                  + encode( password )
                  + "&contentForm%3AloginButton="
                  + encode( "Login" )
                  + "&jakarta.faces.ViewState="
                  + encode( viewState );

    HttpResponse<String> loginResponse = send( client,
                                               HttpRequest.newBuilder( URI.create(
                                                  baseUrl + "/faces/login.xhtml" ) )
                                                  .timeout( HTTP_TIMEOUT )
                                                 .header( "Content-Type",
                                                          "application/x-www-form-urlencoded" )
                                                 .POST( HttpRequest.BodyPublishers.ofString(
                                                   form ) )
                                                 .build() );

    assertTrue( loginResponse.statusCode() == 200
                || loginResponse.statusCode() == 302 );

    return new AuthenticatedClient( client );
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

  private ListContentFixture createListContentFixture() {
    String password = "Lists-123";
    Participant viewer = createSavedParticipant( "http-lists-viewer",
                                                 password );
    Participant owner = createSavedParticipant( "http-lists-owner",
                                                "Owner-123" );
    Variant isolatedVariant = createIsolatedVariant( viewer,
                                                     "http-lists" );
    Venue isolatedVenue = MODEL.getVenueById(
      isolatedVariant.getVenue().getId() );

    Regatta regatta = MODEL.createRegatta( viewer );
    regatta.setVariant( isolatedVariant );
    regatta.setStatus( RegattaStatus.SPEED_TEST );
    regatta.setMinutesRace( 10 );
    regatta.setDatetime( new Date( 1775347200000L ) );
    MODEL.save( regatta );

    Car car = MODEL.createCar( owner );
    car.setNickname( "http-lists-car-" + UUID.randomUUID() );
    car.setWeight( 100.0 );
    car.setWidth( 10.0 );
    MODEL.save( car );

    Registration registration = MODEL.createRegistration( regatta,
                                                          owner );
    registration.setCar( MODEL.getCarById( car.getId() ) );
    registration.setParticipantByIdOwner( owner );
    registration.setParticipantByIdDriver( owner );
    registration.setParticipantByIdBuyer( owner );
    registration.setSecondsLap( 60.0 );
    registration.setPosSpeed( (short) 1 );
    registration.setPosRace( (short) 2 );
    registration.setPosEfficiency( (short) 3 );
    registration.setStatus( RegistrationStatus.OK );
    MODEL.save( registration );

    MODEL.recalculateRegattaPenalties( ( progress )
      -> {
      } );

    Regatta persistedRegatta = MODEL.getRegattaById( regatta.getId() );
    Registration persistedRegistration = MODEL.getRegistrationById(
      registration.getId() );

    long periodId = MODEL.getPeriodId( persistedRegatta.getDatetime(),
                                       LevelPeriod.CONTINUOUS );
    Pointscount pointscount = MODEL.getPointscountById(
      new PointscountId( owner.getId(),
                         LevelPeriod.CONTINUOUS,
                         periodId,
                         LevelTrackset.PLANET,
                         MODEL.getIdTrackset( isolatedVariant,
                                              LevelTrackset.PLANET ) ) );
    Penaltiespl penalties = MODEL.getRegattaPeriodlevelPenaltiesList(
      persistedRegatta )
      .stream()
      .filter( penalty
        -> penalty.getId().getLevelPeriod() == LevelPeriod.CONTINUOUS )
      .findFirst()
      .orElseThrow();

    return new ListContentFixture(
      viewer,
      password,
      owner,
      isolatedVariant.getName(),
      isolatedVenue.getName(),
      String.valueOf( persistedRegatta.getId() ),
      String.valueOf( persistedRegistration.getId() ),
      RegistrationStatus.NAME[ persistedRegistration.getStatus() ],
      persistedRegistration.getCar().getNickname(),
      MODEL.getParticipantFullName( owner ),
      MODEL.getParticipantFullName( viewer ),
      formatPoints( pointscount.getPointsSD() ),
      formatPoints( pointscount.getPointsRD() ),
      formatPoints( pointscount.getPointsED() ),
      formatPoints( pointscount.getPointsSO() ),
      formatPoints( pointscount.getPointsRO() ),
      formatPoints( pointscount.getPointsEO() ),
      formatPenaltyPercent( penalties ) );
  }

  private Variant createIsolatedVariant( Participant creator,
                                         String label ) {
    String unique = label + "-" + UUID.randomUUID();

    Planetregion planetregion = MODEL.createPlanetregion( creator );
    planetregion.setName( unique + "-planet-region" );
    MODEL.save( planetregion );
    planetregion = MODEL.getPlanetregionById( planetregion.getId() );

    Country country = MODEL.createCountry( creator );
    country.setName( unique + "-country" );
    country.setPlanetregion( planetregion );
    MODEL.save( country );
    country = MODEL.getCountryById( country.getId() );

    Countryregion countryregion = MODEL.createCountryregion( creator );
    countryregion.setName( unique + "-country-region" );
    countryregion.setCountry( country );
    MODEL.save( countryregion );
    countryregion = MODEL.getCountryregionById( countryregion.getId() );

    Province province = MODEL.createProvince( creator );
    province.setName( unique + "-province" );
    province.setCountryregion( countryregion );
    MODEL.save( province );
    province = MODEL.getProvinceById( province.getId() );

    Provinceregion provinceregion = MODEL.createProvinceregion( creator );
    provinceregion.setName( unique + "-province-region" );
    provinceregion.setProvince( province );
    MODEL.save( provinceregion );
    provinceregion = MODEL.getProvinceregionById( provinceregion.getId() );

    Venue venue = MODEL.createVenue( creator );
    venue.setName( unique + "-venue" );
    venue.setProvinceregion( provinceregion );
    venue.setMeridian( -99.1332 );
    venue.setParallel( 19.4326 );
    MODEL.save( venue );
    venue = MODEL.getVenueById( venue.getId() );

    Variant variant = MODEL.createVariant( creator );
    variant.setName( unique + "-variant" );
    variant.setVenue( venue );
    variant.setLength( 1.5 );
    variant.setMinWidth( 0.5 );
    MODEL.save( variant );
    return MODEL.getVariantById( variant.getId() );
  }

  private String formatPoints( double value ) {
    return String.valueOf( (long) Math.rint( value ) );
  }

  private String formatPenaltyPercent( Penaltiespl penalties ) {
    java.text.NumberFormat percentFormat = java.text.NumberFormat.getPercentInstance(
      java.util.Locale.US );
    percentFormat.setMinimumFractionDigits( 2 );
    percentFormat.setMaximumFractionDigits( 2 );
    return percentFormat.format( MODEL.points(
      MODEL.getPenaltyValue( penalties,
                             LevelTrackset.PLANET ) + 1 )
                                / 100.0 );
  }

  private ViewBean createViewBeanFixture( ListContentFixture fixture ) {
    ViewBean viewBean = new ViewBean();
    Controller controller = mock( Controller.class );
    viewBean.setModelBean( MODEL );
    viewBean.setCurrentParticipant( fixture.viewer() );
    setPrivateField( viewBean,
                     "theController",
                     controller );
    setPrivateField( viewBean,
                     "currentTracksetLevel",
                     0 );
    setPrivateField( viewBean,
                     "currentPeriodLevel",
                     0 );
    return viewBean;
  }

  private void setPrivateField( Object target,
                                String fieldName,
                                Object value ) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(
        fieldName );
      field.setAccessible( true );
      field.set( target,
                 value );
    } catch( ReflectiveOperationException e ) {
      throw new IllegalStateException( e );
    }
  }

  private String extractViewState( String html ) {
    Matcher matcher = VIEW_STATE_PATTERN.matcher( html );
    assertTrue( matcher.find(),
                "JSF ViewState was not found in login page." );
    return matcher.group( 1 );
  }

  private String encode( String value ) {
    return URLEncoder.encode( value,
                              StandardCharsets.UTF_8 );
  }

  private HttpResponse<String> send( HttpClient client,
                                     HttpRequest request ) throws IOException,
                                                                  InterruptedException {
    return client.send( request,
                        HttpResponse.BodyHandlers.ofString() );
  }

  private final class AuthenticatedClient {

    private final HttpClient client;

    private AuthenticatedClient( HttpClient client ) {
      this.client = client;
    }

    private HttpResponse<String> get( String path ) throws IOException,
                                                           InterruptedException {
      return send( client,
                   HttpRequest.newBuilder( URI.create( baseUrl + path ) )
                     .timeout( HTTP_TIMEOUT )
                     .GET()
                     .build() );
    }
  }

  private static final class ListContentFixture {

    private final Participant viewer;
    private final String password;
    private final Participant owner;
    private final String variantName;
    private final String venueName;
    private final String regattaIdText;
    private final String registrationIdText;
    private final String registrationStatus;
    private final String carName;
    private final String ownerName;
    private final String viewerName;
    private final String pointsSd;
    private final String pointsRd;
    private final String pointsEd;
    private final String pointsSo;
    private final String pointsRo;
    private final String pointsEo;
    private final String penaltyPercent;

    private ListContentFixture( Participant viewer,
                                String password,
                                Participant owner,
                                String variantName,
                                String venueName,
                                String regattaIdText,
                                String registrationIdText,
                                String registrationStatus,
                                String carName,
                                String ownerName,
                                String viewerName,
                                String pointsSd,
                                String pointsRd,
                                String pointsEd,
                                String pointsSo,
                                String pointsRo,
                                String pointsEo,
                                String penaltyPercent ) {
      this.viewer = viewer;
      this.password = password;
      this.owner = owner;
      this.variantName = variantName;
      this.venueName = venueName;
      this.regattaIdText = regattaIdText;
      this.registrationIdText = registrationIdText;
      this.registrationStatus = registrationStatus;
      this.carName = carName;
      this.ownerName = ownerName;
      this.viewerName = viewerName;
      this.pointsSd = pointsSd;
      this.pointsRd = pointsRd;
      this.pointsEd = pointsEd;
      this.pointsSo = pointsSo;
      this.pointsRo = pointsRo;
      this.pointsEo = pointsEo;
      this.penaltyPercent = penaltyPercent;
    }

    private Participant viewer() {
      return viewer;
    }

    private String password() {
      return password;
    }

    private String variantName() {
      return variantName;
    }

    private String venueName() {
      return venueName;
    }

    private String regattaIdText() {
      return regattaIdText;
    }

    private String registrationStatus() {
      return registrationStatus;
    }

    private String registrationIdText() {
      return registrationIdText;
    }

    private String carName() {
      return carName;
    }

    private String ownerName() {
      return ownerName;
    }

    private String viewerName() {
      return viewerName;
    }

    private String pointsSd() {
      return pointsSd;
    }

    private String pointsRd() {
      return pointsRd;
    }

    private String pointsEd() {
      return pointsEd;
    }

    private String pointsSo() {
      return pointsSo;
    }

    private String pointsRo() {
      return pointsRo;
    }

    private String pointsEo() {
      return pointsEo;
    }

    private String penaltyPercent() {
      return penaltyPercent;
    }

    private Participant owner() {
      return owner;
    }
  }
}
