package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Model.ModelBean;
import Tables.Participant;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class LocalAppAuthenticatedListsIT {

  private static final ModelBean MODEL = new ModelBean();
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

  private AuthenticatedClient login( String email,
                                     String password ) throws IOException,
                                                              InterruptedException {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy( CookiePolicy.ACCEPT_ALL );

    HttpClient client = HttpClient.newBuilder()
      .cookieHandler( cookieManager )
      .connectTimeout( Duration.ofSeconds( 10 ) )
      .followRedirects( Redirect.NEVER )
      .build();

    HttpResponse<String> loginPage = send( client,
                                           HttpRequest.newBuilder( URI.create(
                                             baseUrl + "/faces/login.xhtml" ) )
                                             .timeout( Duration.ofSeconds( 20 ) )
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
                                                 .timeout( Duration.ofSeconds(
                                                   20 ) )
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
    participant.setDefaulter( 0 );
    return MODEL.save( participant,
                       false );
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
                     .timeout( Duration.ofSeconds( 20 ) )
                     .GET()
                     .build() );
    }
  }
}
