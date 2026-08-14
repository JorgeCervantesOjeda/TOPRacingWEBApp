// src/test/java/Model/PaypalOnboardingServiceTest.java
// Verifies PayPal onboarding response parsing and readiness decisions.
package Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Tables.Participant;
import org.junit.jupiter.api.Test;

class PaypalOnboardingServiceTest {

  @Test
  void merchantStatusIsUsableOnlyWhenPaymentsAndEmailAreReady() {
    String json = "{"
                  + "\"payments_receivable\":true,"
                  + "\"primary_email_confirmed\":true,"
                  + "\"granted_permissions\":[\"EXPRESS_CHECKOUT\"],"
                  + "\"limitations\":[]"
                  + "}";

    PaypalMerchantStatus status = PaypalMerchantStatus.fromJson( json );

    assertTrue( status.isUsable() );
  }

  @Test
  void merchantStatusIsNotUsableWhenEmailIsNotConfirmed() {
    String json = "{"
                  + "\"payments_receivable\":true,"
                  + "\"primary_email_confirmed\":false,"
                  + "\"granted_permissions\":[\"EXPRESS_CHECKOUT\"],"
                  + "\"limitations\":[]"
                  + "}";

    PaypalMerchantStatus status = PaypalMerchantStatus.fromJson( json );

    assertFalse( status.isUsable() );
  }

  @Test
  void createSignupLinkReturnsPayPalActionUrlFromReferralResponse() {
    FakePaypalTransport transport = new FakePaypalTransport();
    transport.tokenResponse = "{\"access_token\":\"token-123\"}";
    transport.referralResponse = "{"
                                 + "\"links\":["
                                 + "{\"rel\":\"self\",\"href\":\"https://api.example/ref\"},"
                                 + "{\"rel\":\"action_url\",\"href\":\"https://paypal.example/signup\"}"
                                 + "]"
                                 + "}";
    PaypalOnboardingService service = new PaypalOnboardingService(
      PaypalConfiguration.forTest( "https://api-m.sandbox.paypal.com",
                                   "client",
                                   "secret",
                                   "partner",
                                   "BN-CODE",
                                   "EXPRESS_CHECKOUT" ),
      transport );
    Participant participant = new Participant();
    participant.setEmail( "seller@example.com" );

    String signupLink = service.createSignupLink( participant,
                                                  "tracking-1",
                                                  "https://top.example/paypalreturn.xhtml" );

    assertEquals( "https://paypal.example/signup",
                  signupLink );
    assertTrue( transport.lastJsonBody.contains( "\"tracking_id\":\"tracking-1\"" ) );
    assertTrue( transport.lastJsonBody.contains( "\"email\":\"seller@example.com\"" ) );
  }

  @Test
  void createSignupLinkRejectsLiveEndpointUnlessLiveModeIsExplicitlyAllowed() {
    FakePaypalTransport transport = new FakePaypalTransport();
    transport.tokenResponse = "{\"access_token\":\"token-123\"}";
    transport.referralResponse = "{"
                                 + "\"links\":["
                                 + "{\"rel\":\"action_url\",\"href\":\"https://paypal.example/signup\"}"
                                 + "]"
                                 + "}";
    PaypalOnboardingService service = new PaypalOnboardingService(
      PaypalConfiguration.forTest( "https://api-m.paypal.com",
                                   "client",
                                   "secret",
                                   "partner",
                                   "BN-CODE",
                                   "EXPRESS_CHECKOUT" ),
      transport );
    Participant participant = new Participant();
    participant.setEmail( "seller@example.com" );

    assertThrows( IllegalStateException.class,
                  () -> service.createSignupLink( participant,
                                                  "tracking-live",
                                                  "https://top.example/paypalreturn.xhtml" ) );
  }

  @Test
  void createSignupLinkUsesLocalSandboxMockWhenEnabled() {
    FakePaypalTransport transport = new FakePaypalTransport();
    PaypalOnboardingService service = new PaypalOnboardingService(
      PaypalConfiguration.forSandboxMockTest(),
      transport );
    Participant participant = new Participant();
    participant.setEmail( "seller@example.com" );

    String signupLink = service.createSignupLink(
      participant,
      "tracking-local",
      "http://localhost:8080/topracingwebapp/faces/paypalreturn.xhtml" );

    assertEquals(
      "http://localhost:8080/topracingwebapp/faces/paypalsandboxmock.xhtml"
      + "?merchantId=tracking-local"
      + "&merchantIdInPayPal=sandbox-merchant-tracking-local"
      + "&permissionsGranted=true",
      signupLink );
    assertFalse( transport.wasCalled );
  }

  private static final class FakePaypalTransport
    implements PaypalTransport {

    private String tokenResponse;
    private String referralResponse;
    private String lastJsonBody;

    @Override
    public String postForm( String path,
                            String authorizationHeader,
                            String body ) {
      this.wasCalled = true;
      return tokenResponse;
    }

    @Override
    public String postJson( String path,
                            String authorizationHeader,
                            String body ) {
      this.wasCalled = true;
      this.lastJsonBody = body;
      return referralResponse;
    }

    @Override
    public String get( String path,
                       String authorizationHeader ) {
      this.wasCalled = true;
      return "{}";
    }

    private boolean wasCalled;
  }
}
