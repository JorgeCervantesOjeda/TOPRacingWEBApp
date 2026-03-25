/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Car;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editCarBean" )
@RequestScoped
public class EditCarBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Car car;
  String description;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditCarBean() {
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
    this.car = viewBean.getCar();
    if( this.car == null ) {
      this.car = PlaceholderFactory.car();
    }
  }

  public Car getCar() {
    return this.car;
  }

  public void setCar( Car planetregion ) {
    this.car = planetregion;
  }

  public String getDescription() {
    return this.car.getDescription();
  }

  public void setDescription( String description ) {
    this.car.setDescription( description );
  }

  public boolean getDisableEditNicknameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || !Objects.equals(
        viewBean.getCurrentParticipant()
          .getId(),
        this.car.getParticipant()
          .getId() );
  }

  public boolean getDisableEditWeightField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || !Objects.equals(
        viewBean.getCurrentParticipant()
          .getId(),
        this.car.getParticipant()
          .getId() );
  }

  public boolean getDisableEditWidthField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || !Objects.equals(
        viewBean.getCurrentParticipant()
          .getId(),
        this.car.getParticipant()
          .getId() );
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || !Objects.equals(
        viewBean.getCurrentParticipant()
          .getId(),
        this.car.getParticipant()
          .getId() );
  }

  public void clickSave() {
    theController.clickSave( car );
  }

  public String getParticipantName() {
    return theModel.getParticipantFullName( car.getParticipant() );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_CAR );
  }

}

