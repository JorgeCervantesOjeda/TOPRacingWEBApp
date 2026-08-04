// scripts/test-venue-geography-model.mjs
// Verifies that real provinces stay separate from local municipality/county clusters.
import assert from "node:assert/strict";
import { provinceRegionNamesForClusters } from "./province-region-names.mjs";
import { geographyForCachedVenue } from "./venue-geography-model.mjs";

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Netherlands (Kingdom of the)",
    countryRegion: "Gelderland",
    province: "Lochem",
    provinceRegion: "Lochem",
  } ),
  {
    planetRegion: "Europe",
    country: "Netherlands (Kingdom of the)",
    countryRegion: "Northern & Central Netherlands",
    province: "Gelderland",
    localUnit: "Lochem",
    rawCountry: "Netherlands (Kingdom of the)",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "East",
    province: "Balearic Islands",
    provinceRegion: "Sant Antoni de Portmany",
  } ),
  {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "Balearic Islands",
    province: "Balearic Islands",
    localUnit: "Sant Antoni de Portmany",
    rawCountry: "Spain",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "Catalunya [Cataluna]",
    province: "East",
    provinceRegion: "Sils",
  } ),
  {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "East Spain",
    province: "Girona",
    localUnit: "Sils",
    rawCountry: "Spain",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "Valenciana, Comunitat*",
    province: "East",
    provinceRegion: "Cheste",
  } ),
  {
    planetRegion: "Europe",
    country: "Spain",
    countryRegion: "East Spain",
    province: "Valencia",
    localUnit: "Cheste",
    rawCountry: "Spain",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Western & Northern Europe",
    country: "France",
    countryRegion: "Grand-Est",
    province: "metropolitan France",
    provinceRegion: "Sausheim",
  } ),
  {
    planetRegion: "Europe",
    country: "France",
    countryRegion: "Northern & Eastern France",
    province: "Grand-Est",
    localUnit: "Sausheim",
    rawCountry: "France",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Belgium",
    countryRegion: "Vlaams Gewest",
    province: "Antwerpen",
    provinceRegion: "Ruisbroek",
  } ),
  {
    planetRegion: "Europe",
    country: "Belgium",
    countryRegion: "Belgium",
    province: "Antwerpen",
    localUnit: "Ruisbroek",
    rawCountry: "Belgium",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Southeast Asia",
    country: "Indonesia",
    countryRegion: "Jawa",
    province: "Jawa Barat",
    provinceRegion: "Kecamatan Babakan Madang",
  } ),
  {
    planetRegion: "South & Southeast Asia",
    country: "Indonesia",
    countryRegion: "Java",
    province: "Jawa Barat",
    localUnit: "Kecamatan Babakan Madang",
    rawCountry: "Indonesia",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "North America",
    country: "Netherlands (Kingdom of the)",
    countryRegion: "Oranjestad",
    province: "Aruba",
    provinceRegion: "Oranjestad",
  } ),
  {
    planetRegion: "Caribbean, Central America & Northern Andes",
    country: "Aruba",
    countryRegion: "Aruba",
    province: "Aruba",
    localUnit: "Oranjestad",
    rawCountry: "Netherlands (Kingdom of the)",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "North America",
    country: "Mexico",
    countryRegion: "Mexico",
    province: "Ocoyoacac",
    provinceRegion: "Ocoyoacac",
  } ),
  {
    planetRegion: "North America",
    country: "Mexico",
    countryRegion: "Central Mexico",
    province: "Mexico",
    localUnit: "Ocoyoacac",
    rawCountry: "Mexico",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "South America",
    country: "France",
    countryRegion: "French Guiana",
    province: "Arrondissement de Cayenne",
    provinceRegion: "Matoury",
  } ),
  {
    planetRegion: "South America",
    country: "French Guiana",
    countryRegion: "French Guiana",
    province: "French Guiana",
    localUnit: "Arrondissement de Cayenne",
    rawCountry: "France",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Europe",
    country: "Russian Federation (the)",
    countryRegion: "Omskaya oblast'",
    province: "Omsk",
    provinceRegion: "Omsk",
  } ),
  {
    planetRegion: "Central Asia",
    country: "Asian Russia",
    countryRegion: "Siberia",
    province: "Omskaya oblast'",
    localUnit: "Omsk",
    rawCountry: "Russian Federation (the)",
  }
);

assert.deepEqual(
  geographyForCachedVenue( {
    planetRegion: "Asia",
    country: "Singapore",
    countryRegion: "North West",
    province: "North Region",
    provinceRegion: "Sungei Kadut",
  } ),
  {
    planetRegion: "South & Southeast Asia",
    country: "Singapore",
    countryRegion: "Singapore",
    province: "Singapore",
    localUnit: "Sungei Kadut",
    rawCountry: "Singapore",
  }
);

assert.equal(
  provinceRegionNamesForClusters( [
    [
      {
        country: "Mexico",
        province: "Mexico",
        localUnit: "Huixquilucan",
        name: "Go Karts - la marquesa",
        latitude: 19.296849,
        longitude: -99.398487,
      },
      {
        country: "Mexico",
        province: "Mexico",
        localUnit: "Huixquilucan",
        name: "Kartodromo Sabaneta",
        latitude: 19.3083282,
        longitude: -99.3769514,
      },
    ],
  ] )[ 0 ],
  "La Marquesa"
);

assert.equal(
  provinceRegionNamesForClusters( [
    [
      {
        country: "Indonesia",
        province: "Jawa Barat",
        localUnit: "Kecamatan Babakan Madang",
        name: "Sentul International Circuit",
        latitude: -6.536448,
        longitude: 106.857271,
      },
      {
        country: "Indonesia",
        province: "Jawa Barat",
        localUnit: "Kecamatan Citeureup",
        name: "Sentul International Karting Circuit Cirkuit",
        latitude: -6.525278,
        longitude: 106.859539,
      },
    ],
  ] )[ 0 ],
  "Sentul"
);

assert.deepEqual(
  provinceRegionNamesForClusters( [
    [
      {
        country: "Mexico",
        province: "Jalisco",
        localUnit: "North Locality",
        name: "North Circuit",
        latitude: 21.5,
        longitude: -103.5,
      },
    ],
    [
      {
        country: "Mexico",
        province: "Jalisco",
        localUnit: "South Locality",
        name: "South Circuit",
        latitude: 20.5,
        longitude: -103.5,
      },
    ],
  ] ),
  [
    "Northern",
    "Southern",
  ]
);

assert.deepEqual(
  provinceRegionNamesForClusters( [
    [
      {
        country: "Mexico",
        province: "Morelos",
        localUnit: "Cuernavaca",
        name: "Single Circuit",
        latitude: 18.9,
        longitude: -99.2,
      },
    ],
  ] ),
  [
    "Central",
  ]
);

console.log( "venue geography model tests passed" );
