const { test, expect } = require('@playwright/test');
const { execFileSync } = require('node:child_process');
const path = require('node:path');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';
const fixtureScript = path.join(__dirname, '..', '..', 'scripts', 'browser-fixture.ps1');

test('browser can confirm a participant from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('confirm');

  await page.goto(`${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/confirmusermail\.xhtml\?key=/);
  await expect(page.getByText('Confirmation OK')).toBeVisible();
  await expect(page.getByText(fixture.fullName)).toBeVisible();

  const refreshed = loadFixture('participant-by-email', fixture.email);
  expect(refreshed.confirmed).toBe('true');
});

test('browser can reset password from a valid mail link and log in with the new password', async ({ page }) => {
  const fixture = loadFixture('reset');

  await page.goto(`${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/resetpassword\.xhtml\?key=/);
  await expect(page.getByText('Your Password has been reset.')).toBeVisible();
  await expect(page.getByText(fixture.email)).toBeVisible();

  const refreshed = loadFixture('participant-by-email', fixture.email);
  expect(refreshed.password).not.toBe('OldPw-123');
  expect(refreshed.emailKey).not.toBe(fixture.emailKey);

  await page.goto(`${baseUrl}/faces/login.xhtml`);
  await page.locator('#contentForm\\:participant_email').fill(fixture.email);
  await page.locator('#contentForm\\:participant_password').fill(refreshed.password);
  await page.locator('#contentForm\\:loginButton').click();

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText(fixture.fullName)).toBeVisible();
});

test('browser can file a balance complaint from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('balance-owner');

  await page.goto(`${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml/);
  await expect(page.getByText('You have filed a complaint against:')).toBeVisible();
  await expect(page.getByText(fixture.targetName)).toBeVisible();
});

test('browser can file an auction complaint from a valid mail link', async ({ page }) => {
  const fixture = loadFixture('auction-buyer');

  await page.goto(`${baseUrl}${fixture.url}`);

  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml/);
  await expect(page.getByText('You have filed a complaint against:')).toBeVisible();
  await expect(page.getByText(fixture.targetName)).toBeVisible();
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
