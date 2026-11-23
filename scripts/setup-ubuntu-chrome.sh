#!/usr/bin/env bash
set -euo pipefail

# Install Google Chrome Stable from Google's apt repository. Ubuntu's Chromium
# package is a Snap launcher, which can fail to start reliably under ChromeDriver.
if [[ "${EUID}" -ne 0 ]]; then
  echo "Run with sudo: sudo bash scripts/setup-ubuntu-chrome.sh" >&2
  exit 1
fi

apt-get update
apt-get install -y ca-certificates curl gnupg
install -d -m 0755 /etc/apt/keyrings
curl -fsSL https://dl.google.com/linux/linux_signing_key.pub \
  | gpg --dearmor --yes -o /etc/apt/keyrings/google-chrome.gpg
chmod 0644 /etc/apt/keyrings/google-chrome.gpg
echo 'deb [arch=amd64 signed-by=/etc/apt/keyrings/google-chrome.gpg] https://dl.google.com/linux/chrome/deb/ stable main' \
  > /etc/apt/sources.list.d/google-chrome.list

apt-get update
apt-get install -y google-chrome-stable

google-chrome --version
