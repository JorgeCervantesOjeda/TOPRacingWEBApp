// src/main/java/Model/U_HibernateUtil.java
// Builds the Hibernate SessionFactory and applies local database overrides.
package Model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
  static {
    try {
      // Create the SessionFactory from standard (hibernate.cfg.xml) config file.
      Configuration configuration = new Configuration().configure();
      HibernateDatabaseConfiguration.applyRuntimeOverrides( configuration,
                                                            LOG );

      String url = configuration.getProperties()
        .getProperty( "hibernate.connection.url" );
      String catalog = HibernateDatabaseConfiguration.resolveCatalog( url );

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

  private static void addMappingResource( MetadataSources metadataSources,
                                          String mapping,
                                          String catalog ) {
    if( catalog == null
        || catalog.isBlank()
        || HibernateDatabaseConfiguration.isDefaultCatalog( catalog ) ) {
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

}

