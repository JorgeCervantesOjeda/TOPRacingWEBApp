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
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listProvincesBean" )
@RequestScoped
public class ListProvincesBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Province> provinces;
  private List<Province> filteredProvince;

  private Province selectedProvince;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListProvincesBean() {
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
    provinces = theModel.getProvinces();
  }
  //
  // queries
  //

  public void setProvinces( List<Province> provinces ) {
    this.provinces = provinces;
  }

  public List<Province> getProvinces() {
    return provinces;
  }

  public List<Province> getFilteredProvince() {
    return filteredProvince;
  }

  public void setFilteredProvince( List<Province> filteredProvince ) {
    this.filteredProvince = filteredProvince;
  }

  public Province getSelectedProvince() {
    return selectedProvince;
  }

  public void setSelectedProvince( Province selectedProvince ) {
    this.selectedProvince = selectedProvince;
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public long getNumProvinces() {
    return provinces.size();
  }

  public void clickNewProvince() {
    theController.clickNewProvince();
  }

  public void clickSelectProvince( Province province ) {
    theController.clickSelectProvince( province,
                                       viewBean.getProvinceregion() );
  }

  public void rowSelectProvince() {
    theController.clickEditProvince( selectedProvince );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_PROVINCES );
  }

  public String getCreatorName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

  public String getCountryregionName( Province province ) {
    return theModel.getCountryregionName(
      province.getCountryregion()
    );
  }

}

