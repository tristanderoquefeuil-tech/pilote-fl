// Assemble l'application à partir de src/ en trois versions :
//  - pilote-fl.html            version claude.ai (artifact) : données en ligne, cours déposés par la tâche du lundi
//  - android/assets/index.html  appli Android : données sur le téléphone, cours lus sur le site GitHub
//  - docs/index.html            site autonome GitHub Pages : données dans le navigateur, cours publiés par GitHub Actions
const fs = require("fs"), path = require("path");
const root = path.join(__dirname, "..");
const SITE = "https://tristanderoquefeuil-tech.github.io/pilote-fl/";
const XLSX_CDN = "https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js";

const css = ["pro.css", "market.css"].map((f) => fs.readFileSync(path.join(__dirname, f), "utf8")).join("\n");
// Logo de l'application, intégré en image WebP (256 px)
const logo = "data:image/webp;base64," + fs.readFileSync(path.join(__dirname, "logo", "out", "logo-256.webp")).toString("base64");
const base = fs.readFileSync(path.join(__dirname, "app.html"), "utf8").replace("/*CSS*/", () => css).split("__LOGO__").join(logo);
const withUrls = (page, cours, version, news) => page.split("__COURS_URL__").join(cours).split("__VERSION_URL__").join(version).split("__NEWS_URL__").join(news);
const standalone = (page) => '<!doctype html>\n<html lang="fr">\n<head>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">\n<style>body{margin:0}[hidden]{display:none!important}img{max-width:100%}</style>\n' +
  page.replace('<meta name="claude:viewport-fit-sides" content="cover">\n', "").replace(XLSX_CDN, "xlsx.full.min.js") + "\n</html>\n";

// 1. Version claude.ai
fs.writeFileSync(path.join(root, "pilote-fl.html"), withUrls(base, "", "", SITE + "data/news.json"));
// 2. Appli Android (bibliothèque Excel embarquée pour fonctionner sans connexion)
fs.writeFileSync(path.join(root, "android", "assets", "index.html"), standalone(withUrls(base, SITE + "data/cours.json", SITE + "version.json", SITE + "data/news.json")));
// 3. Site GitHub Pages
fs.mkdirSync(path.join(root, "docs", "data"), { recursive: true });
fs.writeFileSync(path.join(root, "docs", "index.html"), standalone(withUrls(base, "data/cours.json", "", "data/news.json")));
fs.copyFileSync(path.join(root, "android", "assets", "xlsx.full.min.js"), path.join(root, "docs", "xlsx.full.min.js"));
console.log("pilote-fl.html, android/assets/index.html et docs/index.html générés");
