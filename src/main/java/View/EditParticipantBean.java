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
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editParticipantBean" )
@ViewScoped
public class EditParticipantBean
  implements Serializable {

  private static final long serialVersionUID = 1L;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;
  private Participant p;

  /**
   * Creates a new instance of RegistrationBean
   */
  public EditParticipantBean() {
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
    p = viewBean.getCurrentParticipant();
  }

  public Participant getParticipant() {
    return this.p;
  }

  public void setParticipant( Participant newParticipant ) {
    this.p = newParticipant;
  }

  public void clickSave() {
    theController.clickSave( this.p );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_USER );
  }

  public void clickViewVenues() {
    theController.clickViewVenues( UI.EDIT_USER );
  }

}

