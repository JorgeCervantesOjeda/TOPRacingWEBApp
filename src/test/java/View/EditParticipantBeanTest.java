// src/test/java/View/EditParticipantBeanTest.java
// Verifies profile behavior for current rules consultation and acceptance.
package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import Controller.Controller;
import Tables.Participant;
import org.junit.jupiter.api.Test;

class EditParticipantBeanTest {

  @Test
  void termsAcceptanceSelectionRecordsCurrentVersionBeforeSave() {
    ViewBean viewBean = mock( ViewBean.class );
    Controller controller = mock( Controller.class );
    Participant participant = new Participant();
    EditParticipantBean bean = new EditParticipantBean();
    bean.setViewBean( viewBean );
    when( viewBean.getController() ).thenReturn( controller );
    when( viewBean.getCurrentParticipant() ).thenReturn( participant );

    bean.init();
    bean.setAcceptCurrentTerms( true );
    bean.clickSave();

    assertEquals( Participant.CURRENT_TERMS_VERSION,
                  participant.getTermsVersionAccepted() );
    assertNotNull( participant.getTermsAcceptedAt() );
  }

  @Test
  void currentTermsTextIncludesEconomicConsequences() {
    EditParticipantBean bean = new EditParticipantBean();

    assertTrue( bean.getCurrentTermsText()
      .contains( "no automatic refund" ) );
  }
}
