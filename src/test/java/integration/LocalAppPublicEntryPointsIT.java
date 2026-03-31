package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class LocalAppPublicEntryPointsIT {

  private final String baseUrl = System.getProperty(
    "topracing.baseUrl",
    "http://localhost:8080/topracingwebapp" );

  private final HttpClient client = HttpClient.newBuilder()
    .connectTimeout( Duration.ofSeconds( 10 ) )
    .followRedirects( Redirect.NEVER )
    .build();

  @Test
  void welcomeAndLoginPagesRespond() throws IOException,
                                           InterruptedException {
    HttpResponse<String> welcome = get( "/faces/welcome.xhtml" );
    HttpResponse<String> login = get( "/faces/login.xhtml" );

    assertEquals( 200,
                  welcome.statusCode() );
    assertTrue( welcome.body().contains( "TOP-Racing" ) );

    assertEquals( 200,
                  login.statusCode() );
    assertTrue( login.body().contains( "participant_email" ) );
    assertTrue( login.body().contains( "participant_password" ) );
  }

  @Test
  void externalEntryPointsArePublic() throws IOException,
                                             InterruptedException {
    HttpResponse<String> editParticipant = get( "/faces/editparticipant.xhtml" );
    HttpResponse<String> complaint = get( "/faces/complaint.xhtml" );

    assertEquals( 200,
                  editParticipant.statusCode() );
    assertTrue( editParticipant.body().contains( "TOP-Racing" ) );

    assertEquals( 200,
                  complaint.statusCode() );
    assertTrue( complaint.body().contains( "TOP-Racing" ) );
  }

  private HttpResponse<String> get( String path ) throws IOException,
                                                         InterruptedException {
    HttpRequest request = HttpRequest.newBuilder( URI.create( baseUrl + path ) )
      .timeout( Duration.ofSeconds( 20 ) )
      .GET()
      .build();
    return client.send( request,
                        HttpResponse.BodyHandlers.ofString() );
  }
}
