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
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listCountryregionsBean" )
@RequestScoped
public class ListCountryregionsBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Countryregion> countryregions;
  private List<Countryregion> filteredCountryregion;

  private Countryregion selectedCountryregion;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListCountryregionsBean() {
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
    countryregions = theModel.getCountryregions();
  }
  //
  // queries
  //

  public void setCountryregions( List<Countryregion> countryregions ) {
    this.countryregions = countryregions;
  }

  public List<Countryregion> getCountryregions() {
    return countryregions;
  }

  public List<Countryregion> getFilteredCountryregion() {
    return filteredCountryregion;
  }

  public void setFilteredCountryregion(
    List<Countryregion> filteredCountryregion ) {
    this.filteredCountryregion = filteredCountryregion;
  }

  public Countryregion getSelectedCountryregion() {
    return selectedCountryregion;
  }

  public void setSelectedCountryregion( Countryregion selectedCountryregion ) {
    this.selectedCountryregion = selectedCountryregion;
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public long getNumCountryregions() {
    return countryregions.size();
  }

  public void clickNewCountryregion() {
    theController.clickNewCountryregion();
  }

  public void clickSelectCountryregion( Countryregion countryregion ) {
    theController.clickSelectCountryregion( countryregion,
                                            viewBean.getProvince() );
  }

  public void rowSelectCountryregion() {
    theController.clickEditCountryregion( selectedCountryregion );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_COUNTRYREGIONS );
  }

  public String getCreatorName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

  public String getCountryName( Countryregion countryregion ) {
    return theModel.getCountryName(
      countryregion.getCountry()
    );
  }

}

