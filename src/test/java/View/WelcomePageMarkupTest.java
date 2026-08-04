/*
 * src/test/java/View/WelcomePageMarkupTest.java
 * Guards the public welcome page structure and contact affordance.
 */
package View;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class WelcomePageMarkupTest {

  @Test
  void welcomePagePresentsPublicActionsBeforeLongReferenceContent()
    throws Exception {
    String welcomeMarkup = Files.readString( Path.of(
      "src/main/webapp/welcome.xhtml" ) );

    assertTrue( welcomeMarkup.contains( "welcome-hero" ) );
    assertTrue( welcomeMarkup.contains( "welcome-actions" ) );
    assertTrue( welcomeMarkup.contains( "welcome-summary-grid" ) );
    assertTrue( welcomeMarkup.contains( "welcome-map-frame" ) );
    assertTrue( welcomeMarkup.contains( "welcome-faq" ) );
    assertFalse( welcomeMarkup.contains(
      "Adjetivos que describen TOP-Racing" ) );
    assertFalse( welcomeMarkup.contains( "height=\"100%\"" ) );
  }

  @Test
  void welcomeStylesGiveMapAndTextExplicitReadableContainers()
    throws Exception {
    String templateMarkup = Files.readString( Path.of(
      "src/main/webapp/TemplateBasic.xhtml" ) );
    String welcomeStyles = Files.readString( Path.of(
      "src/main/webapp/resources/css/welcome.css" ) );

    assertTrue( templateMarkup.contains( "css/welcome.css" ) );
    assertTrue( welcomeStyles.contains( ".welcome-map-frame" ) );
    assertTrue( welcomeStyles.contains( "min-height: 420px" ) );
    assertTrue( welcomeStyles.contains( ".welcome-copy-panel" ) );
    assertTrue( welcomeStyles.contains( ".welcome-cta" ) );
  }

  @Test
  void navContactUsesVisibleMailLinkInsteadOfAlert()
    throws Exception {
    String navMarkup = Files.readString( Path.of(
      "src/main/webapp/TemplateNav.xhtml" ) );

    assertTrue( navMarkup.contains( "mailto:top.racing.org@gmail.com" ) );
    assertFalse( navMarkup.contains( "alert('top.racing.org@gmail.com')" ) );
  }
}
