/*
 * src/main/java/View/MailerAgent.java
 * Sends TOP Racing e-mail notifications through the configured mail transport.
 */
package View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 *
 * @author usuario
 */
public class MailerAgent
  extends Thread {

  private static final Logger LOGGER = Logger.getLogger(
    MailerAgent.class.getName() );

  private static final String MODE_AUTO = "auto";
  private static final String MODE_SMTP = "smtp";
  private static final String MODE_GMAIL_OAUTH = "gmail-oauth";
  private static final String MODE_LOG = "log";
  private static final String MODE_DISABLED = "disabled";

  private static final String GMAIL_SEND_URL =
                              "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";
  private static final String DEFAULT_TOKEN_URL =
                              "https://oauth2.googleapis.com/token";
  private static final long TOKEN_REFRESH_MARGIN_MS = 60_000L;

  private static final Object TOKEN_LOCK = new Object();
  private static volatile String cachedAccessToken = null;
  private static volatile long cachedAccessTokenExpiryMs = 0L;

  private final String senderAddress = getRuntimeValueOrDefault(
        "MAIL_SENDER_EMAIL",
        "top.racing.org@gmail.com" );

  private final Session session;
  private final Message message;
  private final Multipart multipart;
  private final String recipientAddress;
  private final String messageText;
  private final double id;

  private final Semaphore conduct;
  private final long referenceNumber;

  public MailerAgent(
    Semaphore conduct,
    long referenceNumber,
    String recipientAddress,
    String messageText,
    String attachmentName )
    throws AddressException,
           MessagingException {
    this.id = Math.random();
    this.conduct = conduct;
    this.referenceNumber = referenceNumber;
    this.recipientAddress = recipientAddress;
    this.messageText = messageText + "\n" + referenceNumber;
    this.multipart = new MimeMultipart();
    this.session = Session.getInstance( new Properties() );
    this.message = new MimeMessage( session );
  }

  @Override
  public void run() {
    System.out.println( new Date() + " >>>>>> "
                        + new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" )
        .format( new Date() )
                        + " MailerAgent run start: " + id
                        + "\nRecipient: " + this.recipientAddress
                        + "\nMessage length: " + messageText.length() );

    try {
      sendNow();
    } catch( MessagingException ex ) {
      LOGGER.log( Level.SEVERE,
                  "MailerAgent send failed for recipient={0}. Cause: {1}",
                  new Object[]{ recipientAddress,
                                ex.getMessage() } );
    }

    System.out.println( new Date() + " <<<<<< " + new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss.SSS" )
      .format( new Date() ) + " MailerAgent run end: " + id );
  }

  public void sendNow()
    throws MessagingException {
    boolean acquired = false;
    try {
      conduct.acquire();
      acquired = true;

      prepareMessage();
      String encodedMessage = buildEncodedMessage();
      DeliveryConfiguration configuration =
                            deliveryConfigurationFrom( runtimeConfiguration() );
      LOGGER.log( Level.INFO,
                  "MailerAgent selected delivery mode={0}, recipient={1}, reference={2}",
                  new Object[]{ configuration.mode,
                                recipientAddress,
                                referenceNumber } );
      sendWithConfiguredTransport( configuration,
                                   encodedMessage );
    } catch( InterruptedException ex ) {
      Thread.currentThread()
        .interrupt();
      throw new MessagingException(
        "MailerAgent interrupted before sending message.",
        ex );
    } catch( IOException ex ) {
      throw new MessagingException(
        "MailerAgent could not build or submit message. Root cause: "
        + rootCauseSummary( ex ),
        ex );
    } finally {
      if( acquired ) {
        conduct.release();
      }
    }
  }

  private void prepareMessage()
    throws MessagingException {
    message.setFrom( new InternetAddress( senderAddress ) );
    message.addRecipient(
      Message.RecipientType.TO,
      new InternetAddress( recipientAddress ) );
    message.setSubject( "TOP-Racing " );

    BodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setText( messageText + "\n" );
    multipart.addBodyPart( messageBodyPart );

    message.setContent( multipart );
  }

  private String buildEncodedMessage()
    throws MessagingException,
           IOException {
    try( ByteArrayOutputStream output = new ByteArrayOutputStream() ) {
      message.writeTo( output );
      return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString( output.toByteArray() );
    }
  }

  private void sendWithConfiguredTransport( DeliveryConfiguration configuration,
                                            String encodedMessage )
    throws MessagingException,
           IOException {
    if( MODE_SMTP.equals( configuration.mode ) ) {
      sendWithSmtp( configuration );
      return;
    }

    if( MODE_GMAIL_OAUTH.equals( configuration.mode ) ) {
      String accessToken = getAccessToken( configuration );
      sendWithOAuth( accessToken,
                     encodedMessage,
                     configuration );
      return;
    }

    if( MODE_LOG.equals( configuration.mode ) ) {
      LOGGER.log( Level.INFO,
                  "MailerAgent log mode. E-mail not sent. recipient={0}, reference={1}",
                  new Object[]{ recipientAddress,
                                referenceNumber } );
      return;
    }

    if( MODE_DISABLED.equals( configuration.mode ) ) {
      LOGGER.log( Level.WARNING,
                  "MailerAgent disabled. E-mail not sent. recipient={0}, reference={1}",
                  new Object[]{ recipientAddress,
                                referenceNumber } );
      return;
    }

    throw new MessagingException( "Unsupported mail delivery mode: "
                                  + configuration.mode );
  }

  private void sendWithSmtp( DeliveryConfiguration configuration )
    throws MessagingException {
    Properties properties = new Properties();
    properties.put( "mail.smtp.host",
                    configuration.smtpHost );
    properties.put( "mail.smtp.port",
                    String.valueOf( configuration.smtpPort ) );
    properties.put( "mail.smtp.auth",
                    String.valueOf( configuration.smtpAuth ) );
    properties.put( "mail.smtp.starttls.enable",
                    String.valueOf( configuration.smtpStartTls ) );
    properties.put( "mail.smtp.ssl.enable",
                    String.valueOf( configuration.smtpSsl ) );
    properties.put( "mail.smtp.connectiontimeout",
                    String.valueOf( configuration.smtpTimeoutMs ) );
    properties.put( "mail.smtp.timeout",
                    String.valueOf( configuration.smtpTimeoutMs ) );
    properties.put( "mail.smtp.writetimeout",
                    String.valueOf( configuration.smtpTimeoutMs ) );

    Authenticator authenticator = configuration.smtpAuth
                                  ? new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication( configuration.smtpUsername,
                                             configuration.smtpPassword );
        }
      }
                                  : null;

    Session smtpSession = Session.getInstance( properties,
                                               authenticator );
    message.saveChanges();

    try( Transport transport = smtpSession.getTransport( "smtp" ) ) {
      if( configuration.smtpAuth ) {
        transport.connect( configuration.smtpHost,
                           configuration.smtpPort,
                           configuration.smtpUsername,
                           configuration.smtpPassword );
      } else {
        transport.connect();
      }
      transport.sendMessage( message,
                             message.getAllRecipients() );
    }
  }

  private void sendWithOAuth( String accessToken,
                              String encodedMessage,
                              DeliveryConfiguration configuration )
    throws IOException,
           MessagingException {
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) new URL( GMAIL_SEND_URL ).openConnection();
      connection.setRequestMethod( "POST" );
      connection.setDoOutput( true );
      connection.setRequestProperty( "Authorization",
                                     "Bearer " + accessToken );
      connection.setRequestProperty( "Content-Type",
                                     "application/json; charset=UTF-8" );

      String payload = "{\"raw\":\"" + encodedMessage + "\"}";
      byte[] payloadBytes = payload.getBytes( StandardCharsets.UTF_8 );
      configureHttpConnection( connection,
                               configuration.httpTimeoutMs,
                               payloadBytes.length );
      connection.setRequestProperty( "Content-Length",
                                     String.valueOf( payloadBytes.length ) );

      try( OutputStream output = connection.getOutputStream() ) {
        output.write( payloadBytes );
      }

      int status = connection.getResponseCode();
      if( status < 200
          || status >= 300 ) {
        String responseBody = readResponseBody( connection.getErrorStream() );
        throw new MessagingException(
          "Gmail API send failed. HTTP " + status + " " + responseBody );
      }

      readResponseBody( connection.getInputStream() );
      LOGGER.log( Level.INFO,
                  "Gmail API accepted message. recipient={0}, reference={1}, httpStatus={2}",
                  new Object[]{ recipientAddress,
                                referenceNumber,
                                status } );
    } finally {
      if( connection != null ) {
        connection.disconnect();
      }
    }
  }

  private static String getAccessToken( DeliveryConfiguration configuration )
    throws IOException,
           MessagingException {
    long now = System.currentTimeMillis();
    if( cachedAccessToken != null
        && now < ( cachedAccessTokenExpiryMs - TOKEN_REFRESH_MARGIN_MS ) ) {
      return cachedAccessToken;
    }

    synchronized( TOKEN_LOCK ) {
      now = System.currentTimeMillis();
      if( cachedAccessToken != null
          && now < ( cachedAccessTokenExpiryMs - TOKEN_REFRESH_MARGIN_MS ) ) {
        return cachedAccessToken;
      }

      fetchAccessToken( configuration );
      return cachedAccessToken;
    }
  }

  private static void fetchAccessToken( DeliveryConfiguration configuration )
    throws IOException,
           MessagingException {
    String form = "client_id=" + urlEncode( configuration.oauthClientId )
                  + "&client_secret=" + urlEncode(
                    configuration.oauthClientSecret )
                  + "&refresh_token=" + urlEncode(
                    configuration.oauthRefreshToken )
                  + "&grant_type=refresh_token";

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) new URL( configuration.oauthTokenUrl )
        .openConnection();
      connection.setRequestMethod( "POST" );
      connection.setDoOutput( true );
      connection.setRequestProperty(
        "Content-Type",
        "application/x-www-form-urlencoded; charset=UTF-8" );

      byte[] formBytes = form.getBytes( StandardCharsets.UTF_8 );
      configureHttpConnection( connection,
                               configuration.httpTimeoutMs,
                               formBytes.length );
      connection.setRequestProperty( "Content-Length",
                                     String.valueOf( formBytes.length ) );

      try( OutputStream output = connection.getOutputStream() ) {
        output.write( formBytes );
      }

      int status = connection.getResponseCode();
      String responseBody = readResponseBody(
        status >= 400
        ? connection.getErrorStream()
        : connection.getInputStream() );

      if( status < 200
          || status >= 300 ) {
        throw new MessagingException(
          "OAuth token request failed. HTTP " + status + " " + responseBody );
      }

      String accessToken = getJsonString( responseBody,
                                          "access_token" );
      long expiresInSeconds = getJsonLong( responseBody,
                                           "expires_in",
                                           300L );

      if( accessToken == null
          || accessToken.trim()
            .isEmpty() ) {
        throw new MessagingException(
          "OAuth token response has no access_token." );
      }

      cachedAccessToken = accessToken;
      cachedAccessTokenExpiryMs = System.currentTimeMillis()
                                  + ( expiresInSeconds * 1000L );
      LOGGER.log( Level.INFO,
                  "OAuth access token refreshed. expiresInSeconds={0}",
                  expiresInSeconds );
    } finally {
      if( connection != null ) {
        connection.disconnect();
      }
    }
  }

  static DeliveryConfiguration deliveryConfigurationFrom(
    Map<String, String> environment )
    throws MessagingException {
    String requestedMode = getEnvOrDefault( environment,
                                            "MAIL_DELIVERY_MODE",
                                            MODE_AUTO )
      .toLowerCase( Locale.ROOT );
    String mode = resolveMode( requestedMode,
                               environment );

    if( MODE_SMTP.equals( mode ) ) {
      return smtpConfigurationFrom( mode,
                                    environment );
    }

    if( MODE_GMAIL_OAUTH.equals( mode ) ) {
      return oauthConfigurationFrom( mode,
                                     environment );
    }

    if( MODE_LOG.equals( mode )
        || MODE_DISABLED.equals( mode ) ) {
      return new DeliveryConfiguration( mode );
    }

    throw new MessagingException( "Unsupported mail delivery mode: "
                                  + requestedMode );
  }

  private static DeliveryConfiguration smtpConfigurationFrom(
    String mode,
    Map<String, String> environment )
    throws MessagingException {
    DeliveryConfiguration configuration = new DeliveryConfiguration( mode );
    configuration.smtpHost = getRequiredValue( environment,
                                               "MAIL_SMTP_HOST" );
    configuration.smtpPort = getIntValue( environment,
                                          "MAIL_SMTP_PORT",
                                          587 );
    configuration.smtpAuth = getBooleanValue( environment,
                                              "MAIL_SMTP_AUTH",
                                              true );
    configuration.smtpStartTls = getBooleanValue( environment,
                                                  "MAIL_SMTP_STARTTLS",
                                                  true );
    configuration.smtpSsl = getBooleanValue( environment,
                                             "MAIL_SMTP_SSL_ENABLE",
                                             false );
    configuration.smtpTimeoutMs = getIntValue( environment,
                                               "MAIL_SMTP_TIMEOUT_MS",
                                               10000 );
    if( configuration.smtpAuth ) {
      configuration.smtpUsername = getRequiredValue( environment,
                                                     "MAIL_SMTP_USERNAME" );
      configuration.smtpPassword = getRequiredValue( environment,
                                                     "MAIL_SMTP_PASSWORD" );
    }
    return configuration;
  }

  private static DeliveryConfiguration oauthConfigurationFrom(
    String mode,
    Map<String, String> environment )
    throws MessagingException {
    DeliveryConfiguration configuration = new DeliveryConfiguration( mode );
    configuration.oauthClientId = getRequiredValue(
      environment,
      "MAIL_OAUTH_CLIENT_ID" );
    configuration.oauthClientSecret = getRequiredValue(
      environment,
      "MAIL_OAUTH_CLIENT_SECRET" );
    configuration.oauthRefreshToken = getRequiredValue(
      environment,
      "MAIL_OAUTH_REFRESH_TOKEN" );
    configuration.oauthTokenUrl = getEnvOrDefault( environment,
                                                   "MAIL_OAUTH_TOKEN_URL",
                                                   DEFAULT_TOKEN_URL );
    configuration.httpTimeoutMs = getIntValue( environment,
                                               "MAIL_HTTP_TIMEOUT_MS",
                                               10000 );
    return configuration;
  }

  private static String resolveMode( String requestedMode,
                                     Map<String, String> environment )
    throws MessagingException {
    if( !MODE_AUTO.equals( requestedMode ) ) {
      return requestedMode;
    }

    if( hasValue( environment,
                  "MAIL_SMTP_HOST" ) ) {
      return MODE_SMTP;
    }

    if( hasValue( environment,
                  "MAIL_OAUTH_CLIENT_ID" )
        || hasValue( environment,
                     "MAIL_OAUTH_CLIENT_SECRET" )
        || hasValue( environment,
                     "MAIL_OAUTH_REFRESH_TOKEN" ) ) {
      return MODE_GMAIL_OAUTH;
    }

    throw new MessagingException(
      "No mail transport configured. Set MAIL_DELIVERY_MODE=smtp with SMTP variables, "
      + "MAIL_DELIVERY_MODE=gmail-oauth with OAuth variables, or MAIL_DELIVERY_MODE=log for tests." );
  }

  private static String getRequiredValue( Map<String, String> environment,
                                          String key )
    throws MessagingException {
    String value = environment.get( key );
    if( value == null
        || value.trim()
          .isEmpty() ) {
      throw new MessagingException( "Missing required mail environment variable: "
                                    + key );
    }
    return value.trim();
  }

  private static String getEnvOrDefault( String key,
                                         String defaultValue ) {
    return getEnvOrDefault( runtimeConfiguration(),
                            key,
                            defaultValue );
  }

  private static String getEnvOrDefault( Map<String, String> environment,
                                         String key,
                                         String defaultValue ) {
    String value = environment.get( key );
    return ( value == null
             || value.trim()
               .isEmpty() )
           ? defaultValue
           : value.trim();
  }

  private static boolean hasValue( Map<String, String> environment,
                                   String key ) {
    String value = environment.get( key );
    return value != null
           && !value.trim()
             .isEmpty();
  }

  private static boolean getBooleanValue( Map<String, String> environment,
                                          String key,
                                          boolean defaultValue ) {
    String value = environment.get( key );
    if( value == null
        || value.trim()
          .isEmpty() ) {
      return defaultValue;
    }
    return Boolean.parseBoolean( value.trim() );
  }

  private static int getIntValue( Map<String, String> environment,
                                  String key,
                                  int defaultValue )
    throws MessagingException {
    String value = environment.get( key );
    if( value == null
        || value.trim()
          .isEmpty() ) {
      return defaultValue;
    }
    try {
      return Integer.parseInt( value.trim() );
    } catch( NumberFormatException ex ) {
      throw new MessagingException( "Invalid integer mail environment variable: "
                                    + key );
    }
  }

  private static Map<String, String> runtimeConfiguration() {
    Map<String, String> configuration = new HashMap<>( System.getenv() );
    for( String key
         : System.getProperties()
           .stringPropertyNames() ) {
      if( key.startsWith( "MAIL_" ) ) {
        configuration.put( key,
                           System.getProperty( key ) );
      }
    }
    return configuration;
  }

  private static String getRuntimeValueOrDefault( String key,
                                                  String defaultValue ) {
    return getEnvOrDefault( runtimeConfiguration(),
                            key,
                            defaultValue );
  }

  private static String urlEncode( String text )
    throws IOException {
    return URLEncoder.encode( text,
                              "UTF-8" );
  }

  private static String readResponseBody( InputStream input )
    throws IOException {
    if( input == null ) {
      return "";
    }
    try( InputStream stream = input;
         ByteArrayOutputStream output = new ByteArrayOutputStream() ) {
      byte[] buffer = new byte[2048];
      int read;
      while( ( read = stream.read( buffer ) ) != -1 ) {
        output.write( buffer,
                      0,
                      read );
      }
      return new String( output.toByteArray(),
                         StandardCharsets.UTF_8 );
    }
  }

  private static void configureHttpConnection( HttpURLConnection connection,
                                               int timeoutMs,
                                               int contentLength ) {
    connection.setConnectTimeout( timeoutMs );
    connection.setReadTimeout( timeoutMs );
    connection.setFixedLengthStreamingMode( contentLength );
  }

  private static String rootCauseSummary( Throwable throwable ) {
    Throwable current = throwable;
    while( current.getCause() != null ) {
      current = current.getCause();
    }
    return current.getClass().getName() + ": " + current.getMessage();
  }

  private static String getJsonString( String json,
                                       String key ) {
    Pattern pattern = Pattern.compile(
      "\\\"" + Pattern.quote( key ) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"" );
    Matcher matcher = pattern.matcher( json );
    return matcher.find()
           ? matcher.group( 1 )
           : null;
  }

  private static long getJsonLong( String json,
                                   String key,
                                   long defaultValue ) {
    Pattern pattern = Pattern.compile(
      "\\\"" + Pattern.quote( key ) + "\\\"\\s*:\\s*([0-9]+)" );
    Matcher matcher = pattern.matcher( json );
    if( !matcher.find() ) {
      return defaultValue;
    }
    try {
      return Long.parseLong( matcher.group( 1 ) );
    } catch( NumberFormatException ex ) {
      return defaultValue;
    }
  }

  static final class DeliveryConfiguration {

    final String mode;
    String smtpHost;
    int smtpPort;
    String smtpUsername;
    String smtpPassword;
    boolean smtpAuth;
    boolean smtpStartTls;
    boolean smtpSsl;
    int smtpTimeoutMs;
    String oauthClientId;
    String oauthClientSecret;
    String oauthRefreshToken;
    String oauthTokenUrl;
    int httpTimeoutMs;

    DeliveryConfiguration( String mode ) {
      this.mode = mode;
    }
  }
}
