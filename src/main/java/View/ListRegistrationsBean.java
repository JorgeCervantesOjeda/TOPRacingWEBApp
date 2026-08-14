/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.RegistrationStatus;
import Controller.UI;
import Model.ModelForView;
import Tables.Participant;
import Tables.Regatta;
import Tables.Registration;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listRegistrationsBean" )
@ViewScoped
public class ListRegistrationsBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Registration> registrations = null;
  private List<Registration> filteredRegistration = null;

  private Regatta regatta = null;

  private Registration selectedRegistration;

  /**
   * Creates a new instance of RegattaBean
   */
  public ListRegistrationsBean() {
    regatta = null;
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
    registrations = theModel.getRegistrations();
  }
  //
  // queries
  //

  public void setRegistrations( List<Registration> registrations ) {
    this.registrations = registrations;
  }

  public List<Registration> getRegistrations() {
    return registrations;
  }

  public List<Registration> getFilteredRegistration() {
    return filteredRegistration;
  }

  public void setFilteredRegistration( List<Registration> filteredRegistration ) {
    this.filteredRegistration = filteredRegistration;
  }

  public Registration getSelectedRegistration() {
    return selectedRegistration;
  }

  public void setSelectedRegistration( Registration selectedRegistration ) {
    this.selectedRegistration = selectedRegistration;
  }

  public String getParticipantName( Participant participant ) {
    return theModel.getParticipantFullName( participant );
  }

  public String getCurrentParticipantName() {
    return theModel.getParticipantFullName( viewBean.getCurrentParticipant() );
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public void rowSelectRegistration() {
    theController.clickEditRegistration( this.selectedRegistration,
                                         UI.LIST_REGISTRATIONS );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_REGISTRATIONS );
  }

  public Long getNumRegistration() {
    return registrations == null
           ? 0L
           : (long) registrations.size();
  }

  public String getTimeZone() {
    return theModel.getTimeZone();
  }

  public String getStatusName( int status ) {
    return RegistrationStatus.NAME[ status ];
  }

  public String getListRegistrationTitle() {
    return viewBean.bundle(
      "REGISTERED REGISTRATIONS" )
           + " "
           + getNumRegistration();
  }

  public String getRegistrationAdviceTxt() {
    return viewBean.bundle(
      "REGISTRATIONS ADVICE" );
  }

  public String getIdTxt() {
    return viewBean.bundle(
      "ID" );
  }

  public String getStatusTxt() {
    return viewBean.bundle(
      "STATUS" );
  }

  public String getStatusNoteTxt() {
    return viewBean.bundle(
      "STATUS NOTE" );
  }

  public String getRegattaTxt() {
    return viewBean.bundle(
      "REGATTA" );
  }

  public String getVariantTxt() {
    return viewBean.bundle(
      "VARIANT" );
  }

  public String getVenueTxt() {
    return viewBean.bundle(
      "VENUE" );
  }

  public String getProvinceregionTxt() {
    return viewBean.bundle(
      "PROVINCE REGION" );
  }

  public String getDateTxt() {
    return viewBean.bundle(
      "DATE" );
  }

  public String getDriverTxt() {
    return viewBean.bundle(
      "DRIVER" );
  }

  public String getCarTxt() {
    return viewBean.bundle(
      "CAR" );
  }

  public String getCarOwnerTxt() {
    return viewBean.bundle(
      "CAR OWNER" );
  }

  public String getBasevalueTxt() {
    return viewBean.bundle(
      "BASE VALUE" );
  }

  public String getLaptimeTxt() {
    return viewBean.bundle(
      "LAP TIME" );
  }

  public String getSpeedPosTxt() {
    return viewBean.bundle(
      "SPEED POS" );
  }

  public String getGridPosTxt() {
    return viewBean.bundle(
      "GRID POS" );
  }

  public String getRaceLapsTxt() {
    return viewBean.bundle(
      "RACE LAPS" );
  }

  public String getRacePosTxt() {
    return viewBean.bundle(
      "RACE POS" );
  }

  public String getAuctionValueTxt() {
    return viewBean.bundle(
      "AUCTION VALUE" );
  }

  public String getSoldToTxt() {
    return viewBean.bundle(
      "SOLD TO" );
  }

  public String getEfficiencyPosTxt() {
    return viewBean.bundle(
      "EFFICIENCY POS" );
  }

  public String getFinishingBetTxt() {
    return viewBean.bundle(
      "FINISHING BET" );
  }

  public String getFinishingPrizeTxt() {
    return viewBean.bundle(
      "FINISHING PRIZE" );
  }

  public String getEfficiencyBetTxt() {
    return viewBean.bundle(
      "EFFICIENCY BET" );
  }

  public String getEfficiencyPrizeTxt() {
    return viewBean.bundle(
      "EFFICIENCY PRIZE" );
  }

  public String getCurrencySymbol( Registration r ) {
    if( r == null ) {
      return "?";
    }
    return r.getRegatta().getCurrency().getSymbol();
  }

}

