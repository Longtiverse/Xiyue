import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { readFile, readdir } from 'node:fs/promises';
import { exec } from 'node:child_process';
import { promisify } from 'node:util';

const execAsync = promisify(exec);

const MIME_TYPES = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.md': 'text/markdown; charset=utf-8',
};

const ALLOWED_COMMANDS = new Set([
  'test:music-core',
  'test:html-sandbox',
  'test:android',
  'lint',
  'lint:fix',
  'format',
  'format:check',
  'typecheck',
  'sandbox',
  'build:android',
]);

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

function sendJSON(response, data, statusCode = 200) {
  const headers = {
    'content-type': 'application/json; charset=utf-8',
    'access-control-allow-origin': '*',
    'access-control-allow-methods': 'GET, POST, OPTIONS',
  };
  response.writeHead(statusCode, headers);
  response.end(JSON.stringify(data));
}

function sendText(response, text, statusCode = 200) {
  const headers = {
    'content-type': 'text/plain; charset=utf-8',
    'access-control-allow-origin': '*',
  };
  response.writeHead(statusCode, headers);
  response.end(text);
}

async function handleAPI(request, response, rootDir) {
  const url = new URL(request.url ?? '/', `http://${request.headers.host}`);
  const pathname = url.pathname;

  // GET /api/progress
  if (pathname === '/api/progress') {
    try {
      const content = await readFile(path.join(rootDir, '.tmp', 'session-progress.md'), 'utf-8');
      sendText(response, content);
    } catch {
      sendJSON(response, { error: 'session-progress.md not found' }, 404);
    }
    return;
  }

  // GET /api/plans
  if (pathname === '/api/plans') {
    try {
      const tmpDir = path.join(rootDir, '.tmp');
      const files = await readdir(tmpDir);
      const planFiles = files.filter(f => f.startsWith('plan-') && f.endsWith('.md'));
      const plans = [];
      for (const file of planFiles) {
        const content = await readFile(path.join(tmpDir, file), 'utf-8');
        const titleMatch = content.match(/^# Plan:\s*(.+)$/m);
        const title = titleMatch ? titleMatch[1].trim() : file.replace('.md', '');
        const checkboxes = content.match(/^-\s*\[[\sx]\]/gm) || [];
        const checked = content.match(/^-\s*\[x\]/gm) || [];
        const progress = checkboxes.length > 0 ? Math.round((checked.length / checkboxes.length) * 100) : 0;
        plans.push({ title, file, progress, content });
      }
      sendJSON(response, plans);
    } catch (err) {
      sendJSON(response, { error: err.message }, 500);
    }
    return;
  }

  // GET /api/todos
  if (pathname === '/api/todos') {
    try {
      const content = await readFile(path.join(rootDir, 'todos-improvements.json'), 'utf-8');
      sendJSON(response, JSON.parse(content));
      return;
    } catch {
      // fallback: aggregate from session-progress.md and plan-*.md
      try {
        const items = [];
        let id = 1;

        try {
          const progressContent = await readFile(path.join(rootDir, '.tmp', 'session-progress.md'), 'utf-8');
          const nextStepsMatch = progressContent.match(/^## Next Steps\s*\n([\s\S]*?)(?=^## |\z)/m);
          if (nextStepsMatch) {
            for (const line of nextStepsMatch[1].split('\n')) {
              const m = line.match(/^\d+\.\s*(.+)$/);
              if (m) items.push({ id: id++, title: m[1].trim(), status: 'pending', priority: 'high' });
            }
          }
          const blockersMatch = progressContent.match(/^## Blockers\s*\n([\s\S]*?)(?=^## |\z)/m);
          if (blockersMatch) {
            for (const line of blockersMatch[1].split('\n')) {
              const cleaned = line.replace(/^[-•]\s*/, '').trim();
              if (cleaned && cleaned !== '暂无' && cleaned !== '...') {
                items.push({ id: id++, title: `【阻塞】${cleaned}`, status: 'pending', priority: 'high' });
              }
            }
          }
        } catch {}

        try {
          const tmpDir = path.join(rootDir, '.tmp');
          const files = await readdir(tmpDir);
          const planFiles = files.filter(f => f.startsWith('plan-') && f.endsWith('.md'));
          for (const file of planFiles) {
            const content = await readFile(path.join(tmpDir, file), 'utf-8');
            for (const line of content.split('\n')) {
              const m = line.match(/^-\s*\[\s\]\s*(.+)$/) || line.match(/^\d+\.\s*\[\s\]\s*(.+)$/);
              if (m) items.push({ id: id++, title: m[1].trim(), status: 'pending', priority: 'medium' });
            }
          }
        } catch {}

        sendJSON(response, { items, _source: 'aggregated' });
      } catch (err) {
        sendJSON(response, { error: err.message }, 500);
      }
    }
    return;
  }

  // GET /api/git-log
  if (pathname === '/api/git-log') {
    try {
      const { stdout } = await execAsync('git log --pretty=format:"%h|%ci|%s" -10', { cwd: rootDir, timeout: 5000 });
      const lines = stdout.trim().split('\n').filter(Boolean);
      sendJSON(response, lines);
    } catch (err) {
      sendJSON(response, { error: err.message }, 500);
    }
    return;
  }

  // POST /api/run?cmd=xxx
  if (pathname === '/api/run') {
    if (request.method !== 'POST' && request.method !== 'GET') {
      sendJSON(response, { error: 'Method not allowed' }, 405);
      return;
    }
    const cmd = url.searchParams.get('cmd');
    if (!cmd || !ALLOWED_COMMANDS.has(cmd)) {
      sendJSON(response, { error: `Command "${cmd}" is not allowed` }, 403);
      return;
    }
    try {
      const { stdout, stderr } = await execAsync(`npm run ${cmd}`, {
        cwd: rootDir,
        timeout: 120000,
        env: { ...process.env, FORCE_COLOR: '0', NO_COLOR: '1' },
      });
      sendText(response, stdout + (stderr ? '\n\n[stderr]\n' + stderr : ''));
    } catch (err) {
      const output = err.stdout || '';
      const stderr = err.stderr || '';
      const message = err.message || 'Command failed';
      sendText(response, output + (stderr ? '\n\n[stderr]\n' + stderr : '') + '\n\n[error]\n' + message, 500);
    }
    return;
  }

  // Unknown API
  sendJSON(response, { error: 'Unknown API endpoint' }, 404);
}

export function createStaticServer({ rootDir }) {
  return http.createServer(async (request, response) => {
    const urlPath = request.url ?? '/';

    // API routes
    if (urlPath.startsWith('/api/')) {
      await handleAPI(request, response, rootDir);
      return;
    }

    const targetPath = safeResolvePath(rootDir, urlPath);

    if (!targetPath) {
      response.writeHead(403, { 'content-type': 'text/plain; charset=utf-8' });
      response.end('Forbidden');
      return;
    }

    try {
      const content = await readFile(targetPath);
      const headers = {
        'content-type': getContentType(targetPath),
        'access-control-allow-origin': '*',
      };
      response.writeHead(200, headers);
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
  console.log(`- http://localhost:${port}/apps/html-sandbox/project-cockpit.html`);
}

const currentFilePath = fileURLToPath(import.meta.url);

if (process.argv[1] === currentFilePath) {
  startServer();
}
