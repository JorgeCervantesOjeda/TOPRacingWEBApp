// src/main/java/View/PaypalReturnBean.java
// Handles the PayPal seller onboarding return page.
package View;

import Model.ModelForView;
import Tables.Participant;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named( value = "paypalReturnBean" )
@RequestScoped
public class PaypalReturnBean {

  @Inject
  private ModelForView modelBean;

  @Inject
  private ViewBean viewBean;

  private Participant participant;
  private boolean paypalUsable;

  @PostConstruct
  public void init() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if( facesContext == null ) {
      return;
    }
    Map<String, String> params = facesContext.getExternalContext()
      .getRequestParameterMap();
    completePaypalOnboarding( params.get( "merchantId" ),
                              params.get( "merchantIdInPayPal" ),
                              params.get( "permissionsGranted" ) );
  }

  void completePaypalOnboarding( String trackingId,
                                 String merchantIdInPayPal,
                                 String permissionsGranted ) {
    participant = modelBean.confirmPaypalOnboardingReturn(
      trackingId,
      merchantIdInPayPal,
      Boolean.parseBoolean( permissionsGranted ) );
    paypalUsable = participant != null
                   && participant.isPaypalUsable();
  }

  public boolean isPaypalUsable() {
    return paypalUsable;
  }

  public String getResultHeader() {
    return paypalUsable
           ? bundle( "PAYPAL CONFIRMATION OK" )
           : bundle( "PAYPAL CONFIRMATION NOT OK" );
  }

  public String getResultDetail() {
    return paypalUsable
           ? bundle( "PAYPAL CONFIRMATION OK LONG" )
           : bundle( "PAYPAL CONFIRMATION NOT OK LONG" );
  }

  public void setModelBean( ModelForView modelBean ) {
    this.modelBean = modelBean;
  }

  public void setViewBean( ViewBean viewBean ) {
    this.viewBean = viewBean;
  }

  private String bundle( String key ) {
    if( viewBean == null ) {
      return key;
    }
    return viewBean.bundle( key );
  }
}
