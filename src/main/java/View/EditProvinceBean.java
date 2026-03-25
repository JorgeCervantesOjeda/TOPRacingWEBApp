/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Province;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editProvinceBean" )
@RequestScoped
public class EditProvinceBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  Province province;

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditProvinceBean() {
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
    this.province = viewBean.getProvince();
    if( this.province == null ) {
      this.province = PlaceholderFactory.province();
    }
  }

  public Province getProvince() {
    return this.province;
  }

  public void setProvince( Province province ) {
    this.province = province;
  }

  public boolean getDisableEditNameField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.province.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableEditCountryregionField() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.province.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public boolean getDisableSaveButton() {
    return viewBean.getCurrentParticipant() == null
           || viewBean.getCurrentParticipant().getId() == null
           || this.province.getIdCreator()
              != viewBean.getCurrentParticipant().getId();
  }

  public void clickViewCountryregions() {
    theController.clickViewCountryregions();
  }

  public void clickSave() {
    theController.clickSave( province );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_PROVINCE );
  }

  public String getCountryregionName() {
    return theModel.getCountryregionName( province.getCountryregion() );
  }

}

