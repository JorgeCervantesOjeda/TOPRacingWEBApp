/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Model.ModelBean;
import Tables.Participant;
import java.util.Date;
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
@Named( value = "resetPasswordBean" )
@RequestScoped
public class ResetPasswordBean {

  @Inject
  private ModelBean modelBean;

  @Inject
  private ViewBean viewBean;

    private String key;

  private Participant currentParticipant;
  private Participant defaulter;

  public Participant getParticipant() {
    return currentParticipant;
  }

  public void setParticipant( Participant _participant ) {
    this.currentParticipant = _participant;
  }

  public Participant getDefaulter() {
    return defaulter;
  }

  public void setDefaulter( Participant _defaulter ) {
    this.defaulter = _defaulter;
  }

  public ModelBean getModelBean() {
    return modelBean;
  }

  public void setModelBean( ModelBean _modelBean ) {
    this.modelBean = _modelBean;
  }

  public ViewBean getViewBean() {
    return viewBean;
  }

  public void setViewBean( ViewBean viewBean ) {
    this.viewBean = viewBean;
  }

  /**
   * Creates a new instance of ConfirmParticipantMail
   */
  public ResetPasswordBean() {
  }

  @PostConstruct
  public void init() {
    Map<String, String> params = FacesContext.getCurrentInstance()
      .getExternalContext()
      .getRequestParameterMap();
    key = params.get( "key" );

    System.out.println(
      new Date() + " !!! " + " ---------- ResetPasswordBean.init() -------------" );
    System.out.println( new Date() + " !!! " + key );

    currentParticipant = modelBean.getParticipantByEMailKey( key );
    viewBean.setCurrentParticipant( currentParticipant );

    resetPasswordConfirm();
  }

  public void resetPasswordConfirm() {
    System.out.println( new Date() + " !!! "
                        + " ---------- ResetPasswordBean.resetPassword() -------------" );
    if( null == currentParticipant ) {
      return;
    }

    viewBean.getController().clickResetPasswordConfirm( currentParticipant );
  }

  // ...
  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public String getResetPasswordMsg() {
    if( currentParticipant == null ) {
      return "Error: null pointer at getResetPasswordMsg()";
    }

    return "Your Password has been reset. "
           + "Please wait for your new password in your e-mail.";
  }

}

