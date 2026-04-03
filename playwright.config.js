const { defineConfig } = require('@playwright/test');

const baseURL = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 120000,
  workers: 1,
  expect: {
    timeout: 30000
  },
  reporter: 'list',
  use: {
    baseURL,
    headless: true,
    trace: 'retain-on-failure'
  }
});
