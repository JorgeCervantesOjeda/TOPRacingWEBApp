// scripts/venue-geography-model.mjs
// Resolves real venue geography and practical clusters from cached geocoder rows.

const COUNTRY_REGION_BY_COUNTRY_AND_PROVINCE = new Map(
  [
    [ "France", {
      "Auvergne-Rhone-Alpes": "Southeastern France",
      "Bourgogne-Franche-Comte": "Northern & Eastern France",
      "Bretagne": "Western France",
      "Centre-Val de Loire": "Western France",
      "Corse": "Southeastern France",
      "Grand-Est": "Northern & Eastern France",
      "Hauts-de-France": "Northern & Eastern France",
      "Ile-de-France": "Paris Region",
      "Normandie": "Western France",
      "Nouvelle-Aquitaine": "Southwestern France",
      "Occitanie": "Southwestern France",
      "Pays-de-la-Loire": "Western France",
      "Provence-Alpes-Cote-dAzur": "Southeastern France",
    } ],
    [ "Italy", {
      "Abruzzo": "South Italy",
      "Basilicata": "South Italy",
      "Calabria": "South Italy",
      "Campania": "South Italy",
      "Emilia-Romagna": "Northeast Italy",
      "Friuli Venezia Giulia": "Northeast Italy",
      "Lazio": "Central Italy",
      "Liguria": "Northwest Italy",
      "Lombardia": "Northwest Italy",
      "Marche": "Central Italy",
      "Molise": "South Italy",
      "Piemonte": "Northwest Italy",
      "Puglia": "South Italy",
      "Sardegna": "Insular Italy",
      "Sicilia": "Insular Italy",
      "Toscana": "Central Italy",
      "Trentino-Sudtirol": "Northeast Italy",
      "Umbria": "Central Italy",
      "Veneto": "Northeast Italy",
    } ],
    [ "Brazil", regionMap( {
      "North Brazil": [ "Acre", "Para", "Rondonia", "Tocantins" ],
      "Northeast Brazil": [ "Bahia", "Maranhao", "Paraiba", "Pernambuco", "Sergipe" ],
      "Central-West Brazil": [ "Distrito Federal", "Goias", "Mato Grosso", "Mato Grosso do Sul" ],
      "South Brazil": [ "Parana", "Rio Grande do Sul", "Santa Catarina" ],
      "Southeast Brazil": [ "Espirito Santo", "Minas Gerais", "Rio de Janeiro", "Sao Paulo" ],
    } ) ],
    [ "United States of America", regionMap( {
      "West Coast": [ "California", "Hawaii", "Oregon", "Washington" ],
      "Mountain West": [ "Colorado", "Idaho", "Nevada", "Utah" ],
      "Southwest": [ "Arizona", "New Mexico" ],
      "Midwest": [ "Illinois", "Indiana", "Iowa", "Kansas", "Michigan", "Minnesota", "Missouri", "Ohio", "Wisconsin" ],
      "Southeast": [ "Alabama", "Georgia", "Kentucky", "Louisiana", "Mississippi", "North Carolina", "Oklahoma", "South Carolina", "Tennessee", "Virginia", "West Virginia" ],
      "Northeast": [ "Maryland", "Massachusetts", "New Hampshire", "New Jersey", "New York", "Pennsylvania" ],
      "Texas": [ "Texas" ],
      "Florida": [ "Florida" ],
    } ) ],
    [ "Mexico", regionMap( {
      "Northwest Mexico": [ "Chihuahua", "Sonora" ],
      "Northeast Mexico": [ "Coahuila de Zaragoza", "Nuevo Leon" ],
      "Western Mexico": [ "Jalisco" ],
      "Central Mexico": [ "Aguascalientes", "Ciudad de Mexico", "Guanajuato", "Hidalgo", "Mexico", "Morelos", "Puebla", "Queretaro", "San Luis Potosi", "Tlaxcala", "centro" ],
      "Gulf & Southeast Mexico": [ "Chiapas", "Quintana Roo", "Tabasco", "Veracruz de Ignacio de la Llave" ],
    } ) ],
    [ "Spain", regionMap( {
      "North Spain": [ "A Coruna", "Asturias", "Bizkaia", "Cantabria", "Gipuzkoa", "La Rioja", "Lugo", "Navarra", "Pontevedra", "Teruel", "Zaragoza" ],
      "Central Spain": [ "Albacete", "Badajoz", "Burgos", "Caceres", "Guadalajara", "Leon", "Madrid", "Salamanca", "Toledo", "Valladolid" ],
      "East Spain": [ "Alicante", "Barcelona", "Castellon", "Girona", "Lleida", "Tarragona", "Valencia" ],
      "South Spain": [ "Almeria", "Cadiz", "Cordoba", "Granada", "Huelva", "Malaga", "Murcia", "Sevilla" ],
      "Canary Islands": [ "Las Palmas", "Santa Cruz de Tenerife" ],
      "Balearic Islands": [ "Balearic Islands" ],
    } ) ],
    [ "Argentina", regionMap( {
      "Buenos Aires Region": [ "Buenos Aires" ],
      "Central Argentina": [ "Cordoba" ],
      "Cuyo": [ "Mendoza", "San Juan", "San Luis" ],
      "Litoral": [ "Entre Rios", "Formosa", "Misiones", "Santa Fe" ],
      "Northwest Argentina": [ "La Rioja", "Salta" ],
      "Patagonia": [ "Chubut", "Neuquen", "Rio Negro", "Santa Cruz", "Tierra del Fuego" ],
    } ) ],
    [ "Germany", regionMap( {
      "Northern Germany": [ "Mecklenburg-Vorpommern", "Niedersachsen", "Schleswig-Holstein" ],
      "Western Germany": [ "Hessen", "Nordrhein-Westfalen", "Rheinland-Pfalz", "Saarland" ],
      "Southern Germany": [ "Baden-Wurttemberg", "Bayern" ],
      "Eastern Germany": [ "Brandenburg", "Sachsen", "Sachsen-Anhalt", "Thuringen" ],
    } ) ],
    [ "Australia", regionMap( {
      "Eastern Australia": [ "Australian Capital Territory", "New South Wales", "Queensland" ],
      "Northern Australia": [ "Northern Territory" ],
      "Southern Australia": [ "South Australia", "Tasmania", "Victoria" ],
      "Western Australia": [ "Western Australia" ],
    } ) ],
    [ "Japan", regionMap( {
      "Kanto": [ "Chiba", "Ibaraki", "Saitama", "Tochigi", "Tokyo" ],
      "Chubu": [ "Aichi", "Mie", "Nagano", "Shizuoka", "Yamanashi" ],
      "Kansai": [ "Hyogo", "Kyoto", "Nara", "Osaka", "Shiga" ],
      "Chugoku-Shikoku": [ "Hiroshima", "Kagawa", "Okayama", "Tokushima", "Yamaguchi" ],
      "Kyushu": [ "Kagoshima", "Kumamoto", "Oita", "Saga" ],
      "Tohoku": [ "Miyagi" ],
    } ) ],
    [ "Indonesia", regionMap( {
      "Java": [ "Banten", "Jawa Barat" ],
    } ) ],
    [ "Netherlands (Kingdom of the)", regionMap( {
      "Northern & Central Netherlands": [ "Drenthe", "Flevoland", "Gelderland", "Utrecht", "Zuid-Holland" ],
      "Southern Netherlands": [ "Limburg", "Noord-Brabant" ],
    } ) ],
    [ "Canada", regionMap( {
      "Western Canada": [ "Alberta", "British Columbia" ],
      "Central Canada": [ "Ontario", "Quebec" ],
      "Atlantic Canada": [ "Nova Scotia" ],
    } ) ],
    [ "United Kingdom of Great Britain and Northern Ireland (the)", regionMap( {
      "United Kingdom": [ "England", "Scotland", "Wales [Cymru GB-CYM]", "Northern Ireland", "Guernsey", "Isle of Man" ],
    } ) ],
    [ "Asian Russia", regionMap( { "Siberia": [ "Novosibirskaya oblast'", "Omskaya oblast'" ] } ) ],
    [ "Western Russia", regionMap( {
      "Central Western Russia": [ "Moskovskaya oblast'" ],
      "Northwestern Russia": [ "Kaliningradskaya oblast'", "Leningradskaja oblast'" ],
      "Southern Russia": [ "Krasnodarskiy kray" ],
      "Volga Russia": [ "Tatarstan, Respublika" ],
    } ) ],
  ].flatMap( ( [ country, regions ] ) => Object.entries( regions )
    .map( ( [ province, countryRegion ] ) => [
      keyFor( country,
              province ),
      countryRegion,
    ] ) )
);

const PLANET_REGION_BY_COUNTRY = new Map(
  Object.entries( {
    "North America": [ "Canada", "Mexico", "United States of America" ],
    "Caribbean, Central America & Northern Andes": [ "Aruba", "Colombia", "Costa Rica", "Cuba", "Dominican Republic (the)", "Ecuador", "El Salvador", "Guatemala", "Haiti", "Honduras", "Jamaica", "Panama", "Puerto Rico", "Venezuela (Bolivarian Republic of)" ],
    "South America": [ "Argentina", "Bolivia (Plurinational State of)", "Brazil", "Chile", "French Guiana", "Guyana", "Paraguay", "Peru", "Uruguay" ],
    "Europe": [ "Algeria", "Austria", "Belgium", "Czechia", "Denmark", "Estonia", "Finland", "France", "Germany", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Morocco", "Netherlands (Kingdom of the)", "Norway", "Portugal", "Spain", "Sweden", "Switzerland", "Tunisia", "United Kingdom of Great Britain and Northern Ireland (the)" ],
    "Eastern Europe": [ "Albania", "Belarus", "Bulgaria", "Croatia", "Greece", "Hungary", "Poland", "Romania", "Serbia", "Slovakia", "Turkiye", "Ukraine", "Western Russia" ],
    "Middle East": [ "Armenia", "Bahrain", "Cyprus", "Egypt", "Georgia", "Iran (Islamic Republic of)", "Jordan", "Kuwait", "Lebanon", "Oman", "Qatar", "Saudi Arabia", "United Arab Emirates (the)" ],
    "Central Asia": [ "Asian Russia", "Kazakhstan" ],
    "South & Southeast Asia": [ "Brunei Darussalam", "Cambodia", "India", "Indonesia", "Malaysia", "Pakistan", "Philippines (the)", "Singapore", "Sri Lanka", "Thailand" ],
    "East Asia": [ "China", "Japan", "Korea (the Republic of)", "Taiwan Sheng (see also separate country code entry under TW)" ],
    "Oceania": [ "Australia", "New Zealand" ],
    "Sub-Saharan Africa": [ "Angola", "Madagascar", "South Africa", "Togo", "Zimbabwe" ],
  } ).flatMap( ( [ region, countries ] ) => countries.map( ( country ) => [
    normalizeKey( country ),
    region,
  ] ) )
);

const PROVINCE_FROM_GEOCODER_PROVINCE = new Set( [
  "Belgium",
  "Indonesia",
  "Philippines (the)",
] );

const SPANISH_PROVINCE_BY_LOCAL_UNIT = new Map( Object.entries( {
  "Adeje": "Santa Cruz de Tenerife",
  "Alacala del Jucar": "Albacete",
  "Alcala del Rio": "Sevilla",
  "Albaida": "Valencia",
  "Albacete": "Albacete",
  "Alcala del Jucar": "Albacete",
  "Alcarras": "Lleida",
  "Alcaniz": "Teruel",
  "Alicante": "Alicante",
  "Alhama de Murcia": "Murcia",
  "Arona": "Santa Cruz de Tenerife",
  "As Pontes de Garcia Rodriguez": "A Coruna",
  "Bellcaire d'Emporda": "Girona",
  "Blanes": "Girona",
  "Cabanas Raras": "Leon",
  "Cabanillas del Campo": "Guadalajara",
  "Caceres": "Caceres",
  "Calvia": "Balearic Islands",
  "Campillos": "Malaga",
  "Cardedeu": "Barcelona",
  "Cartaya": "Huelva",
  "Casinos": "Valencia",
  "Castelloli": "Barcelona",
  "Cheste": "Valencia",
  "Chinchilla de Monte Aragon": "Albacete",
  "Chiva": "Valencia",
  "Churriana": "Malaga",
  "Cistierna": "Leon",
  "Conil de la Frontera": "Cadiz",
  "El Ejido": "Almeria",
  "El Vendrell": "Tarragona",
  "Empuriabrava": "Girona",
  "Estepona": "Malaga",
  "Finestrat": "Alicante",
  "Fortuna": "Murcia",
  "Garrucha": "Almeria",
  "Guadassuar": "Valencia",
  "Guenes": "Bizkaia",
  "Jerez de la Frontera": "Cadiz",
  "Jumilla": "Murcia",
  "La Laguna": "Santa Cruz de Tenerife",
  "La Puebla de Cazalla": "Sevilla",
  "Leganes": "Madrid",
  "Llica de Vall": "Barcelona",
  "Logrono": "La Rioja",
  "Los Alcazares": "Murcia",
  "Los Arcos": "Navarra",
  "Los Santos de la Humosa": "Madrid",
  "Martinamor": "Salamanca",
  "Massanassa": "Valencia",
  "Norte": "Sevilla",
  "Ocana": "Toledo",
  "Olaberria": "Gipuzkoa",
  "Oliva": "Valencia",
  "Olivenza": "Badajoz",
  "Oropesa del Mar": "Castellon",
  "Outeiro de Rei": "Lugo",
  "Palamos": "Girona",
  "Parres": "Asturias",
  "Pedrezuela": "Madrid",
  "Porrino": "Pontevedra",
  "Pozuelo del Paramo": "Leon",
  "Puerto del Rosario": "Las Palmas",
  "Recas": "Toledo",
  "Regencos": "Girona",
  "Ribera d'Urgellet": "Lleida",
  "Roquetas de Mar": "Almeria",
  "Rosario, El": "Santa Cruz de Tenerife",
  "Roses": "Girona",
  "Sallent": "Barcelona",
  "Salou": "Tarragona",
  "San Bartolome": "Las Palmas",
  "San Bartolome de Tirajana": "Las Palmas",
  "San Fernando de Henares": "Madrid",
  "Sant Antoni de Portmany": "Balearic Islands",
  "Sant Llorenc des Cardassar": "Balearic Islands",
  "Santafe": "Granada",
  "Santa Margalida": "Balearic Islands",
  "Siero": "Asturias",
  "Sils": "Girona",
  "Sueca": "Valencia",
  "Talavera La Real": "Badajoz",
  "Tapia de Casariego": "Asturias",
  "Tias": "Las Palmas",
  "Tordesillas": "Valladolid",
  "Torregrosa": "Lleida",
  "Torroella de Montgri": "Girona",
  "Tubilla del Lago": "Burgos",
  "Tuineje": "Las Palmas",
  "Valga": "Pontevedra",
  "Vegas del Genil": "Granada",
  "Velez-Malaga": "Malaga",
  "Vic": "Barcelona",
  "Villafranca de Cordoba": "Cordoba",
  "Villamanan": "Leon",
  "Villaverde de Medina": "Valladolid",
  "Villaviciosa de Odon": "Madrid",
  "Villena": "Alicante",
  "Vimianzo": "A Coruna",
  "Zuera": "Zaragoza",
  "l'Ametlla de Mar": "Tarragona",
} ).map( ( [ localUnit, province ] ) => [
  normalizeKey( localUnit ),
  province,
] ) );

export function geographyForCachedVenue( cached ) {
  const rawCountry = requiredText( cached.country,
                                   "country" );
  const country = practicalCountryFor( cached );
  const province = provinceFor( country,
                                cached );
  const countryRegion = countryRegionFor( country,
                                          province );
  const localUnit = localUnitFor( country,
                                  province,
                                  cached );
  const planetRegion = PLANET_REGION_BY_COUNTRY.get( normalizeKey( country ) )
                       ?? requiredText( cached.planetRegion,
                                        "planetRegion" );

  return {
    planetRegion,
    country,
    countryRegion,
    province,
    localUnit,
    rawCountry,
  };
}

export function distanceKm( first,
                            second ) {
  const earthRadiusKm = 6371;
  const firstLatitude = radiansFromDegrees( first.latitude );
  const secondLatitude = radiansFromDegrees( second.latitude );
  const latitudeDelta = radiansFromDegrees( second.latitude - first.latitude );
  const longitudeDelta = radiansFromDegrees( second.longitude - first.longitude );
  const halfChord = Math.sin( latitudeDelta / 2 ) ** 2
                    + Math.cos( firstLatitude )
                      * Math.cos( secondLatitude )
                      * Math.sin( longitudeDelta / 2 ) ** 2;
  return 2 * earthRadiusKm * Math.asin( Math.sqrt( halfChord ) );
}

function practicalCountryFor( cached ) {
  const country = requiredText( cached.country,
                                "country" );
  const subdivision = textOrNull( cached.countryRegion );
  if( country === "France" && subdivision === "French Guiana" ) {
    return "French Guiana";
  }
  if( country === "Netherlands (Kingdom of the)"
      && ( subdivision === "Oranjestad"
           || cached.province === "Aruba"
           || cached.provinceRegion === "Aruba" ) ) {
    return "Aruba";
  }
  if( country === "Guernsey" || country === "Isle of Man" ) {
    return "United Kingdom of Great Britain and Northern Ireland (the)";
  }
  if( country === "Russian Federation (the)" ) {
    return [ "Novosibirskaya oblast'", "Omskaya oblast'" ].includes( subdivision )
           ? "Asian Russia"
           : "Western Russia";
  }
  return country;
}

function provinceFor( country,
                      cached ) {
  if( cached.country === "Guernsey" || cached.country === "Isle of Man" ) {
    return cached.country;
  }
  if( country === "French Guiana" ) {
    return "French Guiana";
  }
  if( country === "Aruba" ) {
    return "Aruba";
  }
  if( country === "Spain" ) {
    return spanishProvinceFor( cached );
  }
  if( country === "Singapore" ) {
    return "Singapore";
  }
  if( PROVINCE_FROM_GEOCODER_PROVINCE.has( country ) ) {
    return requiredText( cached.province,
                         "province" );
  }
  return requiredText( cached.countryRegion,
                       "countryRegion" );
}

function spanishProvinceFor( cached ) {
  const localUnit = requiredText( cached.provinceRegion,
                                  "provinceRegion" );
  const mappedProvince = SPANISH_PROVINCE_BY_LOCAL_UNIT.get( normalizeKey( localUnit ) );
  if( mappedProvince ) {
    return mappedProvince;
  }

  const subdivision = requiredText( cached.countryRegion,
                                    "countryRegion" );
  const singleProvinceSubdivision = {
    "Asturias, Principado de": "Asturias",
    "Cantabria": "Cantabria",
    "La Rioja": "La Rioja",
    "Madrid": "Madrid",
    "Murcia, Region de": "Murcia",
    "Nafarroa*": "Navarra",
  }[ subdivision ];
  if( singleProvinceSubdivision ) {
    return singleProvinceSubdivision;
  }

  throw new Error( `Missing Spanish province mapping for ${localUnit} (${subdivision})` );
}

function countryRegionFor( country,
                           province ) {
  return COUNTRY_REGION_BY_COUNTRY_AND_PROVINCE.get( keyFor( country,
                                                             province ) )
         ?? country;
}

function localUnitFor( country,
                       province,
                       cached ) {
  if( country === "Aruba" ) {
    return textOrNull( cached.provinceRegion )
           ?? textOrNull( cached.countryRegion )
           ?? "Aruba";
  }
  if( PROVINCE_FROM_GEOCODER_PROVINCE.has( country ) ) {
    return requiredText( cached.provinceRegion,
                         "provinceRegion" );
  }
  if( isDirectionalLocalUnit( cached.province ) ) {
    return requiredText( cached.provinceRegion,
                         "provinceRegion" );
  }
  if( country === "Singapore" ) {
    return requiredText( cached.provinceRegion,
                         "provinceRegion" );
  }
  if( isGenericLocalUnit( cached.province ) ) {
    return requiredText( cached.provinceRegion,
                         "provinceRegion" );
  }
  const candidates = [
    cached.province,
    cached.provinceRegion,
  ].filter( ( candidate ) => textOrNull( candidate )
                             && candidate !== province );
  return textOrNull( candidates[ 0 ] )
         ?? textOrNull( cached.provinceRegion )
         ?? province;
}

function isGenericLocalUnit( value ) {
  const text = textOrNull( value );
  if( !text ) {
    return false;
  }
  const normalized = normalizeKey( text );
  return normalized === "metropolitan france"
         || normalized === "european france";
}

function isDirectionalLocalUnit( value ) {
  const text = textOrNull( value );
  if( !text ) {
    return false;
  }
  return [
    "centre",
    "east",
    "north",
    "north east",
    "north west",
    "south",
    "west",
  ].includes( normalizeKey( text ) );
}

function regionMap( regions ) {
  return Object.fromEntries( Object.entries( regions )
    .flatMap( ( [ region, provinces ] ) => provinces.map( ( province ) => [
      province,
      region,
    ] ) ) );
}

function keyFor( country,
                 province ) {
  return `${normalizeKey( country )}|${normalizeKey( province )}`;
}

function requiredText( value,
                       label ) {
  const text = textOrNull( value );
  if( !text ) {
    throw new Error( `Missing ${label}` );
  }
  return text;
}

function textOrNull( value ) {
  if( typeof value !== "string" ) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function radiansFromDegrees( degrees ) {
  return degrees * Math.PI / 180;
}

function normalizeKey( value ) {
  return requiredText( value,
                       "key" )
    .normalize( "NFD" )
    .replace( /\p{Diacritic}/gu,
              "" )
    .replace( /&/g,
              " and " )
    .replace( /[^a-zA-Z0-9]+/g,
              " " )
    .trim()
    .toLocaleLowerCase( "en-US" );
}
