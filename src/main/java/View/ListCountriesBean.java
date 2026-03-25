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
import Tables.Planetregion;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listCountriesBean" )
@RequestScoped
public class ListCountriesBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Country> countries;
  private List<Country> filteredCountry;

  private Country selectedCountry;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListCountriesBean() {
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
    countries = theModel.getCountries();
  }
  //
  // queries
  //

  public void setCountries( List<Country> countries ) {
    this.countries = countries;
  }

  public List<Country> getCountries() {
    return countries;
  }

  public List<Country> getFilteredCountry() {
    return filteredCountry;
  }

  public void setFilteredCountry( List<Country> filteredCountry ) {
    this.filteredCountry = filteredCountry;
  }

  public Country getSelectedCountry() {
    return selectedCountry;
  }

  public void setSelectedCountry( Country selectedCountry ) {
    this.selectedCountry = selectedCountry;
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public long getNumCountries() {
    return countries.size();
  }

  public void clickNewCountry() {
    theController.clickNewCountry();
  }

  public void clickSelectCountry( Country country ) {
    theController.clickSelectCountry( country,
                                      viewBean.getCountryregion() );
  }

  public void rowSelectCountry() {
    theController.clickEditCountry( selectedCountry );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_COUNTRIES );
  }

  public String getPlanetregionName( Planetregion planetregion ) {
    return planetregion.getName();
  }

  public String getCreatorName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

}

