const { defineConfig } = require('@playwright/test');

const baseURL = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 180000,
  workers: 1,
  expect: {
    timeout: 45000
  },
  reporter: 'list',
  use: {
    baseURL,
    headless: true,
    navigationTimeout: 120000,
    trace: 'retain-on-failure'
  }
});
