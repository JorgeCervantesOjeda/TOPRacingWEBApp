$ErrorActionPreference = "Stop"

$mysqlsh = if( $env:TOPRACING_MYSQLSH ) {
  $env:TOPRACING_MYSQLSH
} else {
  "C:\Program Files\MySQL\MySQL Shell 8.0\bin\mysqlsh.exe"
}

$sql = @"
CREATE DATABASE IF NOT EXISTS topracing26;
USE topracing26;

CREATE TABLE IF NOT EXISTS appstats (
  id INT NOT NULL,
  sessioncount BIGINT NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO appstats (id, sessioncount)
VALUES (1, 0)
ON DUPLICATE KEY UPDATE id = id;
"@

& $mysqlsh --sql --user=admin --password=admin --host=localhost --port=3306 -e $sql
