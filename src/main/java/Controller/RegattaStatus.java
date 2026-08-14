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
public class RegattaStatus {

  public static final byte CREATED = 0;
  public static final byte REGISTRATIONS_OPEN = 1;
  public static final byte SPEED_TEST = 2;
  public static final byte RACE_TEST = 3;
  public static final byte AUCTION = 4;
  public static final byte PUBLISHED = 5;
  public static final byte CANCELLED = 6;

  public static final String[] NAME = {
    "CREATED",
    "REGISTRATIONS OPEN",
    "SPEED TEST",
    "RACE TEST",
    "AUCTION",
    "PUBLISHED",
    "CANCELLED",
    "NULL"
  };

  public static boolean hasNextStatus( byte status ) {
    return status >= CREATED && status < PUBLISHED;
  }
}

