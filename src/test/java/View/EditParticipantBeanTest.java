package View;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Controller.Controller;
import Controller.UI;
import Tables.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditParticipantBeanTest {

  private EditParticipantBean bean;
  private ViewBean viewBean;
  private Controller controller;
  private Participant participant;

  @BeforeEach
  void setUp() {
    bean = new EditParticipantBean();
    viewBean = mock( ViewBean.class );
    controller = mock( Controller.class );
    participant = new Participant();
    participant.setId( 17L );
    participant.setEmail( "participant@example.com" );

    bean.setViewBean( viewBean );
    bean.setParticipant( participant );

    when( viewBean.getController() ).thenReturn( controller );
    when( viewBean.getCurrentParticipant() ).thenReturn( participant );
  }

  @Test
  void initUsesCurrentParticipantFromViewState() {
    bean.init();

    assertSame( participant,
                bean.getParticipant() );
  }

  @Test
  void clickSaveDelegatesCurrentParticipantToController() {
    bean.init();

    bean.clickSave();

    verify( controller ).clickSave( participant );
  }

  @Test
  void clickViewVenuesKeepsEditUserAsReturnUi() {
    bean.init();

    bean.clickViewVenues();

    verify( controller ).clickViewVenues( UI.EDIT_USER );
  }

  @Test
  void clickEndEditReturnsToEditUserExitPath() {
    bean.init();

    bean.clickEndEdit();

    verify( controller ).clickEndEdit( UI.EDIT_USER );
  }
}
