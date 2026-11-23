// Récupère les cours hebdomadaires réels des fruits et légumes (Commission européenne,
// Agri-food Data Portal : prix moyen départ station, France) et les écrit dans un fichier JSON.
// Usage : node tools/cours.mjs docs/data/cours.json
// Exécuté chaque jour par GitHub Actions (.github/workflows/cours.yml) ; aucune clé n'est nécessaire.
import fs from "node:fs";
import path from "node:path";

const OUT = process.argv[2] || "docs/data/cours.json";
const API = "https://api.tech.ec.europa.eu/agrifood/api/fruitAndVegetable/pricesSupplyChain";
// Produit de l'appli → [produit API, variété suivie, libellé affiché]
const MAP = {
  "Tomate": ["Tomatoes", 'Tomatoes - Trusses=Vine/"Grappes"', "tomate grappe"],
  "Pomme": ["Apples", "Apples - All types and varieties", "toutes variétés"],
  "Poire": ["Pears", "Pears - All types and varieties", "toutes variétés"],
  "Clémentine": ["Clementines", "Clementines - All types and varieties", "toutes variétés"],
  "Chou-fleur": ["Cauliflowers", "Cauliflowers - All types and varieties", "toutes variétés"],
  "Concombre": ["Cucumbers", "Cucumbers - All types and varieties", "toutes variétés"],
  "Raisin": ["Table Grapes", "Table Grapes - All types and varieties", "raisin de table"],
  "Kiwi": ["Kiwis Hayward", "Kiwis Hayward - All types and varieties", "Hayward"],
  "Aubergine": ["Egg Plants", "Egg Plants - All types and varieties", "toutes variétés"],
  "Melon": ["Melons", "Melons - All types and varieties", "toutes variétés"],
  "Pêche / nectarine": ["Peaches", "Peaches - All types and varieties", "pêche"],
  "Abricot": ["Apricots", "Apricots - All types and varieties - large packaging", "grand emballage"],
};
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// Semaine ISO calculée à partir de la date (l'API se trompe parfois d'année fin décembre)
function isoWeek(yy, mm, dd) {
  const dt = new Date(Date.UTC(yy, mm - 1, dd));
  const day = dt.getUTCDay() || 7;
  dt.setUTCDate(dt.getUTCDate() + 4 - day);
  const y = dt.getUTCFullYear();
  const w = Math.ceil(((dt - Date.UTC(y, 0, 1)) / 86400000 + 1) / 7);
  return `${y}-S${String(w).padStart(2, "0")}`;
}

async function getProduct(prod, begin) {
  let last = "";
  for (let i = 0; i < 6; i++) {
    await sleep(2500 + i * 4000); // l'API limite les appels trop rapprochés
    try {
      const r = await fetch(`${API}?memberStateCodes=FR&products=${encodeURIComponent(prod)}&beginDate=${begin}`, { headers: { Accept: "application/json" } });
      const t = await r.text();
      try { const j = JSON.parse(t); if (Array.isArray(j)) return j; } catch { /* réponse non JSON */ }
      last = `HTTP ${r.status} ${t.slice(0, 80).replace(/\s+/g, " ")}`;
    } catch (e) { last = e.message; }
  }
  console.log(`  (${prod} : ${last})`);
  return null;
}

const previous = (() => { try { return JSON.parse(fs.readFileSync(OUT, "utf8")); } catch { return { produits: [] }; } })();
const prevByName = Object.fromEntries((previous.produits || []).map((p) => [p.produit, p]));
const d0 = new Date(); d0.setFullYear(d0.getFullYear() - 2);
const begin = `${String(d0.getDate()).padStart(2, "0")}/${String(d0.getMonth() + 1).padStart(2, "0")}/${d0.getFullYear()}`;
const maj = new Date().toISOString();
const produits = [];
let ok = 0;

// Premier passage, puis un second passage plus tard pour les produits en échec
const results = {};
for (const [nom, [prod]] of Object.entries(MAP)) results[nom] = await getProduct(prod, begin);
const echecs = Object.keys(MAP).filter((n) => !results[n]);
if (echecs.length) {
  console.log(`Second passage dans 30 s pour : ${echecs.join(", ")}`);
  await sleep(30000);
  for (const nom of echecs) results[nom] = await getProduct(MAP[nom][0], begin);
}

for (const [nom, [, variete, label]] of Object.entries(MAP)) {
  const arr = results[nom];
  if (!arr) {
    console.log(`ÉCHEC ${nom} : cours précédents conservés`);
    if (prevByName[nom]) produits.push(prevByName[nom]);
    continue;
  }
  const seen = new Map();
  arr.filter((x) => x.productStage === "Ex-packaging station price" && x.periodType === "Week" && x.variety === variete).forEach((x) => {
    const [dd, mm, yy] = String(x.beginDate).split("/").map(Number);
    const p = Number(String(x.price).replace(/[€\s]/g, "").replace(/,/g, "")) / 100;
    if (p > 0) seen.set(isoWeek(yy, mm, dd), { s: isoWeek(yy, mm, dd), p: Math.round(p * 1000) / 1000, d: `${yy}-${String(mm).padStart(2, "0")}-${String(dd).padStart(2, "0")}` });
  });
  const points = [...seen.values()].sort((a, b) => a.s.localeCompare(b.s));
  if (!points.length) { if (prevByName[nom]) produits.push(prevByName[nom]); continue; }
  produits.push({ produit: nom, variete: label, stade: "Prix départ station", marche: "Moyenne France", unite: "€ HT/kg", source: "Commission européenne · Agri-food Data Portal", maj, points });
  ok++;
  const last = points[points.length - 1];
  console.log(`${nom.padEnd(18)} ${String(points.length).padStart(3)} semaines · dernier ${last.s} = ${last.p.toFixed(2)} €/kg`);
}

if (!produits.length) { console.log("Aucun cours récupéré : fichier inchangé."); process.exit(0); }
// Pas de nouvelle cotation : on garde le fichier tel quel (évite un commit inutile chaque jour)
const sansDate = (list) => JSON.stringify(list.map(({ maj: _m, ...rest }) => rest));
if (sansDate(produits) === sansDate(previous.produits || [])) { console.log("Aucune nouvelle cotation : fichier inchangé."); process.exit(0); }
fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify({ maj, source: "Commission européenne · Agri-food Data Portal", produits }));
console.log(`${ok} produits mis à jour sur ${Object.keys(MAP).length} → ${OUT}`);
