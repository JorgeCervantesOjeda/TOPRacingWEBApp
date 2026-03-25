/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import java.util.ArrayList;

/**
 *
 * @author usuario
 */
public class PilaInteger
  extends ArrayList<Integer> {

  private static final long serialVersionUID = 1L;

  public Integer pop() {
    Integer o = this.get( this.size() - 1 );
    this.remove( this.size() - 1 );
    return o;
  }

  public void push( int n ) {
    super.add( n );
  }

  ;

  public Integer last() {
    return this.get( this.size() - 1 );
  }

}

