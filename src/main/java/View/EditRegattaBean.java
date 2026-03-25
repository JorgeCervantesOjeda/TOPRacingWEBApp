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
import Tables.Currency;
import Tables.Penaltiespl;
import Tables.Regatta;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "editRegattaBean" )
@RequestScoped
public class EditRegattaBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;
  private List<Penaltiespl> levelPeriodPenaltiesList = null;
  private Regatta regatta;
  private String periodLevelStr;
  private String tracksetLevelStr;
  private List<Currency> currencies;

  public List<Penaltiespl> getLevelPeriodPenaltiesList() {
    return levelPeriodPenaltiesList;
  }

  public void setPeriodPenaltyList(
    List<Penaltiespl> _periodPenaltyList ) {
    this.levelPeriodPenaltiesList = _periodPenaltyList;
  }

  /**
   * Creates a new instance of RegistrationBean
   */
  public EditRegattaBean() {
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
    this.regatta = viewBean.getRegatta();
    if( this.regatta == null ) {
      this.regatta = PlaceholderFactory.regatta();
    }
    this.periodLevelStr = LevelPeriod.NAME[ regatta.getLevelPeriod() ];
    this.tracksetLevelStr = LevelTrackset.NAME[ regatta.getLevelTrackset() ];
    this.levelPeriodPenaltiesList =
    theModel.getRegattaPeriodlevelPenaltiesList( this.regatta );
    setCurrencies( theModel.getCurrencies() );
  }

  public Regatta getRegatta() {
    return this.regatta;
  }

  public void setRegatta( Regatta regatta ) {
    this.regatta = regatta;
  }

  public String getPromoterName() {
    return theModel.getParticipantFullName( this.regatta.getParticipant() );
  }

  public String getTimeZone() {
    return theModel.getTimeZone();
  }

  public String getStatusName() {
    return RegattaStatus.NAME[ this.regatta.getStatus() ];
  }

  public String getStatusLong() {
    return viewBean.bundle(
      RegattaStatus.NAME[ this.regatta.getStatus() ]
      + " LONG" );
  }

  public String getNextStatusName() {
    return RegattaStatus.NAME[ this.regatta.getStatus() + 1 ];
  }

  public String getVariantName() {
    return theModel.getVariantName( regatta.getVariant() );
  }

  public boolean getDisableEditVariantField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditMaxweightField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditMinloadField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditDatetimeField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditDateField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditRegistrationCostField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditTrackRentalField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditFinishingPrizeField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditEfficiencyPrizeField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableEditMaxQualifiersField() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableAddToFinishingPrizeButton() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableAddToEfficiencyPrizeButton() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableSaveButton() {
    return !Objects.equals(
      this.regatta.getParticipant()
        .getId(),
      viewBean.getCurrentParticipant()
        .getId() )
           || this.regatta.getStatus() != RegattaStatus.CREATED;
  }

  public boolean getDisableViewEditRegistrationsButton() {
    return false;
  }

  public boolean getDisableSetStatusButton() {
    return viewBean.getCurrentParticipant() == null
           || !Objects.equals(
        this.regatta.getParticipant()
          .getId(),
        viewBean.getCurrentParticipant()
          .getId() )
           || this.regatta.getStatus() >= RegattaStatus.PUBLISHED;
  }

  /*
   * user actions
   */
  public void clickViewVariants() {
    theController.clickViewVariants( this.regatta );
  }

  public void clickAddToFinishingPrize() {
    theController.clickAddToFinishingPrize( this.regatta );
  }

  public void clickAddToEfficiencyPrize() {
    theController.clickAddToEfficiencyPrize( this.regatta );
  }

  public void clickSave() {
    theController.clickSave( this.regatta );
  }

  public void clickEndEdit() {
    theController.clickEndEdit( UI.EDIT_REGATTA );
  }

  public void clickSetStatusToNextStatus() {
    theController.clickSetRegattaStatusToNextStatus( this.regatta );
  }

  public void clickViewEditRegistrations() {
    theController.clickViewEditRegattaResults( this.regatta );
  }

  public void clickPreviousRegatta() {
    theController.clickViewPreviousRegatta( this.regatta.getId() );
  }

  public void clickNextRegatta() {
    theController.clickViewNextRegatta( this.regatta.getId() );
  }

  public String getPeriodLevelName( int level ) {
    return LevelPeriod.NAME[ level ];
  }

  public String getTracksetLevelName( int level ) {
    return LevelTrackset.NAME[ level ];
  }

  public String getPeriodName() {

    return theModel.getPeriodName(
      LevelPeriod.WEEK_OF_MONTH,
      theModel.getPeriodId( regatta.getDatetime(),
                            LevelPeriod.WEEK_OF_MONTH )
    );
  }

  public String[] getPeriodLevelNames() {
    return LevelPeriod.NAME;
  }

  public String[] getTracksetLevelNames() {
    return LevelTrackset.NAME;
  }

  public String getPeriodLevelStr() {
    return periodLevelStr;
  }

  public void setPeriodLevelStr( String _periodLevelStr ) {
    this.periodLevelStr = _periodLevelStr;
    for( int i = 0;
         i < LevelPeriod.NAME.length;
         i++ ) {
      if( LevelPeriod.NAME[ i ].equals( periodLevelStr ) ) {
        regatta.setLevelPeriod( i );
      }
    }
  }

  public String getTracksetLevelStr() {
    return tracksetLevelStr;
  }

  public void setTracksetLevelStr( String _tracksetLevelStr ) {
    this.tracksetLevelStr = _tracksetLevelStr;
    for( int i = 0;
         i < LevelTrackset.NAME.length;
         i++ ) {
      if( LevelTrackset.NAME[ i ].equals( tracksetLevelStr ) ) {
        regatta.setLevelTrackset( i );
      }
    }
  }

  public double getPercentage( long p ) {
    return this.theModel.points( p + 1 ) / 100.0;
  }

  public double getPriorityPoints() {
    return theModel.getRegattaPriorityPoints( regatta );
  }

  public String getRegattaCurrency() {
    if( regatta.getCurrency() == null ) {
      return null;
    }
    return regatta.getCurrency().getCode();
  }

  public void setRegattaCurrency( String currency ) {
    for( Currency c
         : currencies ) {
      if( c.getCode().equals( currency ) ) {
        regatta.setCurrency( c );
      }
    }
  }

  public List<String> getCurrencies() {
    List<String> currenciesStr = new ArrayList<>();
    for( Currency c
         : currencies ) {
      currenciesStr.add( c.getCode() );
    }
    return currenciesStr;
  }

  public void currencyChanged() {

  }

  /**
   * @param currencies the currencies to set
   */
  public void setCurrencies( List<Currency> currencies ) {
    this.currencies = currencies;
  }

  public Date getToday() {
    Date d = new Date();
    return d;
  }

}

