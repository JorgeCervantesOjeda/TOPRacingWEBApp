#!/usr/bin/env node
// scripts/configure-gmail-oauth.mjs
// Obtains a Gmail OAuth refresh token and writes local mail configuration.

import crypto from "node:crypto";
import fs from "node:fs";
import http from "node:http";
import os from "node:os";
import path from "node:path";
import { spawn } from "node:child_process";

const DEFAULT_OUTPUT = "scripts/local-env.ps1";
const DEFAULT_PORT = 53682;
const DEFAULT_SENDER = "top.racing.org@gmail.com";
const GMAIL_SEND_SCOPE = "https://www.googleapis.com/auth/gmail.send";

main().catch((error) => {
  console.error(`OAuth configuration failed: ${error.message}`);
  process.exit(1);
});

async function main() {
  const options = parseArgs(process.argv.slice(2));
  if (!options.clientSecretJson) {
    throw new Error("--client-secret-json is required.");
  }
  const clientSecretPath = path.resolve(options.clientSecretJson);
  const outputPath = path.resolve(options.output || DEFAULT_OUTPUT);
  const clientIndex = Number.parseInt(options.clientIndex || "0", 10);
  const port = Number.parseInt(options.port || String(DEFAULT_PORT), 10);
  const senderEmail = options.senderEmail || DEFAULT_SENDER;

  if (!Number.isInteger(clientIndex) || clientIndex < 0) {
    throw new Error("--client-index must be a non-negative integer.");
  }

  const oauthClient = readOAuthClient(clientSecretPath, clientIndex);
  const redirectUri = options.redirectUri || `http://127.0.0.1:${port}/oauth2callback`;
  const state = crypto.randomBytes(24).toString("hex");

  const code = await receiveAuthorizationCode({
    oauthClient,
    redirectUri,
    state,
    port,
    openBrowser: options.open !== "false",
  });

  const tokenResponse = await exchangeCodeForTokens({
    oauthClient,
    redirectUri,
    code,
  });

  if (!tokenResponse.refresh_token) {
    throw new Error(
      "Google did not return a refresh_token. Revoke the previous grant or run again with prompt=consent."
    );
  }

  writeLocalEnv({
    outputPath,
    senderEmail,
    clientId: oauthClient.clientId,
    clientSecret: oauthClient.clientSecret,
    refreshToken: tokenResponse.refresh_token,
    tokenUrl: oauthClient.tokenUri,
  });

  console.log(`OAuth configured for ${senderEmail}.`);
  console.log(`Wrote ${outputPath}. Restart GlassFish before testing mail delivery.`);
}

function parseArgs(args) {
  const options = {};
  for (let index = 0; index < args.length; index++) {
    const arg = args[index];
    if (!arg.startsWith("--")) {
      continue;
    }
    const keyValue = arg.slice(2).split("=");
    const key = keyValue[0];
    const value = keyValue.length > 1 ? keyValue.slice(1).join("=") : args[++index];
    options[toCamelCase(key)] = value;
  }
  return options;
}

function toCamelCase(text) {
  return text.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
}

function readOAuthClient(clientSecretPath, clientIndex) {
  const raw = fs.readFileSync(clientSecretPath, "utf8");
  const parsed = JSON.parse(raw);
  const root = parsed.installed || parsed.web || parsed;
  const clientIds = splitCredentialField(root.client_id);
  const clientSecrets = splitCredentialField(root.client_secret);

  if (!clientIds[clientIndex] || !clientSecrets[clientIndex]) {
    throw new Error(
      `OAuth client index ${clientIndex} is not available. Found ${clientIds.length} client id(s) and ${clientSecrets.length} secret(s).`
    );
  }

  return {
    authUri: root.auth_uri || "https://accounts.google.com/o/oauth2/auth",
    tokenUri: root.token_uri || "https://oauth2.googleapis.com/token",
    clientId: clientIds[clientIndex],
    clientSecret: clientSecrets[clientIndex],
  };
}

function splitCredentialField(value) {
  if (!value || typeof value !== "string") {
    return [];
  }
  return value.trim().split(/\s+/).filter(Boolean);
}

function receiveAuthorizationCode({
  oauthClient,
  redirectUri,
  state,
  port,
  openBrowser,
}) {
  return new Promise((resolve, reject) => {
    const server = http.createServer((request, response) => {
      const requestUrl = new URL(request.url, redirectUri);
      if (requestUrl.pathname !== "/oauth2callback") {
        response.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
        response.end("Not found");
        return;
      }

      const returnedState = requestUrl.searchParams.get("state");
      const error = requestUrl.searchParams.get("error");
      const code = requestUrl.searchParams.get("code");

      if (error) {
        response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
        response.end(`Google OAuth error: ${error}`);
        closeServer(server);
        reject(new Error(`Google OAuth error: ${error}`));
        return;
      }

      if (returnedState !== state) {
        response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
        response.end("Invalid OAuth state.");
        closeServer(server);
        reject(new Error("Invalid OAuth state."));
        return;
      }

      if (!code) {
        response.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
        response.end("Missing OAuth authorization code.");
        closeServer(server);
        reject(new Error("Missing OAuth authorization code."));
        return;
      }

      response.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
      response.end("<h1>TOP Racing OAuth configurado</h1><p>Ya puedes cerrar esta pestaña.</p>");
      closeServer(server);
      resolve(code);
    });

    server.on("error", reject);
    server.listen(port, "127.0.0.1", () => {
      const authUrl = buildAuthorizationUrl({
        oauthClient,
        redirectUri,
        state,
      });
      console.log("Opening Google OAuth authorization URL in Firefox.");
      console.log("Waiting for browser authorization callback...");
      if (openBrowser) {
        openInFirefox(authUrl);
      } else {
        console.log(authUrl);
      }
    });
  });
}

function buildAuthorizationUrl({ oauthClient, redirectUri, state }) {
  const authUrl = new URL(oauthClient.authUri);
  authUrl.searchParams.set("client_id", oauthClient.clientId);
  authUrl.searchParams.set("redirect_uri", redirectUri);
  authUrl.searchParams.set("response_type", "code");
  authUrl.searchParams.set("scope", GMAIL_SEND_SCOPE);
  authUrl.searchParams.set("access_type", "offline");
  authUrl.searchParams.set("prompt", "consent");
  authUrl.searchParams.set("state", state);
  return authUrl.toString();
}

function openInFirefox(url) {
  const platform = os.platform();
  if (platform === "win32") {
    const firefox = "C:/Program Files/Mozilla Firefox/firefox.exe";
    if (fs.existsSync(firefox)) {
      spawn(firefox, [url], { detached: true, stdio: "ignore" }).unref();
      return;
    }
    spawn("cmd", ["/c", "start", "", url], { detached: true, stdio: "ignore" }).unref();
    return;
  }
  if (platform === "darwin") {
    spawn("open", ["-a", "Firefox", url], { detached: true, stdio: "ignore" }).unref();
    return;
  }
  spawn("xdg-open", [url], { detached: true, stdio: "ignore" }).unref();
}

async function exchangeCodeForTokens({ oauthClient, redirectUri, code }) {
  const response = await fetch(oauthClient.tokenUri, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded; charset=utf-8",
    },
    body: new URLSearchParams({
      code,
      client_id: oauthClient.clientId,
      client_secret: oauthClient.clientSecret,
      redirect_uri: redirectUri,
      grant_type: "authorization_code",
    }),
  });

  const text = await response.text();
  let payload;
  try {
    payload = JSON.parse(text);
  } catch (error) {
    throw new Error(`Token response was not JSON. HTTP ${response.status}`);
  }

  if (!response.ok) {
    throw new Error(`Token request failed. HTTP ${response.status}: ${payload.error || text}`);
  }

  return payload;
}

function writeLocalEnv({
  outputPath,
  senderEmail,
  clientId,
  clientSecret,
  refreshToken,
  tokenUrl,
}) {
  const content = [
    "# scripts/local-env.ps1",
    "# Local TOP Racing mail configuration. This file contains secrets and is ignored by Git.",
    '$env:MAIL_DELIVERY_MODE = "gmail-oauth"',
    `$env:MAIL_SENDER_EMAIL = "${escapePowerShellString(senderEmail)}"`,
    `$env:MAIL_MONITOR_EMAIL = "${escapePowerShellString(senderEmail)}"`,
    `$env:MAIL_OAUTH_CLIENT_ID = "${escapePowerShellString(clientId)}"`,
    `$env:MAIL_OAUTH_CLIENT_SECRET = "${escapePowerShellString(clientSecret)}"`,
    `$env:MAIL_OAUTH_REFRESH_TOKEN = "${escapePowerShellString(refreshToken)}"`,
    `$env:MAIL_OAUTH_TOKEN_URL = "${escapePowerShellString(tokenUrl)}"`,
    "",
  ].join("\r\n");

  fs.writeFileSync(outputPath, content, { encoding: "utf8", mode: 0o600 });
}

function escapePowerShellString(value) {
  return String(value).replace(/`/g, "``").replace(/"/g, '`"').replace(/\$/g, "`$");
}

function closeServer(server) {
  setTimeout(() => server.close(), 250);
}
