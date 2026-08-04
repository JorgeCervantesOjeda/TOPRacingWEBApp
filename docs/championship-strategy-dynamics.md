# Championship Strategy Dynamics

This document explains how a competitor can reason about TOP Racing
championship goals across territorial and temporal levels.

It is explanatory guidance. The normative sources remain the SRS requirements
for period levels, territorial levels, event weight matrices, P/C, and scaled
points.

## Confirmed Rules

TOP Racing combines two axes for championships:

- a temporal level;
- a territorial level.

The target temporal levels are:

1. continuous;
2. decade;
3. year;
4. season;
5. month;
6. week.

The target territorial chain is:

1. planet;
2. planetregion;
3. country;
4. countryregion;
5. province;
6. provinceregion;
7. venue;
8. variant.

An event has a matrix of weights across applicable temporal and territorial
levels. The lowest levels, week and variant, keep fixed weight 100.

When comparable events compete for weight in a higher championship, they are
ordered by P/C. The event that keeps the higher rank keeps the higher value in
the base sequence, and lower ranked comparable events descend through:

`100, 63, 40, 25, 16, 10, 6.3, 4, 2.5, 1.6, 1, ...`

Scaled points are computed from:

`scaled points = base points * event weight / 100`

## Cell-Based Reasoning

A championship at a given pair of levels is evaluated through cells formed by
the immediately lower temporal level and the immediately lower territorial
level.

For example:

- `month + countryregion` creates cells of `week + province`;
- `season + countryregion` creates cells of `month + province`;
- `year + country` creates cells of `season + countryregion`;
- `decade + planetregion` creates cells of `year + country`;
- `continuous + planet` creates cells of `decade + countryregion`.

Each cell has its own ordered list of comparable events. The first event in
that cell has weight 100, the second 63, the third 40, and so on.

This means that a single week can contain several events of weight 100 when
they belong to different territorial cells. It also means that several events
inside the same cell can still be useful, even after the first event, because
the next values in the sequence still contribute points.

## Strategic Tradeoffs

A competitor does not simply choose between "race everything" and "race only
the top event." The real decision mixes:

- coverage across cells;
- depth inside a cell;
- travel cost;
- time cost;
- vehicle preparation and fatigue;
- probability of scoring well;
- event P/C and resulting weight;
- density of additional events in the same trip.

The best strategy depends on the championship level being pursued.

## Weekly Championships

At the weekly temporal level, events keep weight 100. A competitor pursuing a
weekly championship benefits from attending as many computable events as
practical within the relevant territorial scope.

The main constraint is logistical. If several events occur in the same weekend
and same area, attending several of them can be attractive because travel has
already been paid.

Example:

A driver competing for a weekly `province + race` championship may attend the
main event at a venue and then enter additional variants or nearby events. The
additional events may not improve a higher-level matrix position, but they can
still add meaningful points at the weekly level.

## Monthly Championships

A monthly championship is not one flat list of all events in the month.

For `month + countryregion`, the practical cells are:

`week + province`

If a month has 4 weeks and the countryregion has 3 provinces, there are 12
cells. Each cell has its own event list:

- week 1 + province A: `100, 63, 40, ...`;
- week 1 + province B: `100, 63, 40, ...`;
- week 1 + province C: `100, 63, 40, ...`;
- week 2 + province A: `100, 63, 40, ...`;
- and so on.

A competitor can stay in one province and still find a weight-100 event each
week, if that province has events each week. This can be a rational low-travel
strategy.

However, another province may offer a better package in a given week:

- one event at 100;
- another at 63;
- another at 40;
- better expected finishing positions;
- stronger participation;
- better logistics once the trip is already made.

The competitor may travel not because the local province lacks a 100 event, but
because the total opportunity in the other province is better.

## Season Championships

A season championship raises the comparison one level.

For `season + countryregion`, the cells are:

`month + province`

The competitor now asks: in each month of the season, which province offers the
best event package?

Staying local may still work when the home province has strong monthly
opportunities. Traveling becomes more attractive when another province has a
cluster of high-weight events in one month or when the home province is crowded
with comparable events that push some weights down.

## Year Championships

For `year + country`, the cells are:

`season + countryregion`

The competitor thinks nationally. The goal is not just to dominate one local
province, but to decide which countryregions offer the best seasonal packages.

Example:

A Mexican competitor pursuing a `year + Mexico` championship may spend one
season focused on Central Mexico, another on Northeast Mexico, and another on
Western Mexico, depending on where the strongest event packages appear. The
competitor may still skip some countryregions if the travel cost is too high
or if the expected points are weak.

## Decade Championships

For `decade + planetregion`, the cells are:

`year + country`

The competitor now evaluates countries across years. A driver may focus on
nearby countries with strong event calendars instead of trying to appear
everywhere.

Example:

A North American competitor may build a decade campaign around Mexico, the
United States, and Canada. In each year, the driver identifies which country
has the best accessible package and whether crossing the border is worth the
cost.

## Continuous Championships

For `continuous + planet`, the cells are:

`decade + countryregion`

This is the broadest competitive horizon. It does not require a competitor to
travel constantly around the entire planet. A competitor can pursue a serious
planet-level position by dominating a strong planetregion or by selectively
traveling to other regions when the event package is exceptional.

Example:

A Mexican competitor pursuing a planet-level objective may focus on North
America. The driver can travel among Mexico, the United States, and Canada to
attend the best yearly or seasonal packages in the planetregion. This strategy
keeps travel practical while still targeting high-value cells.

The same competitor might travel outside North America only when a European,
South American, or Asian event package has enough weight and expected points to
justify the cost.

## Promoter Dynamics

The matrix also shapes promoter incentives.

If a promoter schedules several events in the same weekend and same territorial
cell, the events compete with one another for weight:

- best P/C event: 100;
- next event: 63;
- next event: 40;
- next event: 25;
- next event: 16.

This does not make the lower-weight events useless. Competitors already present
may enter them because the marginal travel cost is low.

A promoter can therefore create a weekend package:

- one flagship event with the strongest weight;
- secondary events that still add points;
- more reasons for competitors to travel;
- more value from the same logistics.

Alternatively, a promoter can reduce congestion by choosing a different
territory or period. If fewer comparable events overlap in that cell, the event
has a better chance of keeping weight 100.

## Practical Interpretation

The system rewards distributed commitment without forcing impossible travel.

A competitor can choose among several strategies:

- local volume at lower cost;
- regional travel for stronger event packages;
- national campaigns built around countryregions;
- planetregion campaigns that avoid global travel most of the time;
- selective global travel for exceptional opportunities.

The right strategy depends on the championship target, budget, schedule,
vehicle reliability, expected performance, and event density.

