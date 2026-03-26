import test from 'node:test';
import assert from 'node:assert/strict';

import { getSandboxUrl } from '../../../scripts/open-html-sandbox.js';

test('returns the default html sandbox url', () => {
  assert.equal(getSandboxUrl(), 'http://127.0.0.1:4173/apps/html-sandbox/index.html');
  assert.equal(getSandboxUrl({ port: 5000, host: 'localhost' }), 'http://localhost:5000/apps/html-sandbox/index.html');
});
