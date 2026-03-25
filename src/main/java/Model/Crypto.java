/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import org.jasypt.util.text.StrongTextEncryptor;

/**
 *
 * @author usuario
 */
public class Crypto {

  StrongTextEncryptor crypto;

  Crypto() {
    crypto = new StrongTextEncryptor();
    crypto.setPassword( "T0PR4C1N6_00" );
  }

  public String encryptString( String str ) {
    return crypto.encrypt( str );
  }

  public String decryptString( String str ) {
    return crypto.decrypt( str );
  }

}

