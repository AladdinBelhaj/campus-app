# Campus Summer Portal

Application de démonstration utilisée dans les labs OKD / OpenShift.

## Rôle de l’application

Le **Campus Summer Portal** représente le **Portail Horizon Stages**.

Il permet à l’Université Horizon de :

- publier des offres de stage ;
- présenter les départements techniques ;
- recevoir des candidatures ;
- suivre un petit tableau de bord métier.

## Composants

- `backend/` : API Spring Boot, Actuator, endpoint Prometheus, accès PostgreSQL
- `frontend/` : site statique servi par NGINX avec appels AJAX vers le backend
- `prometheus/` : configuration de Prometheus local pour la boucle de validation
- `pgadmin/` : configuration préchargée pour lire la base locale

## Architecture à retenir

Le parcours logique est le suivant :

1. le navigateur appelle le frontend ;
2. le frontend appelle le backend sur `/api/*` ;
3. le backend lit et écrit dans PostgreSQL ;
4. le backend expose aussi `/actuator/health` et `/actuator/prometheus` ;
5. Prometheus lit les métriques applicatives sur cet endpoint.

Schéma source :

- [architecture-campus-app-sandbox.drawio](../assets/architecture-campus-app-sandbox.drawio)

Aperçu :

![Architecture applicative du Portail Horizon Stages](../assets/architecture-campus-app-sandbox.svg)

## Pourquoi Prometheus et Micrometer sont présents

Cette application n’est pas seulement un support de déploiement. Elle sert aussi à introduire l’observabilité.

Le backend utilise :

- **Spring Boot Actuator** pour exposer les endpoints d’observabilité ;
- **Micrometer** pour enregistrer des métriques ;
- le registre **Prometheus** pour rendre ces métriques lisibles par Prometheus.

On y trouve notamment :

- `campus_applications_submitted_total`
- `campus_applications_by_domain_total`
- `campus_published_offers`
- `campus_pending_applications`

L’objectif pédagogique est de pouvoir :

- jouer un scénario métier ;
- observer son effet immédiat dans les métriques ;
- comprendre ensuite comment OpenShift déploie la même application.

## Build local du backend

Cette étape est **optionnelle** si vous utilisez la variante “tout en Docker Compose”.

Elle reste utile si vous voulez valider le code Java localement avant l’image Docker :

```powershell
cd training/campus-app/backend
mvn test
```

## Test local avec Docker Compose

Le fichier [docker-compose.local.yml](/C:/Users/h4mdi/Desktop/okd-aws/training/campus-app/docker-compose.local.yml) démarre désormais la pile locale complète :

- PostgreSQL
- pgAdmin
- Prometheus
- backend Spring Boot
- frontend NGINX

Démarrage :

```powershell
docker compose -f training/campus-app/docker-compose.local.yml up -d --build
```

Le backend tourne alors dans Compose avec :

- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/campus`
- utilisateur `campus`
- mot de passe `campus`

Depuis votre poste, les accès restent :

- frontend : `http://localhost:8081`
- backend : `http://localhost:8080`
- health : `http://localhost:8080/actuator/health`
- métriques Prometheus : `http://localhost:8080/actuator/prometheus`
- Prometheus : `http://localhost:9090`
- pgAdmin : `http://localhost:5050`

Identifiants pgAdmin :

- email : `admin@campus.example.com`
- mot de passe : `admin`

Règle simple :

- pour ouvrir **pgAdmin** : `admin@campus.example.com` / `admin`
- pour ouvrir **PostgreSQL** dans pgAdmin : `campus` / `campus`

Si vous avez l’erreur `password authentication failed for user "campus"`, relancez proprement :

```powershell
docker compose -f training/campus-app/docker-compose.local.yml down -v
docker compose -f training/campus-app/docker-compose.local.yml up -d --build
```

Attention : `down -v` efface la base locale.

Le serveur PostgreSQL `Campus Postgres` est préconfiguré dans pgAdmin.

Pour vérifier rapidement l’état de la pile :

```powershell
docker ps
```

Si vous modifiez le code du backend et que vous voulez reconstruire l’image locale :

```powershell
docker compose -f training/campus-app/docker-compose.local.yml up -d --build
```

Arrêt :

```powershell
docker compose -f training/campus-app/docker-compose.local.yml down
```

## Variante de développement frontend seul

Le fichier [docker-compose.frontend.local.yml](/C:/Users/h4mdi/Desktop/okd-aws/training/campus-app/docker-compose.frontend.local.yml) reste utile si vous voulez lancer uniquement le frontend contre un backend démarré séparément.

## Petit scénario de test métriques

Après avoir démarré l’application, vous pouvez :

1. ouvrir le frontend sur `http://localhost:8081` ;
2. remplir le formulaire de candidature avec votre nom et prénom ;
3. envoyer une seule candidature ;
4. ouvrir Prometheus ;
5. vérifier que les compteurs métier ont évolué.

L’appel direct à l’API reste possible, mais ce n’est pas le scénario principal du lab.  
On privilégie ici un test **réel côté interface web**.

Exemples de requêtes Prometheus :

```text
campus_applications_submitted_total
```

```text
campus_applications_by_domain_total
```

## Build OpenShift recommandé

### Variante cluster privé AWS

Depuis la copie Git du dépôt sur le bastion :

```bash
export LEARNER_ID="<votre-identifiant>"
export TRAINING_NAMESPACE="campus-$LEARNER_ID"
export LAB_REPO_DIR="$HOME/okd-training-$LEARNER_ID"
cd "$LAB_REPO_DIR"
git pull --ff-only
export KUBECONFIG="$LAB_REPO_DIR/install/okdlab/auth/kubeconfig"

oc apply -k training/manifests/student-base
oc start-build campus-backend --from-dir=training/campus-app/backend --follow -n "$TRAINING_NAMESPACE"
oc start-build campus-frontend --from-dir=training/campus-app/frontend --follow -n "$TRAINING_NAMESPACE"
```

### Variante Developer Sandbox

Depuis votre poste local, une fois connecté avec `oc login` :

```powershell
oc apply -k .\training\labs\jour-1-sandbox\correction\sandbox-base
oc start-build campus-backend --from-dir=.\training\campus-app\backend --follow
oc start-build campus-frontend --from-dir=.\training\campus-app\frontend --follow
```

## Accès fonctionnels

- le frontend consomme `/api/*` via proxy NGINX ;
- le backend expose aussi `/actuator/health`, `/actuator/metrics` et `/actuator/prometheus`.

## Variation Jour 3

Pour les labs stockage / backup, remplacez la base simple par la variante `training/manifests/student-kubegres`.
