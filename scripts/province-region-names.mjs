// scripts/province-region-names.mjs
// Names practical province-region zones from clustered venue coordinates.

export function provinceRegionNamesForClusters( clusters ) {
  const centers = clusters.map( centerOfRows );
  const bounds = boundsOfCenters( centers );
  return clusters.map( ( cluster,
                         index ) => explicitProvinceRegionFor( cluster )
                                   ?? cardinalNameFor( centers[ index ],
                                                       bounds ) );
}

function explicitProvinceRegionFor( rows ) {
  const first = rows[ 0 ];
  const text = rows.map( ( row ) => `${row.localUnit} ${row.name}` )
    .join( " " )
    .toLocaleLowerCase( "en-US" );
  if( first.country === "Mexico"
      && first.province === "Mexico"
      && ( text.includes( "marquesa" )
           || text.includes( "sabaneta" ) ) ) {
    return "La Marquesa";
  }
  if( first.country === "Indonesia"
      && first.province === "Jawa Barat"
      && text.includes( "sentul" ) ) {
    return "Sentul";
  }
  return null;
}

function cardinalNameFor( center,
                          bounds ) {
  if( bounds.latitudeRange === 0 && bounds.longitudeRange === 0 ) {
    return "Central";
  }

  const latitudeOffset = normalizedOffset( center.latitude,
                                           bounds.centerLatitude,
                                           bounds.latitudeRange );
  const longitudeOffset = normalizedOffset( center.longitude,
                                            bounds.centerLongitude,
                                            bounds.longitudeRange );
  const distanceFromCenter = Math.hypot( latitudeOffset,
                                         longitudeOffset );
  if( distanceFromCenter < 0.18 ) {
    return "Central";
  }

  const latitudeMagnitude = Math.abs( latitudeOffset );
  const longitudeMagnitude = Math.abs( longitudeOffset );
  if( longitudeMagnitude > latitudeMagnitude * 1.5 ) {
    return longitudeOffset > 0 ? "Eastern" : "Western";
  }
  if( latitudeMagnitude > longitudeMagnitude * 1.5 ) {
    return latitudeOffset > 0 ? "Northern" : "Southern";
  }
  if( latitudeOffset > 0 && longitudeOffset > 0 ) {
    return "Northeastern";
  }
  if( latitudeOffset > 0 && longitudeOffset < 0 ) {
    return "Northwestern";
  }
  if( latitudeOffset < 0 && longitudeOffset > 0 ) {
    return "Southeastern";
  }
  return "Southwestern";
}

function normalizedOffset( value,
                           center,
                           range ) {
  if( range === 0 ) {
    return 0;
  }
  return ( value - center ) / range;
}

function centerOfRows( rows ) {
  return {
    latitude: averageOf( rows.map( ( row ) => row.latitude ) ),
    longitude: averageOf( rows.map( ( row ) => row.longitude ) ),
  };
}

function boundsOfCenters( centers ) {
  const latitudes = centers.map( ( center ) => center.latitude );
  const longitudes = centers.map( ( center ) => center.longitude );
  const minLatitude = Math.min( ...latitudes );
  const maxLatitude = Math.max( ...latitudes );
  const minLongitude = Math.min( ...longitudes );
  const maxLongitude = Math.max( ...longitudes );
  return {
    centerLatitude: ( minLatitude + maxLatitude ) / 2,
    centerLongitude: ( minLongitude + maxLongitude ) / 2,
    latitudeRange: maxLatitude - minLatitude,
    longitudeRange: maxLongitude - minLongitude,
  };
}

function averageOf( values ) {
  return values.reduce( ( total, value ) => total + value,
                        0 )
         / values.length;
}
