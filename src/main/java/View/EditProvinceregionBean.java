/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Provinceregion;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editProvinceregionBean" )
@RequestScoped
public class EditProvinceregionBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Provinceregion provinceregion;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditProvinceregionBean() {
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
    this.provinceregion = viewBean.getProvinceregion();
    if( this.provinceregion == null ) {
      this.provinceregion = PlaceholderFactory.provinceregion();
    }
  }

  public Provinceregion getProvinceregion() {
    return this.provinceregion;
  }

  public void setProvinceregion( Provinceregion provinceregion ) {
    this.provinceregion = provinceregion;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.provinceregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableEditProvinceField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.provinceregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.provinceregion.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public void clickViewProvinces() {
    theController.clickViewProvinces();
  }

  public void clickSave() {
    theController.clickSave( provinceregion );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_PROVINCEREGION );
  }

  public String getProvinceName() {
    return theModel.getProvinceName( provinceregion.getProvince() );
  }

}

