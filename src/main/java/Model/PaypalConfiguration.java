// src/main/java/Model/PaypalConfiguration.java
// Reads PayPal onboarding configuration from runtime properties or environment variables.
package Model;

class PaypalConfiguration {

  private static final String DEFAULT_BASE_URL = "https://api-m.sandbox.paypal.com";
  private static final String DEFAULT_PRODUCT = "EXPRESS_CHECKOUT";

  private final String baseUrl;
  private final String clientId;
  private final String clientSecret;
  private final String partnerId;
  private final String bnCode;
  private final String product;
  private final boolean liveEndpointAllowed;
  private final boolean sandboxMock;

  private PaypalConfiguration( String baseUrl,
                               String clientId,
                               String clientSecret,
                               String partnerId,
                               String bnCode,
                               String product,
                               boolean liveEndpointAllowed,
                               boolean sandboxMock ) {
    this.baseUrl = baseUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.partnerId = partnerId;
    this.bnCode = bnCode;
    this.product = product;
    this.liveEndpointAllowed = liveEndpointAllowed;
    this.sandboxMock = sandboxMock;
  }

  static PaypalConfiguration fromRuntime() {
    return new PaypalConfiguration(
      valueOf( "topracing.paypal.baseUrl",
               "TOPRACING_PAYPAL_BASE_URL",
               DEFAULT_BASE_URL ),
      valueOf( "topracing.paypal.clientId",
               "TOPRACING_PAYPAL_CLIENT_ID",
               "" ),
      valueOf( "topracing.paypal.clientSecret",
               "TOPRACING_PAYPAL_CLIENT_SECRET",
               "" ),
      valueOf( "topracing.paypal.partnerId",
               "TOPRACING_PAYPAL_PARTNER_ID",
               "" ),
      valueOf( "topracing.paypal.bnCode",
               "TOPRACING_PAYPAL_BN_CODE",
               "" ),
      valueOf( "topracing.paypal.product",
               "TOPRACING_PAYPAL_PRODUCT",
               DEFAULT_PRODUCT ),
      booleanValueOf( "topracing.paypal.allowLive",
                      "TOPRACING_PAYPAL_ALLOW_LIVE",
                      false ),
      booleanValueOf( "topracing.paypal.sandboxMock",
                      "TOPRACING_PAYPAL_SANDBOX_MOCK",
                      false ) );
  }

  static PaypalConfiguration forTest( String baseUrl,
                                      String clientId,
                                      String clientSecret,
                                      String partnerId,
                                      String bnCode,
                                      String product ) {
    return new PaypalConfiguration( baseUrl,
                                    clientId,
                                    clientSecret,
                                    partnerId,
                                    bnCode,
                                    product,
                                    false,
                                    false );
  }

  static PaypalConfiguration forSandboxMockTest() {
    return new PaypalConfiguration( DEFAULT_BASE_URL,
                                    "",
                                    "",
                                    "",
                                    "",
                                    DEFAULT_PRODUCT,
                                    false,
                                    true );
  }

  boolean isConfigured() {
    return hasText( clientId )
           && hasText( clientSecret )
           && hasText( partnerId );
  }

  String getBaseUrl() {
    return baseUrl;
  }

  String getClientId() {
    return clientId;
  }

  String getClientSecret() {
    return clientSecret;
  }

  String getPartnerId() {
    return partnerId;
  }

  String getBnCode() {
    return bnCode;
  }

  String getProduct() {
    return product;
  }

  boolean isSandboxMock() {
    return sandboxMock;
  }

  void assertSafeEndpoint() {
    if( isLiveEndpoint()
        && !liveEndpointAllowed ) {
      throw new IllegalStateException(
        "PayPal live endpoint is disabled. Use sandbox URL "
        + DEFAULT_BASE_URL
        + " or set TOPRACING_PAYPAL_ALLOW_LIVE=true explicitly." );
    }
  }

  private boolean isLiveEndpoint() {
    return "https://api-m.paypal.com".equalsIgnoreCase( trimTrailingSlash( baseUrl ) );
  }

  private static String valueOf( String propertyName,
                                 String environmentName,
                                 String defaultValue ) {
    String value = System.getProperty( propertyName );
    if( hasText( value ) ) {
      return value;
    }
    value = System.getenv( environmentName );
    if( hasText( value ) ) {
      return value;
    }
    return defaultValue;
  }

  private static boolean hasText( String value ) {
    return value != null
           && !value.isBlank();
  }

  private static boolean booleanValueOf( String propertyName,
                                         String environmentName,
                                         boolean defaultValue ) {
    String value = System.getProperty( propertyName );
    if( hasText( value ) ) {
      return Boolean.parseBoolean( value );
    }
    value = System.getenv( environmentName );
    if( hasText( value ) ) {
      return Boolean.parseBoolean( value );
    }
    return defaultValue;
  }

  private static String trimTrailingSlash( String value ) {
    if( value == null ) {
      return "";
    }
    while( value.endsWith( "/" ) ) {
      value = value.substring( 0,
                               value.length() - 1 );
    }
    return value;
  }
}
