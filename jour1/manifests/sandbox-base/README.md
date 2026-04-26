# Manifests Developer Sandbox

Ce dossier contient une variante **minimaliste** des manifests pour le **Developer Sandbox OpenShift**.

L’objectif n’est pas de reproduire tout le parcours AWS privé, mais de disposer d’un socle simple pour :

- créer les objets applicatifs dans **votre projet Sandbox** ;
- lancer les builds du frontend et du backend ;
- déployer l’application trois tiers ;
- valider le fonctionnement via une `Route` publique.

## Ce que cette variante contient

- `ImageStream` et `BuildConfig` pour `campus-backend` et `campus-frontend` ;
- un PostgreSQL simple avec `Secret`, `Service`, `StatefulSet` et `PVC` ;
- les `Deployment` et `Service` du backend et du frontend ;
- une `Route` publique vers le frontend.

## Ce que cette variante ne cherche pas à faire

- créer un namespace ;
- poser des quotas ou des `RoleBinding` pédagogiques ;
- activer la supervision cluster-wide ;
- imposer des objets qui dépendent de privilèges supplémentaires.

Autrement dit : ce dossier est pensé pour un utilisateur **standard** dans un namespace déjà fourni par le Sandbox.

## Utilisation recommandée

Depuis votre copie locale du dépôt, une fois connecté au Sandbox avec `oc login` :

```bash
oc project
oc kustomize training/labs/jour-1-sandbox/correction/sandbox-base
oc apply -k training/labs/jour-1-sandbox/correction/sandbox-base
oc get bc,is,deploy,svc,route
```

Puis lancez les builds binaires :

```bash
oc start-build campus-backend --from-dir=training/campus-app/backend --follow
oc start-build campus-frontend --from-dir=training/campus-app/frontend --follow
```

## Pourquoi il n’y a pas de Prometheus ici

Le backend expose déjà `/actuator/prometheus`, mais l’objectif du Jour 1 Sandbox est d’abord :

- comprendre l’application ;
- la tester localement ;
- la builder ;
- la déployer.

L’intégration plus poussée avec Prometheus, `ServiceMonitor` et la lecture des métriques vient ensuite dans le parcours d’observabilité, via :

- [Jour 4 - Variante Developer Sandbox](/C:/Users/h4mdi/Desktop/sandbox/training/labs/jour-4-sandbox/README.md)
- [sandbox-monitoring](/C:/Users/h4mdi/Desktop/sandbox/training/manifests/sandbox-monitoring/README.md)
