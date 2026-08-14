package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import Tables.Participant;
import org.junit.jupiter.api.Test;

class ViewBeanTest {

  @Test
  void currentParticipantFallsBackToPlaceholderWhenCleared() {
    ViewBean viewBean = new ViewBean();

    viewBean.setCurrentParticipant( null );

    Participant participant = viewBean.getCurrentParticipant();
    assertNotNull( participant );
    assertEquals( 0L,
                  participant.getId() );
    assertEquals( "",
                  participant.getEmail() );
    assertEquals( "",
                  participant.getPassword() );
  }

  @Test
  void setPasswordRecreatesPlaceholderAfterLogoutState() {
    ViewBean viewBean = new ViewBean();

    viewBean.setCurrentParticipant( null );
    viewBean.setPassword( "Pw-12345" );

    Participant participant = viewBean.getCurrentParticipant();
    assertNotNull( participant );
    assertEquals( "Pw-12345",
                  participant.getPassword() );
    assertEquals( "",
                  participant.getEmail() );
  }

  @Test
  void paypalConnectButtonIsVisibleOnlyWhenEmailIsConfirmedAndPaypalIsMissing() {
    ViewBean viewBean = new ViewBean();
    Participant participant = new Participant();
    participant.setId( 12L );
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( false );

    viewBean.setCurrentParticipant( participant );

    assertEquals( true,
                  viewBean.renderPaypalConnectButton() );

    participant.setPaypalUsable( true );

    assertEquals( false,
                  viewBean.renderPaypalConnectButton() );
  }
}
