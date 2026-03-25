/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Planetregion;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editPlanetregionBean" )
@RequestScoped
public class EditPlanetregionBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Planetregion planetregion;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditPlanetregionBean() {
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
    this.planetregion = viewBean.getPlanetregion();
    if( this.planetregion == null ) {
      this.planetregion = PlaceholderFactory.planetregion();
    }
  }

  public Planetregion getPlanetregion() {
    return this.planetregion;
  }

  public void setPlanetregion( Planetregion planetregion ) {
    this.planetregion = planetregion;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.planetregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.planetregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public void clickSave() {
    theController.clickSave( planetregion );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_PLANETREGION );
  }

}

