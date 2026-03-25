/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Venue;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editVenueBean" )
@RequestScoped
public class EditVenueBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Venue venue;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditVenueBean() {
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
    this.venue = viewBean.getVenue();
    if( this.venue == null ) {
      this.venue = PlaceholderFactory.venue();
    }
  }

  public Venue getVenue() {
    return this.venue;
  }

  public void setVenue( Venue venue ) {
    this.venue = venue;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public boolean getDisableEditOwnerField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public boolean getDisableEditMeridianField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public boolean getDisableEditParallelField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public boolean getDisableEditProvinceregionField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.venue.getIdCreator()
              != viewBean.getCurrentParticipant()
        .getId();
  }

  public void clickViewProvinceregions() {
    theController.clickViewProvinceregions();
  }

  public void clickSave() {
    theController.clickSave( venue );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_VENUE );
  }

  public String getProvinceregionName() {
    return theModel.getProvinceregionName(
      venue.getProvinceregion() );
  }

}

