// src/main/java/Model/PaypalHttpTransport.java
// Sends PayPal REST API HTTP requests using the Java 11 HTTP client.
package Model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class PaypalHttpTransport
  implements PaypalTransport {

  private final String baseUrl;
  private final HttpClient httpClient;

  PaypalHttpTransport( String baseUrl ) {
    this.baseUrl = trimTrailingSlash( baseUrl );
    this.httpClient = HttpClient.newHttpClient();
  }

  @Override
  public String postForm( String path,
                          String authorizationHeader,
                          String body ) {
    HttpRequest request = requestBuilder( path,
                                          authorizationHeader )
      .header( "Content-Type",
               "application/x-www-form-urlencoded" )
      .POST( HttpRequest.BodyPublishers.ofString( body ) )
      .build();
    return send( request );
  }

  @Override
  public String postJson( String path,
                          String authorizationHeader,
                          String body ) {
    HttpRequest request = requestBuilder( path,
                                          authorizationHeader )
      .header( "Content-Type",
               "application/json" )
      .POST( HttpRequest.BodyPublishers.ofString( body ) )
      .build();
    return send( request );
  }

  @Override
  public String get( String path,
                     String authorizationHeader ) {
    HttpRequest request = requestBuilder( path,
                                          authorizationHeader )
      .GET()
      .build();
    return send( request );
  }

  private HttpRequest.Builder requestBuilder( String path,
                                              String authorizationHeader ) {
    return HttpRequest.newBuilder( URI.create( baseUrl + path ) )
      .header( "Authorization",
               authorizationHeader )
      .header( "Accept",
               "application/json" );
  }

  private String send( HttpRequest request ) {
    try {
      HttpResponse<String> response = httpClient.send(
        request,
        HttpResponse.BodyHandlers.ofString() );
      int statusCode = response.statusCode();
      if( statusCode < 200 || statusCode >= 300 ) {
        throw new IllegalStateException(
          "PayPal request failed. statusCode=" + statusCode
          + ", body=" + response.body() );
      }
      return response.body();
    } catch( IOException e ) {
      throw new IllegalStateException( "PayPal request failed due to I/O error.",
                                       e );
    } catch( InterruptedException e ) {
      Thread.currentThread()
        .interrupt();
      throw new IllegalStateException( "PayPal request was interrupted.",
                                       e );
    }
  }

  private String trimTrailingSlash( String value ) {
    if( value == null || value.isBlank() ) {
      return "https://api-m.sandbox.paypal.com";
    }
    while( value.endsWith( "/" ) ) {
      value = value.substring( 0,
                               value.length() - 1 );
    }
    return value;
  }
}
