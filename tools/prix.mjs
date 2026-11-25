// Récupère les prix de vente au détail RÉELS des fruits et légumes en France et les écrit dans un fichier JSON.
// Usage : node tools/prix.mjs docs/data/prix.json
// Exécuté chaque jour par GitHub Actions (.github/workflows/cours.yml) ; aucune clé n'est nécessaire.
// Sources, par ordre de priorité pour chaque produit :
//  1. INSEE, prix moyens mensuels de vente au détail en métropole (série officielle, € TTC/kg)
//  2. Open Prices (Open Food Facts), prix relevés en magasin en France par les contributeurs :
//     médiane des 120 derniers jours (à défaut, des 365 derniers jours pour les produits hors saison), produits non bio, prix hors remise, au kilo (3 relevés minimum)
import fs from "node:fs";
import path from "node:path";

const OUT = process.argv[2] || "docs/data/prix.json";
const UA = "PiloteFL/1.0 (+https://tristanderoquefeuil-tech.github.io/pilote-fl/)";
// Produit de l'appli → identifiant de la série INSEE (prix moyen mensuel, 1 kg, métropole)
const INSEE = {
  "Tomate": "000641429", "Poire": "000641369", "Poireau": "000641428", "Carotte": "000641422",
  "Haricot vert": "000641359", "Champignon de Paris": "000641423", "Pomme de terre": "000641360",
  "Pomme": "000641367", "Endive": "000641426", "Courgette": "000641425", "Clémentine": "000641435",
};
// Produit de l'appli → catégories Open Food Facts essayées
const OFF = {
  "Tomate": ["en:tomatoes"], "Courgette": ["en:zucchini", "en:courgettes"], "Aubergine": ["en:eggplants", "en:aubergines"],
  "Poivron": ["en:bell-peppers", "en:sweet-peppers"], "Concombre": ["en:cucumbers"], "Haricot vert": ["en:green-beans"],
  "Carotte": ["en:carrots"], "Pomme de terre": ["en:potatoes"], "Poireau": ["en:leeks"], "Chou-fleur": ["en:cauliflowers"],
  "Brocoli": ["en:broccoli"], "Endive": ["en:endives", "en:chicories"], "Épinard": ["en:spinachs", "en:spinach"],
  "Asperge": ["en:asparagus"], "Salade": ["en:lettuces", "en:salads"], "Radis": ["en:radishes"],
  "Courge / potiron": ["en:squashes", "en:pumpkins"], "Betterave": ["en:beetroots", "en:beets"], "Navet": ["en:turnips"],
  "Champignon de Paris": ["en:button-mushrooms", "en:mushrooms"], "Fraise": ["en:strawberries"], "Framboise": ["en:raspberries"],
  "Myrtille": ["en:blueberries"], "Cerise": ["en:cherries"], "Abricot": ["en:apricots"], "Pêche / nectarine": ["en:peaches", "en:nectarines"],
  "Melon": ["en:melons"], "Pastèque": ["en:watermelons"], "Prune": ["en:plums"], "Figue": ["en:figs"], "Raisin": ["en:grapes", "en:table-grapes"],
  "Pomme": ["en:apples"], "Poire": ["en:pears"], "Kiwi": ["en:kiwis", "en:kiwifruits"], "Clémentine": ["en:clementines", "en:mandarins"],
  "Châtaigne": ["en:chestnuts"], "Rhubarbe": ["en:rhubarbs", "en:rhubarb"],
};
const JOURS = 120, MIN_RELEVES = 3;
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const r2 = (x) => Math.round(x * 100) / 100;
const median = (a) => { const s = [...a].sort((x, y) => x - y), m = s.length >> 1; return s.length % 2 ? s[m] : (s[m - 1] + s[m]) / 2; };

async function get(url, type = "json") {
  for (let i = 0; i < 3; i++) {
    try {
      const r = await fetch(url, { headers: { "User-Agent": UA }, signal: AbortSignal.timeout(60000) });
      if (r.ok) return type === "json" ? r.json() : r.text();
      if (r.status === 404) return null;
    } catch { /* nouvel essai */ }
    await sleep(3000 * (i + 1));
  }
  return null;
}

// 1. INSEE : toutes les séries d'un coup (24 derniers mois)
async function insee() {
  const ids = Object.values(INSEE).join("+");
  const xml = await get(`https://bdm.insee.fr/series/sdmx/data/SERIES_BDM/${ids}?lastNObservations=24`, "text");
  const out = {};
  if (!xml) { console.log("INSEE : indisponible"); return out; }
  for (const [nom, id] of Object.entries(INSEE)) {
    const m = xml.match(new RegExp(`<Series [^>]*IDBANK="${id}"[^>]*>([\\s\\S]*?)</Series>`));
    if (!m) continue;
    const pts = [...m[1].matchAll(/TIME_PERIOD="(\d{4}-\d{2})" OBS_VALUE="([\d.]+)"/g)].map((x) => ({ m: x[1], p: +x[2] })).filter((x) => x.p > 0).sort((a, b) => a.m.localeCompare(b.m));
    if (pts.length) out[nom] = pts;
  }
  console.log(`INSEE : ${Object.keys(out).length} produits`);
  return out;
}

// 2. Open Prices : relevés récents en France
async function openPrices(nom, tags, jours = JOURS) {
  const depuis = new Date(Date.now() - jours * 864e5).toISOString().slice(0, 10);
  let rel = [];
  for (const tag of tags) {
    for (let page = 1; page <= 3; page++) {
      const j = await get(`https://prices.openfoodfacts.org/api/v1/prices?category_tag=${encodeURIComponent(tag)}&currency=EUR&date__gte=${depuis}&size=100&page=${page}&order_by=-date`);
      await sleep(600);
      const it = (j && j.items) || [];
      rel.push(...it);
      if (it.length < 100) break;
    }
  }
  const vus = new Set();
  const ok = rel.filter((x) => {
    if (vus.has(x.id)) return false; vus.add(x.id);
    const pays = (x.location && x.location.osm_address_country) || "";
    const bio = (x.labels_tags || []).some((l) => /organic|bio/i.test(l));
    return /^(France|FR)$/i.test(pays) && x.price_per === "KILOGRAM" && !bio;
  }).map((x) => ({ p: Number(x.price_is_discounted && x.price_without_discount ? x.price_without_discount : x.price), d: x.date, e: (x.location && x.location.osm_name) || "" }))
    .filter((x) => x.p > 0.2 && x.p < 60);
  if (ok.length < MIN_RELEVES) return null;
  const enseignes = [...new Set(ok.map((x) => x.e).filter(Boolean))];
  return { prix: r2(median(ok.map((x) => x.p))), n: ok.length, du: ok.map((x) => x.d).sort()[0], au: ok.map((x) => x.d).sort().at(-1), enseignes: enseignes.length };
}

const ins = await insee();
const produits = [];
for (const [nom, tags] of Object.entries(OFF)) {
  const serie = ins[nom];
  if (serie) {
    const last = serie.at(-1);
    produits.push({ produit: nom, prix: r2(last.p), unite: "€ TTC/kg", periode: last.m, source: "insee", libelle: "prix moyen de vente au détail en métropole (INSEE)", historique: serie });
    console.log(`${nom.padEnd(20)} INSEE ${last.m} = ${last.p.toFixed(2)} €/kg`);
    continue;
  }
  const op = (await openPrices(nom, tags)) || (await openPrices(nom, tags, 365));
  if (op) {
    produits.push({ produit: nom, prix: op.prix, unite: "€ TTC/kg", periode: `${op.du} → ${op.au}`, source: "openprices", libelle: `médiane de ${op.n} relevés en magasin (${op.enseignes} magasin${op.enseignes > 1 ? "s" : ""}), Open Prices`, releves: op.n });
    console.log(`${nom.padEnd(20)} Open Prices ${op.n} relevés = ${op.prix.toFixed(2)} €/kg`);
  } else console.log(`${nom.padEnd(20)} aucun prix public suffisant`);
}

if (produits.length < 5) { console.log("Trop peu de prix récupérés : fichier inchangé."); process.exit(0); }
let ancien = null;
try { ancien = JSON.parse(fs.readFileSync(OUT, "utf8")); } catch {}
if (ancien && JSON.stringify(ancien.produits) === JSON.stringify(produits)) { console.log("Aucun changement : fichier inchangé."); process.exit(0); }
fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify({ maj: new Date().toISOString(), produits }));
console.log(`${produits.length} produits sur ${Object.keys(OFF).length} avec un prix de vente réel → ${OUT}`);
