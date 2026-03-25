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
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listProvinceregionsBean" )
@RequestScoped
public class ListProvinceregionsBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Provinceregion> provinceregions;
  private List<Provinceregion> filteredProvinceregion;

  private Provinceregion selectedProvinceregion;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListProvinceregionsBean() {
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
    provinceregions = theModel.getProvinceregions();
  }
  //
  // queries
  //

  public void setProvinceregions( List<Provinceregion> provinceregions ) {
    this.provinceregions = provinceregions;
  }

  public List<Provinceregion> getProvinceregions() {
    return provinceregions;
  }

  public List<Provinceregion> getFilteredProvinceregion() {
    return filteredProvinceregion;
  }

  public void setFilteredProvinceregion(
    List<Provinceregion> filteredProvinceregion ) {
    this.filteredProvinceregion = filteredProvinceregion;
  }

  public Provinceregion getSelectedProvinceregion() {
    return selectedProvinceregion;
  }

  public void setSelectedProvinceregion( Provinceregion selectedProvinceregion ) {
    this.selectedProvinceregion = selectedProvinceregion;
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public long getNumProvinceregions() {
    return provinceregions.size();
  }

  public void clickNewProvinceregion() {
    theController.clickNewProvinceregion();
  }

  public void clickSelectProvinceregion( Provinceregion provinceregion ) {
    theController.clickSelectProvinceregion( provinceregion,
                                             viewBean.getVenue() );
  }

  public void rowSelectProvinceregion() {
    theController.clickEditProvinceregion( selectedProvinceregion );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_PROVINCEREGIONS );
  }

  public String getCreatorName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

  public String getProvinceName( Provinceregion provinceregion ) {
    return theModel.getProvinceName(
      provinceregion.getProvince()
    );
  }

}

