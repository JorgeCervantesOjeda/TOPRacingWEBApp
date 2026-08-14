// src/test/java/View/PaypalReturnBeanTest.java
// Verifies PayPal return handling without trusting callback parameters alone.
package View;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.ModelForView;
import Tables.Participant;
import org.junit.jupiter.api.Test;

class PaypalReturnBeanTest {

  @Test
  void paypalReturnIsSuccessfulOnlyWhenModelConfirmsUsablePaypal() {
    ModelForView model = mock( ModelForView.class );
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( true );
    participant.refreshOperationalConfirmation();
    when( model.confirmPaypalOnboardingReturn( "tracking-1",
                                               "merchant-paypal-1",
                                               true ) ).thenReturn( participant );
    PaypalReturnBean bean = new PaypalReturnBean();
    bean.setModelBean( model );

    bean.completePaypalOnboarding( "tracking-1",
                                   "merchant-paypal-1",
                                   "true" );

    assertTrue( bean.isPaypalUsable() );
    verify( model ).confirmPaypalOnboardingReturn( "tracking-1",
                                                   "merchant-paypal-1",
                                                   true );
  }

  @Test
  void paypalReturnFailsWhenPermissionsAreNotGranted() {
    ModelForView model = mock( ModelForView.class );
    Participant participant = new Participant();
    participant.setEmailConfirmed( true );
    participant.setPaypalUsable( false );
    when( model.confirmPaypalOnboardingReturn( "tracking-2",
                                               "merchant-paypal-2",
                                               false ) ).thenReturn( participant );
    PaypalReturnBean bean = new PaypalReturnBean();
    bean.setModelBean( model );

    bean.completePaypalOnboarding( "tracking-2",
                                   "merchant-paypal-2",
                                   "false" );

    assertFalse( bean.isPaypalUsable() );
  }
}
