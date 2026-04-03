/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

;

import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Participant;
import Tables.Planetregion;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;

/**
 *
 * @author usuario
 */


public interface ViewForController {

  public void showUI( int ui );

  public void showUI( int ui,
                      Participant user );

  public void showUI( int ui,
                      Regatta regatta );

  public void showUI( int ui,
                      Registration registration );

  public void showUI( int ui,
                      Car car );

  public void showUI( int ui,
                      Variant variant );

  public void showUI( int ui,
                      Venue venue );

  public void showUI( int ui,
                      Provinceregion provinceregion );

  public void showUI( int ui,
                      Province province );

  public void showUI( int ui,
                      Countryregion countryregion );

  public void showUI( int ui,
                      Country country );

  public void showUI( int ui,
                      Planetregion planetregion );

  public void showUI( int ui,
                      boolean flag );

  public void showUI( int ui,
                      int _currentTracksetLevel,
                      int _currentPeriodLevel );

  public String bundle( String id );

  public void setProgress( double p );

  public void invalidateSessionAndShowUI( int ui );

}

