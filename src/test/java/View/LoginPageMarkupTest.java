/*
 * src/test/java/View/LoginPageMarkupTest.java
 * Guards login page markup required by unauthenticated password reset flows.
 */
package View;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LoginPageMarkupTest {

  @Test
  void passwordResetButtonIsNotRenderedDisabledBeforeTypingEmail()
    throws Exception {
    String loginMarkup = Files.readString( Path.of(
      "src/main/webapp/login.xhtml" ) );

    assertFalse( loginMarkup.contains( "disableResetButton()" ) );
    assertTrue( loginMarkup.contains( "update=\"dlgInfo\"" ) );
    assertTrue( loginMarkup.contains( "onstart=\"PF('dlgWait').show();\"" ) );
    assertFalse( loginMarkup.contains(
      "id=\"resetPasswordButton\"\r\n"
      + "                             actionListener=\"#{viewBean.clickResetPassword()}\"\r\n"
      + "                             value=\"Request a Password Reset\"\r\n"
      + "                             icon=\"fa fa-fw fa-key\"\r\n"
      + "                             ajax=\"true\"\r\n"
      + "                             process=\"@form\"\r\n"
      + "                             update=\"dlgInfo\"\r\n"
      + "                             onclick=\"PF('dlgWait').show();\"" ) );
  }

  @Test
  void infoDialogDoesNotCallUnsupportedPrimeFacesResetFunction()
    throws Exception {
    String templateMarkup = Files.readString( Path.of(
      "src/main/webapp/TemplateBasic.xhtml" ) );

    assertFalse( templateMarkup.contains( "PF('dlgInfo').reset()" ) );
  }

  @Test
  void webappMarkupDoesNotLinkToLegacyTopRacingDomain()
    throws Exception {
    try( Stream<Path> paths = Files.walk( Path.of( "src/main/webapp" ) ) ) {
      boolean hasLegacyDomain = paths
        .filter( Files::isRegularFile )
        .filter( path -> path.toString()
          .endsWith( ".xhtml" ) )
        .map( this::readMarkup )
        .anyMatch( markup -> markup.contains( "top-racing.org" ) );

      assertFalse( hasLegacyDomain );
    }
  }

  @Test
  void passwordResetFailureMessageExplainsOAuthRecovery()
    throws Exception {
    String englishBundle = Files.readString( Path.of(
      "src/main/java/View/BundleViewEnglish.properties" ) );
    String spanishBundle = Files.readString( Path.of(
      "src/main/java/View/BundleViewSpanish.properties" ) );

    assertTrue( englishBundle.contains( "scripts/configure-gmail-oauth.mjs" ) );
    assertTrue( englishBundle.contains( "scripts/restart-glassfish.ps1" ) );
    assertTrue( spanishBundle.contains( "scripts/configure-gmail-oauth.mjs" ) );
    assertTrue( spanishBundle.contains( "scripts/restart-glassfish.ps1" ) );
  }

  private String readMarkup( Path path ) {
    try {
      return Files.readString( path );
    } catch( Exception ex ) {
      throw new IllegalStateException( "Could not read " + path,
                                       ex );
    }
  }
}
