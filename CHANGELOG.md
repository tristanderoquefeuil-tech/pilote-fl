# Journal des versions — Pilote F&L

Les améliorations sont regroupées par version publiée. La section « En préparation » liste celles qui sont prêtes et testées mais pas encore publiées.

## En préparation

## 3.26 — 25 novembre 2026

- **Changer d'onglet en glissant le doigt (téléphone).** Un glissement vers la gauche passe à l'onglet suivant, vers la droite au précédent, dans l'ordre de la barre du bas en passant par les sous-onglets : Accueil → Analyse → Studio promo → Atelier com → Assortiment → Saisonnalité → Cours d'achat → Simulateur de marge. La page suit un peu le doigt puis glisse vers la suivante ; un geste trop court revient en place. Le geste est ignoré quand on fait défiler la page, sur le graphique des cours, les listes et tableaux qui défilent de côté, les champs, la barre du bas, la fiche produit et le panneau d'opération, et sur les bords de l'écran (geste Retour d'Android).
- **Indicateur d'onglet animé.** Dans la barre du bas (téléphone) et la barre latérale (ordinateur), le fond de l'onglet actif glisse vers le nouvel onglet en s'étirant comme une goutte, puis reprend sa forme. Il s'étire déjà pendant le glissement du doigt. Fonctionne aussi avec les liens internes et le bouton Retour.
- Si l'appareil demande moins d'animations, la page change sans glisser et l'indicateur se déplace simplement, sans se déformer.

## 3.25 — 25 novembre 2026

- **Retour du fond d'origine.** En thème clair, le fond dégradé menthe → crème avec les grandes feuilles floues revient derrière l'interface modernisée de la 3.24. Rien d'autre ne change ; le mode sombre garde son fond uni.

## 3.24 — 25 novembre 2026

- **Interface modernisée.** Un seul système de design pour toute l'appli : une police (Inter) avec chiffres alignés, des couleurs, espacements, arrondis et ombres identiques partout. Fond uni et cartes nettes à la place du fond à feuilles floues et des cartes translucides. Boutons, champs, listes déroulantes, onglets, sous-onglets, pastilles, tableaux et états vides suivent les mêmes règles (survol, appui, focus clavier visible).
- **Barres de navigation plus soignées.** Barre latérale claire avec l'onglet actif bien marqué (ordinateur) ; barre du bas flottante en verre dépoli (téléphone) ; barre du haut plus sobre, sous-titre sur une ligne sur téléphone.
- **Cours d'achat dans le même thème que le reste.** La page n'est plus la seule en sombre : liste, graphique, cartes et panneaux reprennent les couleurs de l'appli. Les couleurs ont le même sens partout : **une baisse de cours est en vert** (bonne nouvelle pour la marge), **une hausse en orange**, y compris sur l'accueil.
- **Mode sombre automatique (site).** Si l'ordinateur ou le navigateur est réglé en sombre, l'appli passe en sombre, graphiques compris. L'appli Android reste en clair.
- Le calendrier des saisons tient toujours sur un écran de téléphone, sans défiler. Aucune fonction ni aucun parcours ne change.

## 3.23 — 25 novembre 2026

- **Vraies illustrations des produits.** Les 37 fruits et légumes ont chacun leur dessin, dans un même style (poireau, chou-fleur, endive, asperge, radis, betterave, navet, framboise, abricot, prune, figue, rhubarbe… n'affichent plus deux lettres). Lisibles en petit dans les listes et en grand dans la fiche produit ; ils apparaissent aussi dans les étiquettes de saison, les suggestions du Studio promo et sur les affiches de l'Atelier com.
- **Widget Android « Cours d'achat ».** À poser sur l'écran d'accueil du téléphone : semaine en cours, plus fortes baisses et hausses des cours (comme le résumé « En bref »), pastille du nombre d'alertes de prix déclenchées. Mis à jour quelques fois par jour ; un toucher ouvre l'appli sur la page Cours.
- **Notifications Android.** À activer dans les Paramètres (« Notifications sur le téléphone ») : une notification quand une de vos alertes de prix se déclenche et quand de nouvelles actus arrivent, même appli fermée, sans serveur (vérification environ toutes les 4 heures). Android 13 et plus demande l'autorisation au premier usage ; si elle est refusée, un bouton ouvre les réglages du téléphone. Un toucher sur la notification ouvre la bonne page.
- **Calendrier des saisons sur un seul écran.** Sur téléphone, le tableau des 37 produits tient entièrement à l'écran, sans glisser : deux colonnes de produits, mois en initiales, cases fines. Les blocs « Pleine saison », « Arrivent » et « Sortent » deviennent trois boutons compacts avec le nombre de produits, qui s'ouvrent d'un toucher. Un toucher sur un produit ouvre sa fiche (avec l'origine d'import). Affichage inchangé sur ordinateur.

## 3.22 — 25 novembre 2026

- **Résumé « En bref » en haut des cours d'achat.** En quelques lignes et sans jargon : combien de produits coûtent moins ou plus cher à l'achat que la semaine dernière, les plus fortes baisses et hausses, et le bon plan de la semaine (produits en pleine saison et en baisse). Un clic sur un produit ouvre sa courbe ; « C'est quoi, un cours d'achat ? » explique la notion aux débutants.
- **Page allégée.** Courbe simple par défaut (les chandeliers restent disponibles), tous les produits affichés d'emblée ; période, type de graphique, comparaison et fiche produit regroupés dans « Options du graphique » ; les alertes (« Me prévenir quand le prix bouge ») et la saisie de vos prix (« Ajouter mes prix d'achat ») sont repliées et s'ouvrent d'un toucher. Le panneau d'alertes s'ouvre seul s'il en existe.

## 3.21 — 25 novembre 2026

- **Rien d'affiché sans donnée.** La liste des cours et le menu « Comparer avec » ne montrent que les produits qui ont des prix réels (cours publics ou vos prix) ; la fiche produit n'affiche que les chiffres connus (la marge seulement si l'achat et la vente le sont) et le bouton « Voir le cours » seulement s'il existe ; la carte « Marge réelle » des cours et les cases vides de la veille prix disparaissent au lieu d'afficher « – ».

## 3.20 — 25 novembre 2026

- **Plus aucun chiffre d'exemple.** Les prix d'achat, prix de vente, freinte, démarque et courbes de cours fictifs sont retirés, ainsi que l'export de ventes et le bilan d'opération d'exemple. Sans donnée publique, l'appli affiche « – » et invite à saisir la vôtre.
- **Prix de vente réels.** Chaque jour, le robot GitHub récupère le prix moyen de vente au détail en France : série officielle de l'INSEE (11 produits, mensuelle) ou, à défaut, médiane des relevés en magasin d'Open Prices (Open Food Facts, hors bio, au kilo ; 15 produits de plus). Ils alimentent le simulateur de marge, la fiche produit, les cours, le Studio promo, les fiches RDV et la veille prix, avec la source affichée.
- **Votre freinte et votre démarque.** Deux nouveaux champs dans les Préférences ; ces taux sont utilisés partout à la place des valeurs inventées.

## 3.19 — 25 novembre 2026

- **Onglets regroupés par thème.** La barre des menus passe de 8 à 5 onglets : Accueil, Analyse, Promo (Studio promo · Atelier com), Assortiment (Assortiment · Saisonnalité) et Prix (Cours d'achat · Simulateur de marge). Les outils d'un même thème sont des sous-onglets en haut de la page ; chaque onglet ramène au dernier outil utilisé. Sur téléphone, la barre tient sur l'écran sans défiler. Aucun outil n'est retiré et les liens directs fonctionnent toujours.
- **Accueil allégé.** Les tuiles du haut n'apparaissent que lorsqu'elles ont un chiffre à montrer ; les temps forts à venir rejoignent « À faire » (ligne « Ensuite ») au lieu d'être affichés deux fois ; les blocs encore vides (dernière analyse, veille prix, météo) sont réunis dans un bloc « Pour commencer » et reprennent leur place dès qu'ils ont du contenu. Textes d'aide raccourcis.

## 3.18 — 24 novembre 2026

- **Page Actualités.** Un bouton journal en haut à gauche (hors de la barre des menus) ouvre les dernières actualités des fruits et légumes et de la grande distribution : source, date, extrait et photo, avec un filtre par rubrique (Fruits et légumes, GMS) et une recherche (« pommes de terre », « Lidl »…). Chaque article s'ouvre sur le site de son éditeur (dans le navigateur du téléphone pour l'appli Android).
- **Pastille « nouveau ».** Un point rouge sur le journal signale les articles arrivés depuis la dernière visite ; ils portent l'étiquette « Nouveau » dans la liste. La dernière liste reste lisible hors ligne.
- **Robot des actus.** GitHub récupère les actualités toutes les 3 heures de 6 h à 21 h (Végétable, FreshPlaza, Réussir Fruits & Légumes, Google Actualités), écarte les sujets hors rayon (céréales, élevage, homonymes sportifs) et les doublons, et garde 14 jours d'articles.

## 3.17 — 23 novembre 2026

- **Cours dans la Saisonnalité.** Les produits « En pleine saison » affichent la variation de leur cours réel de la semaine (baisse en vert, hausse en rouge) : on voit d'un coup d'œil quoi mettre en avant.
- **Styles allégés.** Les styles de l'ancienne roue et de l'ancien panneau « Plus » sont retirés (85 lignes) : feuille de style plus légère, affichage inchangé sur tous les écrans.
- **Nettoyage de l'ancienne roue.** Le code de la roue des outils, inutilisé depuis la barre défilante, est retiré (110 lignes, un écouteur de mouvement du doigt en moins) : appli plus légère, sans effet de bord possible.

## 3.16 — 23 novembre 2026

- **Seuil d'alerte proposé.** Dans les cours d'achat, le champ d'alerte propose le cours actuel −10 % (« passe sous ») ou +10 % (« passe au-dessus ») ; valider sans rien saisir crée l'alerte à ce seuil.
- **Suggestions promo guidées par les cours.** Dans le Studio promo, les produits de pleine saison suggérés sont classés selon leur cours réel de la semaine : les baisses de prix d'abord (les plus faciles à promouvoir), avec la variation affichée.
- **Plus de place pendant la saisie.** Sur téléphone, la barre du bas se retire quand on tape dans un champ (clavier ouvert) et revient dès qu'on a fini : elle ne cache plus le champ ni le bouton juste au-dessus du clavier.

## 3.15 — 23 novembre 2026

- **Flèches sur la barre du bas.** Sur téléphone, des flèches ‹ et › apparaissent aux bords de la barre tant qu'il reste des outils de ce côté ; un toucher fait défiler la barre. La flèche de droite bouge doucement au démarrage pour attirer l'œil.
- **Indice de défilement.** Au premier lancement sur téléphone, la barre du bas glisse brièvement puis revient, pour montrer qu'elle contient d'autres outils (une seule fois, désactivé si les animations sont réduites).

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
