# Lab02 - Déployer Campus avec GitOps et Argo CD sur OpenShift

## Objectif

Dans ce lab, vous allez découvrir une approche **GitOps** avec Argo CD sur OpenShift.

Vous allez :

* installer Argo CD depuis la console OpenShift ;
* exposer l’interface web ;
* donner les accès à votre projet ;
* connecter Argo CD à un dépôt Git ;
* déployer automatiquement l’application Campus ;
* observer la synchronisation Git → cluster ;
* corriger une dérive manuelle.

---

# Contexte

Jusqu’ici, Campus a été déployée manuellement.

L’équipe souhaite maintenant :

* versionner les manifests ;
* fiabiliser les déploiements ;
* tracer les changements ;
* éviter les dérives ;
* rejouer facilement sur plusieurs environnements.

---

# Principe GitOps

```text
Git = source de vérité
Argo CD = contrôleur de synchronisation
Cluster = état réel
```

---

# Architecture cible

```text
Git Repository
      ↓
 Argo CD
      ↓
OpenShift Project campus-p1 / campus-p2
```

---

# Prérequis

* accès console OpenShift avec droits suffisants pour installer un Operator et créer un RoleBinding;
* projet personnel :

```text
campus-p1 ou campus-p2
```

* dépôt Git contenant des manifests Kubernetes/OpenShift.

---

# Étape 1 - Installer Argo CD depuis la console

## Mission

Depuis la console OpenShift :

```text
Operators -> OperatorHub
```

Rechercher :

Red Hat OpenShift GitOps

Installer l’operator dans un namespace dédié :

```text
openshift-gitops
```

Mode recommandé :

```text
All namespaces
```

---

# Étape 2 - Vérifier l’installation

Aller dans :

```text
Installed Operators -> OpenShift GitOps
```

Vérifier la présence des pods :

* argocd-server
* argocd-repo-server
* argocd-application-controller
* redis

---

# Étape 3 - Récupérer l’URL Argo CD

Dans :

```text
Networking -> Routes
Namespace: openshift-gitops
```

Ouvrir la route :

```text
openshift-gitops-server
```

---

# Étape 4 - Donner accès à votre projet

## Mission

Autoriser Argo CD à gérer votre namespace :

```text
campus-p1 ou campus-p2
```

Créer un RoleBinding :

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: argocd-admin
  namespace: campus-p1
subjects:
- kind: ServiceAccount
  name: openshift-gitops-argocd-application-controller
  namespace: openshift-gitops
roleRef:
  kind: ClusterRole
  name: admin
  apiGroup: rbac.authorization.k8s.io
```

(Adapter `campus-p2` si besoin)

---

# Étape 5 - Préparer un repo Git simple

Créer un dépôt Git contenant :

```text
campus-gitops/
 ├── namespace/
 ├── backend/
 ├── frontend/
 └── kustomization.yaml
```

Exemple minimal frontend :

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: campus-frontend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: campus-frontend
  template:
    metadata:
      labels:
        app: campus-frontend
    spec:
      containers:
      - name: web
        image: nginx
```

---

# Étape 6 - Créer l’application Argo CD

Depuis la console OpenShift :

```text
Applications -> Create Application
```

Remplir :

```text
Name: campus
Project: default
Repository URL: https://gitlab.com/.../campus-gitops.git
Path: .
Cluster: in-cluster
Namespace: campus-p1
```

Sync policy :

```text
Automatic
Prune enabled
Self Heal enabled
```

---

# Étape 7 - Synchroniser

Cliquer :

```text
Sync
```

Argo CD applique automatiquement les manifests.

Vérifier dans OpenShift :

* Deployments créés
* Pods Running
* Services présents

---

# Étape 8 - Tester une dérive manuelle

## Mission

Depuis la console OpenShift :

Modifier :

```text
campus-frontend replicas: 1 -> 3
```

Observer ensuite Argo CD.

Résultat attendu :

```text
OutOfSync
```

Puis Argo CD remet :

```text
replicas = 1
```

(si self-heal activé)

---

# Étape 9 - Modifier Git

Changer dans Git :

```yaml
replicas: 2
```

Commit + push.

Observer :

Argo CD détecte le changement puis applique :

```text
replicas = 2
```

---

# Validation finale

Le lab est réussi si :

* Argo CD est installé ;
* l’UI est accessible ;
* l’application Campus est créée ;
* les ressources sont déployées depuis Git ;
* une dérive manuelle est corrigée ;
* une modification Git est synchronisée.

---

# Ce qu’il faut retenir

```text
OpenShift sait déployer.
Argo CD sait maintenir l’état attendu.
```

```text
Sans GitOps :
on applique des YAML

Avec GitOps :
Git applique l’infrastructure
```

---

# Bonus ⭐

Créer 3 dossiers Git :

```text
env/dev
env/test
env/prod
```

Puis déployer 3 environnements avec Argo CD.
