// src/test/java/Model/ModelBeanAppUrlTest.java
// Verifies externally visible application URL normalization.
package Model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ModelBeanAppUrlTest {

  @Test
  void appURLDefaultsToLocalDevelopmentServer() {
    assertEquals( "http://localhost:8080/topracingwebapp/",
                  ModelBean.appURLFrom( null ) );
  }

  @Test
  void appURLAddsHttpSchemeAndTrailingSlashWhenNeeded() {
    assertEquals( "http://example.test/topracingwebapp/",
                  ModelBean.appURLFrom( "example.test/topracingwebapp" ) );
  }

  @Test
  void appURLKeepsConfiguredHttpsURL() {
    assertEquals( "https://racing.example.com/app/",
                  ModelBean.appURLFrom( "https://racing.example.com/app/" ) );
  }
}
