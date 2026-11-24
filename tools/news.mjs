// Récupère les actualités des fruits et légumes et de la grande distribution (GMS)
// à partir de flux RSS publics, et les écrit dans un fichier JSON lu par l'appli.
// Usage : node tools/news.mjs docs/data/news.json
// Exécuté plusieurs fois par jour par GitHub Actions (.github/workflows/news.yml) ; aucune clé n'est nécessaire.
// On ne garde que le titre, la source, la date, un court extrait et le lien vers l'article d'origine.
import fs from "node:fs";
import path from "node:path";

const OUT = process.argv[2] || "docs/data/news.json";
const GN = (q) => "https://news.google.com/rss/search?q=" + encodeURIComponent(q + " when:7d") + "&hl=fr&gl=FR&ceid=FR:fr";
// cat : rubrique par défaut des articles du flux ; max : nombre d'articles gardés par flux
const FEEDS = [
  { nom: "Végétable", url: "https://www.vegetable.fr/feed", cat: "fl", max: 12 },
  { nom: "FreshPlaza", url: "https://www.freshplaza.fr/rss.xml", cat: "fl", max: 12 },
  { nom: "Réussir Fruits & Légumes", url: "https://www.reussir.fr/fruits-legumes/rss.xml", cat: "fl", max: 10, strict: true }, // flux agricole généraliste : seulement les articles qui parlent de fruits ou légumes
  { nom: "Google Actualités", url: GN('"fruits et légumes" (filière OR producteurs OR prix OR rayon OR récolte)'), cat: "fl", max: 15, gn: true, strict: true },
  { nom: "Google Actualités", url: GN('"fruits et légumes" ("grande distribution" OR supermarché OR supermarchés OR enseigne)'), cat: "gms", max: 10, gn: true },
  { nom: "Google Actualités", url: GN('"grande distribution" OR GMS OR hypermarchés OR supermarchés OR "E.Leclerc" OR "magasins Leclerc" OR Carrefour OR Intermarché OR Lidl OR Auchan OR "Système U"'), cat: "gms", max: 25, gn: true },
];
// Hors sujet dans les flux agricoles généralistes
const EXCLU = /c[ée]r[ée]ales|ol[ée]agineux|\bbl[ée]\b|colza|soja|ma[iï]s\b|bovin|porcin|\bporc\b|laitier|lait\b|volaille|ovin|vin\b|viticult/i;
// Homonymes sans rapport (le pilote Charles Leclerc, etc.)
const HORS_GMS = /\bF1\b|Formule 1|Ferrari|Grand Prix|\bGP\b|Hamilton|Charles Leclerc|football|Ligue 1|rugby|tennis/i;
// Pages institutionnelles ou sans rapport renvoyées par la recherche
const HORS_SOURCES = /Carrefour Group|Mairie|^750g$/i;
// Mots qui rattachent aussi un article à l'autre rubrique
const MOTS_FL = /fruit|l[ée]gume|tomate|pomme|poire|agrume|cl[ée]mentine|orange|banane|kiwi|salade|carotte|pomme de terre|melon|fraise|primeur|maraîch|maraich|arboricult/i;
const MOTS_GMS = /grande distribution|\bGMS\b|hypermarch|supermarch|enseigne|Leclerc|Carrefour|Intermarch|Lidl|Auchan|Syst[eè]me U|Casino|Monoprix|Aldi|Franprix|Coopérative U|centrale d'achat|négociations commerciales|EGalim|drive\b|rayon/i;
const UA = "Mozilla/5.0 (compatible; PiloteFL-actus/1.0; +https://tristanderoquefeuil-tech.github.io/pilote-fl/)";
const JOURS = 14, MAX = 90;

const ENT = { amp: "&", lt: "<", gt: ">", quot: '"', apos: "'", nbsp: " ", rsquo: "’", lsquo: "‘", ldquo: "“", rdquo: "”", hellip: "…", eacute: "é", egrave: "è", agrave: "à", ccedil: "ç", ecirc: "ê", ocirc: "ô", laquo: "«", raquo: "»", ndash: "–", mdash: "—" };
const decode = (s) => s.replace(/&(#x[0-9a-f]+|#\d+|[a-z]+);/gi, (m, e) =>
  e[0] === "#" ? String.fromCodePoint(e[1].toLowerCase() === "x" ? parseInt(e.slice(2), 16) : parseInt(e.slice(1), 10)) : ENT[e.toLowerCase()] ?? m);
const cdata = (s) => s.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, "$1");
const texte = (s) => decode(cdata(s || "").replace(/<[^>]+>/g, " ")).replace(/<[^>]+>/g, " ").replace(/\s+/g, " ").trim();
const tag = (item, name) => { const m = item.match(new RegExp(`<${name}\\b[^>]*>([\\s\\S]*?)</${name}>`, "i")); return m ? m[1] : ""; };
const extrait = (s, n = 220) => s.length <= n ? s : s.slice(0, s.lastIndexOf(" ", n) > 80 ? s.lastIndexOf(" ", n) : n).replace(/[\s,;:.–-]+$/, "") + "…";
const cle = (t) => t.toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "").replace(/[^a-z0-9]+/g, " ").trim().slice(0, 70);

async function lire(feed) {
  const r = await fetch(feed.url, { headers: { "User-Agent": UA, Accept: "application/rss+xml, application/xml, text/xml" }, signal: AbortSignal.timeout(20000) });
  if (!r.ok) throw new Error("HTTP " + r.status);
  const xml = await r.text();
  const items = xml.match(/<item\b[\s\S]*?<\/item>/gi) || [];
  const out = [];
  for (const it of items) {
    let titre = texte(tag(it, "title"));
    let source = feed.nom;
    if (feed.gn) { // Google Actualités : « Titre - Média », le média est aussi dans <source>
      const src = texte(tag(it, "source"));
      if (src) { source = src; if (titre.endsWith(" - " + src)) titre = titre.slice(0, -(src.length + 3)); }
    }
    const lien = texte(tag(it, "link"));
    const date = new Date(texte(tag(it, "pubDate")));
    if (!titre || !/^https:\/\//.test(lien) || isNaN(date)) continue;
    const resume = feed.gn ? "" : extrait(texte(tag(it, "description")));
    if (EXCLU.test(titre) && !MOTS_FL.test(titre)) continue;
    if (HORS_GMS.test(titre + " " + resume) || HORS_SOURCES.test(source)) continue;
    if (feed.strict && !MOTS_FL.test(titre + " " + resume)) continue;
    const img = (it.match(/<enclosure[^>]+url="(https:[^"]+)"[^>]*>/i) || it.match(/<media:(?:content|thumbnail)[^>]+url="(https:[^"]+)"/i) || [])[1] || "";
    const cats = new Set([feed.cat]);
    const tout = titre + " " + resume;
    if (MOTS_FL.test(tout)) cats.add("fl");
    if (MOTS_GMS.test(tout)) cats.add("gms");
    out.push({ titre, source, date: date.toISOString(), lien, resume, img: /\.(jpe?g|png|webp)(\?|$)/i.test(img) ? decode(img) : "", cats: [...cats].sort() });
    if (out.length >= feed.max) break;
  }
  return out;
}

const limite = Date.now() - JOURS * 864e5;
const vus = new Set(), articles = [], erreurs = [];
for (const f of FEEDS) {
  let lot = [];
  for (let essai = 1; essai <= 2 && !lot.length; essai++) {
    try { lot = await lire(f); } catch (e) { if (essai === 2) erreurs.push(`${f.nom} (${f.cat}) : ${e.message}`); else await new Promise((r) => setTimeout(r, 3000)); }
  }
  console.log(`${f.nom} (${f.cat}) : ${lot.length} articles`);
  for (const a of lot) {
    const k = cle(a.titre);
    if (vus.has(k) || Date.parse(a.date) < limite) continue;
    vus.add(k); articles.push(a);
  }
}
erreurs.forEach((e) => console.log("Échec : " + e));
articles.sort((a, b) => b.date.localeCompare(a.date));
if (articles.length < 5) { console.log("Trop peu d'articles récupérés : fichier inchangé."); process.exit(erreurs.length ? 1 : 0); }

// On ne réécrit le fichier que si la liste a changé (évite les publications inutiles)
const liste = articles.slice(0, MAX);
let ancien = null;
try { ancien = JSON.parse(fs.readFileSync(OUT, "utf8")); } catch {}
if (ancien && JSON.stringify(ancien.articles) === JSON.stringify(liste)) { console.log("Aucune nouvelle actualité."); process.exit(0); }
fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify({ maj: new Date().toISOString(), articles: liste }, null, 1) + "\n");
console.log(`${liste.length} articles écrits dans ${OUT}`);
