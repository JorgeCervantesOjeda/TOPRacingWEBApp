// src/main/java/Model/JsonText.java
// Provides narrow JSON field extraction helpers for PayPal REST responses.
package Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class JsonText {

  private JsonText() {
  }

  static boolean booleanValueOf( String json,
                                 String key ) {
    String marker = "\"" + key + "\"";
    int keyIndex = indexOf( json,
                            marker );
    if( keyIndex < 0 ) {
      return false;
    }
    int colonIndex = json.indexOf( ':',
                                   keyIndex + marker.length() );
    if( colonIndex < 0 ) {
      return false;
    }
    String value = json.substring( colonIndex + 1 )
      .trim();
    return value.startsWith( "true" );
  }

  static String stringValueOf( String json,
                               String key ) {
    String marker = "\"" + key + "\"";
    int keyIndex = indexOf( json,
                            marker );
    if( keyIndex < 0 ) {
      return "";
    }
    int colonIndex = json.indexOf( ':',
                                   keyIndex + marker.length() );
    if( colonIndex < 0 ) {
      return "";
    }
    int valueStart = json.indexOf( '"',
                                   colonIndex + 1 );
    if( valueStart < 0 ) {
      return "";
    }
    int valueEnd = valueStart + 1;
    boolean escaped = false;
    while( valueEnd < json.length() ) {
      char c = json.charAt( valueEnd );
      if( c == '"' && !escaped ) {
        return json.substring( valueStart + 1,
                               valueEnd );
      }
      escaped = c == '\\' && !escaped;
      if( c != '\\' ) {
        escaped = false;
      }
      valueEnd++;
    }
    return "";
  }

  static boolean arrayIsEmpty( String json,
                               String key ) {
    String marker = "\"" + key + "\"";
    int keyIndex = indexOf( json,
                            marker );
    if( keyIndex < 0 ) {
      return true;
    }
    int openIndex = json.indexOf( '[',
                                  keyIndex + marker.length() );
    if( openIndex < 0 ) {
      return true;
    }
    int closeIndex = json.indexOf( ']',
                                   openIndex + 1 );
    if( closeIndex < 0 ) {
      return true;
    }
    return json.substring( openIndex + 1,
                           closeIndex )
      .trim()
      .isEmpty();
  }

  static String linkHrefByRel( String json,
                               String rel ) {
    String relMarker = "\"rel\":\"" + rel + "\"";
    int relIndex = compact( json )
      .indexOf( relMarker );
    if( relIndex < 0 ) {
      return "";
    }
    String compactJson = compact( json );
    int objectStart = compactJson.lastIndexOf( '{',
                                               relIndex );
    int objectEnd = compactJson.indexOf( '}',
                                         relIndex );
    if( objectStart < 0 || objectEnd < 0 ) {
      return "";
    }
    return stringValueOf( compactJson.substring( objectStart,
                                                 objectEnd + 1 ),
                          "href" );
  }

  static String escape( String value ) {
    if( value == null ) {
      return "";
    }
    return value.replace( "\\",
                          "\\\\" )
      .replace( "\"",
                "\\\"" )
      .replace( "\r",
                "\\r" )
      .replace( "\n",
                "\\n" );
  }

  static String urlValueOf( String value ) {
    if( value == null ) {
      return "";
    }
    return URLEncoder.encode( value,
                              StandardCharsets.UTF_8 );
  }

  private static int indexOf( String json,
                              String marker ) {
    if( json == null ) {
      return -1;
    }
    return compact( json )
      .indexOf( marker );
  }

  private static String compact( String json ) {
    if( json == null ) {
      return "";
    }
    return json.replaceAll( "\\s+(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)",
                            "" );
  }
}
