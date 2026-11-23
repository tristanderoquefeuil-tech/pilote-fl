# Pilote F&L

Boîte à outils du category manager fruits et légumes : analyse express des ventes, studio promo, atelier assortiment, atelier com, cours d'achat réels, simulateur de marge et saisonnalité.

- **Site** : https://tristanderoquefeuil-tech.github.io/pilote-fl/
- **Appli Android** : https://github.com/tristanderoquefeuil-tech/pilote-fl/releases/latest/download/PiloteFL.apk

Les saisies (opérations, rendez-vous, relevés de prix…) restent sur l'appareil de l'utilisateur ; les fichiers de ventes analysés ne sont jamais enregistrés ni envoyés. Les données d'exemple sont fictives.

## Organisation

| Dossier | Contenu |
|---|---|
| `src/` | Sources de l'application (`app.html`, styles, logo) et script d'assemblage `build-web.js` |
| `docs/` | Site publié sur GitHub Pages (généré), cours (`data/cours.json`) et `version.json` |
| `android/` | Projet de l'appli Android (WebView) et script de compilation `build.sh` |
| `tools/cours.mjs` | Récupère les cours réels de la Commission européenne |
| `.github/workflows/cours.yml` | Met à jour les cours chaque jour, sans intervention |

## Cours réels

Source : Commission européenne, [Agri-food Data Portal](https://agridata.ec.europa.eu/extensions/API_Documentation/fruitandvegetables.html) (prix moyen départ station, France, hebdomadaire). Le robot GitHub (`cours.yml`) les met à jour chaque jour ; on peut le lancer à la main depuis l'onglet **Actions**.

## Publier une nouvelle version

1. Modifier les sources dans `src/`, puis assembler : `node src/build-web.js`.
2. Augmenter `versionCode` et `versionName` dans `android/AndroidManifest.xml`, puis compiler : `bash android/build.sh`.
3. Reporter le même numéro dans `docs/version.json`.
4. Publier : `git commit` + `git push`, puis créer une release avec l'APK :
   `gh release create v3.5 android/PiloteFL.apk --title "Pilote F&L 3.5" --notes "…"`

Au démarrage, l'appli Android compare sa version à `docs/version.json` et propose d'installer la nouvelle.
