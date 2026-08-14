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
import Tables.Participant;
import Tables.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editRegistrationBean" )
@RequestScoped
public class EditRegistrationBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Registration registration;

  /**
   * Creates a new instance of RegistrationBean
   */
  public EditRegistrationBean() {
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
    this.registration = viewBean.getCurrentRegistration();
    if( this.registration == null ) {
      this.registration = PlaceholderFactory.registration();
    }
  }

  public Registration getRegistration() {
    return this.registration;
  }

  public void setRegistration( Registration registration ) {
    this.registration = registration;
  }

  public void updateCarOwner() {
    registration.setParticipantByIdOwner(
      registration
        .getCar()
        .getParticipant() );
  }

  public void clickViewCars() {
    theController.clickViewCars();
  }

  public void clickViewDrivers() {
    theController.clickViewDrivers();
  }

  /*
   * public void clickAddToRegistrationPayment() {
   * theController.clickAddToRegistrationPayment( this.registration ); }
   */
  public void clickSave() {
    updateCarOwner();
    theController.clickSave( this.registration );
  }

  public String getStatusName() {
    return RegistrationStatus.NAME[ this.registration.getStatus() ];
  }

  public String getParticipantName( Participant participant ) {
    return theModel.getParticipantFullName( participant );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_REGISTRATION );
  }

  public double getRegattaRegistrationCost() {
    if( this.registration == null || this.registration.getRegatta() == null ) {
      return 0.0;
    }
    if( this.registration.getRegatta().getId() == null
        || this.registration.getRegatta().getId() <= 0 ) {
      return 0.0;
    }
    return theModel.getRegattaById( this.registration.getRegatta().getId() )
      .getEntryfee();
  }

  public boolean getDisableEditCarId() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId()
           || this.registration.getStatus()
              != RegistrationStatus.INCOMPLETE;
  }

  public boolean getDisableAddToPaymentButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId()
           || this.registration.getStatus()
              != RegistrationStatus.INCOMPLETE
           || true;
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId()
           || this.registration.getRegatta().getStatus()
              != RegattaStatus.REGISTRATIONS_OPEN;
  }

  public boolean getDisableViewCarsButton() {
    return false;
//    return this.registration.getParticipantByIdOwner().getId().longValue()
//           != viewBean.getCurrentParticipant().getId()
//           || this.registration.getRegatta().getStatus()
//              >= RegattaStatus.SPEED_TEST;
  }

  public boolean getDisableViewDriversButton() {
    return false;
//    return this.registration.getParticipantByIdOwner().getId().longValue()
//           != viewBean.getCurrentParticipant().getId()
//           || this.registration.getRegatta().getStatus()
//              >= RegattaStatus.SPEED_TEST;
  }

  public boolean getDisableEditDriverId() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId().longValue()
           || this.registration.getRegatta().getStatus()
              > RegattaStatus.REGISTRATIONS_OPEN;
  }

  public boolean getDisableEditBetFinishing() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId()
           || this.registration.getRegatta().getStatus()
              > RegattaStatus.REGISTRATIONS_OPEN;
  }

  public boolean getDisableEditBetEfficiency() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.registration.getParticipantByIdOwner().getId().longValue()
              != viewBean.getCurrentParticipant().getId()
           || this.registration.getRegatta().getStatus()
              > RegattaStatus.REGISTRATIONS_OPEN;
  }

  public String getEditRegistrationTitle() {
    return viewBean.bundle(
      "EDIT REGISTRATION TITLE" );
  }

  public String getRegattaIdLabel() {
    return viewBean.bundle(
      "REGATTA ID" );
  }

  public String getStatusLabel() {
    return viewBean.bundle(
      "STATUS" );
  }

  public String getStatusNoteLabel() {
    return viewBean.bundle(
      "STATUS NOTE" );
  }

  public String getDriverLabel() {
    return viewBean.bundle(
      "DRIVER" );
  }

  public String getCarIdLabel() {
    return viewBean.bundle(
      "CAR ID" );
  }

  public String getViewCarsButton() {
    return viewBean.bundle(
      "VIEW CARS" );
  }

  public String getViewDriversButton() {
    return viewBean.bundle(
      "VIEW DRIVERS" );
  }

  public String getCarOwnerLabel() {
    return viewBean.bundle(
      "CAR OWNER" );
  }

  public String getRegistrationCostLabel() {
    return viewBean.bundle(
      "REGISTRATION COST" );
  }

  public String getPaymentLabel() {
    return viewBean.bundle(
      "PAYMENT" );
  }

  public String getBetFinishingLabel() {
    return viewBean.bundle(
      "FINISHING BET" );
  }

  public String getBetEfficiencyLabel() {
    return viewBean.bundle(
      "EFFICIENCY BET" );
  }

  public String getCurrencySymbol() {
    if( this.registration == null ) {
      return "";
    }
    if( this.registration.getRegatta() == null ) {
      return "";
    }
    if( this.registration.getRegatta().getCurrency() == null ) {
      return "";
    }
    if( this.registration.getRegatta().getCurrency().getSymbol() == null ) {
      return "";
    }
    return this.registration.getRegatta().getCurrency().getSymbol();
  }

}

