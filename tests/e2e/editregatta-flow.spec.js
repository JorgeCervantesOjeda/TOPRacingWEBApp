// tests/e2e/editregatta-flow.spec.js
// Verifies authenticated legacy editor flows for regattas, registrations, and related entities.
const { test, expect } = require('@playwright/test');
const {
  baseUrl,
  clickAndAcceptConfirm,
  clickBackButton,
  createAccount,
  createEventFromPenalties,
  createRegistrationEditor,
  createSavedRegistrationAndReturnToResults,
  dismissWaitUi,
  editRegattaResultsCell,
  loadBrowserFixture,
  logoutIfVisible,
  navigateResultsToStatus,
  openFirstRegistrationFromResults,
  typeIntoFilter,
  typeIntoLastGlobalFilter
} = require('./support/editregatta-flow-helpers');

test.afterEach(async ({ page }) => {
  await logoutIfVisible(page);
});

test('authenticated user can create an event and navigate its legacy editors', async ({ page }) => {
  const email = `codex+regatta-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Regatta', 'Browser');

  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await expect(page.getByText('Create Event')).toBeVisible();

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create Event' }));
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);

  await expect(page.getByRole('heading', { name: 'View/Edit Event' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'View/Edit Registrations' })).toBeVisible();
  await expect(page.getByRole('button', { name: /Next status:/ })).toBeVisible();
  await expect(page.getByRole('button', { name: 'View Variants' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
  await expect(page.getByText('Priorities:')).toBeVisible();

  await page.getByRole('button', { name: 'View Variants' }).click();
  await expect(page).toHaveURL(/\/faces\/listvariants\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.getByRole('heading', { name: /Variants:/ })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create new Variant' })).toBeVisible();

  await page.goBack({ waitUntil: 'domcontentloaded' });
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'View/Edit Registrations' }).click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.getByText(/Results for Regatta id:/)).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
});

test('authenticated user can return from event editor to penalties list', async ({ page }) => {
  const email = `codex+penaltyselect-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Penalty', 'Selector');
  await createEventFromPenalties(page);

  await clickBackButton(page);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.locator('#contentForm\\:penaltiesList')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create Event' })).toBeVisible();
});

test('authenticated user can open registration creation from regatta results', async ({ page }) => {
  const email = `codex+registration-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Registration', 'Browser');
  await createRegistrationEditor(page);

  await expect(page.getByRole('heading', { name: /Registration Information/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /View (Cars|Vehicles)/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /View Drivers/i })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
});

test('authenticated user can select a driver from the drivers list', async ({ page }) => {
  const suffix = `${Date.now()}`;
  const email = `codex+driver-${suffix}@example.com`;
  const password = 'Pw-12345';
  const fixture = loadBrowserFixture('confirmed-participant');

  await createAccount(page, email, password, 'Driver', `Selector${suffix}`);
  await createRegistrationEditor(page);

  await page.getByRole('button', { name: /View Drivers/i }).click();
  await expect(page).toHaveURL(/\/faces\/listdrivers\.xhtml$/);
  await dismissWaitUi(page);

  await typeIntoLastGlobalFilter(page, fixture.familyName);
  const driverRow = page.locator('#contentForm\\:driversList_data tr', { hasText: fixture.familyName }).first();
  await expect(driverRow).toBeVisible();
  await driverRow.getByRole('button', { name: 'Select' }).click();

  await expect(page).toHaveURL(/\/faces\/editregistration\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.locator('#contentForm')).toContainText(fixture.fullName.split(' ')[0]);
  await expect(page.locator('#contentForm')).toContainText(fixture.familyName);
});

test('authenticated user can reopen a registration from regatta results and return', async ({ page }) => {
  test.setTimeout(300000);
  const suffix = `${Date.now()}`;
  const email = `codex+registrationlist-${suffix}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Registration', `Selector${suffix}`);
  await createSavedRegistrationAndReturnToResults(page);

  await openFirstRegistrationFromResults(page);
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editregistration\.xhtml$/);
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
  await clickBackButton(page);
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
});

test('authenticated user can create a car from registration flow and select it', async ({ page }) => {
  const email = `codex+carflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Car', 'Flow');
  await createRegistrationEditor(page);

  await page.getByRole('button', { name: /View (Cars|Vehicles)/i }).click();
  await expect(page).toHaveURL(/\/faces\/listcars\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Car' }));
  await expect(page).toHaveURL(/\/faces\/editcar\.xhtml$/);
  await dismissWaitUi(page);

  const carInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await carInputs.nth(0).fill(`Browser Car ${Date.now()}`);
  await carInputs.nth(1).fill('123.4');
  await carInputs.nth(2).fill('45.6');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editcar\.xhtml$/);
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/listcars\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Select' }).first().click();
  await expect(page).toHaveURL(/\/faces\/editregistration\.xhtml$/);
  await dismissWaitUi(page);

  await expect(page.getByRole('heading', { name: /Registration Information/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /View (Cars|Vehicles)/i })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
});

test('authenticated user can create a variant from a new event', async ({ page }) => {
  const email = `codex+variantflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';
  const variantName = `Browser Variant ${Date.now()}`;

  await createAccount(page, email, password, 'Variant', 'Flow');
  await createEventFromPenalties(page);

  await page.getByRole('button', { name: 'View Variants' }).click();
  await expect(page).toHaveURL(/\/faces\/listvariants\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Variant' }));
  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await dismissWaitUi(page);

  const variantInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await variantInputs.nth(0).fill(variantName);
  await variantInputs.nth(1).fill('1.2');
  await variantInputs.nth(2).fill('3.4');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/listvariants\.xhtml$/);
  await dismissWaitUi(page);

  await typeIntoFilter(page, '#contentForm\\:variants\\:globalFilter', variantName);
  const variantRow = page.locator('#contentForm\\:variants_data tr', { hasText: variantName }).first();
  await expect(variantRow).toBeVisible();
});

test('authenticated user can update registration status from regatta results', async ({ page }) => {
  const email = `codex+resultsflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Results', 'Flow');
  await createSavedRegistrationAndReturnToResults(page);

  const statusSelect = page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:inputStatus_input');
  await statusSelect.selectOption('1', { force: true });
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await expect(page.getByText(/Results for Regatta id:/)).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
});

test('authenticated user can see the created registration reflected in regatta results', async ({ page }) => {
  const suffix = `${Date.now()}`;
  const email = `codex+resultscontent-${suffix}@example.com`;
  const password = 'Pw-12345';
  const givenName = 'Results';
  const familyName = `Viewer${suffix}`;

  await createAccount(page, email, password, givenName, familyName);
  await createSavedRegistrationAndReturnToResults(page);

  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.getByText('Registrations count: 1')).toBeVisible();
  const firstRow = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  await expect(firstRow).toContainText(familyName);
  await expect(firstRow).toContainText(givenName);
  await expect(firstRow).toContainText('Incomplete');
});

test('authenticated user can edit best lap during speed test from regatta results', async ({ page }) => {
  test.setTimeout(600000);
  const email = `codex+speedlap-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Speed', 'Lap');
  await createSavedRegistrationAndReturnToResults(page);
  await navigateResultsToStatus(page, 'SPEED TEST');

  await editRegattaResultsCell(page, 4, '12.34');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await dismissWaitUi(page);

  const firstRow = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  await expect(firstRow).toContainText('12.34');
});

test('authenticated user can edit race position and race laps during race test from regatta results', async ({ page }) => {
  test.setTimeout(600000);
  const email = `codex+raceresults-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Race', 'Results');
  await createSavedRegistrationAndReturnToResults(page);
  await navigateResultsToStatus(page, 'RACE TEST');

  await editRegattaResultsCell(page, 7, '1');
  await editRegattaResultsCell(page, 8, '15');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await dismissWaitUi(page);

  const firstRow = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  await expect(firstRow).toContainText('1');
  await expect(firstRow).toContainText('15');
});

test('authenticated user can edit your bid during auction from regatta results', async ({ page }) => {
  test.setTimeout(600000);
  const email = `codex+auctionbid-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Auction', 'Bid');
  await createSavedRegistrationAndReturnToResults(page);
  await navigateResultsToStatus(page, 'RACE TEST');
  await editRegattaResultsCell(page, 7, '1');
  await editRegattaResultsCell(page, 8, '15');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await dismissWaitUi(page);
  await navigateResultsToStatus(page, 'AUCTION');

  await editRegattaResultsCell(page, 12, '77.7');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.reload({ waitUntil: 'domcontentloaded' });
  await dismissWaitUi(page);

  const firstRow = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  await expect(firstRow).toContainText('77.7');
});

test('authenticated user can create a venue and inspect it on the map', async ({ page }) => {
  const email = `codex+venueflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';
  const venueName = `Browser Venue ${Date.now()}`;

  await createAccount(page, email, password, 'Venue', 'Flow');

  await page.goto(`${baseUrl}/faces/listvenues.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.getByRole('button', { name: 'Create new Venue' })).toBeVisible();

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Venue' }));
  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await dismissWaitUi(page);

  const venueInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await venueInputs.nth(0).fill(venueName);
  await venueInputs.nth(2).fill('-99.1332');
  await venueInputs.nth(3).fill('19.4326');
  await page.getByRole('button', { name: 'View Province Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinceregions\.xhtml$/);
  await dismissWaitUi(page);
  await Promise.all([
    page.waitForURL(/\/faces\/editvenue\.xhtml$/, { timeout: 60000 }),
    page.getByRole('button', { name: 'Select' }).first().click()
  ]);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await expect(venueInputs.nth(0)).toHaveValue(venueName);
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);

  await page.locator('#contentForm\\:venues\\:globalFilter').fill(venueName);
  const venueRow = page.locator('tr', { hasText: venueName }).first();
  await expect(venueRow).toBeVisible();
  await Promise.all([
    page.waitForURL(/\/faces\/editvenueinmap\.xhtml$/, { timeout: 60000 }),
    venueRow.getByRole('button', { name: 'View in Map' }).click()
  ]);
  await dismissWaitUi(page);

  await expect(page.getByRole('heading', { name: venueName })).toBeVisible();
  await expect(page.locator('.leaflet-container')).toHaveCount(1);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);
});
