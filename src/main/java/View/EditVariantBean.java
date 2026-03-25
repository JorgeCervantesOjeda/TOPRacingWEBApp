/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Variant;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editVariantBean" )
@RequestScoped
public class EditVariantBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Variant variant;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditVariantBean() {
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
    this.variant = viewBean.getVariant();

    // parche: mejor quitar campo?
    variant.setMetric( true );
  }

  public Variant getVariant() {
    return this.variant;
  }

  public void setVariant( Variant variant ) {
    this.variant = variant;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.variant.getIdCreator() != viewBean.getCurrentParticipant()
      .getId();
  }

  public boolean getDisableEditVenueField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.variant.getIdCreator() != viewBean.getCurrentParticipant()
      .getId();
  }

  public boolean getDisableEditMinwidthField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.variant.getIdCreator() != viewBean.getCurrentParticipant()
      .getId();
  }

  public boolean getDisableEditLengthField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.variant.getIdCreator() != viewBean.getCurrentParticipant()
      .getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.variant.getIdCreator() != viewBean.getCurrentParticipant()
      .getId();
  }

  public void clickViewVenues() {
    theController.clickViewVenues( UI.EDIT_VARIANT );
  }

  public void clickSave() {
    theController.clickSave( variant );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_VARIANT );
  }

  public String getVenueName() {
    return theModel.getVenueName( variant.getVenue() );
  }

  public String getDistanceUnits() {
    return variant.isMetric()
           ? "km"
           : "mi";
  }

  public String getShortDistanceUnits() {
    return variant.isMetric()
           ? "m"
           : "ft";
  }

}

