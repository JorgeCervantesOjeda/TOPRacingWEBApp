// src/test/java/Model/HibernateDatabaseConfigurationTest.java
// Verifies runtime database configuration normalization.
package Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HibernateDatabaseConfigurationTest {

  @Test
  void localMysqlUrlKeepsDevelopmentCompatibilityDefaults() {
    String normalized = HibernateDatabaseConfiguration.normalizeJdbcUrl(
      "jdbc:mysql://localhost:3306/topracing26?zeroDateTimeBehavior=convertToNull" );

    assertTrue( normalized.contains( "useSSL=false" ) );
    assertTrue( normalized.contains( "allowPublicKeyRetrieval=true" ) );
    assertTrue( normalized.contains( "serverTimezone=America/Mexico_City" ) );
    assertTrue( normalized.contains( "sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'" ) );
  }

  @Test
  void externalMysqlUrlWithTlsRequiredDoesNotAddLocalOnlySslDefaults() {
    String normalized = HibernateDatabaseConfiguration.normalizeJdbcUrl(
      "jdbc:mysql://mysql-service-for-top-racing-001-top-racing.c.aivencloud.com:10614/defaultdb?sslMode=REQUIRED" );

    assertTrue( normalized.contains( "sslMode=REQUIRED" ) );
    assertFalse( normalized.contains( "useSSL=false" ) );
    assertFalse( normalized.contains( "allowPublicKeyRetrieval=true" ) );
    assertTrue( normalized.contains( "serverTimezone=America/Mexico_City" ) );
  }

  @Test
  void catalogDefaultsToDatabaseNameInJdbcUrl() {
    assertEquals( "defaultdb",
                  HibernateDatabaseConfiguration.resolveCatalog(
                    "jdbc:mysql://host.example:10614/defaultdb?sslMode=REQUIRED" ) );
  }
}
