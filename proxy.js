// 反向代理：模拟器通过 10.0.2.2:8888 访问，转发到 127.0.0.1:3000
const http = require('http');

const PROXY_PORT = 8888;
const TARGET = { host: '127.0.0.1', port: 3000 };

const proxy = http.createServer((req, res) => {
  const options = {
    host: TARGET.host,
    port: TARGET.port,
    path: req.url,
    method: req.method,
    headers: { ...req.headers }
  };
  // 保留 Authorization
  delete options.headers.host;

  const proxyReq = http.request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode, proxyRes.headers);
    proxyRes.pipe(res);
  });

  proxyReq.on('error', (e) => {
    console.error(`[proxy] ${req.method} ${req.url} -> ${e.message}`);
    res.writeHead(502);
    res.end(JSON.stringify({ success: false, error: e.message }));
  });

  req.pipe(proxyReq);
});

proxy.listen(PROXY_PORT, '0.0.0.0', () => {
  console.log(`[proxy] listening on 0.0.0.0:${PROXY_PORT} -> ${TARGET.host}:${TARGET.port}`);
});
