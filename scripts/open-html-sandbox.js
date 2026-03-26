import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';

export function getSandboxUrl({ host = '127.0.0.1', port = 4173 } = {}) {
  return `http://${host}:${port}/apps/html-sandbox/index.html`;
}

function openBrowser(url) {
  if (process.platform === 'win32') {
    spawn('cmd', ['/c', 'start', '', url], {
      detached: true,
      stdio: 'ignore',
    }).unref();
    return;
  }

  if (process.platform === 'darwin') {
    spawn('open', [url], { detached: true, stdio: 'ignore' }).unref();
    return;
  }

  spawn('xdg-open', [url], { detached: true, stdio: 'ignore' }).unref();
}

const currentFilePath = fileURLToPath(import.meta.url);

if (process.argv[1] === currentFilePath) {
  const url = getSandboxUrl();
  openBrowser(url);
  console.log(`Opened ${url}`);
}
