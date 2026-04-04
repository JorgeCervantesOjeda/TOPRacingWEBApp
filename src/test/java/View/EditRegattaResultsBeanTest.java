package View;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.UI;
import Model.ModelForView;
import Tables.Bid;
import Tables.BidId;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EditRegattaResultsBeanTest {

  private EditRegattaResultsBean bean;
  private ViewBean viewBean;
  private ModelForView model;
  private Controller controller;
  private Participant currentParticipant;
  private Regatta regatta;

  @BeforeEach
  void setUp() {
    bean = new EditRegattaResultsBean();
    viewBean = mock( ViewBean.class );
    model = mock( ModelForView.class );
    controller = mock( Controller.class );

    currentParticipant = new Participant();
    currentParticipant.setId( 7L );
    currentParticipant.setDefaulter( 0 );

    Participant promoter = new Participant();
    promoter.setId( 7L );

    regatta = new Regatta();
    regatta.setId( 101L );
    regatta.setParticipant( promoter );
    regatta.setStatus( (byte) RegattaStatus.REGISTRATIONS_OPEN );

    bean.setViewBean( viewBean );
    when( viewBean.getModelBean() ).thenReturn( model );
    when( viewBean.getController() ).thenReturn( controller );
    when( viewBean.getCurrentParticipant() ).thenReturn( currentParticipant );
    when( viewBean.getRegatta() ).thenReturn( regatta );
  }

  @Test
  void initAddsPlaceholderBidForEachRegistrationWithoutExistingBid() {
    Registration first = registration( 201L );
    Registration second = registration( 202L );
    Bid existingBid = bid( currentParticipant,
                           first,
                           33.0 );

    when( model.getBids( currentParticipant,
                         regatta ) ).thenReturn( new ArrayList<>( List.of(
                           existingBid ) ) );
    when( model.getRegattaRegistrations( regatta ) ).thenReturn( List.of( first,
                                                                          second ) );

    bean.init();

    assertEquals( 2,
                  bean.getBids().size() );
    long zeroBids = bean.getBids()
      .stream()
      .filter( bid -> bid.getRegistration().getId().equals( second.getId() ) )
      .filter( bid -> bid.getAmmount() == 0.0 )
      .count();
    assertEquals( 1L,
                  zeroBids );
  }

  @Test
  void rowSelectBidReturnsToRegistrationEditorThroughRegattaResultsUi() {
    Registration registration = registration( 301L );
    when( model.getBids( currentParticipant,
                         regatta ) ).thenReturn( new ArrayList<>() );
    when( model.getRegattaRegistrations( regatta ) ).thenReturn( List.of(
      registration ) );
    bean.init();
    bean.setSelectedBid( bid( currentParticipant,
                              registration,
                              44.0 ) );

    bean.rowSelectBid();

    verify( controller ).clickEditRegistration( registration,
                                                UI.VIEW_EDIT_REGATTA_RESULTS );
  }

  @Test
  void clickSavePersistsResultsOnlyBeforePublished() {
    when( model.getBids( currentParticipant,
                         regatta ) ).thenReturn( new ArrayList<>() );
    when( model.getRegattaRegistrations( regatta ) ).thenReturn( List.of() );
    bean.init();

    bean.clickSave();

    verify( controller ).clickSaveRegattaResults( bean.getBids() );

    regatta.setStatus( (byte) RegattaStatus.PUBLISHED );
    bean.clickSave();

    verify( controller,
            times( 1 ) ).clickSaveRegattaResults( bean.getBids() );
  }

  @Test
  void addRegistrationButtonDependsOnStatusAndDefaulterFlag() {
    when( model.getBids( currentParticipant,
                         regatta ) ).thenReturn( new ArrayList<>() );
    when( model.getRegattaRegistrations( regatta ) ).thenReturn( List.of() );
    bean.init();

    assertFalse( bean.disableAddRegistrationButton() );

    regatta.setStatus( (byte) RegattaStatus.SPEED_TEST );
    assertTrue( bean.disableAddRegistrationButton() );

    regatta.setStatus( (byte) RegattaStatus.REGISTRATIONS_OPEN );
    currentParticipant.setDefaulter( 1 );
    assertTrue( bean.disableAddRegistrationButton() );
  }

  private static Registration registration( Long id ) {
    Registration registration = new Registration();
    registration.setId( id );
    registration.setRegatta( new Regatta() );
    registration.getRegatta().setStatus( (byte) RegattaStatus.REGISTRATIONS_OPEN );
    return registration;
  }

  private static Bid bid( Participant bidder,
                          Registration registration,
                          double amount ) {
    return new Bid( new BidId( bidder.getId(),
                               registration.getId() ),
                    bidder,
                    registration,
                    amount,
                    new java.util.Date(),
                    0 );
  }
}
