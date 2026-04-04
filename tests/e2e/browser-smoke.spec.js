const { test, expect } = require('@playwright/test');

const baseUrl = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

test.afterEach(async ({ page }) => {
  await logoutIfVisible(page);
});

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

  await createAccount(page, email, password, 'Codex', 'BrowserTest');

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
  await expect(page.getByText('Codex BrowserTest')).toBeVisible();

  await logout(page);
  await login(page, email, password);

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
  await expect(page.getByText('Codex BrowserTest')).toBeVisible();
});

test('logout and relogin switch to a single active account without stale identity state', async ({ page, context }) => {
  const suffix = `${Date.now()}`;
  const firstName = `Alpha ${suffix}`;
  const secondName = `Beta ${suffix}`;
  const firstEmail = `codex+alpha-${suffix}@example.com`;
  const secondEmail = `codex+beta-${suffix}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, firstEmail, password, 'Codex', firstName);
  await expect(page.getByText(`Codex ${firstName}`)).toBeVisible();
  await logout(page);

  await createAccount(page, secondEmail, password, 'Codex', secondName);
  await expect(page.getByText(`Codex ${secondName}`)).toBeVisible();
  await expect(page.getByText(`Codex ${firstName}`)).toHaveCount(0);
  await logout(page);

  await login(page, firstEmail, password);
  await expect(page.getByText(`Codex ${firstName}`)).toBeVisible();
  await expect(page.getByText(`Codex ${secondName}`)).toHaveCount(0);
  await logout(page);

  await login(page, secondEmail, password);
  await expect(page.getByText(`Codex ${secondName}`)).toBeVisible();
  await expect(page.getByText(`Codex ${firstName}`)).toHaveCount(0);

  const sessionCookies = (await context.cookies()).filter((cookie) => cookie.name === 'JSESSIONID');
  expect(sessionCookies).toHaveLength(1);

  await page.goto(`${baseUrl}/faces/welcome.xhtml`);
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText(`Codex ${secondName}`)).toBeVisible();
  await expect(page.getByText(`Codex ${firstName}`)).toHaveCount(0);
});

test('browser respects anonymous access policy', async ({ page }) => {
  await page.goto(`${baseUrl}/faces/editregatta.xhtml`);
  await expect(page).toHaveURL(/\/faces\/login\.xhtml$/);

  await page.goto(`${baseUrl}/faces/complaint.xhtml`);
  await expect(page).toHaveURL(/\/faces\/complaint\.xhtml$/);
  await expect(page.getByText('TOP-Racing')).toBeVisible();
});

test('logout invalidates access to protected editors', async ({ page }) => {
  const email = `codex+logout-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Logout', 'Guard');

  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await page.getByRole('button', { name: 'Create Event' }).click();
  await page.locator('.ui-confirmdialog-yes').click();
  await expect(page).toHaveURL(/\/faces\/editregatta\.xhtml$/);

  const protectedRegattaUrl = page.url();

  await page.goto(`${baseUrl}/faces/welcome.xhtml`);
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await page.locator('[id$="logoutButton"]').click();
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);

  await page.goto(protectedRegattaUrl);
  await expect(page).toHaveURL(/\/faces\/login\.xhtml$/);

  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/login\.xhtml$/);

  await page.goto(`${baseUrl}/faces/editregistration.xhtml`);
  await expect(page).toHaveURL(/\/faces\/login\.xhtml$/);
});

test('authenticated user can access core authenticated lists', async ({ page }) => {
  const email = `codex+menu-${Date.now()}@example.com`;
  const password = 'Pw-12345';

  await createAccount(page, email, password, 'Menu', 'Navigator');

  await page.goto(`${baseUrl}/faces/listpointscounts.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpointscounts\.xhtml$/);
  await expect(page.locator('[id$="PenaltiesButton"]')).toBeVisible();

  await page.goto(`${baseUrl}/faces/listpenalties.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listpenalties\.xhtml$/);
  await expect(page.locator('#contentForm\\:penaltiesList')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create Event' })).toBeVisible();

  await page.goto(`${baseUrl}/faces/listregistrations.xhtml`);
  await expect(page).toHaveURL(/\/faces\/listregistrations\.xhtml$/);
  await expect(page.locator('#contentForm\\:registrationsList')).toBeVisible();

  await page.goto(`${baseUrl}/faces/welcome.xhtml`);
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
});

test('authenticated user can edit profile data and see it persisted', async ({ page }) => {
  const suffix = `${Date.now()}`;
  const email = `codex+profile-${suffix}@example.com`;
  const password = 'Pw-12345';
  const updatedFamilyName = `Edited${suffix}`;
  const updatedPhone = '5555555511';

  await createAccount(page, email, password, 'Profile', `Original${suffix}`);

  await page.goto(`${baseUrl}/faces/welcome.xhtml`);
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await page.locator('#j_idt13\\:EditProfileButton').click();

  await expect(page).toHaveURL(/\/faces\/editparticipant\.xhtml$/);
  await expect(page.locator('#contentForm\\:familyNamesInput')).toHaveValue(`Original${suffix}`);
  await page.locator('#contentForm\\:passwordInput').fill(password);
  await page.locator('#contentForm\\:familyNamesInput').fill(updatedFamilyName);
  await page.locator('#contentForm\\:phoneInput').fill(updatedPhone);
  await page.locator('#contentForm\\:saveParticipantButton').click();

  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText(`Profile ${updatedFamilyName}`)).toBeVisible();

  await page.locator('#j_idt13\\:EditProfileButton').click();
  await expect(page).toHaveURL(/\/faces\/editparticipant\.xhtml$/);
  await expect(page.locator('#contentForm\\:familyNamesInput')).toHaveValue(updatedFamilyName);
  await expect(page.locator('#contentForm\\:phoneInput')).toHaveValue(updatedPhone);
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

async function login(page, email, password) {
  await page.goto(`${baseUrl}/faces/login.xhtml`);
  await expect(page.locator('#contentForm\\:loginButton')).toBeVisible();
  await page.locator('#contentForm\\:participant_email').fill(email);
  await page.locator('#contentForm\\:participant_password').fill(password);
  await page.locator('#contentForm\\:loginButton').click();
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await expect(page.getByText('Logout').first()).toBeVisible();
}

async function logout(page) {
  await page.goto(`${baseUrl}/faces/welcome.xhtml`);
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
  await page.locator('[id$="logoutButton"]').click();
  await expect(page).toHaveURL(/\/faces\/welcome\.xhtml$/);
}

async function logoutIfVisible(page) {
  if (page.isClosed()) {
    return;
  }

  try {
    const logoutButton = page.locator('[id$="logoutButton"]').first();
    if (await logoutButton.isVisible({ timeout: 1500 }).catch(() => false)) {
      await logoutButton.click({ timeout: 5000 });
      await page.waitForTimeout(300);
    }
  } catch (error) {
    // Best-effort session cleanup for local GlassFish stability.
  }
}
