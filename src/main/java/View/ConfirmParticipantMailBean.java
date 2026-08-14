// src/main/java/View/ConfirmParticipantMailBean.java
// Confirms the participant e-mail key and exposes the confirmation result.
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Model.ModelBean;
import Tables.Participant;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "confirmParticipantMailBean" )
@RequestScoped
public class ConfirmParticipantMailBean {

  @Inject
  private ModelBean modelBean;

    private String key;

  @Inject
  private ViewBean viewBean;

  public ViewBean getViewBean() {
    return viewBean;
  }

  public void setViewBean( ViewBean _viewBean ) {
    this.viewBean = _viewBean;
  }

  /**
   * Creates a new instance of ConfirmParticipantMail
   */
  public ConfirmParticipantMailBean() {
  }

  private Participant participant;

  @PostConstruct
  public void init() {
    Map<String, String> params = FacesContext.getCurrentInstance()
      .getExternalContext()
      .getRequestParameterMap();
    key = params.get( "key" );
    participant = modelBean.confirmParticipantEmailByKey( key );
  }

  // ...
  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public ModelBean getModelBean() {
    return modelBean;
  }

  public void setModelBean( ModelBean _modelBean ) {
    this.modelBean = _modelBean;
  }

  public String getParticipant() {
    if( participant == null ) {
      return "Unknown participant";
    }
    return participant.getNamesGiven()
           + " " + participant.getNamesFamily()
           + " id: " + participant.getId();
  }

  public String getResultMessage() {
    if( null != participant ) {
      return viewBean.bundle(
        "CONFIRMATION OK" );
    }
    return viewBean.bundle(
      "CONFIRMATION NOT OK" );
  }

  public String getAppURL() {
    return modelBean.getAppURL();
  }

}

