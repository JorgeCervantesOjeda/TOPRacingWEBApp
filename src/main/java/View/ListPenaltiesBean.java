/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.UI;
import Model.LevelPeriod;
import Model.LevelTrackset;
import Model.ModelForView;
import Tables.Penaltiespl;
import Tables.Regatta;
import Tables.Variant;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listPenaltiesBean" )
@ViewScoped
public class ListPenaltiesBean
  implements Serializable {

  private static final long serialVersionUID = 1L;
  private int periodLevel;
  private int tracksetLevel;

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Penaltiespl> penaltiespl = null;
  private List<Penaltiespl> filteredPenaltiespl = null;

  private Penaltiespl selectedPenaltiespl;

  public Penaltiespl getSelectedPenaltiespl() {
    return selectedPenaltiespl;
  }

  public void setSelectedPenaltiespl( Penaltiespl _selectedPenaltiespl ) {
    this.selectedPenaltiespl = _selectedPenaltiespl;
  }

  public void rowSelectPenaltiespl( Penaltiespl penaltiespl ) {
    theController.clickEditRegatta( penaltiespl.getRegatta(),
                                    UI.LIST_PENALTIES );
  }

  public void rowSelectPenaltiespl() {
    theController.clickEditRegatta( selectedPenaltiespl.getRegatta(),
                                    UI.LIST_PENALTIES );
  }

  private String periodLevelStr;
  private String tracksetLevelStr;

  /**
   * Creates a new instance of PenaltyBean
   */
  public ListPenaltiesBean() {
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
    tracksetLevel = viewBean.getTracksetLevel();
    periodLevel = viewBean.getPeriodLevel();
    tracksetLevelStr = LevelTrackset.NAME[ tracksetLevel ];
    periodLevelStr = LevelPeriod.NAME[ periodLevel ];
    penaltiespl = theModel.getPenaltiesplForPL( periodLevel );
    penaltiespl.sort( ( a, b )
      -> comparePenaltiespl( a,
                             b ) );
    this.filteredPenaltiespl = penaltiespl;
  }

  //
  // queries
  //
  public void setPenaltiespl( List<Penaltiespl> penaltiespl ) {
    this.penaltiespl = penaltiespl;
  }

  public List<Penaltiespl> getPenaltiespl() {
    return penaltiespl;
  }

  public String[] getPeriodLevelNames() {
    return LevelPeriod.NAME;
  }

  public String[] getTracksetLevelNames() {
    return LevelTrackset.NAME;
  }

  public List<Penaltiespl> getFilteredPenaltiespl() {
    //if( filteredPenalties != null ) {
    //  filteredPenalties.sort( ( a, b )
    //    -> comparePenalties( a,
    //                         b ) );
    //}
    return filteredPenaltiespl;
  }

  public void setFilteredPenaltiespl( List<Penaltiespl> filteredPenaltiespl ) {
//    if( filteredPenaltiespl != null ) {
//      filteredPenaltiespl.sort( ( a, b )
//        -> comparePenaltiespl( a,
//                               b ) );
//    }
    this.filteredPenaltiespl = filteredPenaltiespl;
  }

  private int comparePenaltiespl( Penaltiespl a,
                                  Penaltiespl b ) {
    int d = 0;
    if( d == 0 ) {
      d = (int) Math.signum(
      theModel.getPeriodId( a.getRegatta().getDatetime(),
                            a.getId().getLevelPeriod() )
      - theModel.getPeriodId( b.getRegatta().getDatetime(),
                              b.getId().getLevelPeriod() ) );
    }
    if( d == 0 ) {
      d = (int) Math.signum(
      theModel.getPeriodId( a.getRegatta().getDatetime(),
                            a.getId().getLevelPeriod() + 1 )
      - theModel.getPeriodId( b.getRegatta().getDatetime(),
                              b.getId().getLevelPeriod() + 1 ) );
    }
    if( d == 0 ) {
      d = (int) Math.signum(
      theModel.getIdTrackset( a.getRegatta().getVariant(),
                              tracksetLevel )
      - theModel.getIdTrackset( b.getRegatta().getVariant(),
                                tracksetLevel ) );
    }
    if( d == 0 ) {
      d = (int) Math.signum(
      theModel.getIdTrackset( a.getRegatta().getVariant(),
                              tracksetLevel + 1 )
      - theModel.getIdTrackset( b.getRegatta().getVariant(),
                                tracksetLevel + 1 ) );
    }
    /*
     * if( d == 0 ) { d = (int) Math.signum( theModel.getPenaltyValue( a,
     * tracksetLevel ) - theModel.getPenaltyValue( b, tracksetLevel ) ); } if( d
     * == 0 ) { d = (int) Math.signum( a.getRegatta().getDatetime() .compareTo(
     * b.getRegatta().getDatetime() ) ); }
     */
    if( d == 0 ) {
      return 0;
    }
    return d;
  }

  /*
   * user actions
   */
  public void clickCreateRegatta() {
    theController.clickNewRegatta( UI.LIST_PENALTIES );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_PENALTIES );
  }

  public String getParticipantName( Penaltiespl item ) {
    return theModel.getParticipantFullNameById(
      item.getRegatta().getParticipant().getId() );
  }

  public String getVariantName( Variant variant ) {
    if( variant == null ) {
      return "Unknown";
    }

    return theModel.getVariantName( variant );
  }

  public String getTracksetName( Penaltiespl item ) {
    if( item == null ) {
      return "Unknown";
    }

    return theModel.getTracksetName(
      tracksetLevel + 1,
      theModel.getIdTrackset( item.getRegatta().getVariant(),
                              tracksetLevel + 1 )
    );
  }

  public String getPeriodName( Penaltiespl item ) {
    if( item == null ) {
      return "Unknown";
    }

    return theModel.getPeriodName(
      item.getId().getLevelPeriod() + 1,
      theModel.getPeriodId( item.getRegatta().getDatetime(),
                            item.getId().getLevelPeriod() + 1 )
    );
  }

  public String getDayPeriodName( Penaltiespl item ) {
    if( item == null ) {
      return "Unknown";
    }

    return theModel.getPeriodName(
      LevelPeriod.WEEK_OF_MONTH + 1,
      theModel.getPeriodId( item.getRegatta().getDatetime(),
                            LevelPeriod.WEEK_OF_MONTH + 1 )
    );
  }

  public String getPenaltiesTitle() {
    return viewBean.bundle( "PRIORITIES STANDINGS" );
  }

  public String getPeriodLevelTxt() {
    return viewBean.bundle( "PERIOD LEVEL" );
  }

  public String getPeriodNameTxt() {
    return viewBean.bundle( "DISCR PERIOD" );
  }

  public String getParticipantTxt() {
    return viewBean.bundle( "ORGANIZER" );
  }

  public String getTracksetLevelTxt() {
    return viewBean.bundle( "TRACKSET LEVEL" );
  }

  public String getTracksetNameTxt() {
    return viewBean.bundle( "DISCR TRACKSET" );
  }

  public String getPenaltyTxt() {
    return viewBean.bundle( "PENALTY" );
  }

  public String getRegattaIdTxt() {
    return viewBean.bundle( "REGATTA ID" );
  }

  public String getCreateRegattaButtonTitle() {
    return viewBean.bundle( "CREATE REGATTA" );
  }

  public String getPeriodLevelStr() {
    return periodLevelStr;
  }

  public void setPeriodLevelStr( String _periodLevelStr ) {
    this.periodLevelStr = _periodLevelStr;
  }

  public String getTracksetLevelStr() {
    return tracksetLevelStr;
  }

  public void setTracksetLevelStr( String _tracksetLevelStr ) {
    this.tracksetLevelStr = _tracksetLevelStr;
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
    this.theController.periodLevelChanged( periodLevel );
  }

  public void tracksetLevelChanged() {
    setPeriodTracksetLevels();
    this.theController.tracksetLevelChanged( tracksetLevel );
  }

  public String getDateTxt() {
    return viewBean.bundle(
      "DATE" );
  }

  public String getVariantTxt() {
    return viewBean.bundle(
      "VARIANT" );
  }

  public String getStatusName( int status ) {
    return status + ") " + viewBean.bundle(
      RegattaStatus.NAME[ status ] );
  }

  public String getStatusTxt() {
    return viewBean.bundle(
      "STATUS" );
  }

  public String getPriorityPointsTxt() {
    return viewBean.bundle(
      "PRIORITY POINTS" );
  }

  public double getPriorityPoints( Regatta regatta ) {
    return theModel.getRegattaPriorityPoints( regatta );
  }

  public String getTimeZone() {
    return theModel.getTimeZone();
  }

  public String getMaxWeightTxt() {
    return viewBean.bundle(
      "MAXIMUM WEIGHT" );
  }

  public String getMinLoadTxt() {
    return viewBean.bundle(
      "MINIMUM LOAD" );
  }

  public String getCurrencyTxt() {
    return viewBean.bundle(
      "CURRENCY" );
  }

  public String getRegistrationCostTxt() {
    return viewBean.bundle(
      "REGISTRATION COST" );
  }

  public String getTrackRentalTxt() {
    return viewBean.bundle(
      "TRACK RENTAL" );
  }

  public String getFinishingPrizeTxt() {
    return viewBean.bundle(
      "FINISHING PRIZE" );
  }

  public String getEfficiencyPrizeTxt() {
    return viewBean.bundle(
      "EFFICIENCY PRIZE" );
  }

  public String getNumRegistrationsTxt() {
    return viewBean.bundle(
      "NUM REGISTRATIONS" );
  }

  public String getEditRegattaTooltip() {
    return viewBean.bundle(
      "CLICK TO EDIT REGATTA" );
  }

  public void clickSelectRegatta( Regatta regatta ) {
    theController.clickEditRegatta( regatta,
                                    UI.LIST_PENALTIES );
  }

  public double getPercentage( Penaltiespl p ) {
    return this.theModel.points(
      this.theModel.getPenaltyValue( p,
                                     tracksetLevel )
      + 1
    ) / 100.0;
  }

  public int getNumRegistrations( Penaltiespl p ) {
    return this.theModel.getNumValidRegistrations(
      p.getRegatta()
    );
  }

  public boolean filterByPercent( Object value,
                                  Object filter,
                                  Locale locale ) {
    String filterText = ( filter == null )
                        ? null
                        : filter.toString().trim();
    if( filterText == null || filterText.equals( "" ) ) {
      return true;
    }

    if( value == null ) {
      return false;
    }

    return ( (Comparable) value ).compareTo(
      Double.parseDouble( filterText ) / 100.0 ) > 0;
  }

  public String getPeriodColorBg( Penaltiespl item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = 0x80
                 | getColor(
           theModel.getPeriodId(
             item.getRegatta().getDatetime(),
             item.getId().getLevelPeriod()
           )
         );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;

  }

  public String getTracksetColorBg( Penaltiespl item ) {
    if( item == null ) {
      return "FFFFFF";
    }
    long color = 0x80 | getColor(
         theModel.getIdTrackset(
           item.getRegatta().getVariant(),
           tracksetLevel
         )
       );
    color |= ( color << 16 ) | ( color << 8 );
    String result = String.format( "%06x",
                                   color );
    return result;

  }

  private long getColor( long id ) {
    long color = ( +53 * id ) % 0x7f;
    return color;
  }

}

