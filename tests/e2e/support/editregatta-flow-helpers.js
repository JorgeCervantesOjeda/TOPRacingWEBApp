// tests/e2e/support/editregatta-flow-helpers.js
// Provides shared Playwright helpers for the edit regatta browser flow.
const { expect } = require('@playwright/test');
const { execFileSync } = require('node:child_process');
const path = require('node:path');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';
const fixtureScript = path.join(__dirname, '..', '..', '..', 'scripts', 'browser-fixture.ps1');

async function createAccount(page, email, password, givenNames, familyNames) {
  await gotoWithRetry(page, `${baseUrl}/faces/login.xhtml`);
  const createAccountButton = page.getByRole('button', { name: 'Create Account' });
  await expect(createAccountButton).toBeVisible();
  await createAccountButton.click();

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
  const createEventButton = page.getByRole('button', { name: 'Create Event' });
  await expect(createEventButton).toBeVisible({ timeout: 60000 });

  await clickAndAcceptConfirm(page, createEventButton);
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await dismissWaitUi(page);
}

async function createRegistrationEditor(page) {
  await createEventFromPenalties(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: /Next status:/ }));
  await settleAfterStatusAdvance(page);

  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
  await page.getByRole('button', { name: 'View/Edit Registrations' }).click();
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);

  await clickAndAcceptConfirm(page, page.getByRole('button', { name: 'Create new registration' }));
  await expect(page).toHaveURL(/\/faces\/editregistration\.xhtml$/);
  await dismissWaitUi(page);
}

async function createSavedRegistrationAndReturnToResults(page) {
  await createRegistrationEditor(page);
  await page.getByRole('button', { name: 'Save data' }).click();
  await page.waitForURL(/\/faces\/editregistration\.xhtml$/, {
    timeout: 60000,
    waitUntil: 'domcontentloaded'
  });
  await dismissWaitUi(page);
  await gotoWithRetry(page, `${baseUrl}/faces/editregattaresults.xhtml`);
  await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
  await dismissWaitUi(page);
}

async function navigateResultsToStatus(page, expectedStatusName) {
  await ensureFirstRegistrationOk(page);

  for (let attempt = 0; attempt < 5; attempt++) {
    await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
    await dismissWaitUi(page);

    const header = page.locator('#contentForm');
    const statusText = (await header.textContent()) || '';
    if (statusText.includes(`Status: ${expectedStatusName}`)) {
      return;
    }

    await gotoWithRetry(page, `${baseUrl}/faces/editregatta.xhtml`);
    await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
    await dismissWaitUi(page);
    await clickAndAcceptConfirm(page, page.getByRole('button', { name: /Next status:/ }));
    await settleAfterStatusAdvance(page);
    await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);
    await gotoWithRetry(page, `${baseUrl}/faces/editregattaresults.xhtml`);
    await expect(page).toHaveURL(/\/faces\/editregattaresults\.xhtml$/);
    await dismissWaitUi(page);
  }

  throw new Error(`Could not reach regatta results status ${expectedStatusName}`);
}

async function ensureFirstRegistrationOk(page) {
  const statusSelect = page.locator('#contentForm\\:regattaRegistrationsList\\:0\\:inputStatus_input');
  await expect(statusSelect).toHaveCount(1);
  const currentValue = await statusSelect.inputValue();
  if (currentValue === '1') {
    return;
  }

  await statusSelect.selectOption('1', { force: true });
  await page.getByRole('button', { name: 'Save data' }).click();
  await dismissWaitUi(page);
}

async function editRegattaResultsCell(page, columnIndex, value) {
  const row = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  const cell = row.locator('td').nth(columnIndex);
  await expect(cell).toBeVisible();
  await cell.click();
  await cell.click();

  const input = cell.locator('input').first();
  await expect(input).toBeVisible();
  await input.fill('');
  await input.type(value, { delay: 30 });
  await input.press('Tab');
  await dismissWaitUi(page);
  await expect(input).toBeHidden({ timeout: 10000 });
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
        // Missing PrimeFaces widget state is tolerated during test cleanup only.
      }

      document
        .querySelectorAll('.ui-widget-overlay, .ui-dialog-mask')
        .forEach((node) => node.remove());
    });
  } catch (error) {
    const message = String(error);
    if (!message.includes('Execution context was destroyed')
        && !message.includes('Target page, context or browser has been closed')) {
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
  await expect(page.locator(`#contentForm\\:${tableId}_data tr`, { hasText: text }).first()).toBeVisible();
  await page.locator(`#contentForm\\:${tableId}_data tr`, { hasText: text }).first()
    .getByRole('button', { name: 'Select' })
    .click();
}

async function clickBackButton(page) {
  await page.locator('#contentForm button:has(.fa-arrow-left)').first().click();
}

async function clickAndAcceptConfirm(page, buttonLocator) {
  await expect(buttonLocator).toBeVisible({ timeout: 30000 });
  try {
    await buttonLocator.click({ timeout: 10000 });
  } catch (error) {
    await buttonLocator.evaluate((button) => button.click());
  }
  const confirmYesButton = page.locator('.ui-confirmdialog-yes:visible').first();
  const confirmVisible = await confirmYesButton.isVisible({ timeout: 2000 }).catch(() => false);
  if (!confirmVisible) {
    return;
  }
  await confirmYesButton.click({ force: true });
  await expect(confirmYesButton).toBeHidden({ timeout: 10000 });
}

async function settleAfterStatusAdvance(page) {
  const infoOkButton = page.locator('#contentForm\\:infoMessageOK');

  try {
    await infoOkButton.waitFor({ state: 'visible', timeout: 60000 });
    await infoOkButton.click();
  } catch (error) {
    // Some runs transition without surfacing the intermediate OK button.
  }

  await dismissWaitUi(page);
}

async function openFirstRegistrationFromResults(page) {
  const registrationRow = page.locator('#contentForm\\:regattaRegistrationsList_data tr').first();
  await expect(registrationRow).toBeVisible();

  for (let attempt = 0; attempt < 2; attempt++) {
    const clicked = registrationRow.locator('td').nth(1);
    await clicked.evaluate((cell) => cell.click());

    try {
      await page.waitForURL(/\/faces\/editregistration\.xhtml$/, {
        timeout: 15000,
        waitUntil: 'domcontentloaded'
      });
      return;
    } catch (error) {
      if (attempt === 1) {
        throw error;
      }
    }
  }
}

async function typeIntoLastGlobalFilter(page, value) {
  const filterInput = page.locator('#contentForm input[id$="globalFilter"]').last();
  await typeIntoFilter(page, filterInput, value);
}

async function typeIntoFilter(page, locatorOrSelector, value) {
  const filterInput = typeof locatorOrSelector === 'string'
    ? page.locator(locatorOrSelector)
    : locatorOrSelector;
  await filterInput.click();
  await filterInput.fill('');
  await filterInput.type(value, { delay: 50 });
  await expect(filterInput).toHaveValue(value);
}

async function gotoWithRetry(page, url) {
  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 300000 });
      return;
    } catch (error) {
      if (attempt === 1) {
        throw error;
      }
      await page.waitForLoadState('domcontentloaded', { timeout: 30000 }).catch(() => {});
    }
  }
}

function loadBrowserFixture(command, argument) {
  const args = [
    '-ExecutionPolicy',
    'Bypass',
    '-File',
    fixtureScript,
    '-Command',
    command
  ];

  if (argument) {
    args.push('-Argument', argument);
  }

  const output = execFileSync('powershell', args, {
    cwd: path.join(__dirname, '..', '..', '..'),
    encoding: 'utf8',
    stdio: ['ignore', 'pipe', 'pipe']
  });

  return output
    .split(/\r?\n/)
    .filter(Boolean)
    .reduce((fixture, line) => {
      const separator = line.indexOf('=');
      if (separator <= 0) {
        return fixture;
      }
      fixture[line.slice(0, separator)] = line.slice(separator + 1);
      return fixture;
    }, {});
}

async function logoutIfVisible(page) {
  if (page.isClosed()) {
    return;
  }

  try {
    await dismissWaitUi(page);
    const logoutButton = page.locator('[id$="logoutButton"]').first();
    if (await logoutButton.isVisible({ timeout: 1500 }).catch(() => false)) {
      await logoutButton.click({ timeout: 5000 });
      await page.waitForLoadState('domcontentloaded', { timeout: 5000 }).catch(() => {});
    }
  } catch (error) {
    // Best-effort session cleanup for local GlassFish stability.
  }
}

module.exports = {
  baseUrl,
  clickAndAcceptConfirm,
  clickBackButton,
  createAccount,
  createEventFromPenalties,
  createRegistrationEditor,
  createSavedRegistrationAndReturnToResults,
  dismissWaitUi,
  editRegattaResultsCell,
  fillFirstTextInput,
  loadBrowserFixture,
  logoutIfVisible,
  navigateResultsToStatus,
  openFirstRegistrationFromResults,
  selectRowByText,
  typeIntoFilter,
  typeIntoLastGlobalFilter
};
