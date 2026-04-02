const { test, expect } = require('@playwright/test');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

test('authenticated user can create an event and navigate its legacy editors', async ({ page }) => {
  const email = `codex+regatta-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Regatta', 'Browser');

  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await expect(page.getByText('Create Event')).toBeVisible();

  await page.getByRole('button', { name: 'Create Event' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
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

  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'View/Edit Registrations' }).click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.getByText(/Results for Regatta id:/)).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
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

test('authenticated user can create a car from registration flow and select it', async ({ page }) => {
  const email = `codex+carflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Car', 'Flow');
  await createRegistrationEditor(page);

  await page.getByRole('button', { name: /View (Cars|Vehicles)/i }).click();
  await expect(page).toHaveURL(/\/faces\/listcars\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Car' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
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

test('authenticated user can create and select a variant for a new event', async ({ page }) => {
  const email = `codex+variantflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';
  const variantName = `Browser Variant ${Date.now()}`;

  await createAccount(page, email, password, 'Variant', 'Flow');
  await createEventFromPenalties(page);

  await page.getByRole('button', { name: 'View Variants' }).click();
  await expect(page).toHaveURL(/\/faces\/listvariants\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Variant' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
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

  const variantRow = page.locator('tr', { hasText: variantName }).first();
  await expect(variantRow).toBeVisible();
  await Promise.all([
    page.waitForURL(/\/faces\/editregatta\.xhtml$/, { timeout: 60000 }),
    variantRow.getByRole('button', { name: 'Select' }).click()
  ]);
  await dismissWaitUi(page);

  await expect(page.getByRole('heading', { name: 'View/Edit Event' })).toBeVisible();
  await expect(page.getByText(variantName)).toBeVisible();
});

test('authenticated user can update registration status and save speed-test lap time', async ({ page }) => {
  const email = `codex+resultsflow-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Results', 'Flow');
  await createSavedRegistrationAndReturnToResults(page);

  const statusSelect = page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:inputStatus_input');
  await statusSelect.selectOption('1', { force: true });
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await expect(page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:inputStatus_label')).toHaveText('OK');

  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: /Next status: SPEED TEST/i }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page.locator('#contentForm\\:infoMessageOK')).toBeVisible();
  await page.locator('#contentForm\\:infoMessageOK').click();
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'View/Edit Registrations' }).click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);

  const lapTimeInput = page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:j_idt36');
  await lapTimeInput.evaluate((input, value) => {
    input.value = value;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    input.dispatchEvent(new Event('change', { bubbles: true }));
  }, '12.34');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await page.reload();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
  await expect(page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:j_idt36')).toHaveValue('12.34');
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

  await page.getByRole('button', { name: 'Create new Venue' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await dismissWaitUi(page);

  const venueInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await venueInputs.nth(0).fill(venueName);
  await venueInputs.nth(2).fill('-99.1332');
  await venueInputs.nth(3).fill('19.4326');
  await page.getByRole('button', { name: 'View Province Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinceregions\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Select' }).first().click();
  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await expect(venueInputs.nth(0)).toHaveValue(venueName);
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);

  await page.locator('#contentForm\\:venues\\:globalFilter').fill(venueName);
  await page.waitForTimeout(500);
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

test('authenticated user can build the geographic chain from variant to planet region', async ({ page }) => {
  const email = `codex+geochain-${Date.now()}@example.com`;
  const password = 'Pw-12345';
  const suffix = `${Date.now()}`;
  const variantName = `Geo Variant ${suffix}`;
  const venueName = `Geo Venue ${suffix}`;
  const provinceRegionName = `Geo Province Region ${suffix}`;
  const provinceName = `Geo Province ${suffix}`;
  const countryRegionName = `Geo Country Region ${suffix}`;
  const countryName = `Geo Country ${suffix}`;
  const planetRegionName = `Geo Planet Region ${suffix}`;

  await createAccount(page, email, password, 'Geo', 'Chain');

  await page.goto(`${baseUrl}/faces/listvariants.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listvariants\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Variant' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await dismissWaitUi(page);

  const variantInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await variantInputs.nth(0).fill(variantName);
  await variantInputs.nth(1).fill('1.1');
  await variantInputs.nth(2).fill('2.2');
  await page.getByRole('button', { name: 'View Venues' }).click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Venue' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await dismissWaitUi(page);

  const venueInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await venueInputs.nth(0).fill(venueName);
  await venueInputs.nth(2).fill('-99.1000');
  await venueInputs.nth(3).fill('19.4000');
  await page.getByRole('button', { name: 'View Province Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinceregions\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Province Region' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editprovinceregion\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, provinceRegionName);
  await page.getByRole('button', { name: 'View Provinces' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinces\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Province' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editprovince\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, provinceName);
  await page.getByRole('button', { name: 'View Country Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listcountryregions\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Country Region' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editcountryregion\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, countryRegionName);
  await page.getByRole('button', { name: 'View Countries' }).click();
  await expect(page).toHaveURL(/\/faces\/listcountries\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Country' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editcountry\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, countryName);
  await page.getByRole('button', { name: 'View Planet Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listplanetregions\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new Planet Region' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editplanetregion\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, planetRegionName);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await expect(page).toHaveURL(/\/faces\/editplanetregion\.xhtml$/);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listplanetregions\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'planetregions', planetRegionName);
  await expect(page).toHaveURL(/\/faces\/editcountry\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listcountries\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'countries', countryName);
  await expect(page).toHaveURL(/\/faces\/editcountryregion\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listcountryregions\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'countryregions', countryRegionName);
  await expect(page).toHaveURL(/\/faces\/editprovince\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listprovinces\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'provinces', provinceName);
  await expect(page).toHaveURL(/\/faces\/editprovinceregion\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listprovinceregions\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'provinceregions', provinceRegionName);
  await expect(page).toHaveURL(/\/faces\/editvenue\.xhtml$/);
  await dismissWaitUi(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.getByRole('button').filter({ has: page.locator('.fa-arrow-left') }).first().click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);

  await selectRowByText(page, 'venues', venueName);
  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await dismissWaitUi(page);
  const variantInputsAfterVenueSelection = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await variantInputsAfterVenueSelection.nth(0).fill(variantName);
  await variantInputsAfterVenueSelection.nth(1).fill('1.1');
  await variantInputsAfterVenueSelection.nth(2).fill('2.2');
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await expect(page.locator('#contentForm')).toContainText(venueName);
  await expect(variantInputsAfterVenueSelection.nth(0)).toHaveValue(variantName);
  await expect(variantInputsAfterVenueSelection.nth(1)).toHaveValue('1.1');
  await expect(variantInputsAfterVenueSelection.nth(2)).toHaveValue('2.2');
});

async function createAccount(page, email, password, givenNames, familyNames) {
  await page.goto(`${baseUrl}/faces/login.xhtml`);
  await page.locator('#contentForm\\:newParticipantButton').click();

  await expect(page).toHaveURL(/\/faces\/editparticipant\.xhtml$/);
  await expect(page.locator('#contentForm\\:saveParticipantButton')).toBeVisible();

  await page.locator('#contentForm\\:passwordInput').fill(password);
  await page.locator('#contentForm\\:givenNamesInput').fill(givenNames);
  await page.locator('#contentForm\\:familyNamesInput').fill(familyNames);
  await page.locator('#contentForm\\:emailInput').fill(email);
  await page.locator('#contentForm\\:phoneInput').fill('5555555555');
  await page.locator('#contentForm\\:saveParticipantButton').click();

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
}

async function createEventFromPenalties(page) {
  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await expect(page.getByText('Create Event')).toBeVisible();

  await page.getByRole('button', { name: 'Create Event' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);
}

async function createRegistrationEditor(page) {
  await createEventFromPenalties(page);

  await page.getByRole('button', { name: /Next status:/ }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page.locator('#contentForm\\:infoMessageOK')).toBeVisible();
  await page.locator('#contentForm\\:infoMessageOK').click();
  await dismissWaitUi(page);

  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await page.getByRole('button', { name: 'View/Edit Registrations' }).click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);

  await page.getByRole('button', { name: 'Create new registration' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editregistration\.xhtml$/);
  await dismissWaitUi(page);
}

async function createSavedRegistrationAndReturnToResults(page) {
  await createRegistrationEditor(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
}

async function dismissWaitUi(page) {
  try {
    await page.evaluate(() => {
      try {
        if (window.PF) {
          const waitDialog = PF('dlgWait');
          if (waitDialog && typeof waitDialog.hide === 'function') {
            waitDialog.hide();
          }
        }
      } catch (error) {
        // Ignore missing PrimeFaces widget state in tests.
      }

      document
        .querySelectorAll('.ui-widget-overlay, .ui-dialog-mask')
        .forEach((node) => node.remove());
    });
  } catch (error) {
    if (!String(error).includes('Execution context was destroyed')) {
      throw error;
    }
  }
}

async function fillFirstTextInput(page, value) {
  const inputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await inputs.nth(0).fill(value);
}

async function selectRowByText(page, tableId, text) {
  await page.locator(`#contentForm\\:${tableId}\\:globalFilter`).fill(text);
  await page.waitForTimeout(500);
  const row = page.locator('tr', { hasText: text }).first();
  await expect(row).toBeVisible();
  await row.getByRole('button', { name: 'Select' }).click();
}
