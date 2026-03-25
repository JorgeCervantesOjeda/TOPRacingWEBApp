/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Participant;
import Tables.Venue;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listVenuesBean" )
@RequestScoped
public class ListVenuesBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Venue> venues;
  private List<Venue> filteredVenue;

  private Venue selectedVenue;

  public Venue getSelectedVenue() {
    return selectedVenue;
  }

  public void setSelectedVenue( Venue selectedVenue ) {
    this.selectedVenue = selectedVenue;
  }

  /**
   * Creates a new instance of PointscountBean
   */
  public ListVenuesBean() {
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
    venues = theModel.getVenues();

  }

  //
  // queries
  //
  public void setVenues( List<Venue> venues ) {
    this.venues = venues;
  }

  public List<Venue> getVenues() {
    return venues;
  }

  public List<Venue> getFilteredVenue() {
    return filteredVenue;
  }

  public void setFilteredVenue( List<Venue> filteredVenue ) {
    this.filteredVenue = filteredVenue;
  }

  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////
  public long getNumVenues() {
    return venues.size();
  }

  public void clickNewVenue() {
    theController.clickNewVenue();
  }

  public void clickSelectVenue( Venue venue ) {
    theController.clickSelectVenue( venue,
                                    viewBean.getVariant() );
  }

  public void clickViewVenueInMap( Venue venue ) {
    theController.clickViewVenueInMap( venue,
                                       UI.LIST_VENUES );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_VENUES );
  }

  public String getOwnerName( Participant participant ) {
    return theModel.getParticipantFullName( participant );
  }

  public String getCreatorName( long participantId ) {
    return theModel.getParticipantFullNameById( participantId );
  }

  public void rowSelectVenue() {
    theController.clickEditVenue( selectedVenue );
  }

  public String getProvinceregionName( Venue venue ) {
    return theModel.getProvinceregionName(
      venue.getProvinceregion()
    );
  }

}

