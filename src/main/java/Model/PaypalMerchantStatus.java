// src/main/java/Model/PaypalMerchantStatus.java
// Interprets PayPal seller status responses for TOP Racing account activation.
package Model;

class PaypalMerchantStatus {

  private final boolean paymentsReceivable;
  private final boolean primaryEmailConfirmed;
  private final boolean hasGrantedPermissions;
  private final boolean hasLimitations;

  private PaypalMerchantStatus( boolean paymentsReceivable,
                                boolean primaryEmailConfirmed,
                                boolean hasGrantedPermissions,
                                boolean hasLimitations ) {
    this.paymentsReceivable = paymentsReceivable;
    this.primaryEmailConfirmed = primaryEmailConfirmed;
    this.hasGrantedPermissions = hasGrantedPermissions;
    this.hasLimitations = hasLimitations;
  }

  static PaypalMerchantStatus fromJson( String json ) {
    return new PaypalMerchantStatus(
      JsonText.booleanValueOf( json,
                               "payments_receivable" ),
      JsonText.booleanValueOf( json,
                               "primary_email_confirmed" ),
      !JsonText.arrayIsEmpty( json,
                              "granted_permissions" ),
      !JsonText.arrayIsEmpty( json,
                              "limitations" ) );
  }

  boolean isUsable() {
    return paymentsReceivable
           && primaryEmailConfirmed
           && hasGrantedPermissions
           && !hasLimitations;
  }
}
