/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.UI;
import Model.ModelForView;
import Tables.Car;
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
@Named( value = "listCarsBean" )
@ViewScoped
public class ListCarsBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Car> cars;
  private List<Car> filteredCar;

  private Car selectedCar;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListCarsBean() {
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
    cars = theModel.getCars( viewBean.getCurrentParticipant() );
  }
  //
  // queries
  //

  public void setCars( List<Car> cars ) {
    this.cars = cars;
  }

  public List<Car> getCars() {
    return cars;
  }

  public List<Car> getFilteredCar() {
    return filteredCar;
  }

  public void setFilteredCar( List<Car> filteredCar ) {
    this.filteredCar = filteredCar;
  }

  public Car getSelectedCar() {
    return selectedCar;
  }

  public void setSelectedCar( Car selectedCar ) {
    this.selectedCar = selectedCar;
  }

  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////
  public long getNumCar() {
    return cars.size();
  }

  public void clickNewCar() {
    theController.clickNewCar();
  }

  public void rowSelectCar( Car car ) {
    theController.clickEditCar( car );
  }

  public void clickSelectCar( Car car ) {
    theController.clickSelectCar( car,
                                  viewBean.getCurrentRegistration() );
  }

  public boolean disableSelectCar( Car car ) {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || viewBean.getCurrentParticipant().getId().longValue()
              != car.getParticipant().getId().longValue()
           || viewBean.getCurrentRegistration().getRegatta().getStatus() > RegattaStatus.REGISTRATIONS_OPEN;
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_CARS );
  }

  public void rowSelectCar() {
    theController.clickEditCar( selectedCar );
  }

  public String getOwnerName( Car car ) {
    return theModel.getParticipantFullName( car.getParticipant() );
  }

}

