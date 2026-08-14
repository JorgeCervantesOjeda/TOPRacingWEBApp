/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

;
import Tables.Bid;
import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Currency;
import Tables.Participant;
import Tables.Penaltiespl;
import Tables.Planetregion;
import Tables.Pointscount;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import java.util.Date;
import java.util.List;

/**
 *
 * @author usuario
 */


public interface ModelForView {

  public List<Bid> getBids( Participant _currentParticipant,
                            Regatta _regatta );

  public List<Car> getCars( Participant _currentParticipant );

  public List<Currency> getCurrencies();

  public double getExpectedLaps( Registration _r );

  public List<Participant> getParticipants();

  public List<Penaltiespl> getPenaltiesplForPL( int _periodLevel );

  public int getPenaltyValue( Penaltiespl _a,
                              int _tracksetLevel );

  public List<Penaltiespl> getRegattaPeriodlevelPenaltiesList(
    Regatta _regatta );

  public List<Regatta> getRegattas();

  public Variant getVariantById( long _variantId );

  public String getTimeZone();

  public int getNumValidRegistrations( Regatta _regatta );

  public double getRegattaPriorityPoints( Regatta _regatta );

  public Participant getParticipantByEmail( Participant _user );

  public Participant getParticipantById( Participant _user );

  public boolean hasActiveGlobalExclusion( Participant participant );

  public boolean hasActiveLocalPromoterBlock( Participant participant,
                                              Participant promoter );

  public Car getCarById( long _carId );

  public Country getCountryById( long _countryId );

  public Registration getRegistrationById( long _registrationId );

  public Regatta getRegattaById( long _regattaId );

  public List<Registration> getRegattaRegistrations( Regatta regatta );

  public Planetregion getPlanetregionById( long _planetregionId );

  public Countryregion getCountryregionById( long _countryregionId );

  public Province getProvinceById( long _provinceId );

  public Provinceregion getProvinceregionById( long _provinceregionId );

  public Venue getVenueById( long _venueId );

  public List<Country> getCountries();

  public List<Registration> getRegistrations();

  public List<Planetregion> getPlanetregions();

  public List<Countryregion> getCountryregions();

  public List<Pointscount> getPointscounts( int periodLevel,
                                            int tracksetLevel );

  public String getVariantName( Variant variant );

  public String getVenueName( Venue venue );

  public String getProvinceregionName( Provinceregion provinceregion );

  public String getProvinceName( Province province );

  public String getCountryregionName( Countryregion countryregion );

  public String getCountryName( Country country );

  public String getPlanetregionName( Planetregion planetregion );

  public String getPeriodName( int _periodlevel,
                               long _periodid );

  public long getPeriodId( Date date,
                           int level );

  public List<Provinceregion> getProvinceregions();

  public List<Province> getProvinces();

  public List<Variant> getVariants();

  public List<Venue> getVenues();

  public String getParticipantFullNameById( long participantNumber );

  public String getParticipantFullName( Participant participant );

  public long getIdTrackset( Variant variant,
                             int tracksetLevel );

  public double points( long pos );

  public String getTracksetName( int tsl,
                                 long idTrackset );

}

