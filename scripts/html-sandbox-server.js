import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { readFile } from 'node:fs/promises';

const MIME_TYPES = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.md': 'text/markdown; charset=utf-8',
};

function getContentType(filePath) {
  return MIME_TYPES[path.extname(filePath)] ?? 'application/octet-stream';
}

export function safeResolvePath(rootDir, requestUrl) {
  const requestPath = decodeURIComponent((requestUrl ?? '/').split('?')[0].split('#')[0] || '/');
  const normalizedPath = path.normalize(requestPath.replace(/^\/+/, ''));
  const absolutePath = path.resolve(rootDir, normalizedPath);
  const relativePath = path.relative(rootDir, absolutePath);

  if (relativePath.startsWith('..') || path.isAbsolute(relativePath)) {
    return null;
  }

  return absolutePath;
}

export function createStaticServer({ rootDir }) {
  return http.createServer(async (request, response) => {
    const targetPath = safeResolvePath(rootDir, request.url ?? '/');

    if (!targetPath) {
      response.writeHead(403, { 'content-type': 'text/plain; charset=utf-8' });
      response.end('Forbidden');
      return;
    }

    try {
      const content = await readFile(targetPath);
      response.writeHead(200, { 'content-type': getContentType(targetPath) });
      response.end(content);
    } catch {
      response.writeHead(404, { 'content-type': 'text/plain; charset=utf-8' });
      response.end('Not Found');
    }
  });
}

async function startServer() {
  const rootDir = process.cwd();
  const server = createStaticServer({ rootDir });
  const port = Number(process.env.PORT ?? 4173);
  const host = '127.0.0.1';

  await new Promise((resolve) => server.listen(port, host, resolve));
  console.log(`HTML sandbox available at:`);
  console.log(`- http://${host}:${port}/apps/html-sandbox/index.html`);
  console.log(`- http://localhost:${port}/apps/html-sandbox/index.html`);
}

const currentFilePath = fileURLToPath(import.meta.url);

if (process.argv[1] === currentFilePath) {
  startServer();
}
