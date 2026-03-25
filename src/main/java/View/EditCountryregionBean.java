/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Countryregion;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editCountryregionBean" )
@ViewScoped
public class EditCountryregionBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Countryregion countryregion;

  /**
   * Creates a new instance of EditCountryregionBean
   */
  public EditCountryregionBean() {
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
    this.countryregion = viewBean.getCountryregion();
    if( this.countryregion == null ) {
      this.countryregion = PlaceholderFactory.countryregion();
    }
  }

  public Countryregion getCountryregion() {
    return this.countryregion;
  }

  public void setCountryregion( Countryregion countryregion ) {
    this.countryregion = countryregion;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.countryregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableEditCountryField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.countryregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.countryregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public void clickViewCountries() {
    theController.clickViewCountries();
  }

  public void clickSave() {
    theController.clickSave( countryregion );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_COUNTRYREGION );
  }

  public String getCountryName() {
    return theModel.getCountryName( countryregion.getCountry() );
  }

}

