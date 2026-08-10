<!-- docs/superpowers/plans/2026-08-10-topracing-mcp-championship-table-core.md - Core implementation plan for MCP championship table parsing and response shaping. -->

# TOP Racing MCP Championship Table Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the pure, testable core for a local MCP `get_championship_table` tool.

**Architecture:** Keep championship query parsing and table shaping in small Node `.mjs` modules before adding database or MCP transport code. This lets tests verify the public contract without needing MySQL or a chatbot client.

**Tech Stack:** Node.js ES modules, `node:test`, future MCP SDK integration.

## Global Constraints

- Do not modify JSF, Hibernate mappings, or production Java behavior.
- Do not connect to MySQL in this core plan.
- Successful output must always be a table object with `columns`, `rows`, `metadata`, and optional `markdownTable`.
- Use Spanish labels by default.
- Preserve public privacy rules: participant names must already be sanitized before table rendering.
- Add file header comments to new files.

---

## File Structure

- Create `scripts/mcp/championship-levels.mjs`: parses user/chatbot filters and defines point-column presets.
- Create `scripts/mcp/championship-table.mjs`: converts normalized records into table or estimate responses.
- Create `tests/mcp/championship-levels.test.mjs`: tests parsing and point-column presets.
- Create `tests/mcp/championship-table.test.mjs`: tests estimate and standings table shaping.
- Modify `package.json`: add `test:mcp` and future MCP dependencies.

---

### Task 1: Level And Filter Parsing

**Files:**
- Create: `scripts/mcp/championship-levels.mjs`
- Test: `tests/mcp/championship-levels.test.mjs`
- Modify: `package.json`

**Interfaces:**
- Produces: `parseChampionshipInput(input: object): object`
- Produces: `pointColumnsFor(role: string, modality: string, language: string): Array<object>`
- Produces: `levelLabel(kind: "period" | "trackset", value: number, language: string): string`

- [ ] **Step 1: Write the failing test**

```javascript
// tests/mcp/championship-levels.test.mjs
// Verifies championship MCP filter parsing.
import test from "node:test";
import assert from "node:assert/strict";
import { parseChampionshipInput, pointColumnsFor } from "../../scripts/mcp/championship-levels.mjs";

test("parses championship filters into app numeric levels", () => {
  const parsed = parseChampionshipInput({
    periodLevel: "month",
    tracksetLevel: "country",
    role: "driver",
    modality: "race",
    language: "es",
  });

  assert.equal(parsed.periodLevel, 4);
  assert.equal(parsed.tracksetLevel, 2);
  assert.equal(parsed.role, "driver");
  assert.equal(parsed.modality, "race");
  assert.equal(parsed.language, "es");
});

test("selects race driver point column", () => {
  assert.deepEqual(pointColumnsFor("driver", "race", "es"), [
    { key: "raceDriver", label: "Carrera Piloto", source: "pointsRD" },
  ]);
});
```

- [ ] **Step 2: Add test script and dependencies**

Update `package.json` while keeping existing fields:

```json
"scripts": {
  "test:e2e:live": "playwright test",
  "test:mcp": "node --test tests/mcp/*.test.mjs",
  "mcp:championships": "node scripts/topracing-mcp-stdio.mjs"
},
"dependencies": {
  "@modelcontextprotocol/sdk": "^1.17.0",
  "mysql2": "^3.14.0",
  "zod": "^3.25.0"
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `npm run test:mcp`

Expected: FAIL because `scripts/mcp/championship-levels.mjs` does not exist.

- [ ] **Step 4: Write minimal implementation**

Create `scripts/mcp/championship-levels.mjs` with:

```javascript
// scripts/mcp/championship-levels.mjs
// Parses championship table filters and point-column presets for the TOP Racing MCP prototype.

const periodAliases = new Map([
  ["continuous", 0], ["continuo", 0], ["decade", 1], ["década", 1],
  ["year", 2], ["año", 2], ["season", 3], ["temporada", 3],
  ["quarter", 3], ["trimestre", 3], ["month", 4], ["mes", 4],
  ["week", 5], ["semana", 5],
]);

const tracksetAliases = new Map([
  ["planet", 0], ["planeta", 0], ["planet_region", 1], ["región_planetaria", 1],
  ["country", 2], ["país", 2], ["country_region", 3], ["región_país", 3],
  ["province", 4], ["provincia", 4], ["province_region", 5], ["región_provincia", 5],
  ["venue", 6], ["sede", 6], ["variant", 7], ["variante", 7],
]);

const pointColumnDefinitions = {
  speedDriver: ["Velocidad Piloto", "Speed Driver", "pointsSD"],
  raceDriver: ["Carrera Piloto", "Race Driver", "pointsRD"],
  efficiencyDriver: ["Eficiencia Piloto", "Efficiency Driver", "pointsED"],
  speedOwner: ["Velocidad Dueño", "Speed Owner", "pointsSO"],
  raceOwner: ["Carrera Dueño", "Race Owner", "pointsRO"],
  efficiencyOwner: ["Eficiencia Dueño", "Efficiency Owner", "pointsEO"],
};

export function parseChampionshipInput(input = {}) {
  return {
    periodLevel: parseLevel(input.periodLevel, periodAliases, 0),
    periodId: optionalInteger(input.periodId),
    tracksetLevel: parseLevel(input.tracksetLevel, tracksetAliases, 0),
    tracksetId: optionalInteger(input.tracksetId),
    role: normalizeChoice(input.role, ["driver", "owner", "both"], "both"),
    modality: normalizeChoice(input.modality, ["speed", "race", "efficiency", "speed_race_total", "all"], "all"),
    limit: boundedInteger(input.limit, 100, 1, 1000),
    offset: boundedInteger(input.offset, 0, 0, 1000000),
    sortBy: typeof input.sortBy === "string" ? input.sortBy : "period",
    sortDirection: normalizeChoice(input.sortDirection, ["asc", "desc"], undefined),
    language: normalizeChoice(input.language, ["es", "en"], "es"),
    estimateOnly: input.estimateOnly === true,
  };
}

export function pointColumnsFor(role, modality, language = "es") {
  if (role === "driver" && modality === "speed_race_total") {
    return [{ key: "driverSpeedRaceTotal", label: label("Velocidad + Carrera Piloto", "Speed + Race Driver", language), source: "driverSpeedRaceTotal" }];
  }
  const keys = [];
  if ((role === "driver" || role === "both") && (modality === "speed" || modality === "all")) keys.push("speedDriver");
  if ((role === "driver" || role === "both") && (modality === "race" || modality === "all")) keys.push("raceDriver");
  if ((role === "driver" || role === "both") && (modality === "efficiency" || modality === "all")) keys.push("efficiencyDriver");
  if ((role === "owner" || role === "both") && (modality === "speed" || modality === "all")) keys.push("speedOwner");
  if ((role === "owner" || role === "both") && (modality === "race" || modality === "all")) keys.push("raceOwner");
  if ((role === "owner" || role === "both") && (modality === "efficiency" || modality === "all")) keys.push("efficiencyOwner");
  return keys.map((key) => {
    const [labelEs, labelEn, source] = pointColumnDefinitions[key];
    return { key, label: label(labelEs, labelEn, language), source };
  });
}

export function levelLabel(kind, value, language = "es") {
  const periods = language === "en" ? ["Continuous", "Decade", "Year", "Season", "Month", "Week"] : ["Continuo", "Década", "Año", "Temporada", "Mes", "Semana"];
  const tracksets = language === "en" ? ["Planet", "Planet Region", "Country", "Country Region", "Province", "Province Region", "Venue", "Variant"] : ["Planeta", "Región Planetaria", "País", "Región De País", "Provincia", "Región De Provincia", "Sede", "Variante"];
  return kind === "period" ? periods[value] : tracksets[value];
}

function label(labelEs, labelEn, language) { return language === "en" ? labelEn : labelEs; }
function parseLevel(value, aliases, defaultValue) {
  if (value === undefined || value === null || value === "") return defaultValue;
  if (Number.isInteger(value)) return value;
  const normalized = String(value).trim().toLowerCase().replaceAll(" ", "_");
  if (!aliases.has(normalized)) throw new Error(`Unsupported championship level: ${value}`);
  return aliases.get(normalized);
}
function optionalInteger(value) {
  if (value === undefined || value === null || value === "") return undefined;
  const numeric = Number(value);
  if (!Number.isInteger(numeric)) throw new Error(`Expected integer value: ${value}`);
  return numeric;
}
function boundedInteger(value, defaultValue, minimum, maximum) {
  const numeric = value === undefined || value === null || value === "" ? defaultValue : Number(value);
  if (!Number.isInteger(numeric) || numeric < minimum || numeric > maximum) throw new Error(`Expected integer from ${minimum} to ${maximum}: ${value}`);
  return numeric;
}
function normalizeChoice(value, allowed, defaultValue) {
  if (value === undefined || value === null || value === "") return defaultValue;
  const normalized = String(value).trim().toLowerCase();
  if (!allowed.includes(normalized)) throw new Error(`Unsupported value: ${value}`);
  return normalized;
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `npm install`, then `npm run test:mcp`

Expected: PASS for `championship-levels.test.mjs`.

- [ ] **Step 6: Commit**

```bash
git add package.json package-lock.json scripts/mcp/championship-levels.mjs tests/mcp/championship-levels.test.mjs
git commit -m "feat: add championship MCP filter parsing"
```

---

### Task 2: Pure Table Builder

**Files:**
- Create: `scripts/mcp/championship-table.mjs`
- Test: `tests/mcp/championship-table.test.mjs`

**Interfaces:**
- Consumes: `parseChampionshipInput`
- Consumes: `pointColumnsFor`
- Produces: `buildChampionshipEstimate(estimatedRows: number, parsedInput: object, options?: object): object`
- Produces: `buildChampionshipTable(records: Array<object>, parsedInput: object): object`

- [ ] **Step 1: Write the failing test**

```javascript
// tests/mcp/championship-table.test.mjs
// Verifies tabular championship MCP responses.
import test from "node:test";
import assert from "node:assert/strict";
import { parseChampionshipInput } from "../../scripts/mcp/championship-levels.mjs";
import { buildChampionshipEstimate, buildChampionshipTable } from "../../scripts/mcp/championship-table.mjs";

test("builds estimate table without standings rows", () => {
  const parsed = parseChampionshipInput({ estimateOnly: true, periodLevel: "month", tracksetLevel: "country" });
  const table = buildChampionshipEstimate(725, parsed);

  assert.equal(table.metadata.estimateOnly, true);
  assert.equal(table.metadata.isLargeResult, true);
  assert.deepEqual(table.rows[0], { metric: "Renglones estimados", value: 725 });
});

test("builds all-column public standings table", () => {
  const parsed = parseChampionshipInput({ role: "both", modality: "all", limit: 10 });
  const table = buildChampionshipTable([{
    periodName: "Continuo", tracksetName: "Planeta", participantName: "Participant pending",
    pointsSD: 10, pointsRD: 20, pointsED: 5, pointsSO: 7, pointsRO: 9, pointsEO: 3,
  }], parsed);

  assert.equal(table.columns[0].key, "rank");
  assert.equal(table.rows[0].rank, 1);
  assert.equal(table.rows[0].raceDriver, 20);
  assert.equal(table.metadata.publicOnly, true);
  assert.match(table.markdownTable, /Participant pending/);
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test:mcp`

Expected: FAIL because `scripts/mcp/championship-table.mjs` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `scripts/mcp/championship-table.mjs`:

```javascript
// scripts/mcp/championship-table.mjs
// Builds table-shaped MCP responses for public TOP Racing championship standings.
import { pointColumnsFor } from "./championship-levels.mjs";

const largeResultThreshold = 500;

export function buildChampionshipEstimate(estimatedRows, parsedInput, options = {}) {
  const threshold = options.largeResultThreshold ?? largeResultThreshold;
  const isLargeResult = estimatedRows > threshold;
  const rows = [
    { metric: "Renglones estimados", value: estimatedRows },
    { metric: "Consulta grande", value: isLargeResult ? "Sí" : "No" },
    { metric: "Límite recomendado", value: parsedInput.limit },
  ];
  const columns = [{ key: "metric", label: "Métrica" }, { key: "value", label: "Valor" }];
  return {
    columns,
    rows,
    metadata: { estimateOnly: true, publicOnly: true, estimatedRows, isLargeResult, recommendedLimit: parsedInput.limit, suggestedFilters: isLargeResult ? ["periodId", "tracksetId", "role", "modality", "limit"] : [] },
    markdownTable: renderMarkdownTable(columns, rows),
  };
}

export function buildChampionshipTable(records, parsedInput) {
  const pointColumns = pointColumnsFor(parsedInput.role, parsedInput.modality, parsedInput.language);
  const columns = [
    { key: "rank", label: parsedInput.language === "en" ? "Rank" : "Posición" },
    { key: "periodName", label: parsedInput.language === "en" ? "Period" : "Periodo" },
    { key: "tracksetName", label: parsedInput.language === "en" ? "Trackset" : "Ámbito" },
    { key: "participantName", label: parsedInput.language === "en" ? "Participant" : "Participante" },
    ...pointColumns.map(({ key, label }) => ({ key, label })),
  ];
  const rows = records.map((record, index) => {
    const row = { rank: parsedInput.offset + index + 1, periodName: record.periodName, tracksetName: record.tracksetName, participantName: record.participantName };
    for (const column of pointColumns) row[column.key] = Number(record[column.source] ?? 0);
    return row;
  });
  return {
    columns,
    rows,
    metadata: { estimateOnly: false, publicOnly: true, limit: parsedInput.limit, offset: parsedInput.offset, returnedRows: rows.length, role: parsedInput.role, modality: parsedInput.modality },
    markdownTable: renderMarkdownTable(columns, rows),
  };
}

function renderMarkdownTable(columns, rows) {
  const keys = columns.map((column) => column.key);
  const header = `| ${columns.map((column) => column.label ?? column.key).join(" | ")} |`;
  const divider = `| ${columns.map(() => "---").join(" | ")} |`;
  const body = rows.map((row) => `| ${keys.map((key) => String(row[key] ?? "")).join(" | ")} |`);
  return [header, divider, ...body].join("\n");
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `npm run test:mcp`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add scripts/mcp/championship-table.mjs tests/mcp/championship-table.test.mjs
git commit -m "feat: build championship MCP table responses"
```

---

## Plan Self-Review

- Spec coverage: This core plan covers parsing levels, roles, modalities, estimate table shape, standings table shape, and Spanish default labels.
- Placeholder scan: No deferred implementation markers remain in task steps.
- Type consistency: `parseChampionshipInput` output is consumed by the table builder exactly as defined.
- Continuation: Implement database access, MCP stdio transport, and smoke testing in `2026-08-10-topracing-mcp-championship-table-server.md`.

