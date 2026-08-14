// src/main/java/Model/U_HibernateUtil.java
// Builds the Hibernate SessionFactory and applies local database overrides.
package Model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class U_HibernateUtil {

  private static final Logger LOG = LoggerFactory.getLogger( U_HibernateUtil.class );
  private static final String[] MAPPING_RESOURCES = new String[] {
    "Tables/Countryregion.hbm.xml",
    "Tables/Province.hbm.xml",
    "Tables/Penaltiespl.hbm.xml",
    "Tables/Regatta.hbm.xml",
    "Tables/Appstats.hbm.xml",
    "Tables/Variant.hbm.xml",
    "Tables/Provinceregion.hbm.xml",
    "Tables/Bid.hbm.xml",
    "Tables/Pointscount.hbm.xml",
    "Tables/Currency.hbm.xml",
    "Tables/Country.hbm.xml",
    "Tables/Registration.hbm.xml",
    "Tables/Car.hbm.xml",
    "Tables/Planetregion.hbm.xml",
    "Tables/Venue.hbm.xml",
    "Tables/ParticipantAccessDecisionRecord.hbm.xml",
    "Tables/ParticipantGlobalExclusion.hbm.xml",
    "Tables/ParticipantLocalRestriction.hbm.xml",
    "Tables/Participant.hbm.xml",
  };

  private static final SessionFactory sessionFactory;
  private static ServiceRegistry serviceRegistry;
  private static final String DEFAULT_DB_URL =
    "jdbc:mysql://localhost:3306/topracing26?zeroDateTimeBehavior=convertToNull"
      + "&useSSL=false&allowPublicKeyRetrieval=true"
      + "&serverTimezone=America/Mexico_City";
  private static final String DEFAULT_DB_CATALOG = "topracing26";
  private static final String DEFAULT_DB_USERNAME = "admin";
  private static final String DEFAULT_DB_PASSWORD = "admin";

  static {
    try {
      // Create the SessionFactory from standard (hibernate.cfg.xml) config file.
      Configuration configuration = new Configuration().configure();
      applyEnvironmentOverrides( configuration );

      // Set session to non-strict SQL mode so long strings are truncated
      Properties props = configuration.getProperties();
      String url = normalizeJdbcUrl( props.getProperty( "hibernate.connection.url" ) );
      configuration.setProperty( "hibernate.connection.url",
                                 url );
      String catalog = resolveCatalog( url );

      StandardServiceRegistryBuilder registryBuilder =
        new StandardServiceRegistryBuilder()
          .applySettings( configuration.getProperties() );
      serviceRegistry = registryBuilder.build();

      MetadataSources metadataSources = new MetadataSources( serviceRegistry );
      for( String mapping : MAPPING_RESOURCES ) {
        addMappingResource( metadataSources,
                            mapping,
                            catalog );
      }
      Metadata metadata = metadataSources.getMetadataBuilder().build();

      sessionFactory = metadata.getSessionFactoryBuilder().build();
    } catch( HibernateException ex ) {
      // Log the exception.
      System.err.println( "Initial SessionFactory creation failed." + ex );
      throw new ExceptionInInitializerError( ex );
    }
  }

  public static SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  public static void shutdown() {
    if( sessionFactory != null && !sessionFactory.isClosed() ) {
      sessionFactory.close();
    }
    if( serviceRegistry != null ) {
      StandardServiceRegistryBuilder.destroy( serviceRegistry );
    }
  }

  private static void applyEnvironmentOverrides( Configuration configuration ) {
    overrideSetting( configuration,
                     "hibernate.connection.url",
                     "TOPRACING_DB_URL",
                     DEFAULT_DB_URL );
    overrideSetting( configuration,
                     "hibernate.connection.username",
                     "TOPRACING_DB_USERNAME",
                     DEFAULT_DB_USERNAME );
    overrideSetting( configuration,
                     "hibernate.connection.password",
                     "TOPRACING_DB_PASSWORD",
                     DEFAULT_DB_PASSWORD );
  }

  private static String resolveCatalog( String url ) {
    String configured = System.getProperty( "topracing.db.catalog" );
    if( configured == null || configured.isBlank() ) {
      configured = System.getenv( "TOPRACING_DB_CATALOG" );
    }
    if( configured != null && !configured.isBlank() ) {
      assertSafeCatalogName( configured );
      return configured;
    }

    String normalized = normalizeJdbcUrl( url );
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

  private static void addMappingResource( MetadataSources metadataSources,
                                          String mapping,
                                          String catalog ) {
    if( catalog == null
        || catalog.isBlank()
        || DEFAULT_DB_CATALOG.equals( catalog ) ) {
      metadataSources.addResource( mapping );
      return;
    }

    URL transformed = createCatalogAwareMapping( mapping,
                                                 catalog );
    metadataSources.addURL( transformed );
  }

  private static URL createCatalogAwareMapping( String mapping,
                                                String catalog ) {
    try( InputStream input = U_HibernateUtil.class.getClassLoader()
           .getResourceAsStream( mapping ) ) {
      if( input == null ) {
        String message = "Hibernate mapping resource was not found: " + mapping;
        LOG.error( message
                   + "; fallback=none; impact=SessionFactory startup is aborted." );
        throw new HibernateException( message );
      }

      String xml = new String( input.readAllBytes(),
                               StandardCharsets.UTF_8 );
      String transformed = xml.replaceAll( "catalog=\"[^\"]+\"",
                                           "catalog=\"" + catalog + "\"" );
      Path tempFile = Files.createTempFile(
        mapping.replace( '/',
                         '_' )
        .replace( '\\',
                  '_' )
        .replace( '.',
                  '_' ),
        ".hbm.xml" );
      Files.writeString( tempFile,
                         transformed,
                         StandardCharsets.UTF_8 );
      tempFile.toFile().deleteOnExit();
      return tempFile.toUri().toURL();
    } catch( IOException e ) {
      LOG.error( "Could not create catalog-aware Hibernate mapping for "
                 + mapping
                 + "; fallback=none; impact=SessionFactory startup is aborted.",
                 e );
      throw new HibernateException( "Could not create catalog-aware mapping: "
                                    + mapping,
                                    e );
    }
  }

  private static void assertSafeCatalogName( String catalog ) {
    if( !catalog.matches( "[A-Za-z0-9_]+" ) ) {
      throw new HibernateException( "Unsafe database catalog name: " + catalog );
    }
  }

  private static void overrideSetting( Configuration configuration,
                                       String propertyName,
                                       String envName,
                                       String fallbackValue ) {
    String value = System.getProperty( propertyName );
    if( value == null || value.isBlank() ) {
      value = System.getenv( envName );
    }
    if( value == null || value.isBlank() ) {
      value = fallbackValue;
    }
    configuration.setProperty( propertyName,
                               value );
  }

  private static String normalizeJdbcUrl( String url ) {
    String normalized = ( url == null || url.isBlank()
                          ? DEFAULT_DB_URL
                          : url );
    if( !normalized.contains( "useSSL=" ) ) {
      normalized += normalized.contains( "?" )
                    ? "&useSSL=false"
                    : "?useSSL=false";
    }
    if( !normalized.contains( "allowPublicKeyRetrieval=" ) ) {
      normalized += normalized.contains( "?" )
                    ? "&allowPublicKeyRetrieval=true"
                    : "?allowPublicKeyRetrieval=true";
    }
    if( !normalized.contains( "serverTimezone=" ) ) {
      normalized += normalized.contains( "?" )
                    ? "&serverTimezone=America/Mexico_City"
                    : "?serverTimezone=America/Mexico_City";
    }
    if( !normalized.contains( "sessionVariables=" ) ) {
      normalized += normalized.contains( "?" )
                    ? "&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'"
                    : "?sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'";
    }
    return normalized;
  }

}

