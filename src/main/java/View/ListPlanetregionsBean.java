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
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listPlanetregionsBean" )
@RequestScoped
public class ListPlanetregionsBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Planetregion> planetregions;
  private List<Planetregion> filteredPlanetregion;

  private Planetregion selectedPlanetregion;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListPlanetregionsBean() {
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
    planetregions = theModel.getPlanetregions();
  }
  //
  // queries
  //

  public void setPlanetregions( List<Planetregion> planetregions ) {
    this.planetregions = planetregions;
  }

  public List<Planetregion> getPlanetregions() {
    return planetregions;
  }

  public List<Planetregion> getFilteredPlanetregion() {
    return filteredPlanetregion;
  }

  public void setFilteredPlanetregion( List<Planetregion> filteredPlanetregion ) {
    this.filteredPlanetregion = filteredPlanetregion;
  }

  public Planetregion getSelectedPlanetregion() {
    return selectedPlanetregion;
  }

  public void setSelectedPlanetregion( Planetregion selectedPlanetregion ) {
    this.selectedPlanetregion = selectedPlanetregion;
  }
  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////

  public long getNumPlanetregions() {
    return planetregions.size();
  }

  public void clickNewPlanetregion() {
    theController.clickNewPlanetregion();
  }

  public void clickSelectPlanetregion( Planetregion planetregion ) {
    theController.clickSelectPlanetregion( planetregion,
                                           viewBean.getCountry() );
  }

  public void rowSelectPlanetregion() {
    theController.clickEditPlanetregion( selectedPlanetregion );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_PLANETREGIONS );
  }

  public String getCreatorName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

}

