// src/test/java/View/EditParticipantPageMarkupTest.java
// Verifies profile markup exposes current rules consultation and acceptance.
package View;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EditParticipantPageMarkupTest {

  @Test
  void profileShowsCurrentRulesModalAndAcceptanceCheckbox() throws Exception {
    String markup = Files.readString( Path.of(
      "src/main/webapp/editparticipant.xhtml" ) );

    assertTrue( markup.contains( "currentRulesDialog" ) );
    assertTrue( markup.contains( "acceptCurrentTerms" ) );
    assertTrue( markup.contains( "currentTermsText" ) );
    assertTrue( markup.contains( "PF('currentRulesDialog').show()" ) );
  }
}
