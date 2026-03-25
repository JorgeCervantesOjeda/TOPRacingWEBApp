/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.Controller;
import Controller.UI;
import Model.ModelForView;
import Tables.Variant;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;

/**
 *
 * @author usuario
 */
@Named( value = "listVariantsBean" )
@RequestScoped
public class ListVariantsBean {

  @Inject
  private ViewBean viewBean;

  private ModelForView theModel;
  private Controller theController;

  private List<Variant> variants;
  private List<Variant> filteredVariants;

  private Variant selectedVariant;

  private boolean disableSelectVariant;

  /**
   * Creates a new instance of PointscountBean
   */
  public ListVariantsBean() {
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
    variants = theModel.getVariants();
    this.disableSelectVariant = viewBean.getDisableSelectVariant();
  }
  //
  // queries
  //

  public void setVariant( List<Variant> variants ) {
    this.variants = variants;
  }

  public List<Variant> getVariants() {
    return variants;
  }

  public List<Variant> getFilteredVariants() {
    return filteredVariants;
  }

  public void setFilteredVariants( List<Variant> filteredVariant ) {
    this.filteredVariants = filteredVariant;
  }

  ////////////////////////////////////////////////////////
  // Functionality Methods
  ////////////////////////////////////////////////////////
  public long getNumVariants() {
    return variants.size();
  }

  public void clickNewVariant() {
    theController.clickNewVariant();
  }

  public void clickSelectVariant( Variant variant ) {
    theController.clickSelectVariant( variant,
                                      viewBean.getRegatta() );
  }

  public void clickReturn() {
    theController.clickReturn( UI.LIST_VARIANTS );
  }

  public String getCreatorName( long user ) {
    return theModel.getParticipantFullNameById( user );
  }

  public Variant getSelectedVariant() {
    return selectedVariant;
  }

  public void setSelectedVariant( Variant variant ) {
    this.selectedVariant = variant;
  }

  public void rowSelectVariant() {
    theController.clickEditVariant( selectedVariant );
  }

  public boolean disableSelectVariant( Variant variant ) {
    return this.disableSelectVariant;
  }

  public String getDistanceUnits( boolean isMetric ) {
    return isMetric
           ? "km"
           : "mi";
  }

  public String getShortDistanceUnits( boolean isMetric ) {
    return isMetric
           ? "m"
           : "ft";
  }

  public String getVenueName( Variant variant ) {
    return theModel.getVenueName(
      variant.getVenue()
    );
  }

}

