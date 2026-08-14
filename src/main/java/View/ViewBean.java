/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.RegattaStatus;
import Controller.UI;
import Model.ModelForView;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

/**
 *
 * @author usuario
 */
@Named( value = "viewBean" )
@SessionScoped
public class ViewBean
  implements Serializable,
             ViewForController {

  private static final long serialVersionUID = 1L;
  private Participant currentDefaulter;
  private int currentTracksetLevel;
  private int currentPeriodLevel;
  private final String[] language = {
    "View/BundleViewEnglish",
    "View/BundleViewSpanish"
  };
  private double progress;

  public double getProgress() {
    return progress;
  }

  @Override
  public void setProgress( double _progress ) {
    this.progress = _progress;
  }

  private Theme theme;
  private List<Theme> themes;

  @Inject
  private ModelForView modelBean;
  private Controller theController;

  private Participant currentParticipant = null; //
  private Regatta currentRegatta = null;
  private Variant currentVariant = null; //
  private Venue currentVenue = null;
  private Provinceregion currentProvinceregion = null;
  private Province currentProvince = null; //
  private Countryregion currentCountryregion = null;
  private Country currentCountry = null;
  private Planetregion currentPlanetregion = null;
  private Registration currentRegistration = null; //
  private Car currentCar = null;

  private String messageHeader = "";
  private String messageDetail = "";
  private long session;
  private boolean disableSelectVariant;

  private int languageId = 0;

  public int getLanguageId() {
    return languageId;
  }

  public void setLanguageId( int _languageId ) {
    this.languageId = _languageId;
  }

  public void languageIdChanged() {
    return;
  }

  /**
   * Creates a new instance of NewJSFManagedBean
   */
  public ViewBean() {
    themes = new ArrayList<>();
    themes.add( new Theme( 0,
                           "Dark-Hive",
                           "dark-hive" ) );
    themes.add( new Theme( 1,
                           "EggPlant",
                           "eggplant" ) );
    themes.add( new Theme( 2,
                           "Glass-X",
                           "glass-x" ) );
    themes.add( new Theme( 3,
                           "Luna-Amber",
                           "luna-amber" ) );
    theme = new Theme( 2,
                       "Home",
                       "home" );
  }

  @PostConstruct
  public void init() {
    System.out.println(
      new Date() + " !!! " + "---- PostConstruct en ViewBean.java ---------------" );
    this.currentParticipant = createPlaceholderParticipant();
    theController = new Controller( this );
    this.session = theController.newSession( modelBean );
  }

  @PreDestroy
  public void exit() {
    theController.preDestroySession();
  }

  public long getNumSessions() {
    return this.session;
  }

  public void setModelBean( ModelForView model ) {
    this.modelBean = model;
  }

  public ModelForView getModelBean() {
    return modelBean;
  }

  public Controller getController() {
    return theController;
  }

  public void clickLogin() {
    theController.clickLogin( this.currentParticipant );
  }

  public void clickResetPassword() {
    theController.clickResetPasswordRequest( this.currentParticipant );
  }

  public void clickConnectPaypal() {
    ensureCurrentParticipant();
    try {
      String signupLink = modelBean.createPaypalSignupLink( this.currentParticipant );
      FacesContext instance = FacesContext.getCurrentInstance();
      if( instance == null ) {
        return;
      }
      ExternalContext context = instance.getExternalContext();
      context.redirect( signupLink );
      instance.responseComplete();
    } catch( IOException e ) {
      throw new RuntimeException( "Unable to redirect to PayPal onboarding.",
                                  e );
    } catch( RuntimeException e ) {
      showModal( bundle( "ERROR PAYPAL REQUIRED" ),
                 bundle( "ERROR PAYPAL CONFIGURATION LONG" ) );
    }
  }

  public boolean renderPaypalConnectButton() {
    return currentParticipant != null
           && currentParticipant.getId() != null
           && currentParticipant.getId() > 0
           && currentParticipant.isEmailConfirmed()
           && !currentParticipant.isPaypalUsable();
  }

  public boolean disableResetButton() {
    boolean disable =
            this.currentParticipant == null
            || this.currentParticipant.getEmail() == null
            || this.currentParticipant.getEmail().length() <= 0;
    return disable;
  }

  public void clickNewParticipant() {
    theController.clickNewParticipant();
  }

  public void clickEditProfile() {
    theController.clickEditProfile();
  }

  public void clickLogout() {
    theController.clickLogout();
  }

  @Override
  public void invalidateSessionAndShowUI( int ui ) {
    FacesContext instance = FacesContext.getCurrentInstance();
    if( instance == null ) {
      return;
    }

    ExternalContext context = instance.getExternalContext();
    String target = resolveTargetForUi( context,
                                        ui );
    try {
      context.invalidateSession();
      context.redirect( target );
      instance.responseComplete();
    } catch( IOException e ) {
      throw new RuntimeException( "Unable to invalidate session and redirect.", e );
    }
  }

  public void clickOK() {
    theController.clickOK();
  }

  public Participant getCurrentParticipant() {
    ensureCurrentParticipant();
    return this.currentParticipant;
  }

  public void setCurrentParticipant( Participant user ) {
    this.currentParticipant = ( user != null )
                              ? user
                              : createPlaceholderParticipant();
  }

  public void setPassword( String password ) {
    ensureCurrentParticipant();
    this.currentParticipant.setPassword( password );
  }

  public Regatta getRegatta() {
    return this.currentRegatta;
  }

  public Variant getVariant() {
    return this.currentVariant;
  }

  public Venue getVenue() {
    return this.currentVenue;
  }

  public Provinceregion getProvinceregion() {
    return this.currentProvinceregion;
  }

  public Province getProvince() {
    return this.currentProvince;
  }

  public Countryregion getCountryregion() {
    return this.currentCountryregion;
  }

  public Country getCountry() {
    return this.currentCountry;
  }

  public Planetregion getPlanetregion() {
    return this.currentPlanetregion;
  }

  public Registration getCurrentRegistration() {
    return this.currentRegistration;
  }

  void setCurrentRegistration( Registration _registration ) {
    this.currentRegistration = _registration;
  }

  public Car getCar() {
    return this.currentCar;
  }

  public String getEditProfileButton() {
    if( currentParticipant == null
        || currentParticipant.getId() == null
        || currentParticipant.getId() <= 0
        || currentParticipant.getNamesFamily() == null ) {
      return "Log In";
    }

    return currentParticipant.getNamesGiven()
           + " "
           + currentParticipant.getNamesFamily();
  }

  public void clickWelcome() {
    theController.clickViewWelcome();
  }

  public void clickPointscount() {
    theController.clickViewPointscounts();
  }

  public void clickPenalties() {
    theController.clickViewPenalties();
  }

  public void clickRegistrations() {
    theController.clickViewRegistrations();
  }

  @Override
  public void showUI( int ui ) {

//    PrimeFaces.current().executeScript( "PF('dlgWait').hide()" );
    try {
      FacesContext instance = FacesContext.getCurrentInstance();
      ExternalContext context = instance.getExternalContext();
      switch( ui ) {
        case UI.PASSWORD_RESET_REQUEST:
          showModal(
            bundle(
              "PASSWORD RESET REQUEST" ),

            bundle(
              "PASSWORD RESET REQUEST LONG" ) );
          return;
        case UI.ERROR_PASSWORD_RESET_REQUEST:
          showModal(
            bundle(
              "ERROR PASSWORD RESET REQUEST" ),

            bundle(
              "ERROR PASSWORD RESET REQUEST LONG" ) );
          return;
        case UI.LOGIN:
          currentParticipant = createPlaceholderParticipant();
          redirect( context,
                    context.getRequestContextPath() + "/login.xhtml" );
          break;
        case UI.ERROR_LOGIN:
          showModal(
            bundle(
              "ERROR LOGIN" ),

            bundle(
              "ERROR LOGIN LONG" ) );
          return;
        case UI.ERROR_EMAIL_CONFIRMATION_REQUIRED:
          showModal(
            bundle(
              "ERROR EMAIL CONFIRMATION REQUIRED" ),
            bundle(
              "ERROR EMAIL CONFIRMATION REQUIRED LONG" ) );
          return;
        case UI.ERROR_PAYPAL_REQUIRED:
          showModal(
            bundle(
              "ERROR PAYPAL REQUIRED" ),
            bundle(
              "ERROR PAYPAL REQUIRED LONG" ) );
          return;

        case UI.EDIT_USER:
          redirect( context,
                    "editparticipant.xhtml" );
          break;
        case UI.WELCOME:
          redirect( context,
                    "welcome.xhtml" );
          break;

        case UI.LIST_POINTSCOUNTS:
          redirect( context,
                    "listpointscounts.xhtml" );
          break;

        case UI.LIST_REGATTAS:
          redirect( context,
                    "listregattas.xhtml" );
          break;
        case UI.LIST_PENALTIES:
          redirect( context,
                    "listpenalties.xhtml" );
          break;
        case UI.ERROR_REGATTA_NOT_FOUND:
          showModal(
            bundle(
              "ERROR REGATTA NOT FOUND" ),

            bundle(
              "ERROR REGATTA NOT FOUND LONG" ) );
          return;
        case UI.ERROR_REGATTA_NOT_OPEN:
          showModal(
            bundle(
              "ERROR REGATTA NOT OPEN" ),

            bundle(
              "ERROR REGATTA NOT OPEN LONG" ) );
          return;
        case UI.ERROR_LOCAL_PROMOTER_BLOCKED:
          showModal(
            bundle(
              "ERROR LOCAL PROMOTER BLOCKED" ),
            bundle(
              "ERROR LOCAL PROMOTER BLOCKED LONG" ) );
          return;
        case UI.ERROR_REGATTA_PUBLISHED:
          showModal(
            bundle(
              "ERROR REGATTA PUBLISHED" ),
            "" );
          return;
        case UI.ERROR_REGATTA_NOT_OWNED:
          showModal(
            bundle(
              "ERROR REGATTA NOT YOURS" ),
            "" );
          return;
        case UI.ERROR_SAVE_REGATTA:
          showModal(
            bundle(
              "ERROR SAVE REGATTA" ),

            bundle(
              "ERROR SAVE REGATTA LONG" ) );
          return;
        case UI.ERROR_NOT_CAR_OWNER:
          showModal(
            bundle(
              "ERROR CAR NOT YOURS" ),
            "" );
          return;
        case UI.EDIT_REGATTA:
          redirect( context,
                    "editregatta.xhtml" );
          break;
        case UI.VIEW_EDIT_REGATTA_RESULTS:
          redirect( context,
                    "editregattaresults.xhtml" );
          break;
        case UI.REGATTA_STATUS_CHANGED:
          showModal(
            bundle(
              RegattaStatus.NAME[ currentRegatta.getStatus() ] ),
            bundle(
              RegattaStatus.NAME[ currentRegatta.getStatus() ] + " LONG" ) );
          return;

        case UI.LIST_VARIANTS:
          redirect( context,
                    "listvariants.xhtml" );
          break;
        case UI.ERROR_EDIT_VARIANT:
          showModal(
            bundle(
              "ERROR EDIT VARIANT" ),
            "" );
          return;
        case UI.EDIT_VARIANT:
          redirect( context,
                    "editvariant.xhtml" );
          break;

        case UI.LIST_VENUES:
          redirect( context,
                    "listvenues.xhtml" );
          break;
        case UI.ERROR_EDIT_VENUE:
          showModal(
            bundle(
              "ERROR EDIT VENUE" ),
            "" );
          return;
        case UI.EDIT_VENUE:
          redirect( context,
                    "editvenue.xhtml" );
          break;

        case UI.LIST_PROVINCEREGIONS:
          redirect( context,
                    "listprovinceregions.xhtml" );
          break;
        case UI.ERROR_EDIT_PROVINCEREGION:
          showModal(
            bundle(
              "ERROR EDIT PROVINCE REGION" ),
            "" );
          return;
        case UI.EDIT_PROVINCEREGION:
          redirect( context,
                    "editprovinceregion.xhtml" );
          break;

        case UI.LIST_PROVINCES:
          redirect( context,
                    "listprovinces.xhtml" );
          break;
        case UI.ERROR_EDIT_PROVINCE:
          showModal(
            bundle(
              "ERROR EDIT PROVINCE" ),
            "" );
          return;
        case UI.EDIT_PROVINCE:
          redirect( context,
                    "editprovince.xhtml" );
          break;

        case UI.LIST_COUNTRYREGIONS:
          redirect( context,
                    "listcountryregions.xhtml" );
          break;
        case UI.ERROR_EDIT_COUNTRYREGION:
          showModal(
            bundle(
              "ERROR EDIT COUNTRYREGION" ),
            "" );
          return;
        case UI.EDIT_COUNTRYREGION:
          redirect( context,
                    "editcountryregion.xhtml" );
          break;

        case UI.LIST_COUNTRIES:
          redirect( context,
                    "listcountries.xhtml" );
          break;
        case UI.ERROR_EDIT_COUNTRY:
          showModal(
            bundle(
              "ERROR EDIT COUNTRY" ),
            "" );
          return;
        case UI.EDIT_COUNTRY:
          redirect( context,
                    "editcountry.xhtml" );
          break;

        case UI.LIST_PLANETREGIONS:
          redirect( context,
                    "listplanetregions.xhtml" );
          break;
        case UI.ERROR_EDIT_PLANETREGION:
          showModal(
            bundle(
              "ERROR EDIT PLANETREGION" ),
            "" );
          return;
        case UI.EDIT_PLANETREGION:
          redirect( context,
                    "editplanetregion.xhtml" );
          break;

        case UI.LIST_REGISTRATIONS:
          redirect( context,
                    "listregistrations.xhtml" );
          break;
        case UI.EDIT_REGISTRATION:
          redirect( context,
                    "editregistration.xhtml" );
          break;
        case UI.ERROR_CREATE_REGISTRATION_DUPLICATE:
          showModal(
            bundle(
              "ERROR CREATE REGISTRATION DUPLICATE" ),
            "" );
          return;
        case UI.ERROR_EDIT_REGISTRATION_USER:
          showModal(
            bundle(
              "ERROR EDIT REGISTRATION USER" ),
            "" );
          return;
        case UI.ERROR_EDIT_REGISTRATION_CLOSED:
          showModal( "This Registration is already closed.",
                     "" );
          return;

        case UI.LIST_CARS:
          redirect( context,
                    "listcars.xhtml" );
          break;
        case UI.LIST_DRIVERS:
          redirect( context,
                    "listdrivers.xhtml" );
          break;
        case UI.ERROR_EDIT_CAR:
          showModal(
            bundle(
              "ERROR EDIT CAR" ),
            "" );
          return;
        case UI.ERROR_EDIT_USER_PASSWORD:
          showModal(
            bundle(
              "ERROR EDIT USER PASSWORD" ),
            "" );
          return;
        case UI.ERROR_EDIT_USER_EXISTS:
          showModal(
            bundle(
              "ERROR EDIT USER EXISTS" ),
            "" );
          return;
        case UI.EDIT_CAR:
          redirect( context,
                    "editcar.xhtml" );
          break;

        case UI.EDIT_VENUE_IN_MAP:
          redirect( context,
                    "editvenueinmap.xhtml" );
          break;

        case UI.INVALID_SPEED_RESULTS:
          showModal(
            bundle(
              "ERROR INVALID SPEED RESULTS" ),
            "" );
          return;

        case UI.INVALID_RACE_RESULTS:
          showModal(
            bundle(
              "ERROR INVALID RACE RESULTS" ),
            "" );
          return;

      }
      instance.responseComplete();

    } catch( IOException ex ) {
      System.out.println( new Date() + " !!! " + ex );
    }
  }

  @Override
  public void showUI( int ui,
                      Registration registration ) {
    this.currentRegistration = registration;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      boolean flag ) {
    switch( ui ) {
      case UI.LIST_VARIANTS:
        this.disableSelectVariant = flag;
        break;
    }

    showUI( ui );

  }

  private void showModal( String header,
                          String detail ) {

    this.messageHeader = header;
    this.messageDetail = detail;

    PrimeFaces.current()
      .ajax()
      .update( "contentForm:dlgInfo" );
    PrimeFaces.current()
      .executeScript( "PF('dlgWait').hide(); PF('dlgInfo').show();" );

    FacesContext instance = FacesContext.getCurrentInstance();
    ExternalContext context = instance.getExternalContext();


    /*
     * HttpServletRequest req = (HttpServletRequest)
     * FacesContext.getCurrentInstance() .getExternalContext().getRequest();
     * String url = req.getRequestURL() .toString(); String s = url.substring(
     * 0, url.length() - req.getRequestURI().length() ) + req.getContextPath() +
     * "/";
     *
     * fm = new FacesMessage( FacesMessage.SEVERITY_INFO, "Steering:",
     * "<table><tr>" + "<td><img src='" + s + "/resources/images/steering25.jpg'
     * alt ='Steering' height ='50'></td > " + " < td > <h2>" + text + "</h2 > <
     * / td > " + "</tr></table>" );
     *
     *
     *
     * Map<String, Object> options = new HashMap<>(); options.put( "modal", true
     * );
     *
     * RequestContext.getCurrentInstance().openDialog( text, options, null );
     */
  }

  private void redirect( ExternalContext context,
                         String target ) throws IOException {
    String resolvedTarget = resolveNavigationTarget( context,
                                                     target );
    if( target == null || target.isBlank() ) {
      context.redirect( resolvedTarget );
      return;
    }
    context.redirect( resolvedTarget );
  }

  private String resolveNavigationTarget( ExternalContext context,
                                          String target ) {
    if( target == null || target.isBlank() ) {
      return context.getRequestContextPath() + "/faces/welcome.xhtml";
    }
    if( target.contains( "://" ) ) {
      return target;
    }
    if( target.startsWith( context.getRequestContextPath() + "/" ) ) {
      return target;
    }
    if( target.startsWith( "/" ) ) {
      return context.getRequestContextPath() + target;
    }
    return context.getRequestContextPath() + "/faces/" + target;
  }

  private String resolveTargetForUi( ExternalContext context,
                                     int ui ) {
    switch( ui ) {
      case UI.LOGIN:
        return resolveNavigationTarget( context,
                                        "login.xhtml" );
      case UI.WELCOME:
      default:
        return resolveNavigationTarget( context,
                                        "welcome.xhtml" );
    }
  }

  public String getMessage() {
    return messageHeader;
  }

  public void setMessage( String message ) {
    this.messageHeader = message;
  }

  public String getMessageDetail() {
    return messageDetail;
  }

  public void setMessageDetail( String messageDetail ) {
    this.messageDetail = messageDetail;
  }

  public String getMessageHeader() {
    return messageHeader;
  }

  public void setMessageHeader( String messageHeader ) {
    this.messageHeader = messageHeader;
  }

  public String getPleaseWaitMsg() {
    return bundle( "PLEASE WAIT" );
  }

  public String getWelcomeMsg() {
    return bundle( "WELCOME TO TOP-RACING" );
  }

  public String getWhatIsTOPRacing() {
    return bundle( "WHAT IS TOP RACING" );
  }

  public String getWhatIsP1() {
    return bundle( "WHAT IS P1" );
  }

  public String getWhatIsP2() {
    return bundle( "WHAT IS P2" );
  }

  public String getWhatIsP3() {
    return bundle( "WHAT IS P3" );
  }

  public String getWhatIsP4() {
    return bundle( "WHAT IS P4" );
  }

  public String getWhatIsP5() {
    return bundle( "WHAT IS P5" );
  }

  public String getTOPRacingFeatures() {
    return bundle( "TOP RACING FEATURES" );
  }

  public String getGeneralFeatures() {
    return bundle( "GENERAL FEATURES" );
  }

  public String getGeneralFeature1() {
    return bundle( "GENERAL FEATURE 1" );
  }

  public String getGeneralFeature2() {
    return bundle( "GENERAL FEATURE 2" );
  }

  public String getGeneralFeature3() {
    return bundle( "GENERAL FEATURE 3" );
  }

  public String getGeneralFeature4() {
    return bundle( "GENERAL FEATURE 4" );
  }

  public String getGeneralFeature5() {
    return bundle( "GENERAL FEATURE 5" );
  }

  public String getGeneralFeature6() {
    return bundle( "GENERAL FEATURE 6" );
  }

  public String getGeneralFeature7() {
    return bundle( "GENERAL FEATURE 7" );
  }

  public String getGeneralFeature8() {
    return bundle( "GENERAL FEATURE 8" );
  }

  public String getGeneralFeature9() {
    return bundle( "GENERAL FEATURE 9" );
  }

  public String getGeneralFeature10() {
    return bundle( "GENERAL FEATURE 10" );
  }

  public String getWebsiteFeatures() {
    return bundle( "WEBSITE FEATURES" );
  }

  public String getWebsiteFeature1() {
    return bundle( "WEBSITE FEATURE 1" );
  }

  public String getWebsiteFeature2() {
    return bundle( "WEBSITE FEATURE 2" );
  }

  public String getWebsiteFeature3() {
    return bundle( "WEBSITE FEATURE 3" );
  }

  public String getWebsiteFeature4() {
    return bundle( "WEBSITE FEATURE 4" );
  }

  public String getWebsiteFeature5() {
    return bundle( "WEBSITE FEATURE 5" );
  }

  public String getWebsiteFeature6() {
    return bundle( "WEBSITE FEATURE 6" );
  }

  public String getWebsiteFeature7() {
    return bundle( "WEBSITE FEATURE 7" );
  }

  public String getWebsiteFeature8() {
    return bundle( "WEBSITE FEATURE 8" );
  }

  public String getTOPRacingFAQ() {
    return bundle( "TOP RACING FAQ" );
  }

  public String getChampionshipLevels() {
    return bundle( "CHAMPIONSHIP LEVELS" );
  }

  public String getTerritorialHierarchy() {
    return bundle( "TERRITORIAL HIERARCHY" );
  }

  public String getTerritorialHierarchyLong() {
    return bundle( "TERRITORIAL HIERARCHY LONG" );
  }

  public String getTemporalHierarchy() {
    return bundle( "TEMPORAL HIERARCHY" );
  }

  public String getTemporalHierarchyLong() {
    return bundle( "TEMPORAL HIERARCHY LONG" );
  }

  public String getTOPRacingVenues() {
    return bundle( "TOP RACING VENUES" );
  }

  public String getEnjoyMsg() {
    return bundle( "ENJOY" );
  }

  public String getMaintenanceMsg() {
    return bundle( "TOP RACING MAINTAINED AT" );
  }

  public String getLogin1Msg() {
    return bundle( "LOGIN1" );
  }

  public String getLogin2Msg() {
    return bundle( "LOGIN2" );
  }

  public String getParticipantNumberLabel() {
    return bundle( "USER NUMBER LABEL" );
  }

  public String getParticipantNumberTooltip() {
    return bundle( "USER NUMBER TOOLTIP" );
  }

  public String getPasswordLabel() {
    return bundle( "PASSWORD LABEL" );
  }

  public String getLoginButtonTxt() {
    return bundle( "LOGIN BUTTON" );
  }

  public String getNewParticipantButtonTxt() {
    return bundle( "NEW USER BUTTON" );
  }

  public String getConnectPaypalButtonLabel() {
    return bundle( "CONNECT PAYPAL" );
  }

  public String getWelcomeButtonTitle() {
    return bundle( "WELCOME BUTTON" );
  }

  public String getPointscountButtonTitle() {
    return bundle( "STANDINGS BUTTON" );
  }

  public String getRegattaButtonTitle() {
    return bundle( "REGATTAS BUTTON" );
  }

  public String getPenaltiesButtonTitle() {
    return bundle( "PENALTIES BUTTON" );
  }

  public String getRegistrationButtonTitle() {
    return bundle( "REGISTRATIONS BUTTON" );
  }

  public String getCarButtonTitle() {
    return bundle( "CARS BUTTON" );
  }

  public String getDriverButtonTitle() {
    return bundle( "DRIVERS BUTTON" );
  }

  public String getForumsLinkTxt() {
    return bundle( "FORUMS LINK" );
  }

  public String getNewsLinkTxt() {
    return bundle( "NEWS LINK" );
  }

  public String getParticipantLabel() {
    return bundle( "USER LABEL" );
  }

  public String getContactLabel() {
    return bundle( "CONTACT LABEL" );
  }

  public String getLogoutButtonTitle() {
    return bundle( "LOGOUT TITLE" );
  }

  public String getProfileButtonTitle() {
    if( currentParticipant == null
        || currentParticipant.getId() == null
        || currentParticipant.getId() <= 0
        || currentParticipant.getEmail() == null ) {
      return "Click here to log in.";
    }
    return bundle( "PROFILE TITLE" );
  }

  public boolean renderLogoutButton() {
    if( currentParticipant == null
        || currentParticipant.getId() == null
        || currentParticipant.getId() <= 0
        || currentParticipant.getEmail() == null ) {
      return false;
    }
    return true;
  }

  public String getListEmptyMsg() {
    return bundle( "LIST EMPTY" );
  }

  public String getSearchAllFieldsLabel() {
    return bundle( "SEARCH ALL FIELDS" );
  }

  public String getEnterKeywordPlaceholder() {
    return bundle( "ENTER KEYWORD" );
  }

  public String getReturnButtonTitle() {
    return bundle( "RETURN BUTTON" );
  }

  public String getWhatIs() {
    return bundle( "WHAT IS" );
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme( Theme theme ) {
    this.theme = theme;
  }

  public List<Theme> getThemes() {
    return themes;
  }

  public void setThemes( List<Theme> themes ) {
    this.themes = themes;
  }

  @Override
  public void showUI( int ui,
                      Participant u ) {
    setCurrentParticipant( u );
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Regatta regatta ) {
    this.currentRegatta = regatta;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Car car ) {
    this.currentCar = car;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Variant variant ) {
    this.currentVariant = variant;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Venue venue ) {
    this.currentVenue = venue;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Provinceregion provinceregion ) {
    this.currentProvinceregion = provinceregion;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Province province ) {
    this.currentProvince = province;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Countryregion countryregion ) {
    this.currentCountryregion = countryregion;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Country country ) {
    this.currentCountry = country;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      Planetregion planetregion ) {
    this.currentPlanetregion = planetregion;
    showUI( ui );
  }

  @Override
  public void showUI( int ui,
                      int tracksetLevel,
                      int periodLevel ) {
    this.currentTracksetLevel = tracksetLevel;
    this.currentPeriodLevel = periodLevel;

    showUI( ui );
  }

  void setDefaulter( Participant _defaulter ) {
    this.currentDefaulter = _defaulter;
  }

  boolean getDisableSelectVariant() {
    return this.disableSelectVariant;
  }

  int getPeriodLevel() {
    return currentPeriodLevel;
  }

  int getTracksetLevel() {
    return currentTracksetLevel;
  }

  @Override
  public String bundle( String messageId ) {
    return java.util.ResourceBundle
      .getBundle( this.language[ languageId ] )
      .getString( messageId );
  }

  private void ensureCurrentParticipant() {
    if( this.currentParticipant == null ) {
      this.currentParticipant = createPlaceholderParticipant();
    }
  }

  private Participant createPlaceholderParticipant() {
    Participant participant = new Participant();
    participant.setId( 0L );
    participant.setEmail( "" );
    participant.setPassword( "" );
    return participant;
  }

}


