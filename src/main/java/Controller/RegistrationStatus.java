/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

/**
 *
 * @author usuario
 */
public class RegistrationStatus {

  public static final byte INCOMPLETE = 0;
  public static final byte OK = 1;
  public static final byte INVALID = 2;
  public static final byte DISQUALIFIED = 3;
  public static final byte DIDNOTFINISH = 4;
  public static final byte CANCELLED = 5;

  public static final String[] NAME = {
    "Incomplete",
    "OK",
    "Invalid",
    "Disqualified",
    "Did not finish",
    "Cancelled"
  };

  public static boolean isComputable( byte status ) {
    return status == OK;
  }

  public static boolean requiresStatusNote( byte status ) {
    return status == DISQUALIFIED || status == CANCELLED;
  }
}

