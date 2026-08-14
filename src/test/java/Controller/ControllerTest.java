package Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.ModelBean;
import Tables.Bid;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import View.ViewForController;
import java.util.List;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ControllerTest {

  private ViewForController view;
  private ModelBean modelBean;
  private Controller controller;

  @BeforeEach
  void setUp() throws Exception {
    view = mock( ViewForController.class );
    modelBean = mock( ModelBean.class );
    controller = new Controller( view );

    setPrivateField( controller,
                     "modelBean",
                     modelBean );
    setPrivateField( controller,
                     "session",
                     42L );
  }

  @Test
  void clickLoginShowsErrorWhenCredentialsAreInvalid() {
    Participant attempt = participant( 10L,
                                       false );
    when( modelBean.getValidParticipant( attempt ) ).thenReturn( null );

    controller.clickLogin( attempt );

    verify( view ).showUI( UI.ERROR_LOGIN );
    verify( modelBean,
            never() ).sendEmail( any(),
                                 any(),
                                 anyLong() );
  }

  @Test
  void clickLoginSendsConfirmationForUsersWithoutConfirmedEmail() {
    Participant attempt = participant( 10L,
                                       false );
    attempt.setEmailConfirmed( false );
    attempt.setPaypalUsable( false );
    when( modelBean.getValidParticipant( attempt ) ).thenReturn( attempt );
    when( view.bundle( "PLEASE FOLLOW THIS LINK TO CONFIRM YOUR E-MAIL ADDRESS THANK YOU" ) )
      .thenReturn( "confirm-message" );

    controller.clickLogin( attempt );

    verify( modelBean ).sendConfirmationRequest( attempt,
                                                 "confirm-message",
                                                 42L );
    verify( modelBean,
            never() ).sendEmail( any(),
                                 any(),
                                 anyLong() );
    verify( modelBean,
            never() ).incNumUsuariosActivos();
    verify( view ).showUI( UI.ERROR_EMAIL_CONFIRMATION_REQUIRED,
                           attempt );
  }

  @Test
  void clickLoginRequiresUsablePaypalAfterEmailConfirmation() {
    Participant attempt = participant( 10L,
                                       false );
    attempt.setEmailConfirmed( true );
    attempt.setPaypalUsable( false );
    when( modelBean.getValidParticipant( attempt ) ).thenReturn( attempt );

    controller.clickLogin( attempt );

    verify( modelBean,
            never() ).sendConfirmationRequest( any(),
                                               any(),
                                               anyLong() );
    verify( modelBean,
            never() ).sendEmail( any(),
                                 any(),
                                 anyLong() );
    verify( modelBean,
            never() ).incNumUsuariosActivos();
    verify( view ).showUI( UI.ERROR_PAYPAL_REQUIRED,
                           attempt );
  }

  @Test
  void clickLoginCreatesSessionOnlyForOperationallyConfirmedUsers() {
    Participant attempt = participant( 10L,
                                       true );
    attempt.setEmailConfirmed( true );
    attempt.setPaypalUsable( true );
    when( modelBean.getValidParticipant( attempt ) ).thenReturn( attempt );
    when( modelBean.incNumUsuariosActivos() ).thenReturn( 1L );
    FacesContext facesContext = mock( FacesContext.class );
    ExternalContext externalContext = mock( ExternalContext.class );
    Map<String, Object> sessionMap = new HashMap<>();
    when( facesContext.getExternalContext() ).thenReturn( externalContext );
    when( externalContext.getSessionMap() ).thenReturn( sessionMap );

    try( MockedStatic<FacesContext> facesContextMock = mockStatic( FacesContext.class ) ) {
      facesContextMock.when( FacesContext::getCurrentInstance )
        .thenReturn( facesContext );

      controller.clickLogin( attempt );
    }

    verify( modelBean ).sendEmail( attempt,
                                   "You have logged in to TOP Racing.",
                                   42L );
    verify( view ).showUI( UI.WELCOME,
                           attempt );
  }

  @Test
  void clickNewParticipantShowsEditUserWhenPlaceholderWasCreated() {
    Participant created = participant( 11L,
                                       false );
    when( modelBean.createParticipant() ).thenReturn( created );

    controller.clickNewParticipant();

    verify( view ).showUI( UI.EDIT_USER,
                           created );
  }

  @Test
  void clickNewParticipantShowsErrorWhenPlaceholderCreationFails() {
    when( modelBean.createParticipant() ).thenReturn( null );

    controller.clickNewParticipant();

    verify( view ).showUI( UI.ERROR_CREATE_REGISTRATION_DUPLICATE );
  }

  @Test
  void clickResetPasswordRequestShowsSuccessWhenMailIsAccepted() {
    Participant attempt = participant( 10L,
                                       false );
    when( modelBean.resetPasswordRequest( attempt,
                                          42L ) ).thenReturn( true );

    controller.clickResetPasswordRequest( attempt );

    verify( view ).showUI( UI.PASSWORD_RESET_REQUEST,
                           attempt );
  }

  @Test
  void clickResetPasswordRequestShowsErrorWhenMailFails() {
    Participant attempt = participant( 10L,
                                       false );
    when( modelBean.resetPasswordRequest( attempt,
                                          42L ) ).thenReturn( false );

    controller.clickResetPasswordRequest( attempt );

    verify( view ).showUI( UI.ERROR_PASSWORD_RESET_REQUEST,
                           attempt );
  }

  @Test
  void clickLogoutInvalidatesSessionAndRedirectsToWelcome() throws Exception {
    Participant current = participant( 12L,
                                       true );
    setPrivateField( controller,
                     "currentParticipant",
                     current );
    when( modelBean.decNumUsuariosActivos() ).thenReturn( 0L );
    FacesContext facesContext = mock( FacesContext.class );
    ExternalContext externalContext = mock( ExternalContext.class );
    Map<String, Object> sessionMap = new HashMap<>();
    when( facesContext.getExternalContext() ).thenReturn( externalContext );
    when( externalContext.getSessionMap() ).thenReturn( sessionMap );

    try( MockedStatic<FacesContext> facesContextMock = mockStatic( FacesContext.class ) ) {
      facesContextMock.when( FacesContext::getCurrentInstance )
        .thenReturn( facesContext );

      controller.clickLogout();
    }

    verify( view ).invalidateSessionAndShowUI( UI.WELCOME );
  }

  @Test
  void promoterBalanceComplaintMarksPromoterAndRequestsRecalculation() {
    Registration registration = registration( 55L,
                                              21L,
                                              31L );

    controller.promoterBalanceComplaint( registration );

    verify( modelBean ).setParticipantAsLocalDefaulter(
      eq( registration.getRegatta().getParticipant() ),
      eq( registration.getRegatta().getParticipant() ),
      any(),
      eq( "Promoter balance default reported for registration 55" ) );
    verify( modelBean ).requestRecalculateRegattaPenalties( any() );
    verify( modelBean ).sendMonitorMail( eq( registration.getRegatta().getParticipant() ),
                                         eq( "A PROMOTER DEFAULTER HAS BEEN REPORTED"
                                             + "\nRegatta Id: 31"
                                             + "\nOwner Id: 21"
                                             + "\nPromoter Id: 31"
                                             + "\nRegistration Id: 55" ),
                                         eq( 42L ) );
  }

  @Test
  void clickSetRegattaStatusToNextStatusRejectsNonOwner() throws Exception {
    Participant current = participant( 5L,
                                       true );
    setPrivateField( controller,
                     "currentParticipant",
                     current );

    Regatta detached = new Regatta();
    detached.setId( 99L );

    Regatta persisted = new Regatta();
    persisted.setId( 99L );
    persisted.setParticipant( participant( 7L,
                                          true ) );
    persisted.setStatus( (byte) RegattaStatus.CREATED );

    when( modelBean.getValidParticipant( current ) ).thenReturn( current );
    when( modelBean.getRegattaById( 99L ) ).thenReturn( persisted );

    controller.clickSetRegattaStatusToNextStatus( detached );

    verify( view ).showUI( UI.ERROR_REGATTA_NOT_OWNED );
    verify( modelBean,
            never() ).save( any( Regatta.class ) );
  }

  @Test
  void clickAddRegistrationRejectsLocalPromoterBlock() throws Exception {
    Participant current = participant( 5L,
                                       true );
    Participant promoter = participant( 7L,
                                        true );
    Regatta regatta = new Regatta();
    regatta.setId( 99L );
    regatta.setParticipant( promoter );
    regatta.setStatus( RegattaStatus.REGISTRATIONS_OPEN );
    setPrivateField( controller,
                     "currentParticipant",
                     current );

    when( modelBean.getValidParticipant( current ) ).thenReturn( current );
    when( modelBean.hasActiveLocalPromoterBlock( current,
                                                 promoter ) ).thenReturn( true );

    controller.clickAddRegistration( regatta );

    verify( view ).showUI( UI.ERROR_LOCAL_PROMOTER_BLOCKED );
    verify( modelBean,
            never() ).createRegistration( regatta,
                                          current );
  }

  @Test
  void clickSaveRegattaResultsDoesNotPersistBidAmountWhenLocalPromoterBlocked()
    throws Exception {
    Participant current = participant( 5L,
                                       true );
    Participant promoter = participant( 7L,
                                        true );
    Regatta regatta = new Regatta();
    regatta.setId( 99L );
    regatta.setParticipant( promoter );
    regatta.setStatus( RegattaStatus.AUCTION );
    Registration registration = new Registration();
    registration.setId( 55L );
    registration.setRegatta( regatta );
    registration.setStatus( RegistrationStatus.OK );
    Bid bid = new Bid();
    bid.setAmmount( 50.0 );
    bid.setRegistration( registration );
    setPrivateField( controller,
                     "currentParticipant",
                     current );

    when( modelBean.getValidParticipant( current ) ).thenReturn( current );
    when( modelBean.getRegistrationById( 55L ) ).thenReturn( registration );
    when( modelBean.hasActiveLocalPromoterBlock( current,
                                                 promoter ) ).thenReturn( true );

    controller.clickSaveRegattaResults( List.of( bid ) );

    ArgumentCaptor<List> captor = ArgumentCaptor.forClass( List.class );
    verify( modelBean ).save( captor.capture() );
    Bid sanitizedBid = (Bid) captor.getValue()
      .get( 0 );
    org.junit.jupiter.api.Assertions.assertEquals( 0.0,
                                                  sanitizedBid.getAmmount() );
  }

  private static Participant participant( Long id,
                                          boolean confirmed ) {
    Participant participant = new Participant();
    participant.setId( id );
    participant.setConfirmed( confirmed );
    participant.setEmail( "user" + id + "@example.com" );
    participant.setPassword( "pw" );
    return participant;
  }

  private static Registration registration( Long registrationId,
                                            Long ownerId,
                                            Long promoterId ) {
    Participant owner = participant( ownerId,
                                     true );
    Participant promoter = participant( promoterId,
                                        true );

    Regatta regatta = new Regatta();
    regatta.setId( promoterId );
    regatta.setParticipant( promoter );

    Registration registration = new Registration();
    registration.setId( registrationId );
    registration.setParticipantByIdOwner( owner );
    registration.setRegatta( regatta );
    return registration;
  }

  private static void setPrivateField( Object target,
                                       String fieldName,
                                       Object value ) throws Exception {
    Field field = Controller.class.getDeclaredField( fieldName );
    field.setAccessible( true );
    field.set( target,
               value );
  }
}
