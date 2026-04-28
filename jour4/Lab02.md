# Lab 2 - Afficher les métriques Campus dans OpenShift Prometheus

## Objectif pédagogique

Dans ce lab, vous allez utiliser **la vue métriques intégrée d’OpenShift** pour afficher :

- des métriques cluster ;
- des métriques applicatives ;
- des métriques métier de Campus.

Le but est volontairement simple :

- rester sur le chemin standard du Sandbox ;
- utiliser `Observe > Metrics` ;
- lire ce que Prometheus collecte déjà pour votre projet.

## Étape 1 - Ouvrir la vue métriques d’OpenShift

Dans la console OpenShift :

1. passez dans la perspective **Developer** ;
2. ouvrez `Observe` ;
3. cliquez sur `Metrics` ;
4. gardez votre projet Sandbox comme contexte de travail.

Le bon réflexe dans cette variante Sandbox est de commencer ici, parce que c’est l’outil intégré le plus simple pour un projet utilisateur.

### Si `Accès limité` s’affiche

Vérifiez d’abord :

- que vous êtes bien dans la perspective **Developer** ;
- que le bon projet est sélectionné ;
- que le `ServiceMonitor` existe bien.

Si le message persiste, cela signifie généralement que ce tenant Sandbox limite l’accès à cette vue.  
Dans ce cas :

- gardez tout de même `Observe > Metrics` comme chemin de référence du support ;
- mais validez les métriques côté backend avec le port-forward du Lab 2.

## Étape 2 - Lire d’abord une métrique cluster

Commencez par une métrique qui parle de l’environnement d’exécution.

Exemple pour le CPU du backend :

```text
sum(rate(container_cpu_usage_seconds_total{namespace="<votre-projet>",pod=~"campus-backend-.*"}[5m]))
```

Exemple pour la mémoire du backend :

```text
sum(container_memory_working_set_bytes{namespace="<votre-projet>",pod=~"campus-backend-.*"})
```

Exemple pour les redémarrages :

```text
kube_pod_container_status_restarts_total{namespace="<votre-projet>",pod=~"campus-backend-.*"}
```

Le message à retenir :

- ces métriques parlent du **pod et du cluster** ;
- elles ne parlent pas encore du métier Campus.

## Étape 3 - Lire ensuite une métrique applicative

Passez maintenant à des métriques exposées par Spring Boot et Micrometer.

Essayez par exemple :

```text
process_cpu_usage
```

```text
jvm_threads_live_threads
```

```text
http_server_requests_seconds_count
```

Ces métriques parlent de l’application elle-même :

- processus Java ;
- JVM ;
- trafic HTTP.

## Étape 4 - Lire les métriques métier Campus

Passez maintenant aux métriques métier du backend.

Testez :

```text
campus_applications_submitted_total
```

```text
campus_published_offers
```

```text
campus_pending_applications
```

```text
campus_applications_by_domain_total
```

Ce sont les métriques les plus intéressantes pour le fil rouge, car elles répondent directement à des questions métier :

- combien de candidatures ont été envoyées ?
- combien d’offres sont publiées ?
- combien de candidatures restent en attente ?
- sur quels domaines les candidatures se répartissent-elles ?

## Étape 5 - Relier le scénario métier aux courbes

Pour voir les métriques évoluer :

1. revenez dans le frontend ;
2. envoyez une nouvelle candidature ;
3. revenez dans `Observe > Metrics` ;
4. relancez les requêtes.

Ce que vous devez voir :

- `campus_applications_submitted_total` augmente ;
- `campus_pending_applications` peut évoluer ;
- `campus_applications_by_domain_total` évolue selon l’offre choisie.

Si une requête reste vide au premier essai :

- attendez 30 à 60 secondes ;
- relancez la requête ;
- vérifiez que le `ServiceMonitor` existe bien.

## Étape 6 - Interpréter correctement ce que vous observez

Vous devez maintenant distinguer trois familles :

- **métriques cluster** : CPU, mémoire, état des pods, redémarrages ;
- **métriques applicatives** : JVM, process, trafic HTTP ;
- **métriques métier** : candidatures, offres, répartition par domaine.

Cette distinction est essentielle, car une bonne exploitation ne repose pas sur un seul type de signal.

## Ce qu’il faut retenir

Le résultat attendu de ce Jour 4 Sandbox est simple :

- vous savez afficher une courbe Prometheus dans OpenShift ;
- vous savez faire la différence entre infrastructure, application et métier ;
- vous savez montrer les métriques propres à Campus dans la console OpenShift.

## Vérification

À la fin de ce lab, vous devez pouvoir :

1. afficher une métrique cluster du backend ;
2. afficher une métrique Spring Boot ou JVM ;
3. afficher une métrique métier `campus_*` ;
4. expliquer pourquoi ces trois familles ne racontent pas la même chose.
