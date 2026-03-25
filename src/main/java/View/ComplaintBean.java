/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Model.ModelBean;
import Tables.Participant;
import Tables.Registration;
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
@Named( value = "complaintBean" )
@RequestScoped
public class ComplaintBean {

  @Inject
  private ModelBean modelBean;

  @Inject
  private ViewBean viewBean;

    private String key;

    private String r1;

    private String r2;

    private String s;

    private String b;

  private Participant currentParticipant;
  private Participant defaulter;
  private Registration currentRegistration;

  public Registration getRegistration() {
    return currentRegistration;
  }

  public void setRegistration( Registration _registration ) {
    this.currentRegistration = _registration;
  }

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
  public ComplaintBean() {
  }

  @PostConstruct
  public void init() {
    Map<String, String> params = FacesContext.getCurrentInstance()
      .getExternalContext()
      .getRequestParameterMap();
    key = params.get( "key" );
    r1 = params.get( "r1" );
    r2 = params.get( "r2" );
    s = params.get( "s" );
    b = params.get( "b" );

    if( key == null || r1 == null || s == null || b == null ) {
      return;
    }

    System.out.println(
      new Date() + " !!! " + " ---------- ComplaintBean.init() -------------" );
    System.out.println( new Date() + " !!! " + key );
    System.out.println( new Date() + " !!! " + s );
    System.out.println( new Date() + " !!! " + b );
    System.out.println( new Date() + " !!! " + r1 );

    currentParticipant = modelBean.getParticipantByEMailKey( key );
    viewBean.setCurrentParticipant( currentParticipant );
    currentRegistration = modelBean.getRegistrationById( Long.parseLong( r1 ) );
    viewBean.setCurrentRegistration( currentRegistration );

    Participant p = new Participant();
    p.setId( Long.parseLong( s ) );
    defaulter = modelBean.getParticipantById( p );
    if( defaulter.getId() == currentParticipant.getId().longValue() ) {
      p.setId( Long.parseLong( b ) );
      defaulter = modelBean.getParticipantById( p );
      viewBean.setDefaulter( defaulter );
      sellerComplaint();
    } else {
      viewBean.setDefaulter( defaulter );
      buyerComplaint();
    }
  }

  public void buyerComplaint() {
    System.out.println( new Date() + " !!! "
                        + " ---------- ComplaintBean.buyerComplaint() -------------" );
    if( null == currentParticipant ) {
      return;
    }

    viewBean.getController().buyerComplaint( currentRegistration );
  }

  public void sellerComplaint() {
    System.out.println( new Date() + " !!! "
                        + " ---------- ComplaintBean.sellerComplaint() -------------" );
    if( null == currentParticipant ) {
      return;
    }
    viewBean.getController().sellerComplaint( currentRegistration );
  }

  // ...
  public String getKey() {
    return key;
  }

  public void setKey( String key ) {
    this.key = key;
  }

  public String getR1() {
    return r1;
  }

  public void setR1( String _r1 ) {
    this.r1 = _r1;
  }

  public String getR2() {
    return r2;
  }

  public void setR2( String _r2 ) {
    this.r2 = _r2;
  }

  public String getS() {
    return s;
  }

  public void setS( String _s ) {
    this.s = _s;
  }

  public String getB() {
    return b;
  }

  public void setB( String _b ) {
    this.b = _b;
  }

  public String getComplaintMsg() {
    if( defaulter == null || currentParticipant == null ) {
      return "Error: null pointer at getComplaintMsg()";
    }
    if( defaulter.getId() == currentParticipant.getId().longValue() ) {
      return "You cannot file a complaint on yourself.";
    }

    return "You have filed a complaint against:"
           + "\nid:" + defaulter.getId()
           + "\nname: " + defaulter.getNamesGiven() + " " + defaulter
      .getNamesFamily();
  }

}

