// tests/e2e/geographic-chain-flow.spec.js
// Verifies the geographic selection chain from variant to planet region.
const { test, expect } = require('@playwright/test');
const {
  baseUrl,
  clickAndAcceptConfirm,
  createAccount,
  dismissWaitUi,
  fillFirstTextInput,
  logoutIfVisible,
  selectRowByText
} = require('./support/editregatta-flow-helpers');

test.afterEach(async ({ page }) => {
  await logoutIfVisible(page);
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

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Variant' }));
  await expect(page).toHaveURL(/\/faces\/editvariant\.xhtml$/);
  await dismissWaitUi(page);

  const variantInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await variantInputs.nth(0).fill(variantName);
  await variantInputs.nth(1).fill('1.1');
  await variantInputs.nth(2).fill('2.2');
  await page.getByRole('button', { name: 'View Venues' }).click();
  await expect(page).toHaveURL(/\/faces\/listvenues\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Venue' }));
  await page.waitForURL(/\/faces\/editvenue\.xhtml$/, { timeout: 60000 });
  await dismissWaitUi(page);

  const venueInputs = page.locator('#contentForm input[type="text"]').filter({ hasNot: page.locator('[type="hidden"]') });
  await venueInputs.nth(0).fill(venueName);
  await venueInputs.nth(2).fill('-99.1000');
  await venueInputs.nth(3).fill('19.4000');
  await page.getByRole('button', { name: 'View Province Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinceregions\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Province Region' }));
  await expect(page).toHaveURL(/\/faces\/editprovinceregion\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, provinceRegionName);
  await page.getByRole('button', { name: 'View Provinces' }).click();
  await expect(page).toHaveURL(/\/faces\/listprovinces\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Province' }));
  await expect(page).toHaveURL(/\/faces\/editprovince\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, provinceName);
  await page.getByRole('button', { name: 'View Country Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listcountryregions\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Country Region' }));
  await expect(page).toHaveURL(/\/faces\/editcountryregion\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, countryRegionName);
  await page.getByRole('button', { name: 'View Countries' }).click();
  await expect(page).toHaveURL(/\/faces\/listcountries\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Country' }));
  await expect(page).toHaveURL(/\/faces\/editcountry\.xhtml$/);
  await dismissWaitUi(page);

  await fillFirstTextInput(page, countryName);
  await page.getByRole('button', { name: 'View Planet Regions' }).click();
  await expect(page).toHaveURL(/\/faces\/listplanetregions\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new Planet Region' }));
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
  await expect(page.locator('#contentForm')).toContainText('Venue:');
  await expect(page.locator('#contentForm')).not.toContainText('Venue not set yet.');
  await expect(variantInputsAfterVenueSelection.nth(0)).toHaveValue(variantName);
  await expect(variantInputsAfterVenueSelection.nth(1)).toHaveValue('1.1');
  await expect(variantInputsAfterVenueSelection.nth(2)).toHaveValue('2.2');
});
