const { test, expect } = require('@playwright/test');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

test('login page renders public auth controls', async ({ page }) => {
  await page.goto(`${baseUrl}/faces/login.xhtml`);

  await expect(page.locator('#contentForm\\:loginButton')).toBeVisible();
  await expect(page.locator('#contentForm\\:newParticipantButton')).toBeVisible();
  await expect(page.locator('#contentForm\\:resetPasswordButton')).toBeVisible();
  await expect(page.getByText('Please login or')).toBeVisible();
});

test('create account, logout, and login again', async ({ page }) => {
  const email = `codex+${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await page.goto(`${baseUrl}/faces/login.xhtml`);
  await page.locator('#contentForm\\:newParticipantButton').click();

  await expect(page).toHaveURL(/\/faces\/editparticipant\.xhtml$/);
  await expect(page.locator('#contentForm\\:saveParticipantButton')).toBeVisible();

  await page.locator('#contentForm\\:passwordInput').fill(password);
  await page.locator('#contentForm\\:givenNamesInput').fill('Codex');
  await page.locator('#contentForm\\:familyNamesInput').fill('BrowserTest');
  await page.locator('#contentForm\\:emailInput').fill(email);
  await page.locator('#contentForm\\:phoneInput').fill('5555555555');
  await page.locator('#contentForm\\:saveParticipantButton').click();

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
  await expect(page.getByText('Codex BrowserTest')).toBeVisible();

  await page.getByText('Logout').first().click();
  await page.goto(`${baseUrl}/faces/login.xhtml`);

  await page.locator('#contentForm\\:participant_email').fill(email);
  await page.locator('#contentForm\\:participant_password').fill(password);
  await page.locator('#contentForm\\:loginButton').click();

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
  await expect(page.getByText('Codex BrowserTest')).toBeVisible();
});

test('browser respects anonymous access policy', async ({ page }) => {
  await page.goto(`${baseUrl}/faces/editregatta.xhtml`);
  await expect(page).toHaveURL(/\/faces\/login\.xhtml$/);

  await page.goto(`${baseUrl}/faces/complaint.xhtml`);
  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml$/);
  await expect(page.getByText('TOP-Racing')).toBeVisible();
});
