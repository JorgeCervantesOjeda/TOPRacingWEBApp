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
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;

/**
 *
 * @author usuario
 */
@Named( value = "listDriversBean" )
@ViewScoped
public class ListDriversBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Participant> drivers;
  private List<Participant> filteredDriver;

  private Participant selectedDriver;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListDriversBean() {
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
    drivers = theModel.getParticipants();
  }
  //
  // queries
  //

  public void setDrivers( List<Participant> drivers ) {
    this.drivers = drivers;
  }

  public List<Participant> getDrivers() {
    return this.drivers;
  }

  public List<Participant> getFilteredDriver() {
    return this.filteredDriver;
  }

  public void setFilteredDriver( List<Participant> filteredDriver ) {
    this.filteredDriver = filteredDriver;
  }

  public Participant getSelectedDriver() {
    return selectedDriver;
  }

  public void setSelectedDriver( Participant selectedDriver ) {
    this.selectedDriver = selectedDriver;
  }

  public void rowSelectDriver() {

  }

  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////
  public long getNumDriver() {
    return drivers.size();
  }

  public void clickSelectDriver( Participant driver ) {
    theController.clickSelectDriver( driver,
                                     viewBean.getCurrentRegistration() );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_DRIVERS );
  }

  public boolean disableSelectDriver( Participant driver ) {
    Participant promoter = viewBean.getCurrentRegistration()
      .getRegatta()
      .getParticipant();
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || viewBean.getCurrentRegistration().getStatus() == RegistrationStatus.DISQUALIFIED
           || viewBean.getCurrentRegistration().getStatus() == RegistrationStatus.INVALID
           || viewBean.getCurrentRegistration().getRegatta().getStatus() > RegattaStatus.REGISTRATIONS_OPEN
           || theModel.hasActiveLocalPromoterBlock( driver,
                                                    promoter )
           || !driver.isConfirmed()
           || theModel.hasActiveLocalPromoterBlock(
             viewBean.getCurrentParticipant(),
             promoter );
  }

  public String getVenueName( Participant driver ) {
    return driver.getVenue().getName();
  }

  public String getDefaulterTxt() {
    return viewBean.bundle(
      "DEFAULTER" );
  }

  public String getConfirmedTxt() {
    return viewBean.bundle(
      "CONFIRMED" );
  }

}

