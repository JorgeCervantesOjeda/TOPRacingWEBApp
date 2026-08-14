// src/main/java/Model/PaypalOnboardingService.java
// Creates PayPal seller onboarding links and verifies merchant readiness.
package Model;

import Tables.Participant;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class PaypalOnboardingService {

  private static final String TOKEN_PATH = "/v1/oauth2/token";
  private static final String REFERRAL_PATH = "/v2/customer/partner-referrals";

  private final PaypalConfiguration configuration;
  private final PaypalTransport transport;

  PaypalOnboardingService( PaypalConfiguration configuration,
                           PaypalTransport transport ) {
    this.configuration = configuration;
    this.transport = transport;
  }

  static PaypalOnboardingService fromRuntime() {
    PaypalConfiguration configuration = PaypalConfiguration.fromRuntime();
    return new PaypalOnboardingService(
      configuration,
      new PaypalHttpTransport( configuration.getBaseUrl() ) );
  }

  String createSignupLink( Participant participant,
                           String trackingId,
                           String returnUrl ) {
    if( configuration.isSandboxMock() ) {
      return sandboxMockSignupLink( trackingId,
                                    returnUrl );
    }
    assertConfigured();
    String accessToken = accessToken();
    String response = transport.postJson(
      REFERRAL_PATH,
      bearerHeader( accessToken ),
      referralBody( participant,
                    trackingId,
                    returnUrl ) );
    String signupLink = JsonText.linkHrefByRel( response,
                                                "action_url" );
    if( signupLink.isBlank() ) {
      throw new IllegalStateException(
        "PayPal referral response did not include an action_url link." );
    }
    return signupLink;
  }

  PaypalMerchantStatus getMerchantStatus( String merchantId ) {
    if( configuration.isSandboxMock() ) {
      return PaypalMerchantStatus.fromJson(
        "{"
        + "\"payments_receivable\":true,"
        + "\"primary_email_confirmed\":true,"
        + "\"granted_permissions\":[\"EXPRESS_CHECKOUT\"],"
        + "\"limitations\":[]"
        + "}" );
    }
    assertConfigured();
    String response = transport.get(
      "/v1/customer/partners/"
      + urlSegment( configuration.getPartnerId() )
      + "/merchant-integrations/"
      + urlSegment( merchantId ),
      bearerHeader( accessToken() ) );
    return PaypalMerchantStatus.fromJson( response );
  }

  private String accessToken() {
    String response = transport.postForm(
      TOKEN_PATH,
      basicHeader(),
      "grant_type=client_credentials" );
    String accessToken = JsonText.stringValueOf( response,
                                                 "access_token" );
    if( accessToken.isBlank() ) {
      throw new IllegalStateException(
        "PayPal OAuth response did not include an access_token." );
    }
    return accessToken;
  }

  private String referralBody( Participant participant,
                               String trackingId,
                               String returnUrl ) {
    StringBuilder body = new StringBuilder();
    body.append( "{" );
    body.append( "\"tracking_id\":\"" )
      .append( JsonText.escape( trackingId ) )
      .append( "\"," );
    body.append( "\"email\":\"" )
      .append( JsonText.escape( participant.getEmail() ) )
      .append( "\"," );
    body.append( "\"partner_config_override\":{" );
    body.append( "\"return_url\":\"" )
      .append( JsonText.escape( returnUrl ) )
      .append( "\"" );
    body.append( "}," );
    body.append( "\"operations\":[{" );
    body.append( "\"operation\":\"API_INTEGRATION\"," );
    body.append( "\"api_integration_preference\":{" );
    body.append( "\"rest_api_integration\":{" );
    body.append( "\"integration_method\":\"PAYPAL\"," );
    body.append( "\"integration_type\":\"THIRD_PARTY\"," );
    body.append( "\"third_party_details\":{" );
    body.append( "\"features\":[\"PAYMENT\",\"REFUND\",\"ACCESS_MERCHANT_INFORMATION\"]" );
    body.append( "}}}}" );
    body.append( "}]," );
    body.append( "\"products\":[\"" )
      .append( JsonText.escape( configuration.getProduct() ) )
      .append( "\"]," );
    body.append( "\"legal_consents\":[{" );
    body.append( "\"type\":\"SHARE_DATA_CONSENT\"," );
    body.append( "\"granted\":true" );
    body.append( "}]" );
    body.append( "}" );
    return body.toString();
  }

  private String basicHeader() {
    String credentials = configuration.getClientId()
                         + ":"
                         + configuration.getClientSecret();
    String encoded = Base64.getEncoder()
      .encodeToString( credentials.getBytes( StandardCharsets.UTF_8 ) );
    return "Basic " + encoded;
  }

  private String bearerHeader( String accessToken ) {
    return "Bearer " + accessToken;
  }

  private String sandboxMockSignupLink( String trackingId,
                                        String returnUrl ) {
    String base = returnUrl.replace( "paypalreturn.xhtml",
                                     "paypalsandboxmock.xhtml" );
    return base
           + "?merchantId="
           + JsonText.urlValueOf( trackingId )
           + "&merchantIdInPayPal=sandbox-merchant-"
           + JsonText.urlValueOf( trackingId )
           + "&permissionsGranted=true";
  }

  private void assertConfigured() {
    configuration.assertSafeEndpoint();
    if( !configuration.isConfigured() ) {
      throw new IllegalStateException(
        "PayPal onboarding is not configured. Set TOPRACING_PAYPAL_CLIENT_ID, "
        + "TOPRACING_PAYPAL_CLIENT_SECRET and TOPRACING_PAYPAL_PARTNER_ID." );
    }
  }

  private String urlSegment( String value ) {
    return value == null
           ? ""
           : value.replace( " ",
                            "%20" );
  }
}
