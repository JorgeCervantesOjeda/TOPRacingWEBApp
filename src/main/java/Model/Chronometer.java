/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author usuario
 */
public final class Chronometer {

  private long from;
  private long lastDiff;
  private long stopTime;
  private boolean running = false;

  public Chronometer() {
    this.reset();
  }

  public void reset() {
    from = System.nanoTime();
    stopTime = from;
    lastDiff = 0;
  }

  public void go() {
    long restartTime = System.nanoTime();
    long stoppedTime = restartTime - stopTime;
    from += stoppedTime;
    running = true;
  }

  public void pause() {
    stopTime = System.nanoTime();
    running = false;
  }

  public void lap( String msg ) {
    if( !running ) {
      return;
    }
    long now = System.nanoTime();
    long diff = now - from;
    System.out.print( "t: " + diff + " diff: " + ( diff - lastDiff ) + " " + msg );
    lastDiff = diff;
    long afterPrint = System.nanoTime();
    diff = afterPrint - now;
    from += diff;
  }

}

