/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Country;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editCountryBean" )
@RequestScoped
public class EditCountryBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Country country;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditCountryBean() {
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
    this.country = viewBean.getCountry();
    if( this.country == null ) {
      this.country = PlaceholderFactory.country();
    }
  }

  public Country getCountry() {
    return this.country;
  }

  public void setCountry( Country country ) {
    this.country = country;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.country.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableEditPlanetregionField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.country.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.country.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public void clickViewPlanetregions() {
    theController.clickViewPlanetregions();
  }

  public void clickSave() {
    theController.clickSave( country );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_COUNTRY );
  }

  public String getPlanetregionName() {
    return theModel.getPlanetregionName( country.getPlanetregion() );
  }

}

