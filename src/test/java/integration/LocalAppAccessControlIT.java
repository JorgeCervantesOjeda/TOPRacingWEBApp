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

class LocalAppAccessControlIT {

  private final String baseUrl = System.getProperty(
    "topracing.baseUrl",
    "http://localhost:8080/topracingwebapp" );

  private final HttpClient client = HttpClient.newBuilder()
    .connectTimeout( Duration.ofSeconds( 10 ) )
    .followRedirects( Redirect.NEVER )
    .build();

  @Test
  void anonymousUsersAreRedirectedAwayFromProtectedPages() throws IOException,
                                                                  InterruptedException {
    HttpResponse<String> editRegatta = get( "/faces/editregatta.xhtml" );

    assertEquals( 302,
                  editRegatta.statusCode() );
    assertTrue( editRegatta.headers()
      .firstValue( "location" )
      .orElse( "" )
      .endsWith( "/faces/login.xhtml" ) );
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
