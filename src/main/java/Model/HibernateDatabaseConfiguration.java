// src/main/java/Model/HibernateDatabaseConfiguration.java
// Resolves runtime database settings for local and hosted Hibernate deployments.
package Model;

import java.util.Locale;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;

final class HibernateDatabaseConfiguration {

  static final String DEFAULT_DB_URL =
    "jdbc:mysql://localhost:3306/topracing26?zeroDateTimeBehavior=convertToNull"
      + "&useSSL=false&allowPublicKeyRetrieval=true"
      + "&serverTimezone=America/Mexico_City";
  private static final String DEFAULT_DB_USERNAME = "admin";
  private static final String DEFAULT_DB_PASSWORD = "admin";
  private static final String DEFAULT_DB_POOL_SIZE = "50";
  private static final String DEFAULT_DB_CATALOG = "topracing26";

  private HibernateDatabaseConfiguration() {
  }

  static void applyRuntimeOverrides( Configuration configuration,
                                     Logger log ) {
    String url = configuredValue( "hibernate.connection.url",
                                  "TOPRACING_DB_URL",
                                  DEFAULT_DB_URL,
                                  "local development database URL",
                                  log );
    configuration.setProperty( "hibernate.connection.url",
                               normalizeJdbcUrl( url ) );
    configuration.setProperty(
      "hibernate.connection.username",
      configuredValue( "hibernate.connection.username",
                       "TOPRACING_DB_USERNAME",
                       DEFAULT_DB_USERNAME,
                       "local development database user",
                       log ) );
    configuration.setProperty(
      "hibernate.connection.password",
      configuredValue( "hibernate.connection.password",
                       "TOPRACING_DB_PASSWORD",
                       DEFAULT_DB_PASSWORD,
                       "local development database password",
                       log ) );
    configuration.setProperty(
      "hibernate.connection.pool_size",
      configuredValue( "hibernate.connection.pool_size",
                       "TOPRACING_DB_POOL_SIZE",
                       DEFAULT_DB_POOL_SIZE,
                       "local development database pool size",
                       log ) );
  }

  static String normalizeJdbcUrl( String url ) {
    String normalized = ( url == null || url.isBlank()
                          ? DEFAULT_DB_URL
                          : url.trim() );
    if( !isMysqlJdbcUrl( normalized ) ) {
      return normalized;
    }

    if( isLocalMysqlUrl( normalized ) ) {
      if( !hasQueryParameter( normalized,
                              "useSSL" ) ) {
        normalized = appendQueryParameter( normalized,
                                           "useSSL=false" );
      }
      if( !hasQueryParameter( normalized,
                              "allowPublicKeyRetrieval" ) ) {
        normalized = appendQueryParameter( normalized,
                                           "allowPublicKeyRetrieval=true" );
      }
    }
    if( !hasQueryParameter( normalized,
                            "serverTimezone" ) ) {
      normalized = appendQueryParameter( normalized,
                                         "serverTimezone=America/Mexico_City" );
    }
    if( !hasQueryParameter( normalized,
                            "sessionVariables" ) ) {
      normalized = appendQueryParameter(
        normalized,
        "sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'" );
    }
    return normalized;
  }

  static String resolveCatalog( String url ) {
    String configured = System.getProperty( "topracing.db.catalog" );
    if( configured == null || configured.isBlank() ) {
      configured = System.getenv( "TOPRACING_DB_CATALOG" );
    }
    if( configured != null && !configured.isBlank() ) {
      assertSafeCatalogName( configured );
      return configured;
    }

    String normalized = ( url == null || url.isBlank()
                          ? DEFAULT_DB_URL
                          : url.trim() );
    int questionMark = normalized.indexOf( '?' );
    String connectionPath = questionMark < 0
                            ? normalized
                            : normalized.substring( 0,
                                                    questionMark );
    int slash = connectionPath.lastIndexOf( '/' );
    if( slash < 0 || slash + 1 >= connectionPath.length() ) {
      return DEFAULT_DB_CATALOG;
    }

    String catalog = connectionPath.substring( slash + 1 )
      .trim();
    if( catalog.isBlank() ) {
      return DEFAULT_DB_CATALOG;
    }
    assertSafeCatalogName( catalog );
    return catalog;
  }

  static boolean isDefaultCatalog( String catalog ) {
    return DEFAULT_DB_CATALOG.equals( catalog );
  }

  private static String configuredValue( String propertyName,
                                         String envName,
                                         String fallbackValue,
                                         String fallbackDescription,
                                         Logger log ) {
    String value = System.getProperty( propertyName );
    if( value != null && !value.isBlank() ) {
      return value;
    }

    value = System.getenv( envName );
    if( value != null && !value.isBlank() ) {
      return value;
    }

    if( log != null ) {
      log.info( "{} was not configured; fallback={}; impact=local development default is used.",
                envName,
                fallbackDescription );
    }
    return fallbackValue;
  }

  private static void assertSafeCatalogName( String catalog ) {
    if( !catalog.matches( "[A-Za-z0-9_]+" ) ) {
      throw new HibernateException( "Unsafe database catalog name: " + catalog );
    }
  }

  private static boolean isMysqlJdbcUrl( String url ) {
    return url.toLowerCase( Locale.ROOT )
      .startsWith( "jdbc:mysql://" );
  }

  private static boolean isLocalMysqlUrl( String url ) {
    String lowerCaseUrl = url.toLowerCase( Locale.ROOT );
    return lowerCaseUrl.startsWith( "jdbc:mysql://localhost" )
           || lowerCaseUrl.startsWith( "jdbc:mysql://127.0.0.1" )
           || lowerCaseUrl.startsWith( "jdbc:mysql://[::1]" );
  }

  private static boolean hasQueryParameter( String url,
                                            String parameterName ) {
    int questionMark = url.indexOf( '?' );
    if( questionMark < 0 || questionMark + 1 >= url.length() ) {
      return false;
    }

    String query = url.substring( questionMark + 1 );
    int fragment = query.indexOf( '#' );
    if( fragment >= 0 ) {
      query = query.substring( 0,
                               fragment );
    }

    for( String pair : query.split( "&" ) ) {
      int equals = pair.indexOf( '=' );
      String name = equals < 0
                    ? pair
                    : pair.substring( 0,
                                      equals );
      if( parameterName.equalsIgnoreCase( name.trim() ) ) {
        return true;
      }
    }
    return false;
  }

  private static String appendQueryParameter( String url,
                                              String parameter ) {
    return url + ( url.contains( "?" )
                   ? "&"
                   : "?" )
           + parameter;
  }
}
