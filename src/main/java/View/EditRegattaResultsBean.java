/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.RegistrationStatus;
import Controller.UI;
import Model.ModelForView;
import Tables.Bid;
import Tables.BidId;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editRegattaResultsBean" )
@ViewScoped
public class EditRegattaResultsBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Bid> bids = null;
  private Regatta regatta;

  private Bid selectedBid;

  public Bid getSelectedBid() {
    return selectedBid;
  }

  public void setSelectedBid( Bid _selectedBid ) {
    this.selectedBid = _selectedBid;
  }

  /**
   * Creates a new instance of EditResultsRegattaBean
   */
  public EditRegattaResultsBean() {
  }

  public ViewBean getViewBean() {
    return viewBean;
  }

  public void setViewBean( ViewBean viewBean ) {
    this.viewBean = viewBean;
  }

  @PostConstruct
  public void init() {
    theModel = viewBean.getModelBean();
    theController = viewBean.getController();
    regatta = viewBean.getRegatta();
    if( regatta == null || viewBean.getCurrentParticipant() == null ) {
      regatta = PlaceholderFactory.regatta();
      bids = new ArrayList<>();
      return;
    }
    bids = theModel.getBids( viewBean.getCurrentParticipant(),
                             this.regatta );

    List<Registration> registrations;
    registrations = theModel.getRegattaRegistrations( regatta );

    for( Registration r
         : registrations ) {

      // add a bid of current user for each regtta registration
      if( !existsBid( viewBean.getCurrentParticipant().getId(),
                      r.getId() ) ) {
        bids.add( new Bid(
          new BidId(
            viewBean.getCurrentParticipant().getId(),
            r.getId() ),                // registration id
          viewBean.getCurrentParticipant(),    // bidder
          r,             // registration
          0.0f,          // ammount
          new Date(),
          0 // status
        ) );
      }
    }
    //this.clickSave(); // para guardar los bids del usuario actual
  }

  private boolean existsBid( long participantId,
                             long registrationId ) {
    for( Bid b
         : this.bids ) {
      if( b.getParticipant().getId()
          == participantId
          && b.getRegistration().getId()
             == registrationId ) {
        return true;
      }
    }
    return false;
  }

  public void setBids( List<Bid> bids ) {
    this.bids = bids;
  }

  public List<Bid> getBids() {
    return bids;
  }

  public Regatta getRegatta() {
    return this.regatta;
  }

  public String getRegattaStatusName() {
    return RegattaStatus.NAME[ this.regatta.getStatus() ];
  }

  public boolean getDisableSaveButton() {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() >= RegattaStatus.PUBLISHED;
  }

  public boolean getDisableEditStatus( Registration r ) {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() >= RegattaStatus.PUBLISHED;
  }

  public boolean getDisableEditStatusNote( Registration r ) {
    return getDisableEditStatus( r );
  }

  public boolean getDisableEditLaptime( Registration r ) {
    return r.getStatus() != RegistrationStatus.OK
           || this.regatta.getParticipant().getId().longValue()
              != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.SPEED_TEST;
  }

  public String getLaptimeStyle() {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.SPEED_TEST
           ? "white-space: pre-line; text-align: right; width: 50px;"
           : "background-color: burlywood; white-space: pre-line; text-align: right; width: 40px;";
  }

  public boolean disableEditRacelaps( Registration r ) {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.RACE_TEST
           || !RegistrationStatus.isComputable( r.getStatus() );
  }

  public String getRaceLapsStyle() {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.RACE_TEST
           ? "white-space: pre-line; text-align: right; width: 50px;"
           : "background-color: burlywood; white-space: pre-line; text-align: right; width: 40px;";
  }

  public boolean disableEditRacepos( Registration r ) {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.RACE_TEST
           || !RegistrationStatus.isComputable( r.getStatus() );
  }

  public String getRacePosStyle() {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.RACE_TEST
           ? "white-space: pre-line; text-align: right; width: 50px;"
           : "background-color: burlywood; white-space: pre-line; text-align: right; width: 40px;";
  }

  public boolean disableEditAuctionvalue( Registration r ) {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.AUCTION
           || !RegistrationStatus.isComputable( r.getStatus() );
  }

  public boolean disableEditSoldto( Registration r ) {
    return this.regatta.getParticipant().getId().longValue()
           != viewBean.getCurrentParticipant().getId().longValue()
           || this.regatta.getStatus() != RegattaStatus.AUCTION
           || !RegistrationStatus.isComputable( r.getStatus() );
  }

  public boolean disableEditBid( Registration r ) {
    return r.getRegatta().getStatus() != RegattaStatus.AUCTION
           || theModel.hasActiveLocalPromoterBlock(
             viewBean.getCurrentParticipant(),
             r.getRegatta().getParticipant() )
           || r.getStatus() != RegistrationStatus.OK;
  }

  public String getEditBidStyle() {
    return this.regatta.getStatus() != RegattaStatus.AUCTION
           ? ""
           : "background-color: burlywood;";
  }

  public String getRegistrationStatusName( int status ) {
    return RegistrationStatus.NAME[ status ];
  }

  public String getParticipantName( Participant user ) {
    return theModel.getParticipantFullName( user );
  }

  public Long getNumRegistrations() {
    return bids == null
           ? 0L
           : (long) bids.size();
  }

  public String getTimeZone() {
    return theModel.getTimeZone();
  }

  public void rowSelectBid() {
    theController.clickEditRegistration( selectedBid.getRegistration(),
                                         UI.VIEW_EDIT_REGATTA_RESULTS );
  }

  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////
  public void clickSave() {
    if( regatta.getStatus() < RegattaStatus.PUBLISHED ) {
      theController.clickSaveRegattaResults( bids );
    }
  }

  public void clickSaveBid( Bid bid ) {
    bid.setDate( new Date() );
    theController.clickSaveRegattaResults( bids );
  }

  public void clickReturn() {
    theController.returnFromRegattaResults( bids );
  }

  public void clickAddRegistration() {
    theController.clickAddRegistration( this.regatta );
  }

  public boolean disableAddRegistrationButton() {
    return this.regatta.getStatus()
           != RegattaStatus.REGISTRATIONS_OPEN
           || theModel.hasActiveLocalPromoterBlock(
             viewBean.getCurrentParticipant(),
             this.regatta.getParticipant() );
  }

  public double getExpectedLaps( Bid b ) {
    return theModel.getExpectedLaps( b.getRegistration() );
  }

  public void statusChanged( Registration r ) {
    theController.registrationStatusChanged( this.regatta );
  }

}

