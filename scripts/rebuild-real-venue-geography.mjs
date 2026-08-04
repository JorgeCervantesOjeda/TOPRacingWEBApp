// scripts/rebuild-real-venue-geography.mjs
// Rebuilds venue geography from real administrative levels and practical clusters.
import { spawnSync } from "node:child_process";
import fs from "node:fs/promises";
import path from "node:path";
import {
  distanceKm,
  geographyForCachedVenue,
} from "./venue-geography-model.mjs";
import { provinceRegionNamesForClusters } from "./province-region-names.mjs";

const DEFAULT_MYSQL = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
const DEFAULT_DB = "topracing26";
const DEFAULT_USER = "admin";
const DEFAULT_PASSWORD = "admin";
const DEFAULT_CACHE = path.join( "data", "venue-geocode-cache.json" );
const DEFAULT_CREATOR_ID = 1;
const DEFAULT_EDGE_KM = 16;
const DEFAULT_DIAMETER_KM = 30;

const options = parseArgs( process.argv.slice( 2 ) );
const mysqlPath = options.mysql ?? DEFAULT_MYSQL;
const dbName = options.db ?? DEFAULT_DB;
const username = options.user ?? DEFAULT_USER;
const password = options.password ?? DEFAULT_PASSWORD;
const cachePath = options.cache ?? DEFAULT_CACHE;
const creatorId = Number.parseInt( options[ "creator-id" ] ?? DEFAULT_CREATOR_ID, 10 );
const edgeKm = Number.parseFloat( options[ "edge-km" ] ?? DEFAULT_EDGE_KM );
const diameterKm = Number.parseFloat( options[ "diameter-km" ] ?? DEFAULT_DIAMETER_KM );
const shouldApply = Boolean( options.apply );

assertSafeDbName( dbName );
if( !Number.isInteger( creatorId ) || creatorId <= 0 ) {
  throw new Error( `Invalid --creator-id: ${options[ "creator-id" ]}` );
}

const cache = JSON.parse( await fs.readFile( cachePath,
                                             "utf8" ) );
const venues = queryVenueRows();
const resolvedVenues = venues.map( ( venue ) => resolveVenue( venue,
                                                               cache ) );
const clusteredVenues = clusteredVenueRows( resolvedVenues );
const sql = buildUpdateSql( clusteredVenues );

console.log( JSON.stringify( {
  venuesChecked: venues.length,
  venuesResolved: clusteredVenues.length,
  countryRegions: countOfUnique( clusteredVenues,
                                 "countryRegionKey" ),
  provinces: countOfUnique( clusteredVenues,
                            "provinceKey" ),
  provinceRegions: countOfUnique( clusteredVenues,
                                  "provinceRegionKey" ),
  mode: shouldApply ? "apply" : "dry-run",
}, null, 2 ) );

if( shouldApply ) {
  runMysql( sql );
  console.log( "Real venue geography rebuild applied." );
} else {
  console.log( sql );
}

function queryVenueRows() {
  const sql = [
    "SELECT",
    "id,",
    "name,",
    "parallel,",
    "meridian",
    "FROM venue",
    "ORDER BY id;",
  ].join( " " );
  const result = runMysql( sql,
                           [
                             "--batch",
                             "--raw",
                             "--skip-column-names",
                           ] );
  return result.trim()
    .split( /\r?\n/ )
    .filter( Boolean )
    .map( ( line ) => {
      const [ id, name, latitude, longitude ] = line.split( "\t" );
      return {
        id: Number.parseInt( id, 10 ),
        name,
        latitude: Number.parseFloat( latitude ),
        longitude: Number.parseFloat( longitude ),
      };
    } );
}

function resolveVenue( venue,
                       cache ) {
  const cached = cache[ cacheKeyFor( venue ) ];
  if( !cached ) {
    throw new Error( `Missing geocode cache for venue=${venue.id} coordinate=${cacheKeyFor( venue )}` );
  }
  const geography = geographyForCachedVenue( cached );
  return {
    ...venue,
    ...geography,
    countryRegionKey: [
      geography.country,
      geography.countryRegion,
    ].join( "\t" ),
    provinceKey: [
      geography.country,
      geography.countryRegion,
      geography.province,
    ].join( "\t" ),
  };
}

function clusteredVenueRows( rows ) {
  const output = [];
  for( const group of groupedByProvince( rows ).values() ) {
    const clusters = clustersFor( group );
    const provinceRegionNames = provinceRegionNamesForClusters( clusters );
    for( const [ index, cluster ] of clusters.entries() ) {
      const provinceRegion = provinceRegionNames[ index ];
      for( const row of cluster ) {
        output.push( {
          ...row,
          provinceRegion,
          provinceRegionKey: [
            row.country,
            row.countryRegion,
            row.province,
            provinceRegion,
          ].join( "\t" ),
        } );
      }
    }
  }
  return output.sort( ( left, right ) => left.id - right.id );
}

function groupedByProvince( rows ) {
  const byProvince = new Map();
  for( const row of rows ) {
    if( !byProvince.has( row.provinceKey ) ) {
      byProvince.set( row.provinceKey,
                      [] );
    }
    byProvince.get( row.provinceKey ).push( row );
  }
  return byProvince;
}

function clustersFor( rows ) {
  const parent = rows.map( ( _row,
                             index ) => index );
  for( let first = 0; first < rows.length; first += 1 ) {
    for( let second = first + 1; second < rows.length; second += 1 ) {
      if( distanceKm( rows[ first ],
                      rows[ second ] ) <= edgeKm ) {
        join( parent,
              first,
              second );
      }
    }
  }

  const groups = new Map();
  rows.forEach( ( row,
                  index ) => {
    const root = find( parent,
                       index );
    if( !groups.has( root ) ) {
      groups.set( root,
                  [] );
    }
    groups.get( root ).push( row );
  } );

  return [ ...groups.values() ].flatMap( splitOversizedCluster );
}

function splitOversizedCluster( rows ) {
  if( diameterOf( rows ) <= diameterKm ) {
    return [
      rows,
    ];
  }
  return rows.map( ( row ) => [
    row,
  ] );
}

function diameterOf( rows ) {
  let diameter = 0;
  for( let first = 0; first < rows.length; first += 1 ) {
    for( let second = first + 1; second < rows.length; second += 1 ) {
      diameter = Math.max( diameter,
                           distanceKm( rows[ first ],
                                       rows[ second ] ) );
    }
  }
  return diameter;
}

function buildUpdateSql( rows ) {
  const lines = [
    "SET NAMES utf8mb4;",
    "START TRANSACTION;",
  ];
  for( const row of rows ) {
    lines.push(
      `INSERT INTO planetregion (name, id_creator) SELECT ${sqlString( row.planetRegion )}, ${creatorId} WHERE NOT EXISTS (SELECT 1 FROM planetregion WHERE name = ${sqlString( row.planetRegion )});`,
      `SET @planetregion_id = (SELECT id FROM planetregion WHERE name = ${sqlString( row.planetRegion )} ORDER BY id LIMIT 1);`,
      `INSERT INTO country (name, id_planetregion, id_creator) SELECT ${sqlString( row.country )}, @planetregion_id, ${creatorId} WHERE NOT EXISTS (SELECT 1 FROM country WHERE name = ${sqlString( row.country )} AND id_planetregion = @planetregion_id);`,
      `SET @country_id = (SELECT id FROM country WHERE name = ${sqlString( row.country )} AND id_planetregion = @planetregion_id ORDER BY id LIMIT 1);`,
      `INSERT INTO countryregion (name, id_country, id_creator) SELECT ${sqlString( row.countryRegion )}, @country_id, ${creatorId} WHERE NOT EXISTS (SELECT 1 FROM countryregion WHERE name = ${sqlString( row.countryRegion )} AND id_country = @country_id);`,
      `SET @countryregion_id = (SELECT id FROM countryregion WHERE name = ${sqlString( row.countryRegion )} AND id_country = @country_id ORDER BY id LIMIT 1);`,
      `INSERT INTO province (name, id_countryregion, id_creator) SELECT ${sqlString( row.province )}, @countryregion_id, ${creatorId} WHERE NOT EXISTS (SELECT 1 FROM province WHERE name = ${sqlString( row.province )} AND id_countryregion = @countryregion_id);`,
      `SET @province_id = (SELECT id FROM province WHERE name = ${sqlString( row.province )} AND id_countryregion = @countryregion_id ORDER BY id LIMIT 1);`,
      `INSERT INTO provinceregion (name, id_province, id_creator) SELECT ${sqlString( row.provinceRegion )}, @province_id, ${creatorId} WHERE NOT EXISTS (SELECT 1 FROM provinceregion WHERE name = ${sqlString( row.provinceRegion )} AND id_province = @province_id);`,
      `SET @provinceregion_id = (SELECT id FROM provinceregion WHERE name = ${sqlString( row.provinceRegion )} AND id_province = @province_id ORDER BY id LIMIT 1);`,
      `UPDATE venue SET id_provinceregion = @provinceregion_id WHERE id = ${row.id};`
    );
  }
  lines.push(
    "DELETE pr FROM provinceregion pr LEFT JOIN venue v ON v.id_provinceregion = pr.id WHERE v.id IS NULL;",
    "DELETE p FROM province p LEFT JOIN provinceregion pr ON pr.id_province = p.id WHERE pr.id IS NULL;",
    "DELETE cr FROM countryregion cr LEFT JOIN province p ON p.id_countryregion = cr.id WHERE p.id IS NULL;",
    "DELETE c FROM country c LEFT JOIN countryregion cr ON cr.id_country = c.id WHERE cr.id IS NULL;",
    "DELETE pl FROM planetregion pl LEFT JOIN country c ON c.id_planetregion = pl.id WHERE c.id IS NULL;",
    "COMMIT;"
  );
  return `USE \`${dbName}\`;\n${lines.join( "\n" )}\n`;
}

function runMysql( sql,
                   extraArgs = [] ) {
  const result = spawnSync( mysqlPath,
                            [
                              "--protocol=tcp",
                              `-u${username}`,
                              `-p${password}`,
                              "--default-character-set=utf8mb4",
                              ...extraArgs,
                              dbName,
                            ],
                            {
                              input: sql,
                              encoding: "utf8",
                              maxBuffer: 1024 * 1024 * 80,
                            } );
  if( result.stderr ) {
    process.stderr.write( result.stderr );
  }
  if( result.status !== 0 ) {
    throw new Error( `mysql exited with status ${result.status}` );
  }
  return result.stdout;
}

function parseArgs( args ) {
  const parsed = {};
  for( let index = 0; index < args.length; index += 1 ) {
    const arg = args[ index ];
    if( !arg.startsWith( "--" ) ) {
      throw new Error( `Unexpected argument: ${arg}` );
    }
    const key = arg.slice( 2 );
    const next = args[ index + 1 ];
    if( !next || next.startsWith( "--" ) ) {
      parsed[ key ] = true;
    } else {
      parsed[ key ] = next;
      index += 1;
    }
  }
  return parsed;
}

function join( parent,
               first,
               second ) {
  const firstRoot = find( parent,
                          first );
  const secondRoot = find( parent,
                           second );
  if( firstRoot !== secondRoot ) {
    parent[ secondRoot ] = firstRoot;
  }
}

function find( parent,
               index ) {
  let current = index;
  while( parent[ current ] !== current ) {
    parent[ current ] = parent[ parent[ current ] ];
    current = parent[ current ];
  }
  return current;
}

function countOfUnique( rows,
                        key ) {
  return new Set( rows.map( ( row ) => row[ key ] ) ).size;
}

function cacheKeyFor( row ) {
  return `${row.latitude.toFixed( 6 )},${row.longitude.toFixed( 6 )}`;
}

function sqlString( value ) {
  return `'${value.replace( /\\/g, "\\\\" )
    .replace( /'/g,
              "''" )}'`;
}

function assertSafeDbName( value ) {
  if( !/^[A-Za-z0-9_]+$/.test( value ) ) {
    throw new Error( `Unsafe database name: ${value}` );
  }
}
