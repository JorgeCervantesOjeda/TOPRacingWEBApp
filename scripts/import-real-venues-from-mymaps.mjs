// scripts/import-real-venues-from-mymaps.mjs
// Imports real TOP-Racing venue data from a Google My Maps CSV export.
import { spawnSync } from "node:child_process";
import fs from "node:fs/promises";
import path from "node:path";
import { geographyForCachedVenue } from "./venue-geography-model.mjs";

const DEFAULT_MYSQL = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
const DEFAULT_CSV = "TOP-Racing venues- Saved_Places.csv";
const DEFAULT_CACHE = path.join( "data", "venue-geocode-cache.json" );
const DEFAULT_DB = "topracing26";
const DEFAULT_USER = "admin";
const DEFAULT_PASSWORD = "admin";
const DEFAULT_CREATOR_ID = 1;
const VARIANT_NAME = "Main Layout";
const GEOCODE_DELAY_MS = 150;

const options = parseArgs( process.argv.slice( 2 ) );
const csvPath = options.csv ?? DEFAULT_CSV;
const cachePath = options.cache ?? DEFAULT_CACHE;
const mysqlPath = options.mysql ?? DEFAULT_MYSQL;
const dbName = options.db ?? DEFAULT_DB;
const username = options.user ?? DEFAULT_USER;
const password = options.password ?? DEFAULT_PASSWORD;
const creatorId = Number.parseInt( options[ "creator-id" ] ?? DEFAULT_CREATOR_ID, 10 );
const rowLimit = Number.parseInt( options.limit ?? "0", 10 );
const shouldApply = Boolean( options.apply );
const shouldCleanTestData = Boolean( options[ "clean-test-data" ] );

assertSafeDbName( dbName );
if( !Number.isInteger( creatorId ) || creatorId <= 0 ) {
  throw new Error( `Invalid --creator-id: ${options[ "creator-id" ]}` );
}
if( !Number.isInteger( rowLimit ) || rowLimit < 0 ) {
  throw new Error( `Invalid --limit: ${options.limit}` );
}

const sourceRows = await readMyMapsRows( csvPath );
const uniqueRows = uniqueVenueRows( sourceRows )
  .slice( 0,
          rowLimit > 0 ? rowLimit : undefined );
const cache = await readJsonCache( cachePath );
const resolvedRows = [];
let cacheMisses = 0;
let renamedCoordinateRows = 0;
let sanitizedRows = 0;

for( const [ index, row ] of uniqueRows.entries() ) {
  const cacheKey = cacheKeyFor( row );
  let geography = cache[ cacheKey ];
  if( !geography ) {
    geography = await geocodeRow( row );
    cache[ cacheKey ] = geography;
    cacheMisses += 1;
    if( cacheMisses % 25 === 0 ) {
      await writeJsonCache( cachePath,
                            cache );
      console.log( `Geocoded ${cacheMisses} new locations; ${index + 1}/${uniqueRows.length} rows processed.` );
    }
    await delay( GEOCODE_DELAY_MS );
  }

  const normalizedName = normalizeVenueName( row.name,
                                             row.lat,
                                             row.lon );
  if( normalizedName.wasCoordinateOnly ) {
    renamedCoordinateRows += 1;
  }
  const cleanName = sanitizeMysqlText( normalizedName.name );
  if( cleanName !== normalizedName.name ) {
    sanitizedRows += 1;
  }
  const resolvedGeography = geographyForCachedVenue( geography );

  resolvedRows.push( {
    ...row,
    name: cleanName,
    planetRegion: sanitizeMysqlText( resolvedGeography.planetRegion ),
    country: sanitizeMysqlText( resolvedGeography.country ),
    countryRegion: sanitizeMysqlText( resolvedGeography.countryRegion ),
    province: sanitizeMysqlText( resolvedGeography.province ),
    provinceRegion: sanitizeMysqlText( resolvedGeography.localUnit ),
  } );
}
await writeJsonCache( cachePath,
                      cache );

const sql = buildImportSql( resolvedRows,
                            dbName,
                            creatorId,
                            shouldCleanTestData );

console.log( JSON.stringify( {
  csvRecords: sourceRows.length,
  uniqueVenues: uniqueRows.length,
  cacheMisses,
  renamedCoordinateRows,
  sanitizedRows,
  cleanTestData: shouldCleanTestData,
  mode: shouldApply ? "apply" : "dry-run",
}, null, 2 ) );

if( shouldApply ) {
  const result = spawnSync( mysqlPath,
                            [
                              "--protocol=tcp",
                              `-u${username}`,
                              `-p${password}`,
                              "--default-character-set=utf8mb4",
                              dbName,
                            ],
                            {
                              input: sql,
                              encoding: "utf8",
                              maxBuffer: 1024 * 1024 * 80,
                            } );
  if( result.stdout ) {
    process.stdout.write( result.stdout );
  }
  if( result.stderr ) {
    process.stderr.write( result.stderr );
  }
  if( result.status !== 0 ) {
    throw new Error( `mysql exited with status ${result.status}` );
  }
} else {
  const previewPath = path.join( "target",
                                 "import-real-venues-preview.sql" );
  await fs.mkdir( path.dirname( previewPath ),
                  { recursive: true } );
  await fs.writeFile( previewPath,
                      sql,
                      "utf8" );
  console.log( `Dry run SQL written to ${previewPath}` );
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

async function readMyMapsRows( filePath ) {
  const content = await fs.readFile( filePath,
                                     "utf8" );
  const lines = content.split( /\r?\n/ );
  const records = [];
  let current = null;
  for( const line of lines.slice( 1 ) ) {
    if( line.length === 0 ) {
      continue;
    }
    if( line.startsWith( "\"POINT (" ) ) {
      if( current ) {
        records.push( current );
      }
      current = line;
    } else if( current ) {
      current += ` ${line}`;
    }
  }
  if( current ) {
    records.push( current );
  }

  return records.map( parseMyMapsRecord );
}

function parseMyMapsRecord( record ) {
  const match = record.match( /^"POINT \(([-0-9.]+) ([-0-9.]+)\)",(.+)$/ );
  if( !match ) {
    throw new Error( `Could not parse WKT record: ${record}` );
  }

  const lon = Number.parseFloat( match[ 1 ] );
  const lat = Number.parseFloat( match[ 2 ] );
  const tail = match[ 3 ];
  const urlIndex = tail.search( /,https?:\/\// );
  const beforeUrl = urlIndex >= 0
                    ? tail.slice( 0,
                                  urlIndex )
                    : tail.replace( /,+$/,
                                    "" );
  const descriptionIndex = beforeUrl.indexOf( ",{" );
  const rawName = descriptionIndex >= 0
                  ? beforeUrl.slice( 0,
                                     descriptionIndex )
                  : beforeUrl;
  if( !Number.isFinite( lat ) || !Number.isFinite( lon ) ) {
    throw new Error( `Invalid coordinates for record: ${record}` );
  }

  return {
    lat,
    lon,
    name: rawName.trim(),
  };
}

function uniqueVenueRows( rows ) {
  const byKey = new Map();
  for( const row of rows ) {
    const key = [
      row.name.toLocaleLowerCase( "en-US" ),
      row.lat.toFixed( 7 ),
      row.lon.toFixed( 7 ),
    ].join( "|" );
    if( !byKey.has( key ) ) {
      byKey.set( key,
                 row );
    }
  }
  return [ ...byKey.values() ];
}

function normalizeVenueName( name,
                             lat,
                             lon ) {
  const trimmed = name.trim();
  if( /^-?\d+(\.\d+)?,-?\d+(\.\d+)?$/.test( trimmed )
      || /^-?\d+(\.\d+)?$/.test( trimmed )
      || trimmed.length === 0 ) {
    return {
      name: `Mapped circuit ${lat.toFixed( 6 )}, ${lon.toFixed( 6 )}`,
      wasCoordinateOnly: true,
    };
  }
  return {
    name: trimmed,
    wasCoordinateOnly: false,
  };
}

async function readJsonCache( filePath ) {
  try {
    return JSON.parse( await fs.readFile( filePath,
                                          "utf8" ) );
  } catch( error ) {
    if( error.code === "ENOENT" ) {
      return {};
    }
    throw error;
  }
}

async function writeJsonCache( filePath,
                               cache ) {
  await fs.mkdir( path.dirname( filePath ),
                  { recursive: true } );
  await fs.writeFile( filePath,
                      JSON.stringify( cache,
                                      null,
                                      2 ) + "\n",
                      "utf8" );
}

async function geocodeRow( row ) {
  const url = new URL( "https://api.bigdatacloud.net/data/reverse-geocode-client" );
  url.searchParams.set( "latitude",
                        row.lat.toString() );
  url.searchParams.set( "longitude",
                        row.lon.toString() );
  url.searchParams.set( "localityLanguage",
                        "en" );
  const response = await fetch( url,
                                {
                                  headers: {
                                    "User-Agent": "TOPRacingWEBApp local data import",
                                  },
                                } );
  if( !response.ok ) {
    throw new Error( `Reverse geocode failed for ${row.name} (${row.lat}, ${row.lon}): ${response.status}` );
  }
  const geocode = await response.json();
  return geographyFromGeocode( row,
                               geocode );
}

function geographyFromGeocode( row,
                               geocode ) {
  const administrative = Array.isArray( geocode.localityInfo?.administrative )
                         ? geocode.localityInfo.administrative
                         : [];
  const countryRecord = administrative.find( ( item ) => item.adminLevel === 2 );
  const administrativeCountry = textOrNull( countryRecord?.name );
  const country = administrativeCountry
                  ?? textOrNull( geocode.countryName );
  const principalSubdivision = textOrNull( geocode.principalSubdivision );
  const belowCountry = administrative
    .filter( ( item ) => item.adminLevel && item.adminLevel > 2 )
    .filter( ( item ) => !countryRecord?.geonameId
                         || item.geonameId !== countryRecord.geonameId )
    .map( ( item ) => textOrNull( item.name ) )
    .filter( ( item ) => item && item !== country && item !== administrativeCountry );
  const uniqueBelowCountry = [ ...new Set( belowCountry ) ];
  const planetRegion = textOrNull( geocode.continent )
                       ?? textOrNull( geocode.continentCode );
  const rawCountryRegion = principalSubdivision
                           ?? uniqueBelowCountry[ 0 ];
  const rawProvince = uniqueBelowCountry.find( ( item ) => item !== rawCountryRegion )
                      ?? rawCountryRegion;
  const provinceRegion = textOrNull( geocode.locality )
                         ?? textOrNull( geocode.city )
                         ?? uniqueBelowCountry.at( -1 )
                         ?? rawProvince;

  const missing = [];
  for( const [ key, value ] of Object.entries( {
    planetRegion,
    country,
    countryRegion: rawCountryRegion,
    province: rawProvince,
    provinceRegion,
  } ) ) {
    if( !value ) {
      missing.push( key );
    }
  }
  if( missing.length > 0 ) {
    throw new Error( `Incomplete geography for ${row.name} (${row.lat}, ${row.lon}): ${missing.join( ", " )}` );
  }

  return {
    planetRegion,
    country,
    countryRegion: rawCountryRegion,
    province: rawProvince,
    provinceRegion,
  };
}

function textOrNull( value ) {
  if( typeof value !== "string" ) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function cacheKeyFor( row ) {
  return `${row.lat.toFixed( 6 )},${row.lon.toFixed( 6 )}`;
}

function buildImportSql( rows,
                         db,
                         idCreator,
                         cleanTestData ) {
  const lines = [
    "SET NAMES utf8mb4;",
    "START TRANSACTION;",
  ];
  if( cleanTestData ) {
    lines.push( ...buildCleanTestDataSql() );
  }
  for( const row of rows ) {
    lines.push( ...buildVenueSql( row,
                                  idCreator ) );
  }
  lines.push(
    "COMMIT;",
    "SELECT COUNT(*) AS planetregions FROM planetregion;",
    "SELECT COUNT(*) AS countries FROM country;",
    "SELECT COUNT(*) AS countryregions FROM countryregion;",
    "SELECT COUNT(*) AS provinces FROM province;",
    "SELECT COUNT(*) AS provinceregions FROM provinceregion;",
    "SELECT COUNT(*) AS venues FROM venue;",
    "SELECT COUNT(*) AS variants FROM variant;"
  );
  return `USE \`${db}\`;\n${lines.join( "\n" )}\n`;
}

function buildCleanTestDataSql() {
  return [
    "SET FOREIGN_KEY_CHECKS=0;",
    "CREATE TEMPORARY TABLE tmp_test_participant AS SELECT id FROM participant WHERE id > 21 AND (email LIKE '%@example.com' OR email LIKE 'codex+%' OR names_given IN ('Codex','Geo','Browser','Regatta','Registration','Driver','Car','Variant','Venue','Results','Speed','Race','Auction','Penalty','Profile','Logout','Menu') OR names_family REGEXP 'Browser|Flow|Fixture|Selector|promoter-|owner-|seller-|buyer-|reset-|confirm-|advance-|efficiency-|finishing-|bidder|driver|negative');",
    "CREATE TEMPORARY TABLE tmp_test_venue (id BIGINT UNSIGNED NOT NULL PRIMARY KEY);",
    "INSERT IGNORE INTO tmp_test_venue SELECT id FROM venue WHERE id > 1 AND id_owner IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_venue SELECT id FROM venue WHERE id > 1 AND id_creator IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_venue SELECT id FROM venue WHERE id > 1 AND name REGEXP '^(Browser Venue|Geo Venue|Venue-|Owned Venue|Foreign Venue|hijacked-)';",
    "CREATE TEMPORARY TABLE tmp_test_variant (id BIGINT UNSIGNED NOT NULL PRIMARY KEY);",
    "INSERT IGNORE INTO tmp_test_variant SELECT id FROM variant WHERE id > 1 AND id_venue IN (SELECT id FROM tmp_test_venue);",
    "INSERT IGNORE INTO tmp_test_variant SELECT id FROM variant WHERE id > 1 AND id_creator IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_variant SELECT id FROM variant WHERE id > 1 AND name REGEXP '^(Browser Variant|Geo Variant|Variant-|Selected Variant-|Intruder Variant-|hijacked-)';",
    "CREATE TEMPORARY TABLE tmp_test_car (id BIGINT UNSIGNED NOT NULL PRIMARY KEY);",
    "INSERT IGNORE INTO tmp_test_car SELECT id FROM car WHERE id_owner IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_car SELECT id FROM car WHERE nickname REGEXP '^(Browser Car|Car-|hijacked-)';",
    "CREATE TEMPORARY TABLE tmp_test_regatta (id BIGINT UNSIGNED NOT NULL PRIMARY KEY);",
    "INSERT IGNORE INTO tmp_test_regatta SELECT id FROM regatta WHERE id_promoter IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_regatta SELECT id FROM regatta WHERE id_variant IN (SELECT id FROM tmp_test_variant);",
    "CREATE TEMPORARY TABLE tmp_test_registration (id BIGINT UNSIGNED NOT NULL PRIMARY KEY);",
    "INSERT IGNORE INTO tmp_test_registration SELECT id FROM registration WHERE id_regatta IN (SELECT id FROM tmp_test_regatta);",
    "INSERT IGNORE INTO tmp_test_registration SELECT id FROM registration WHERE id_driver IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_registration SELECT id FROM registration WHERE id_owner IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_registration SELECT id FROM registration WHERE id_buyer IN (SELECT id FROM tmp_test_participant);",
    "INSERT IGNORE INTO tmp_test_registration SELECT id FROM registration WHERE id_car IN (SELECT id FROM tmp_test_car);",
    "DELETE FROM bid WHERE id_participant IN (SELECT id FROM tmp_test_participant) OR id_registration IN (SELECT id FROM tmp_test_registration);",
    "DELETE FROM penaltiespl WHERE id_regatta IN (SELECT id FROM tmp_test_regatta);",
    "DELETE FROM registration WHERE id IN (SELECT id FROM tmp_test_registration) OR id_regatta IN (SELECT id FROM tmp_test_regatta);",
    "DELETE FROM pointscount WHERE id_participant IN (SELECT id FROM tmp_test_participant);",
    "DELETE FROM regatta WHERE id IN (SELECT id FROM tmp_test_regatta);",
    "DELETE FROM car WHERE id IN (SELECT id FROM tmp_test_car);",
    "DELETE FROM variant WHERE id IN (SELECT id FROM tmp_test_variant);",
    "DELETE FROM venue WHERE id IN (SELECT id FROM tmp_test_venue);",
    "DELETE FROM participant WHERE id IN (SELECT id FROM tmp_test_participant);",
    "DELETE pr FROM provinceregion pr LEFT JOIN venue v ON v.id_provinceregion = pr.id WHERE v.id IS NULL AND pr.name REGEXP '^(Geo Province Region|ProvinceRegion-|lists-|http-lists-|Province Region-)';",
    "DELETE p FROM province p LEFT JOIN provinceregion pr ON pr.id_province = p.id WHERE pr.id IS NULL AND p.name REGEXP '^(Geo Province|Province-|lists-|http-lists-)';",
    "DELETE cr FROM countryregion cr LEFT JOIN province p ON p.id_countryregion = cr.id WHERE p.id IS NULL AND cr.name REGEXP '^(Geo Country Region|CountryRegion-|lists-|http-lists-|Country Region-)';",
    "DELETE c FROM country c LEFT JOIN countryregion cr ON cr.id_country = c.id WHERE cr.id IS NULL AND c.name REGEXP '^(Geo Country|Country-|lists-|http-lists-)';",
    "DELETE pl FROM planetregion pl LEFT JOIN country c ON c.id_planetregion = pl.id WHERE c.id IS NULL AND pl.name REGEXP '^(Geo Planet Region|PlanetRegion-|lists-|http-lists-|Planet Region-)';",
    "SET FOREIGN_KEY_CHECKS=1;",
  ];
}

function buildVenueSql( row,
                        idCreator ) {
  const planet = sqlString( row.planetRegion );
  const country = sqlString( row.country );
  const countryRegion = sqlString( row.countryRegion );
  const province = sqlString( row.province );
  const provinceRegion = sqlString( row.provinceRegion );
  const venue = sqlString( row.name );
  const variant = sqlString( VARIANT_NAME );
  return [
    `INSERT INTO planetregion (name, id_creator) SELECT ${planet}, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM planetregion WHERE name = ${planet});`,
    `SET @planetregion_id = (SELECT id FROM planetregion WHERE name = ${planet} ORDER BY id LIMIT 1);`,
    `INSERT INTO country (name, id_planetregion, id_creator) SELECT ${country}, @planetregion_id, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM country WHERE name = ${country} AND id_planetregion = @planetregion_id);`,
    `SET @country_id = (SELECT id FROM country WHERE name = ${country} AND id_planetregion = @planetregion_id ORDER BY id LIMIT 1);`,
    `INSERT INTO countryregion (name, id_country, id_creator) SELECT ${countryRegion}, @country_id, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM countryregion WHERE name = ${countryRegion} AND id_country = @country_id);`,
    `SET @countryregion_id = (SELECT id FROM countryregion WHERE name = ${countryRegion} AND id_country = @country_id ORDER BY id LIMIT 1);`,
    `INSERT INTO province (name, id_countryregion, id_creator) SELECT ${province}, @countryregion_id, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM province WHERE name = ${province} AND id_countryregion = @countryregion_id);`,
    `SET @province_id = (SELECT id FROM province WHERE name = ${province} AND id_countryregion = @countryregion_id ORDER BY id LIMIT 1);`,
    `INSERT INTO provinceregion (name, id_province, id_creator) SELECT ${provinceRegion}, @province_id, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM provinceregion WHERE name = ${provinceRegion} AND id_province = @province_id);`,
    `SET @provinceregion_id = (SELECT id FROM provinceregion WHERE name = ${provinceRegion} AND id_province = @province_id ORDER BY id LIMIT 1);`,
    `INSERT INTO venue (name, id_owner, meridian, parallel, id_provinceregion, id_creator) SELECT ${venue}, ${idCreator}, ${row.lon}, ${row.lat}, @provinceregion_id, ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM venue WHERE name = ${venue} AND ABS(meridian - ${row.lon}) < 0.000001 AND ABS(parallel - ${row.lat}) < 0.000001);`,
    `SET @venue_id = (SELECT id FROM venue WHERE name = ${venue} AND ABS(meridian - ${row.lon}) < 0.000001 AND ABS(parallel - ${row.lat}) < 0.000001 ORDER BY id LIMIT 1);`,
    `INSERT INTO variant (name, id_venue, min_width, length, metric, id_creator) SELECT ${variant}, @venue_id, 0, 0, b'1', ${idCreator} WHERE NOT EXISTS (SELECT 1 FROM variant WHERE id_venue = @venue_id);`,
  ];
}

function sanitizeMysqlText( value ) {
  return value.replace( /[\u{10000}-\u{10FFFF}]/gu,
                        "" )
    .replace( /\s+/g,
              " " )
    .trim();
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

function delay( ms ) {
  return new Promise( ( resolve ) => setTimeout( resolve,
                                                 ms ) );
}
