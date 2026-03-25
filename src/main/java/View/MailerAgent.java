/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
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

  private static final String GMAIL_SEND_URL =
                              "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";
  private static final String DEFAULT_TOKEN_URL =
                              "https://oauth2.googleapis.com/token";
  private static final long TOKEN_REFRESH_MARGIN_MS = 60_000L;

  private static final Object TOKEN_LOCK = new Object();
  private static volatile String cachedAccessToken = null;
  private static volatile long cachedAccessTokenExpiryMs = 0L;

  private final String senderAddress = getEnvOrDefault(
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
                        + "\n" + messageText );

    boolean acquired = false;
    try {
      conduct.acquire();
      acquired = true;

      prepareMessage();
      String encodedMessage = buildEncodedMessage();
      String accessToken = getAccessToken();
      sendWithOAuth( accessToken,
                     encodedMessage );

    } catch( InterruptedException ex ) {
      Thread.currentThread()
        .interrupt();
      System.out.println( new Date() + " !!! MailerAgent interrupted: " + ex
        .getMessage() );
    } catch( MessagingException |
             IOException ex ) {
      System.out.println( new Date() + " !!! MailerAgent send failed: " + ex
        .getMessage() );
    } finally {
      if( acquired ) {
        conduct.release();
      }
    }

    System.out.println( new Date() + " <<<<<< " + new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss.SSS" )
      .format( new Date() ) + " MailerAgent run end: " + id );
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

  private void sendWithOAuth( String accessToken,
                              String encodedMessage )
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
    } finally {
      if( connection != null ) {
        connection.disconnect();
      }
    }
  }

  private static String getAccessToken()
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

      fetchAccessToken();
      return cachedAccessToken;
    }
  }

  private static void fetchAccessToken()
    throws IOException,
           MessagingException {
    String clientId = getRequiredEnv( "MAIL_OAUTH_CLIENT_ID" );
    String clientSecret = getRequiredEnv( "MAIL_OAUTH_CLIENT_SECRET" );
    String refreshToken = getRequiredEnv( "MAIL_OAUTH_REFRESH_TOKEN" );
    String tokenUrl = getEnvOrDefault( "MAIL_OAUTH_TOKEN_URL",
                                       DEFAULT_TOKEN_URL );

    String form = "client_id=" + urlEncode( clientId )
                  + "&client_secret=" + urlEncode( clientSecret )
                  + "&refresh_token=" + urlEncode( refreshToken )
                  + "&grant_type=refresh_token";

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) new URL( tokenUrl ).openConnection();
      connection.setRequestMethod( "POST" );
      connection.setDoOutput( true );
      connection.setRequestProperty(
        "Content-Type",
        "application/x-www-form-urlencoded; charset=UTF-8" );

      byte[] formBytes = form.getBytes( StandardCharsets.UTF_8 );
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
    } finally {
      if( connection != null ) {
        connection.disconnect();
      }
    }
  }

  private static String getRequiredEnv( String key )
    throws MessagingException {
    String value = System.getenv( key );
    if( value == null
        || value.trim()
          .isEmpty() ) {
      throw new MessagingException( "Missing required environment variable: "
                                    + key );
    }
    return value.trim();
  }

  private static String getEnvOrDefault( String key,
                                         String defaultValue ) {
    String value = System.getenv( key );
    return ( value == null
             || value.trim()
               .isEmpty() )
           ? defaultValue
           : value.trim();
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

}

