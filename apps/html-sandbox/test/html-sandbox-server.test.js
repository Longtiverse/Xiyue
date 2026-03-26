import test from 'node:test';
import assert from 'node:assert/strict';

import { createStaticServer, safeResolvePath } from '../../../scripts/html-sandbox-server.js';

async function startServer() {
  const server = createStaticServer({ rootDir: process.cwd() });

  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));

  const address = server.address();
  return {
    server,
    baseUrl: `http://127.0.0.1:${address.port}`,
  };
}

test('serves html sandbox entry and module files over http', async () => {
  const { server, baseUrl } = await startServer();

  try {
    const indexResponse = await fetch(`${baseUrl}/apps/html-sandbox/index.html`);
    const appResponse = await fetch(`${baseUrl}/apps/html-sandbox/app.js`);
    const coreResponse = await fetch(`${baseUrl}/packages/music-core/src/index.js`);

    assert.equal(indexResponse.status, 200);
    assert.match(indexResponse.headers.get('content-type') ?? '', /text\/html/);
    assert.match(await indexResponse.text(), /HTML Sandbox/);

    assert.equal(appResponse.status, 200);
    assert.match(appResponse.headers.get('content-type') ?? '', /javascript/);

    assert.equal(coreResponse.status, 200);
    assert.match(coreResponse.headers.get('content-type') ?? '', /javascript/);
  } finally {
    await new Promise((resolve, reject) => server.close((error) => (error ? reject(error) : resolve())));
  }
});

test('rejects path traversal outside the repository root', () => {
  const resolvedPath = safeResolvePath(process.cwd(), '/../../Windows/win.ini');

  assert.equal(resolvedPath, null);
});
