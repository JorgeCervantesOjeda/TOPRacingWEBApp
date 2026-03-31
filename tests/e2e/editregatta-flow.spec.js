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

  await expect(page.getByRole('heading', { name: /Registration Information/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /View (Cars|Vehicles)/i })).toBeVisible();
  await expect(page.getByRole('button', { name: /View Drivers/i })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Save data' })).toBeVisible();
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

async function dismissWaitUi(page) {
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
}
