/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.LevelPeriod;
import Model.LevelTrackset;
import Model.ModelForView;
import Tables.Pointscount;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import org.primefaces.model.chart.HorizontalBarChartModel;

/**
 *
 * @author usuario
 */
@Named( value = "listPointscountsBean" )
@ViewScoped
public class ListPointscountsBean
  implements Serializable {

  private static final long serialVersionUID = 1L;
  private int periodLevel;
  private int tracksetLevel;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Pointscount> pointscount;
  private List<Pointscount> filteredPointscount;
  private Long index = 0L;
  private HorizontalBarChartModel horizontalBarModel[];
  private String periodLevelStr;
  private String tracksetLevelStr;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListPointscountsBean() {
    horizontalBarModel = new HorizontalBarChartModel[ 7 ];
  }

  public ViewBean getViewBean() {
    return viewBean;
  }

  public void setViewBean( ViewBean viewBean ) {
    this.viewBean = viewBean;
  }

  @PostConstruct
  public void init() {
    theModel = viewBean.getModelBean();
    theController = viewBean.getController();
    index = 0L;
    horizontalBarModel = new HorizontalBarChartModel[ 7 ];
    periodLevelStr = LevelPeriod.NAME[ 0 ];
    tracksetLevelStr = LevelTrackset.NAME[ 0 ];
    pointscount = theModel.getPointscounts( 0,
                                            0 );
    filteredPointscount = pointscount;
    //updateCharts( pointscount );
  }

  //
  // queries
  //
  public void setPointscount( List<Pointscount> pointscount ) {
    this.pointscount = pointscount;
  }

  public List<Pointscount> getPointscount() {
    return pointscount;
  }

  public Long getIndex() {
    return ++index;
  }

  public String[] getPeriodLevelNames() {
    return LevelPeriod.NAME;
  }

  public String[] getTracksetLevelNames() {
    return LevelTrackset.NAME;
  }

  public void setPeriodLevelStr( String level ) {
    periodLevelStr = level;
  }

  public void setTracksetLevelStr( String level ) {
    tracksetLevelStr = level;
  }

  public String getPeriodLevelStr() {
    return periodLevelStr;
  }

  public String getTracksetLevelStr() {
    return tracksetLevelStr;
  }

  public List<Pointscount> getFilteredPointscount() {
    index = 0L;
    return filteredPointscount;
  }

  public void setFilteredPointscount( List<Pointscount> filteredPointscount ) {
    this.filteredPointscount = filteredPointscount;
    if( filteredPointscount != null ) {
      filteredPointscount.sort( ( a, b )
        -> comparePointscounts( a,
                                b ) );
    }
  }

  private int comparePointscounts( Pointscount a,
                                   Pointscount b ) {
    int d = (int) Math.signum( a.getId().getIdPeriod()
                               - b.getId().getIdPeriod() );
    if( d == 0 ) {
      d = (int) Math.signum( a.getId().getIdTrackset()
                             - b.getId().getIdTrackset() );
    }

    return d;
  }

  private void setPeriodTracksetLevels() {
    for( int i = 0;
         i < LevelPeriod.NAME.length;
         i++ ) {
      if( LevelPeriod.NAME[ i ].equals( periodLevelStr ) ) {
        periodLevel = i;
      }
    }
    for( int i = 0;
         i < LevelTrackset.NAME.length;
         i++ ) {
      if( LevelTrackset.NAME[ i ].equals( tracksetLevelStr ) ) {
        tracksetLevel = i;
      }
    }
  }

  public void periodLevelChanged() {
    setPeriodTracksetLevels();
    pointscount = theModel.getPointscounts( periodLevel,
                                            tracksetLevel );
    if( pointscount != null ) {
      pointscount.sort( ( a, b )
        -> comparePointscounts( a,
                                b ) );
//      this.updateCharts( filteredPointscount );
    }
  }

  public void tracksetLevelChanged() {
    setPeriodTracksetLevels();
    pointscount = theModel.getPointscounts( periodLevel,
                                            tracksetLevel );
    if( pointscount != null ) {
      pointscount.sort( ( a, b )
        -> comparePointscounts( a,
                                b ) );
//    this.setFilteredPointscount( pointscount );
    }
  }

  /*
   * user actions
   */
  public void clickReturn() {
    theController.clickReturn( UI.LIST_POINTSCOUNTS );
  }

  public String getParticipantName( long userNumber ) {
    return theModel.getParticipantFullNameById( userNumber );
  }

  private long getColor( long id ) {
    long color = ( +53 * id ) % 0x7f;
    return color;
  }

  public String getPeriodColorFg( Pointscount item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = getColor( item.getId().getIdPeriod() );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;
  }

  public String getTracksetColorFg( Pointscount item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = getColor( item.getId().getIdTrackset() );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;
  }

  public String getPeriodColorBg( Pointscount item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = 0x80 | getColor( item.getId().getIdPeriod() );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;

  }

  public String getTracksetColorBg( Pointscount item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = 0x80 | getColor( item.getId().getIdTrackset() );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;

  }

  public String getTracksetName( Pointscount item ) {
    return item == null
           ? "Unknown"
           : theModel.getTracksetName(
        item.getId()
          .getLevelTrackset(),
        item.getId()
          .getIdTrackset()
      );
  }

  public String getPeriodName( Pointscount item ) {
    return item == null
           ? "Unknown"
           : theModel.getPeriodName(
        item.getId()
          .getLevelPeriod(),
        item.getId()
          .getIdPeriod() );
  }

  public String getTracksetLevelName( Pointscount item ) {
    int level = item.getId()
        .getLevelTrackset();
    return LevelTrackset.NAME[ level ];
  }

  public String getPeriodLevelName( Pointscount item ) {
    int level = item.getId()
        .getLevelPeriod();
    return LevelPeriod.NAME[ level ];
  }

  public String getPointscountTitle() {
    return viewBean.bundle(
      "CHAMPIONSHIPS STANDINGS" );
  }

  public String getRowTxt() {
    return viewBean.bundle(
      "ROW" );
  }

  public String getPeriodLevelTxt() {
    return viewBean.bundle(
      "PERIOD LEVEL" );
  }

  public String getPeriodNameTxt() {
    return viewBean.bundle(
      "PERIOD" );
  }

  public String getParticipantTxt() {
    return viewBean.bundle(
      "USER" );
  }

  public String getTracksetLevelTxt() {
    return viewBean.bundle(
      "TRACKSET LEVEL" );
  }

  public String getTracksetNameTxt() {
    return viewBean.bundle(
      "TRACKSET" );
  }

  public String getSR_DriverPointsTxt() {
    return viewBean.bundle(
      "SR DRIVER POINTS" );
  }

  public String getS_DriverPointsTxt() {
    return viewBean.bundle(
      "S DRIVER POINTS" );
  }

  public String getR_DriverPointsTxt() {
    return viewBean.bundle(
      "R DRIVER POINTS" );
  }

  public String getE_DriverPointsTxt() {
    return viewBean.bundle(
      "E DRIVER POINTS" );
  }

  public String getS_OwnerPointsTxt() {
    return viewBean.bundle(
      "S OWNER POINTS" );
  }

  public String getR_OwnerPointsTxt() {
    return viewBean.bundle(
      "R OWNER POINTS" );
  }

  public String getE_OwnerPointsTxt() {
    return viewBean.bundle(
      "E OWNER POINTS" );
  }

}

