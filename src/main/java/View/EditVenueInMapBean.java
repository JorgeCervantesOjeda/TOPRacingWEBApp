/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Venue;
import java.io.Serializable;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author usuario
 */
@Named( value = "editVenueInMapBean" )
@ViewScoped
public class EditVenueInMapBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private Venue venue;

  private MapModel simpleModel;
  private Marker marker;

  @PostConstruct
  public void init() {

    theModel = viewBean.getModelBean();
    theController = viewBean.getController();
    this.venue = viewBean.getVenue();

    simpleModel = new DefaultMapModel();

    //Shared coordinates
    LatLng coord1 = new LatLng( venue.getParallel(),
                                venue.getMeridian() );
    marker = new Marker( coord1,
                         venue.getName() );
    marker.setDraggable(
      viewBean.getCurrentParticipant() != null
      && viewBean.getCurrentParticipant().getId() != null
      && venue
        .getIdCreator()
         == viewBean
        .getCurrentParticipant()
        .getId()
    );

    //Basic marker
    simpleModel.addOverlay( marker );
  }

  public MapModel getSimpleModel() {
    return simpleModel;
  }

  public void onMarkerSelect( OverlaySelectEvent event ) {
    marker = (Marker) event.getOverlay();

    FacesContext
      .getCurrentInstance()
      .addMessage(
        null,
        new FacesMessage(
          FacesMessage.SEVERITY_INFO,
          "Marker Selected",
          marker.getTitle()
        ) );
  }

  public Marker getMarker() {
    return marker;
  }

  public Venue getVenue() {
    return venue;
  }

  public void setVenue( Venue venue ) {
    this.venue = venue;
  }

  /**
   * Creates a new instance of EditVariantBean
   */
  public EditVenueInMapBean() {
  }

  public ViewBean getViewBean() {
    return viewBean;
  }

  public void setViewBean( ViewBean viewBean ) {
    this.viewBean = viewBean;
  }

  public String getVenueName() {
    return this.venue.getName();
  }

  public double getVenueParallel() {
    return venue.getParallel();
  }

  public double getVenueMeridian() {
    return venue.getMeridian();
  }

  public String getVenueCoordinates() {
    if( venue.getParallel() == 0
        && venue.getMeridian() == 0 ) {
      venue.setParallel( 19.352301477794523 );
      venue.setMeridian( -99.28258674070315 );
    }
    return venue.getParallel() + "," + venue.getMeridian();
  }

  public void onMarkerDrag( MarkerDragEvent regatta ) {
    marker = regatta.getMarker();

    FacesContext
      .getCurrentInstance()
      .addMessage(
        null,
        new FacesMessage(
          FacesMessage.SEVERITY_INFO,
          "Marker Dragged",
          "Lat:" + marker.getLatlng()
            .getLat() + ", Lng:" + marker.getLatlng()
            .getLng() ) );
    venue.setParallel( marker.getLatlng()
      .getLat() );
    venue.setMeridian( marker.getLatlng()
      .getLng() );

    theController.venueCoordinatesChanged( venue );
  }

  public String getSubtitle() {
    if( venue.getIdCreator()
        == viewBean.getCurrentParticipant()
        .getId() ) {
      return "Click on map to set coordinates: " + getVenueCoordinates();
    }

    return "Coordinates: " + getVenueCoordinates();
  }

  public void clickReturn() {
    theController.clickReturn( UI.EDIT_VENUE_IN_MAP );
  }

  public void setLatLng() {
    Map<String, String> requestParamMap = FacesContext.getCurrentInstance()
                        .getExternalContext().getRequestParameterMap();

    String parallelStr = requestParamMap.get( "parallel" ); //Hello
    String meridianStr = requestParamMap.get( "meridian" ); //World

    this.venue.setParallel( Double.parseDouble( parallelStr ) );
    this.venue.setMeridian( Double.parseDouble( meridianStr ) );

    this.theController.saveVenue( venue );
  }

}

