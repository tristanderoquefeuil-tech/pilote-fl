// Petit serveur local pour tester l'appli Android (android/assets) dans un navigateur.
// /proxy?u=...      relaie les appels vers l'API des prix de la Commission européenne (test uniquement)
// /logo-source      sert le logo d'origine (src/logo/logo-source.webp)
// POST /save?f=nom  enregistre une image générée dans src/logo/out/ (préparation des icônes)
const http = require("http"), fs = require("fs"), path = require("path");
const root = path.join(__dirname, "..", "android", "assets");
const outDir = path.join(__dirname, "logo", "out");
const types = { ".html": "text/html; charset=utf-8", ".js": "text/javascript; charset=utf-8" };
http.createServer(async (req, res) => {
  const u = new URL(req.url, "http://localhost");
  if (u.pathname === "/proxy") {
    const target = u.searchParams.get("u") || "";
    if (!target.startsWith("https://api.tech.ec.europa.eu/agrifood/")) { res.writeHead(403); res.end("adresse non autorisée"); return; }
    try { const r = await fetch(target); res.writeHead(r.status, { "content-type": "application/json" }); res.end(await r.text()); }
    catch (e) { res.writeHead(502); res.end(String(e.message)); }
    return;
  }
  if (u.pathname === "/logo-source") {
    res.writeHead(200, { "content-type": "image/webp" });
    res.end(fs.readFileSync(path.join(__dirname, "logo", "logo-source.webp")));
    return;
  }
  if (u.pathname === "/save" && req.method === "POST") {
    const name = (u.searchParams.get("f") || "").replace(/[^A-Za-z0-9._-]/g, "");
    if (!name) { res.writeHead(400); res.end(); return; }
    const chunks = []; for await (const c of req) chunks.push(c);
    const b64 = Buffer.concat(chunks).toString("utf8").replace(/^data:[^,]+,/, "");
    fs.mkdirSync(outDir, { recursive: true });
    fs.writeFileSync(path.join(outDir, name), Buffer.from(b64, "base64"));
    res.writeHead(200); res.end("ok"); return;
  }
  const rel = decodeURIComponent(u.pathname).replace(/^\/+/, "") || "index.html";
  const file = path.join(root, rel);
  if (!file.startsWith(root) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) { res.writeHead(404); res.end(); return; }
  res.writeHead(200, { "content-type": types[path.extname(file)] || "application/octet-stream" });
  res.end(fs.readFileSync(file));
}).listen(5178);
