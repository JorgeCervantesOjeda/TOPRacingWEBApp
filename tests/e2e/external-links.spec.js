const { test, expect } = require('@playwright/test');
const { execFileSync } = require('node:child_process');
const path = require('node:path');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';
const fixtureScript = path.join(__dirname, '..', '..', 'scripts', 'browser-fixture.ps1');

test('browser can confirm a participant from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('confirm');

  await gotoAllowingAbort(page, `${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/confirmusermail\.xhtml\?key=/);
  await expect(page.getByText('Confirmation OK')).toBeVisible();
  await expect(page.getByText(fixture.fullName)).toBeVisible();

  const refreshed = loadFixture('participant-by-email', fixture.email);
  expect(refreshed.confirmed).toBe('true');
});

test('browser can reset password from a valid mail link and log in with the new password', async ({ page }) => {
  const fixture = loadFixture('reset');

  await gotoAllowingAbort(page, `${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/resetpassword\.xhtml\?key=/);
  await expect(page.getByText('Your Password has been reset.')).toBeVisible();
  await expect(page.getByText(fixture.email)).toBeVisible();

  const refreshed = loadFixture('participant-by-email', fixture.email);
  expect(refreshed.password).not.toBe('OldPw-123');
  expect(refreshed.emailKey).not.toBe(fixture.emailKey);
});

test('browser can file a balance complaint from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('balance-owner');

  await gotoAllowingAbort(page, `${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml/);
  await expect(page.getByText('You have filed a complaint against:')).toBeVisible();
  await expect(page.getByText(fixture.targetName)).toBeVisible();
});

test('browser can file an auction complaint from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('auction-buyer');

  await gotoAllowingAbort(page, `${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml/);
  await expect(page.getByText('You have filed a complaint against:')).toBeVisible();
  await expect(page.getByText(fixture.targetName)).toBeVisible();
});

test('browser shows a graceful message for an invalid confirmation link', async ({ page }) => {
  await gotoAllowingAbort(page, `${baseUrl}/faces/confirmusermail.xhtml?key=%27invalid%27`);

  await expect(page).toHaveURL(/\/faces\/confirmusermail\.xhtml\?key=/);
  await expect(page.getByText('Confirmation not OK')).toBeVisible();
  await expect(page.getByText('Unknown participant')).toBeVisible();
});

test('browser shows a friendly error for an invalid reset-password link', async ({ page }) => {
  await gotoAllowingAbort(page, `${baseUrl}/faces/resetpassword.xhtml?key=%27invalid%27`);

  await expect(page).toHaveURL(/\/faces\/resetpassword\.xhtml\?key=/);
  await expect(page.getByText('Password reset request is invalid or expired.')).toBeVisible();
  await expect(page.getByText('null pointer')).toHaveCount(0);
});

test('browser does not leak car details for an invalid complaint link', async ({ page }) => {
  const fixture = loadFixture('balance-owner');
  const invalidUrl = fixture.url.replace(fixture.emailKey, 'invalid');

  await gotoAllowingAbort(page, `${baseUrl}${invalidUrl}`);

  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml/);
  await expect(page.getByText('Complaint request is incomplete or invalid.')).toBeVisible();
  await expect(page.getByText('Car:')).toHaveCount(0);
  await expect(page.getByText(fixture.targetName)).toHaveCount(0);
});

function loadFixture(command, argument) {
  const pwsh = process.env.ComSpec ? 'powershell' : 'powershell';
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

  const output = execFileSync(pwsh, args, {
    cwd: path.join(__dirname, '..', '..'),
    encoding: 'utf8',
    stdio: ['ignore', 'pipe', 'pipe']
  });

  return parseFixture(output);
}

function parseFixture(output) {
  return output
    .split(/\r?\n/)
    .filter(Boolean)
    .reduce((fixture, line) => {
      const separator = line.indexOf('=');
      if (separator <= 0) {
        return fixture;
      }

      const key = line.slice(0, separator);
      const value = line.slice(separator + 1);
      fixture[key] = value;
      return fixture;
    }, {});
}

async function gotoAllowingAbort(page, url) {
  for (let attempt = 0; attempt < 2; attempt++) {
    try {
      await page.goto(url, { waitUntil: 'commit', timeout: 120000 });
    } catch (error) {
      if (!String(error).includes('net::ERR_ABORTED')) {
        throw error;
      }
    }

    if (page.url()) {
      try {
        await page.waitForLoadState('domcontentloaded', { timeout: 30000 });
      } catch (error) {
        // Some JSF flows abort and still land on the target page correctly.
      }
      return;
    }

    await page.waitForTimeout(500);
  }

  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 120000 });
}
