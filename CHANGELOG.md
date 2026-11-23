# Journal des versions — Pilote F&L

Les améliorations sont regroupées par version publiée. La section « En préparation » liste celles qui sont prêtes et testées mais pas encore publiées.

## En préparation

## 3.14 — 23 novembre 2026

- **Barre du bas défilante.** Sur téléphone, la roue est remplacée par une barre qui contient tous les outils (Accueil, Analyse, Promo, Assortiment, Com, Cours, Marge, Saison) et qu'on fait glisser du doigt ; l'onglet ouvert est ramené au centre, les bords en fondu signalent qu'il y a d'autres outils.

## 3.13 — 23 novembre 2026

- **Accords au singulier partout.** Tableau de bord, Analyse express, Studio promo, assortiment et cours : « 1 produit », « dans 1 jour », « 0 étape à faire », « 1 étape faite » au lieu des pluriels systématiques.
- **Restauration depuis un mail.** Une sauvegarde collée avec le texte du mail autour (salutations, signature « Envoyé de mon téléphone ») est maintenant reconnue et restaurée ; le message d'erreur est plus clair quand le texte ne contient pas de sauvegarde.
- **Import plus robuste.** L'Analyse express lit les nombres négatifs au format comptable d'Excel, « (120) », et le signe moins typographique « −50 » : ces lignes étaient auparavant ignorées. Accord corrigé : « 1 produit analysé », « 1 ligne ignorée ».

## 3.12 — 23 novembre 2026

- **Achat maximum dans la fiche produit.** La fiche indique le prix d'achat maximum pour tenir la marge visée au prix de vente actuel et, quand un cours réel existe, si le marché est au-dessus, proche ou en dessous.
- **Démarrage plus léger.** La bibliothèque de lecture Excel (près de 900 Ko) n'est plus chargée à chaque ouverture de l'appli, mais seulement en ouvrant l'Analyse express (préchargement discret) ou au moment de lire un fichier.
- **Cours du marché dans la fiche RDV fournisseur.** Pour chaque produit, la fiche de préparation ajoute le cours réel récent, sa tendance sur 4 semaines et la position de négociation : au-dessus, proche ou sous votre prix d'achat maximum.

## 3.11 — 23 novembre 2026

- **Cours d'achat dans le mail hebdo.** « Remplir automatiquement » ajoute aux points d'attention les produits dont le cours réel a bougé d'au moins 10 % sur la semaine, avec une piste d'action (surveiller la marge en hausse, renforcer la mise en avant en baisse).
- **Produits reconnus partout, même mal orthographiés.** Toutes les saisies de produit (ajout en promo, prix d'achat, relevés de prix, fiche produit) reconnaissent les noms sans accents, au pluriel ou avec d'autres séparateurs : « clementines » ajoute bien la Clémentine, avec son cours du jour.
- **Recherche de produit tolérante.** La recherche en haut de l'écran trouve le produit même sans accents, au pluriel ou avec d'autres séparateurs : « clementine », « pommes », « chataigne », « CHOU FLEUR », « nectarines ».

## 3.10 — 23 novembre 2026

- **Tableau de bord sans débordement sur téléphone.** Le bloc « Cours d'achat de la semaine » dépassait de l'écran et faisait glisser la page de côté ; les noms longs sont maintenant raccourcis proprement.
- **Rappel de sauvegarde.** Dans l'appli Android et sur le site, vos données ne sont gardées que sur l'appareil. Sans sauvegarde depuis 14 jours, le tableau de bord ajoute « Sauvegarder mes données » dans « À faire », avec un lien direct vers la sauvegarde.
- **Promo au cours du jour.** Dans le Studio promo, un produit ajouté prend comme prix d'achat le cours réel (ou vos prix d'achat récents) quand il existe ; le prix promo proposé garde au moins 10 % de marge HT, arrondi à l'étiquette en ,x9.

## 3.9 — 23 novembre 2026

- **Alerte revente à perte.** Dans le Studio promo, un produit dont le prix promo HT passe sous le prix d'achat est signalé (« sous l'achat ») avec un rappel : la revente à perte est interdite en France, sauf exceptions encadrées. Pistes : remonter le prix promo ou négocier une participation du fournisseur.
- **Tableau par famille triable.** Dans l'Analyse express, le tableau des familles se trie comme le détail par produit (CA, écart N-1, évolution, poids, marge, nom), au clic ou au clavier : on repère d'un coup la famille qui recule le plus.
- **Prix de rayon dans le simulateur de marge.** À côté du prix conseillé, l'appli propose l'étiquette en ,x9 juste au-dessus (ex. 2,43 € → 2,49 €) et la marge réelle obtenue à ce prix.

## 3.8 — 23 novembre 2026

- **Tri du tableau produits au clavier.** Dans l'Analyse express, les en-têtes du « Détail par produit » sont de vrais boutons : on trie avec Entrée ou Espace, le sens du tri est annoncé aux lecteurs d'écran et le focus reste sur la colonne triée.
- **Roue utilisable au clavier et au lecteur d'écran.** Tab et Maj+Tab font tourner la roue d'un outil à l'autre sans en sortir ; à la fermeture (Échap, Retour, toucher à côté), le focus revient sur le bouton de la roue.
- **Pastille « Hors ligne ».** Sans réseau, une pastille l'indique en haut de l'écran : on sait que les cours affichés ne sont pas rafraîchis. Au retour du réseau, elle disparaît et les cours se mettent à jour d'eux-mêmes.

## 3.7 — 23 novembre 2026

- **Roue des outils.** Le bouton « Plus » devient une roue qu'on fait tourner du doigt (ou à la molette et aux flèches) pour choisir un outil ; elle s'arrête sur l'outil le plus proche, avec son nom et sa description.
- **Ressenti d'appli native.** Le bouton Retour d'Android referme d'abord la roue, la fiche produit ou le panneau ouvert ; petite vibration au changement d'onglet ; écrans qui glissent à l'ouverture ; boutons qui s'enfoncent au toucher ; ombre sous la barre du haut au défilement ; plus de rebond, de zoom sur les champs ni de sélection de texte accidentelle.
- **Colonnes mémorisées.** L'Analyse express retient la correspondance des colonnes corrigée à la main pour chaque format de fichier (noms de colonnes seulement, jamais les chiffres) : l'export de la semaine suivante est reconnu directement.

## 3.6 — 23 novembre 2026

- **Barre de menu repensée.** Sur téléphone : 4 onglets principaux (Accueil, Promo, Assortiment, Cours) et un bouton « Plus » qui ouvre un panneau avec les autres outils, en grand et avec une description. Pastilles d'alerte sur les onglets (étapes promo en retard, alertes de prix déclenchées). Sur ordinateur : la barre latérale se réduit aux icônes (choix mémorisé), avec le nom complet en infobulle.
- **Marge au cours du jour.** Le simulateur de marge et la fiche produit utilisent le cours réel (ou vos prix d'achat récents) comme prix d'achat quand il existe, avec la source et la semaine indiquées. Une valeur saisie à la main n'est plus écrasée en revenant sur le simulateur.

## 3.5 — 23 novembre 2026

- Bloc « Cours d'achat de la semaine » sur le tableau de bord (cours réels récents).
- Fiche produit : cours d'achat et chiffres de la dernière analyse à la place de l'ancienne section Démarque.
- Téléphone : les 8 onglets tiennent dans la barre du bas ; matrice d'assortiment lisible sur petit écran, étiquettes sans chevauchement.
- Paramètres : engrenage en haut à droite, un second clic referme.
- Robot des cours : second passage pour les produits en échec, raison de l'erreur dans le journal.
- Retrait de l'ancien module Démarque.

## 3.4 — 23 novembre 2026

- Site autonome sur GitHub Pages ; cours réels mis à jour chaque jour par GitHub Actions.
- Appli Android : lit les cours sur le site, propose les nouvelles versions au démarrage.

## 3.3 et avant

- Logo de l'application, cours réels de la Commission européenne, paramètres et guide, ateliers assortiment, com et promo, analyse express, design.
