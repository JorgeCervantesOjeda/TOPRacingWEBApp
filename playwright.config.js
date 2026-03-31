const { defineConfig } = require('@playwright/test');

const baseURL = process.env.TOPRACING_BASE_URL || 'http://localhost:8080/topracingwebapp';

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 60000,
  expect: {
    timeout: 20000
  },
  reporter: 'list',
  use: {
    baseURL,
    headless: true,
    trace: 'retain-on-failure'
  }
});
