/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Model.LevelTrackset;

/**
 *
 * @author usuario
 */
public class PeriodlevelPenalties {

  private final long regattaid;
  private final long periodlevel;
  private final long penalty[];

  public PeriodlevelPenalties( long regattaid,
                               long _periodlevel ) {
    this.regattaid = regattaid;
    this.periodlevel = _periodlevel;
    this.penalty = new long[ LevelTrackset.NAME.length ];
    for( int i = 0;
         i < penalty.length;
         i++ ) {
      penalty[ i ] = 1;
    }
  }

  public long getPenalty( int tracksetlevel ) {
    return penalty[ tracksetlevel ];
  }

  public void setPenalty( long tracksetlevel,
                          long _penalty ) {
    if( tracksetlevel >= LevelTrackset.NAME.length ) {
      return;
    }
    this.penalty[ (int) tracksetlevel ] = _penalty;
  }

  @Override
  public boolean equals( Object other ) {
    if( ( this == other ) ) {
      return true;
    }
    if( ( other == null ) ) {
      return false;
    }
    if( !( other instanceof PeriodlevelPenalties ) ) {
      return false;
    }
    PeriodlevelPenalties castOther = (PeriodlevelPenalties) other;

    return this.getRegattaid() == castOther.getRegattaid()
           && this.getPeriodLevel() == castOther.getPeriodLevel();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + (int) ( this.getRegattaid() ^ ( this.getRegattaid() >>> 32 ) );
    hash = 47 * hash + (int) ( this.getPeriodLevel() ^ ( this.getPeriodLevel() >>> 32 ) );
    return hash;
  }

  /**
   * @return the regattaid
   */
  public long getRegattaid() {
    return regattaid;
  }

  /**
   * @return the periodLevel
   */
  public long getPeriodLevel() {
    return periodlevel;
  }

}

