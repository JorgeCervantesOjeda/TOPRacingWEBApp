package View;

import Tables.Car;
import Tables.Country;
import Tables.Countryregion;
import Tables.Currency;
import Tables.Participant;
import Tables.Planetregion;
import Tables.Province;
import Tables.Provinceregion;
import Tables.Regatta;
import Tables.Registration;
import Tables.Variant;
import Tables.Venue;
import java.util.Date;

final class PlaceholderFactory {

  private PlaceholderFactory() {
  }

  static Participant participant() {
    Participant participant = new Participant();
    participant.setId( 0L );
    participant.setNamesGiven( "" );
    participant.setNamesFamily( "" );
    participant.setDefaulter( 0 );
    return participant;
  }

  static Planetregion planetregion() {
    Planetregion planetregion = new Planetregion();
    planetregion.setId( 0L );
    planetregion.setIdCreator( 0L );
    planetregion.setName( "" );
    return planetregion;
  }

  static Country country() {
    Country country = new Country();
    country.setId( 0L );
    country.setIdCreator( 0L );
    country.setName( "" );
    country.setPlanetregion( planetregion() );
    return country;
  }

  static Countryregion countryregion() {
    Countryregion countryregion = new Countryregion();
    countryregion.setId( 0L );
    countryregion.setIdCreator( 0L );
    countryregion.setName( "" );
    countryregion.setCountry( country() );
    return countryregion;
  }

  static Province province() {
    Province province = new Province();
    province.setId( 0L );
    province.setIdCreator( 0L );
    province.setName( "" );
    province.setCountryregion( countryregion() );
    return province;
  }

  static Provinceregion provinceregion() {
    Provinceregion provinceregion = new Provinceregion();
    provinceregion.setId( 0L );
    provinceregion.setIdCreator( 0L );
    provinceregion.setName( "" );
    provinceregion.setProvince( province() );
    return provinceregion;
  }

  static Venue venue() {
    Venue venue = new Venue();
    venue.setId( 0L );
    venue.setIdCreator( 0L );
    venue.setName( "" );
    venue.setParallel( 0.0 );
    venue.setMeridian( 0.0 );
    venue.setParticipant( participant() );
    venue.setProvinceregion( provinceregion() );
    return venue;
  }

  static Variant variant() {
    Variant variant = new Variant();
    variant.setId( 0L );
    variant.setIdCreator( 0L );
    variant.setName( "" );
    variant.setMetric( true );
    variant.setVenue( venue() );
    return variant;
  }

  static Currency currency() {
    Currency currency = new Currency();
        currency.setId( 0 );
    currency.setCode( "USD" );
    currency.setSymbol( "$" );
    return currency;
  }

  static Car car() {
    Car car = new Car();
    car.setId( 0L );
    car.setNickname( "" );
    car.setDescription( "" );
    car.setParticipant( participant() );
    return car;
  }

  static Regatta regatta() {
    Regatta regatta = new Regatta();
    regatta.setId( 0L );
    regatta.setParticipant( participant() );
    regatta.setVariant( variant() );
    regatta.setCurrency( currency() );
    regatta.setDatetime( new Date() );
    regatta.setLevelPeriod( 0 );
    regatta.setLevelTrackset( 0 );
        regatta.setStatus( (byte) 0 );
    return regatta;
  }

  static Registration registration() {
    Registration registration = new Registration();
    registration.setId( 0L );
        registration.setStatus( (byte) 0 );
    registration.setRegatta( regatta() );
    registration.setCar( car() );
    registration.setParticipantByIdDriver( participant() );
    registration.setParticipantByIdOwner( participant() );
    registration.setParticipantByIdBuyer( participant() );
    registration.setBetFinishing( 0 );
    registration.setBetEfficiency( 0 );
    return registration;
  }
}
