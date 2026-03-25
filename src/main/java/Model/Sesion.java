/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class Sesion {

  public String IP;
  public Date inicio;
  public Date fin;
  public long id;

  public Sesion( String IP,
                 Date inicio,
                 Date fin,
                 long id ) {
    this.IP = IP;
    this.inicio = inicio;
    this.fin = fin;
    this.id = id;
  }

  @Override
  public String toString() {
    Date now = new Date();
    long duration = ( fin == null
                      ? now.getTime()
                      : fin.getTime() )
                    - inicio.getTime();
    return "id:" + id + "\t" + duration + "\tid:" + id + " ini:" + inicio + " fin:" + fin + "\tIP:" + IP;
  }

}

