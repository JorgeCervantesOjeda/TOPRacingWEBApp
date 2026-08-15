// src/test/java/Tables/ParticipantOperationalStatusTest.java
// Verifies account confirmation semantics for e-mail, PayPal, and rules readiness.
package Tables;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ParticipantOperationalStatusTest {

  @Test
  void emailConfirmationAloneDoesNotConfirmOperationalAccount() {
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( false );

    participant.refreshOperationalConfirmation();

    assertFalse( participant.isConfirmed() );
  }

  @Test
  void emailConfirmationUsablePaypalAndCurrentTermsConfirmOperationalAccount() {
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( true );
    participant.acceptCurrentTerms();

    participant.refreshOperationalConfirmation();

    assertTrue( participant.isConfirmed() );
  }

  @Test
  void missingEmailConfirmationPreventsOperationalAccountEvenWithPaypal() {
    Participant participant = new Participant();
    participant.setEmailConfirmed( false );
    participant.setPaypalUsable( true );

    participant.refreshOperationalConfirmation();

    assertFalse( participant.isConfirmed() );
  }
}
