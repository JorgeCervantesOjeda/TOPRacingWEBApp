<!-- docs/superpowers/specs/2026-08-10-topracing-mcp-championship-table-design.md - Defines the first local MCP championship table prototype contract. -->

# TOP Racing MCP Championship Table Design

## Purpose

Build the first local TOP Racing MCP prototype around a single real read-only query: public championship standings. The server must let a chatbot request championship state in many natural ways while the MCP returns a deterministic table contract.

## Scope

The prototype exposes championship standings derived from existing `Pointscount` data. It does not create, modify, recalculate, or publish domain data. It does not expose private participant data, credentials, bids, payments, or subauction data.

## Observed Domain Model

`Pointscount` stores standings by participant, temporal level, period id, territorial level, and trackset id. The current implementation separates points by role and result type:

- `pointsSD`: speed as driver.
- `pointsSO`: speed as owner.
- `pointsRD`: race as driver.
- `pointsRO`: race as owner.
- `pointsED`: efficiency as driver.
- `pointsEO`: efficiency as owner.

The public UI already exposes these as tabular standings. Public participant names must follow the existing privacy rule: confirmed public name when allowed, neutral marker otherwise.

## MCP Tool

Expose one tool:

`get_championship_table`

Inputs:

- `periodLevel`: optional temporal level. Valid values are `continuous`, `decade`, `year`, `season`, `month`, `week`, or the corresponding app numeric level.
- `periodId`: optional period identifier. When absent, include all matching periods.
- `tracksetLevel`: optional territorial level. Valid values are `planet`, `planet_region`, `country`, `country_region`, `province`, `province_region`, `venue`, `variant`, or the corresponding app numeric level.
- `tracksetId`: optional territorial identifier. When absent, include all matching tracksets at the selected level.
- `role`: optional role filter. Valid values are `driver`, `owner`, `both`. Default is `both`.
- `modality`: optional modality filter. Valid values are `speed`, `race`, `efficiency`, `speed_race_total`, `all`. Default is `all`.
- `limit`: optional maximum number of rows returned. Default is `100`.
- `offset`: optional row offset for pagination. Default is `0`.
- `sortBy`: optional sort key. Valid values include `period`, `trackset`, `participant`, `speed_driver`, `race_driver`, `efficiency_driver`, `speed_owner`, `race_owner`, `efficiency_owner`, `driver_speed_race_total`.
- `sortDirection`: optional. Valid values are `asc` and `desc`. Default depends on `sortBy`; point columns default to `desc`.
- `language`: optional. Valid values are `es` and `en`. Default is `es`.
- `estimateOnly`: optional boolean. When true, return only a metadata table describing row count and suggested filters.

Output:

The result must always be a table object:

- `columns`: ordered column definitions with stable `key` and human label.
- `rows`: row objects matching the column keys.
- `metadata`: query summary, privacy flags, estimate flags, pagination, and any warnings.
- `markdownTable`: optional rendered Markdown table for clients that prefer text.

## Estimate Mode

When `estimateOnly` is true, the tool must not return standings rows. It must return a small table with:

- estimated row count;
- whether the result is large;
- recommended limit;
- selected filters;
- suggested additional filters such as `periodId`, `tracksetId`, `role`, `modality`, or `limit`.

A result is considered large when it exceeds the configured threshold. The prototype threshold is `500` rows. The chatbot should use this mode before broad queries and ask the user whether to narrow the request when the estimate is large.

## Public Data Rules

The tool returns public championship state only. It may expose:

- period name and id;
- territorial scope name and id;
- public participant display name or neutral marker;
- standings point columns;
- table metadata.

The tool must not expose:

- e-mail addresses;
- phone numbers;
- passwords or cryptographic material;
- confirmation, recovery, PayPal, or complaint keys;
- payment data;
- bid details;
- unpublished auction state.

## First Prototype Behavior

The first working prototype should support:

- local MCP transport over stdio;
- MySQL read-only access to `topracing26`;
- `get_championship_table` with `periodLevel`, `tracksetLevel`, `limit`, `offset`, `estimateOnly`, `role`, and `modality`;
- table output in Spanish labels by default;
- no writes to the application database.

If the app database is unavailable, the server must return an explicit tool error with the cause and impact. It must not silently fall back to sample data.

## Success Criteria

- A local MCP client can list the tool.
- A local MCP client can call `get_championship_table` in estimate mode and receive a table.
- A local MCP client can call `get_championship_table` for default public standings and receive a table.
- Returned rows do not include private participant fields.
- Broad queries can be estimated before fetching row data.
