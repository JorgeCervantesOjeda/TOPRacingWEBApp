/*
 * src/test/java/View/MailerAgentTest.java
 * Verifies mail delivery configuration without sending network traffic.
 */
package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MailerAgentTest {

  @AfterEach
  void clearMailDeliveryModeOverride() {
    System.clearProperty( "MAIL_DELIVERY_MODE" );
  }

  @Test
  void autoModeUsesSmtpWhenSmtpHostIsConfigured() throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put( "MAIL_SMTP_HOST",
                     "smtp.example.com" );
    environment.put( "MAIL_SMTP_USERNAME",
                     "sender@example.com" );
    environment.put( "MAIL_SMTP_PASSWORD",
                     "secret" );

    MailerAgent.DeliveryConfiguration configuration =
      MailerAgent.deliveryConfigurationFrom( environment );

    assertEquals( "smtp",
                  configuration.mode );
    assertEquals( "smtp.example.com",
                  configuration.smtpHost );
    assertEquals( 587,
                  configuration.smtpPort );
  }

  @Test
  void autoModeUsesGmailOAuthWhenOAuthCredentialsAreConfigured()
    throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put( "MAIL_OAUTH_CLIENT_ID",
                     "client-id" );
    environment.put( "MAIL_OAUTH_CLIENT_SECRET",
                     "client-secret" );
    environment.put( "MAIL_OAUTH_REFRESH_TOKEN",
                     "refresh-token" );

    MailerAgent.DeliveryConfiguration configuration =
      MailerAgent.deliveryConfigurationFrom( environment );

    assertEquals( "gmail-oauth",
                  configuration.mode );
    assertEquals( "https://oauth2.googleapis.com/token",
                  configuration.oauthTokenUrl );
    assertEquals( 10000,
                  configuration.httpTimeoutMs );
  }

  @Test
  void oauthModeUsesConfiguredHttpTimeout()
    throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put( "MAIL_OAUTH_CLIENT_ID",
                     "client-id" );
    environment.put( "MAIL_OAUTH_CLIENT_SECRET",
                     "client-secret" );
    environment.put( "MAIL_OAUTH_REFRESH_TOKEN",
                     "refresh-token" );
    environment.put( "MAIL_HTTP_TIMEOUT_MS",
                     "15000" );

    MailerAgent.DeliveryConfiguration configuration =
      MailerAgent.deliveryConfigurationFrom( environment );

    assertEquals( 15000,
                  configuration.httpTimeoutMs );
  }

  @Test
  void logModeDoesNotRequireTransportSecrets() throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put( "MAIL_DELIVERY_MODE",
                     "log" );

    MailerAgent.DeliveryConfiguration configuration =
      MailerAgent.deliveryConfigurationFrom( environment );

    assertEquals( "log",
                  configuration.mode );
  }

  @Test
  void autoModeFailsClearlyWithoutConfiguredTransport() {
    MessagingException exception = assertThrows(
      MessagingException.class,
      () -> MailerAgent.deliveryConfigurationFrom( Map.of() ) );

    assertTrue( exception.getMessage()
      .contains( "No mail transport configured" ) );
  }

  @Test
  void explicitSmtpModeRequiresHost() {
    MessagingException exception = assertThrows(
      MessagingException.class,
      () -> MailerAgent.deliveryConfigurationFrom(
        Map.of( "MAIL_DELIVERY_MODE",
                "smtp" ) ) );

    assertTrue( exception.getMessage()
      .contains( "MAIL_SMTP_HOST" ) );
  }

  @Test
  void sendNowCompletesWhenLogModeIsConfigured() throws Exception {
    System.setProperty( "MAIL_DELIVERY_MODE",
                        "log" );
    MailerAgent agent = new MailerAgent( new Semaphore( 1 ),
                                         99L,
                                         "recipient@example.com",
                                         "message",
                                         "" );

    agent.sendNow();
  }
}
