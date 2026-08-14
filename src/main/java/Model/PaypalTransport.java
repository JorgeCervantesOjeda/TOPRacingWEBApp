// src/main/java/Model/PaypalTransport.java
// Defines the HTTP operations required by the PayPal onboarding service.
package Model;

interface PaypalTransport {

  String postForm( String path,
                   String authorizationHeader,
                   String body );

  String postJson( String path,
                   String authorizationHeader,
                   String body );

  String get( String path,
              String authorizationHeader );
}
