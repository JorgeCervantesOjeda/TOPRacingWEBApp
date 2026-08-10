<!-- docs/superpowers/plans/2026-08-10-topracing-mcp-championship-table-server.md - Server implementation plan for MCP championship table database access and stdio transport. -->

# TOP Racing MCP Championship Table Server Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Connect the tested championship table core to read-only MySQL queries and expose it through a local stdio MCP server.

**Architecture:** Add a MySQL data-access module that returns sanitized public standings records, then register a single MCP tool that returns the table or estimate response. Verify the server with unit tests and a local stdio client smoke script.

**Tech Stack:** Node.js ES modules, `node:test`, official `@modelcontextprotocol/sdk`, `mysql2/promise`, `zod`, MySQL `topracing26`.

## Global Constraints

- Do not modify JSF, Hibernate mappings, or production Java behavior.
- Use only `SELECT` or `COUNT` queries.
- Do not expose e-mail, phone, password, confirmation keys, recovery keys, PayPal fields, complaint keys, payment data, bids, or unpublished auction state.
- Do not silently fall back to sample data when MySQL is unavailable.
- Successful tool output must be a table object.
- Add file header comments to new files.

---

## File Structure

- Create `scripts/mcp/topracing-db.mjs`: connection pool and read-only championship queries.
- Create `scripts/topracing-mcp-stdio.mjs`: MCP server entrypoint and tool registration.
- Create `scripts/test-mcp-championship-table.mjs`: local smoke test client.
- Create `tests/mcp/topracing-db-sql.test.mjs`: SQL builder tests without MySQL.
- Create `tests/mcp/topracing-mcp-tool.test.mjs`: tool handler tests with injected dependencies.

---

### Task 3: Read-Only MySQL Query Builder

**Files:**
- Create: `scripts/mcp/topracing-db.mjs`
- Test: `tests/mcp/topracing-db-sql.test.mjs`

**Interfaces:**
- Consumes: parsed input object from `parseChampionshipInput`
- Produces: `buildChampionshipCountQuery(parsedInput: object): { sql: string, params: Array<unknown> }`
- Produces: `buildChampionshipRowsQuery(parsedInput: object): { sql: string, params: Array<unknown> }`
- Produces: `createTopRacingPool(env?: object): mysql.Pool`
- Produces: `countChampionshipRows(pool: object, parsedInput: object): Promise<number>`
- Produces: `fetchChampionshipRows(pool: object, parsedInput: object): Promise<Array<object>>`

- [ ] **Step 1: Write the failing test**

```javascript
// tests/mcp/topracing-db-sql.test.mjs
// Verifies read-only SQL construction for the TOP Racing MCP prototype.
import test from "node:test";
import assert from "node:assert/strict";
import { parseChampionshipInput } from "../../scripts/mcp/championship-levels.mjs";
import { buildChampionshipCountQuery, buildChampionshipRowsQuery } from "../../scripts/mcp/topracing-db.mjs";

test("builds parameterized row query with level filters and pagination", () => {
  const parsed = parseChampionshipInput({ periodLevel: "month", tracksetLevel: "country", limit: 25, offset: 50 });
  const query = buildChampionshipRowsQuery(parsed);

  assert.match(query.sql, /^SELECT /);
  assert.doesNotMatch(query.sql, /;/);
  assert.match(query.sql, /FROM pointscount pc/);
  assert.match(query.sql, /pc\.level_period = \?/);
  assert.deepEqual(query.params.slice(-4), [4, 2, 25, 50]);
});

test("builds count query without limit or offset", () => {
  const parsed = parseChampionshipInput({ periodLevel: "week", tracksetLevel: "venue" });
  const query = buildChampionshipCountQuery(parsed);

  assert.match(query.sql, /^SELECT COUNT\(\*\) AS countOfRows/);
  assert.doesNotMatch(query.sql, /LIMIT/);
  assert.deepEqual(query.params, [5, 6]);
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test:mcp`

Expected: FAIL because `scripts/mcp/topracing-db.mjs` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `scripts/mcp/topracing-db.mjs`. It must:

- import `mysql2/promise`;
- create a pool from `TOPRACING_DB_HOST`, `TOPRACING_DB_PORT`, `TOPRACING_DB_USERNAME`, `TOPRACING_DB_PASSWORD`, and `TOPRACING_DB_CATALOG`, defaulting to local `admin/admin/topracing26`;
- build `COUNT(*) AS countOfRows FROM pointscount pc WHERE pc.level_period = ? AND pc.level_trackset = ?` plus optional `pc.id_period = ?` and `pc.id_trackset = ?`;
- build the row query with selected public fields from `pointscount` and a left join to `participant`;
- use `CASE WHEN p.confirmed = 1 THEN CONCAT(p.names_family, ', ', p.names_given) ELSE 'Participant pending' END AS participantName`;
- append `LIMIT ? OFFSET ?`;
- never concatenate user values into SQL.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm run test:mcp`

Expected: PASS.

- [ ] **Step 5: Manual schema check**

Run:

```powershell
mysql -u admin -padmin -D topracing26 -e "SHOW COLUMNS FROM pointscount; SHOW COLUMNS FROM participant;"
```

Expected: columns include `id_participant`, `level_period`, `id_period`, `level_trackset`, `id_trackset`, point columns, `confirmed`, `names_family`, and `names_given`. If actual column names differ, update SQL and tests to match the schema.

- [ ] **Step 6: Commit**

```bash
git add scripts/mcp/topracing-db.mjs tests/mcp/topracing-db-sql.test.mjs
git commit -m "feat: add read-only championship SQL queries"
```

---

### Task 4: MCP Stdio Server

**Files:**
- Create: `scripts/topracing-mcp-stdio.mjs`
- Test: `tests/mcp/topracing-mcp-tool.test.mjs`

**Interfaces:**
- Consumes: `parseChampionshipInput`, `countChampionshipRows`, `fetchChampionshipRows`, `buildChampionshipEstimate`, `buildChampionshipTable`
- Produces: MCP tool `get_championship_table`
- Produces: `handleGetChampionshipTable(input: object, dependencies: object): Promise<object>`
- Produces: `createServer(dependencies: object): McpServer`

- [ ] **Step 1: Write the failing test**

```javascript
// tests/mcp/topracing-mcp-tool.test.mjs
// Verifies MCP tool handler behavior without opening stdio transport.
import test from "node:test";
import assert from "node:assert/strict";
import { handleGetChampionshipTable } from "../../scripts/topracing-mcp-stdio.mjs";

test("returns estimate table from injected database functions", async () => {
  const result = await handleGetChampionshipTable(
    { estimateOnly: true, periodLevel: "month", tracksetLevel: "country" },
    {
      countChampionshipRows: async () => 725,
      fetchChampionshipRows: async () => { throw new Error("fetch should not run in estimate mode"); },
    },
  );

  assert.equal(result.structuredContent.metadata.estimateOnly, true);
  assert.equal(result.structuredContent.metadata.estimatedRows, 725);
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `npm run test:mcp`

Expected: FAIL because `scripts/topracing-mcp-stdio.mjs` does not exist.

- [ ] **Step 3: Write minimal implementation**

Create `scripts/topracing-mcp-stdio.mjs`. It must:

- import `McpServer` from `@modelcontextprotocol/sdk/server/mcp.js`;
- import `StdioServerTransport` from `@modelcontextprotocol/sdk/server/stdio.js`;
- import `z` from `zod`;
- define `toolSchema` for the spec inputs;
- export `handleGetChampionshipTable`;
- return `{ structuredContent: table, content: [{ type: "text", text: table.markdownTable }] }`;
- export `createServer`;
- register `get_championship_table` with title, description, input schema, and handler;
- start stdio transport only when run as the main script.

- [ ] **Step 4: Run test to verify it passes**

Run: `npm run test:mcp`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add scripts/topracing-mcp-stdio.mjs tests/mcp/topracing-mcp-tool.test.mjs
git commit -m "feat: expose championship table MCP tool"
```

---

### Task 5: Local End-To-End MCP Smoke Test

**Files:**
- Create: `scripts/test-mcp-championship-table.mjs`

**Interfaces:**
- Consumes: `scripts/topracing-mcp-stdio.mjs`
- Produces: local stdio client smoke test for `get_championship_table`

- [ ] **Step 1: Write smoke script**

Create `scripts/test-mcp-championship-table.mjs`. It must:

- import `Client` from `@modelcontextprotocol/sdk/client/index.js`;
- import `StdioClientTransport` from `@modelcontextprotocol/sdk/client/stdio.js`;
- start `node scripts/topracing-mcp-stdio.mjs`;
- call `client.listTools()`;
- verify `get_championship_table` exists;
- call `get_championship_table` with `periodLevel: "continuous"`, `tracksetLevel: "planet"`, and `estimateOnly: true`;
- print `structuredContent` as formatted JSON;
- close the client.

- [ ] **Step 2: Run smoke test**

Run: `node scripts/test-mcp-championship-table.mjs`

Expected: JSON table with `metadata.estimateOnly=true`, or a clear database connection error naming the cause.

- [ ] **Step 3: Run table query manually**

Temporarily call the tool with:

```javascript
{
  periodLevel: "continuous",
  tracksetLevel: "planet",
  estimateOnly: false,
  limit: 10
}
```

Expected: JSON table with standings rows or an empty public table if the database has no matching `pointscount` records.

- [ ] **Step 4: Restore smoke script**

Restore the committed smoke script to `estimateOnly: true`.

- [ ] **Step 5: Full verification**

Run:

```powershell
npm run test:mcp
node scripts/test-mcp-championship-table.mjs
```

Expected: tests pass; smoke test returns a table or a clear database error.

- [ ] **Step 6: Commit**

```bash
git add scripts/test-mcp-championship-table.mjs
git commit -m "test: add championship MCP smoke test"
```

---

## Plan Self-Review

- Spec coverage: This server plan covers read-only MySQL access, explicit no-fallback failure, stdio MCP transport, tool registration, estimate smoke test, and local verification.
- Placeholder scan: No deferred implementation markers remain in task steps.
- Type consistency: Tool handler consumes the core modules from `2026-08-10-topracing-mcp-championship-table-core.md`.
- Known residual risk: SQL column names must be verified against the live MySQL schema before treating the prototype as working.

