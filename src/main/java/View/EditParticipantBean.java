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
  private boolean acceptCurrentTerms;

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
    acceptCurrentTerms = p != null
                         && p.hasAcceptedCurrentTerms();
  }

  public Participant getParticipant() {
    return this.p;
  }

  public void setParticipant( Participant newParticipant ) {
    this.p = newParticipant;
    this.acceptCurrentTerms = newParticipant != null
                              && newParticipant.hasAcceptedCurrentTerms();
  }

  public boolean isAcceptCurrentTerms() {
    return acceptCurrentTerms;
  }

  public void setAcceptCurrentTerms( boolean acceptCurrentTerms ) {
    this.acceptCurrentTerms = acceptCurrentTerms;
  }

  public boolean isCurrentTermsAccepted() {
    return this.p != null
           && this.p.hasAcceptedCurrentTerms();
  }

  public String getCurrentTermsVersion() {
    return Participant.CURRENT_TERMS_VERSION;
  }

  public String getCurrentTermsEffectiveDate() {
    return Participant.CURRENT_TERMS_EFFECTIVE_DATE;
  }

  public String getCurrentTermsText() {
    return "TOP Racing current rules version "
           + Participant.CURRENT_TERMS_VERSION
           + ", effective "
           + Participant.CURRENT_TERMS_EFFECTIVE_DATE
           + ". Participants must keep a confirmed e-mail address, a usable "
           + "PayPal account when payment operations apply, and current rules "
           + "acceptance before protected operations. Registrations, bids, "
           + "vehicle delivery, cancellations, refunds, local blocks, defaults "
           + "and global exclusions follow the rules in force for the event. "
           + "A refundable cancellation not attributable to the participant may "
           + "produce the applicable refund. Disqualification for breach, fraud, "
           + "unsafe conduct, non-delivery, tampering or rule violation produces "
           + "no automatic refund for the disqualified participant; ambiguous "
           + "cases require documented review under the event rules and "
           + "applicable law. Country-specific legal wording must be validated "
           + "before real-money operation in that jurisdiction.";
  }

  public void clickSave() {
    if( this.acceptCurrentTerms && this.p != null ) {
      this.p.acceptCurrentTerms();
    }
    theController.clickSave( this.p );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_USER );
  }

  public void clickViewVenues() {
    theController.clickViewVenues( UI.EDIT_USER );
  }

}

