package Model;

import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class U_HibernateUtil {

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
    "Tables/Participant.hbm.xml",
  };

  private static final SessionFactory sessionFactory;
  private static ServiceRegistry serviceRegistry;
  private static final String DEFAULT_DB_URL =
    "jdbc:mysql://localhost:3306/topracing26?zeroDateTimeBehavior=convertToNull"
      + "&useSSL=false&allowPublicKeyRetrieval=true"
      + "&serverTimezone=America/Mexico_City";
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

      StandardServiceRegistryBuilder registryBuilder =
        new StandardServiceRegistryBuilder()
          .applySettings( configuration.getProperties() );
      serviceRegistry = registryBuilder.build();

      MetadataSources metadataSources = new MetadataSources( serviceRegistry );
      for( String mapping : MAPPING_RESOURCES ) {
        metadataSources.addResource( mapping );
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

