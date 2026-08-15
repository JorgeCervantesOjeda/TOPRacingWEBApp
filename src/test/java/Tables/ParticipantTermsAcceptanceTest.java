// src/test/java/Tables/ParticipantTermsAcceptanceTest.java
// Verifies participant acceptance of the current TOP Racing rules version.
package Tables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParticipantTermsAcceptanceTest {

  @Test
  void currentTermsAcceptanceIsSeparateFromOperationalConfirmation() {
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( true );

    participant.refreshOperationalConfirmation();

    assertTrue( participant.isConfirmed() );
    assertFalse( participant.hasAcceptedCurrentTerms() );

    participant.acceptCurrentTerms();

    assertTrue( participant.hasAcceptedCurrentTerms() );
  }

  @Test
  void staleAcceptedTermsDoNotSatisfyCurrentTerms() {
    Participant participant = new Participant();
    participant.setTermsVersionAccepted( "SRS-2.6-legacy" );
    participant.setTermsAcceptedAt( new java.util.Date() );

    assertFalse( participant.hasAcceptedCurrentTerms() );
  }

  @Test
  void acceptCurrentTermsRecordsVersionAndTimestamp() {
    Participant participant = new Participant();

    participant.acceptCurrentTerms();

    assertEquals( Participant.CURRENT_TERMS_VERSION,
                  participant.getTermsVersionAccepted() );
    assertNotNull( participant.getTermsAcceptedAt() );
    assertTrue( participant.hasAcceptedCurrentTerms() );
  }
}
