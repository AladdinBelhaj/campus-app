# Lab — Simulation maintenance nœud
# Cordon / Drain / Remise en service sur OpenShift

---

## Contexte

L'équipe infrastructure doit effectuer une **mise à jour matérielle** sur un nœud worker du cluster ROSA.  
Avant d'intervenir physiquement, il faut :

- Empêcher tout nouveau pod de s'y déployer
- Évacuer les pods existants proprement
- Vérifier que les workloads sont reschedulés
- Remettre le nœud en service après intervention

---

## Objectifs

À travers ce lab, vous allez manipuler :

- `oc adm cordon` — marquer un nœud non-schedulable
- `oc adm drain` — évacuer les pods d'un nœud
- `oc adm uncordon` — remettre le nœud en service
- La lecture de l'état des nœuds et pods
- Le comportement des `Deployment` pendant le drain
- Les `PodDisruptionBudget` (PDB) et leur impact

---

## Environnement

| Élément | Valeur |
|---|---|
| Cluster | ROSA Classic |
| Namespace | `p1-lab` / `p2-lab` |
| Nœuds disponibles | 3 workers minimum |
| Droits requis | `cluster-admin` ou `node-admin` |

---

## Étape 0 — Observer l'état initial du cluster

### Mission

Avant toute intervention, prenez une photo de l'état du cluster
et identifiez le nœud cible de maintenance.

### Commandes

```bash
# Lister tous les nœuds avec leur statut
oc get nodes

# Voir le détail des nœuds
oc get nodes -o wide

# Identifier et stocker le nœud cible dans une variable
export NODE=$(oc get nodes \
  -l node-role.kubernetes.io/worker \
  -o jsonpath='{.items[0].metadata.name}')
echo "Nœud cible : $NODE"

# Voir les pods sur le nœud cible
oc get pods -A -o wide --field-selector "spec.nodeName=$NODE"

# Voir la charge de chaque nœud
oc adm top nodes
```

> **Note :** La variable `$NODE` sera utilisée dans toutes les étapes suivantes.
> Si vous fermez votre terminal, relancez la commande `export NODE=...` avant de continuer.

### Résultat attendu

```
Nœud cible : ip-10-0-1-10.ec2.internal

NAME                          STATUS   ROLES    AGE   VERSION
ip-10-0-1-10.ec2.internal     Ready    worker   5d    v1.28.0
ip-10-0-1-11.ec2.internal     Ready    worker   5d    v1.28.0
ip-10-0-1-12.ec2.internal     Ready    worker   5d    v1.28.0
```

### Validation attendue

- Tous les nœuds sont en `Ready`
- Variable `$NODE` définie et affichée

---

## Étape 1 — Déployer une application de test

### Mission

Déployez une application avec plusieurs réplicas pour observer le rescheduling pendant la maintenance.

### Contraintes attendues

Créer un `Deployment` :

- nom : `webapp-test`
- image : `registry.access.redhat.com/ubi9/ubi-minimal:latest`
- réplicas : `3`
- anti-affinité pour répartir les pods sur les nœuds

```yaml
# webapp-test.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: webapp-test
spec:
  replicas: 3
  selector:
    matchLabels:
      app: webapp-test
  template:
    metadata:
      labels:
        app: webapp-test
    spec:
      containers:
        - name: app
          image: registry.access.redhat.com/ubi9/ubi-minimal:latest
          command: ["/bin/sh", "-c", "sleep 3600"]
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app: webapp-test
                topologyKey: kubernetes.io/hostname
```

```bash
oc apply -f webapp-test.yaml
oc get pods -o wide
```

### Validation attendue

- 3 pods en `Running`
- Pods répartis sur plusieurs nœuds

---

## Étape 2 — Cordon : marquer le nœud non-schedulable

### Mission

Simulez le début de la fenêtre de maintenance.  
Le nœud ne doit plus recevoir de nouveaux pods, mais les pods existants continuent de tourner.

### Commandes

```bash
# Marquer le nœud comme non-schedulable
oc adm cordon $NODE

# Vérifier le statut
oc get nodes
```

### Résultat attendu

```
NAME                          STATUS                     ROLES
ip-10-0-1-10.ec2.internal     Ready,SchedulingDisabled   worker
ip-10-0-1-11.ec2.internal     Ready                      worker
ip-10-0-1-12.ec2.internal     Ready                      worker
```

### Vérification du comportement

```bash
# Les pods existants sont toujours là
oc get pods -o wide

# Scaler le deployment — les nouveaux pods n'iront PAS sur le nœud cordoned
oc scale deployment webapp-test --replicas=6
oc get pods -o wide
```

### Validation attendue

- Le nœud affiche `SchedulingDisabled`
- Les pods existants continuent de tourner
- Les nouveaux pods se déploient uniquement sur les autres nœuds

---

## Étape 3 — Drain : évacuer les pods du nœud

### Mission

Videz le nœud de tous ses pods avant l'intervention physique.

### Simulation avant action

```bash
# Toujours simuler d'abord avec --dry-run
oc adm drain $NODE \
  --ignore-daemonsets \
  --delete-emptydir-data \
  --dry-run
```

### Commande réelle

```bash
oc adm drain $NODE \
  --ignore-daemonsets \
  --delete-emptydir-data
```

### Options importantes

| Option | Rôle |
|---|---|
| `--ignore-daemonsets` | Ne pas bloquer sur les pods DaemonSet |
| `--delete-emptydir-data` | Accepter la suppression des volumes emptyDir |
| `--force` | Forcer même sans ReplicaSet (pods orphelins) |
| `--timeout=120s` | Timeout d'attente pour l'évacuation |
| `--dry-run` | Simuler sans agir |
| `--disable-eviction` | Contourner les PDB (à utiliser avec précaution) |

### Observation en temps réel

```bash
# Dans un second terminal — observer le rescheduling
watch oc get pods -o wide
```

### Validation attendue

- Les pods de `webapp-test` sont recréés sur les autres nœuds
- Le nœud drainé n'a plus de pods (hors DaemonSets)

---

## Étape 4 — Cas particulier : PodDisruptionBudget

### Mission

Observez comment un PDB peut bloquer ou ralentir un drain.

### Créer un PDB

```yaml
# webapp-pdb.yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: webapp-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: webapp-test
```

```bash
oc apply -f webapp-pdb.yaml

# Réduire à 2 réplicas pour déclencher le blocage
oc scale deployment webapp-test --replicas=2

# Tenter le drain — doit être bloqué
oc adm drain $NODE --ignore-daemonsets --delete-emptydir-data
```

### Résultat attendu

Le drain est **bloqué** — il ne peut pas descendre en dessous de 2 pods disponibles.

```
error when evicting pods/"webapp-test-xxx": 
Cannot evict pod as it would violate the pod's disruption budget.
```

### Solutions

```bash
# Option 1 : augmenter les réplicas avant le drain (recommandé)
oc scale deployment webapp-test --replicas=4
oc adm drain $NODE --ignore-daemonsets --delete-emptydir-data

# Option 2 : forcer en contournant les PDB (attention en production !)
oc adm drain $NODE \
  --ignore-daemonsets \
  --delete-emptydir-data \
  --disable-eviction
```

### Validation attendue

- Drain bloqué avec message d'erreur PDB clair
- Drain réussi après augmentation des réplicas à 4

---

## Étape 5 — Intervention simulée

### Mission

Simulez la fenêtre de maintenance — le nœud est isolé, les workloads sont ailleurs.

### Vérifications avant intervention

```bash
# Confirmer que le nœud est vide (hors daemonsets)
oc get pods -A -o wide --field-selector "spec.nodeName=$NODE"

# Confirmer que tous les workloads sont sains ailleurs
oc get pods -o wide
oc get deployment webapp-test
```

### État attendu

```
# Sur le nœud drainé   : uniquement des DaemonSets
# Sur les autres nœuds : tous les pods en Running, aucun Pending
```

```bash
# Simulation de l'intervention
echo "Intervention en cours..."
sleep 30
echo "Intervention terminée"
```

---

## Étape 6 — Uncordon : remise en service

### Mission

Après l'intervention, remettez le nœud en service et observez la reprise d'activité.

### Commande

```bash
# Remettre le nœud en service
oc adm uncordon $NODE

# Vérifier le statut
oc get nodes
```

### Résultat attendu

```
NAME                          STATUS   ROLES    AGE
ip-10-0-1-10.ec2.internal     Ready    worker   5d
ip-10-0-1-11.ec2.internal     Ready    worker   5d
ip-10-0-1-12.ec2.internal     Ready    worker   5d
```

### Observation du rescheduling naturel

```bash
# Le nœud redevient éligible mais les pods existants ne migrent pas
# automatiquement — ils restent où ils sont
oc get pods -o wide

# Pour forcer un rééquilibrage (optionnel)
oc rollout restart deployment/webapp-test
oc get pods -o wide
```

### Validation attendue

- Le nœud est de nouveau en `Ready` sans `SchedulingDisabled`
- Les nouveaux pods peuvent de nouveau s'y déployer
- L'application est toujours opérationnelle

---

## Étape 7 — Inventaire final et nettoyage

### Vérifications finales

```bash
# État des nœuds
oc get nodes

# État de l'application
oc get deployment webapp-test
oc get pods -o wide

# Supprimer les ressources de test
oc delete deployment webapp-test
oc delete pdb webapp-pdb

# Vérifier que le namespace est propre
oc get all
```

### Résultat attendu

- Cluster propre
- Tous les nœuds en `Ready`
- Aucune ressource de test restante

---

## Points clés à retenir

| Concept | Commande | Effet |
|---|---|---|
| Cordon | `oc adm cordon` | Stoppe les nouveaux pods, conserve les existants |
| Drain | `oc adm drain` | Évacue tous les pods du nœud |
| Uncordon | `oc adm uncordon` | Remet le nœud en service |
| PDB | `minAvailable` / `maxUnavailable` | Protège la disponibilité pendant le drain |
| DaemonSet | `--ignore-daemonsets` | Toujours ignorer lors du drain |
| Rescheduling | Automatique | Les pods ne migrent pas après uncordon |

---

## Pièges fréquents en production

- **Oublier `--ignore-daemonsets`** → drain bloqué sur les pods système
- **PDB trop strict** → drain impossible sans intervention manuelle
- **Pods sans ReplicaSet** → nécessite `--force`, données potentiellement perdues
- **Oublier l'uncordon** → nœud définitivement exclu du scheduling
- **PVC en RWO** → un pod avec volume RWO peut bloquer le drain si le volume ne se détache pas
- **Variable `$NODE` non exportée** → toutes les commandes échouent silencieusement

---

<details>
<summary>💡 Hint — Étape 2 : que fait exactement cordon ?</summary>

`cordon` ajoute une **taint** sur le nœud :

```
node.kubernetes.io/unschedulable:NoSchedule
```

Les pods existants ne sont **pas** affectés — seuls les nouveaux pods sont bloqués.

Pour vérifier la taint ajoutée :

```bash
oc get node $NODE -o jsonpath='{.spec.taints}' | python3 -m json.tool
```

</details>

---

<details>
<summary>💡 Hint — Étape 3 : drain bloqué ?</summary>

Si le drain est bloqué, vérifiez :

```bash
# Voir les pods qui bloquent
oc get pods -A -o wide --field-selector "spec.nodeName=$NODE"

# Vérifier les PDB actifs
oc get pdb -A

# Voir les events du drain
oc get events --sort-by='.lastTimestamp'
```

</details>

---

<details>
<summary>💡 Hint — Étape 4 : comprendre minAvailable vs maxUnavailable</summary>

```yaml
# minAvailable : nombre minimum de pods qui doivent rester UP
spec:
  minAvailable: 2       # Au moins 2 pods toujours disponibles

# maxUnavailable : nombre maximum de pods qui peuvent être DOWN
spec:
  maxUnavailable: 1     # Au plus 1 pod peut être indisponible
```

Pour 3 réplicas et un drain d'1 pod :

- `minAvailable: 2` → autorise le drain (3 - 1 = 2 ≥ 2) ✅
- `minAvailable: 3` → bloque le drain (3 - 1 = 2 < 3) ❌

</details>

---

<details>
<summary>💡 Hint — Étape 6 : pourquoi les pods ne reviennent pas sur le nœud ?</summary>

Kubernetes ne rééquilibre **pas automatiquement** les pods après un uncordon.  
Les pods existants restent sur leurs nœuds actuels.  
Seuls les **nouveaux pods** (scaling, rollout, crash) pourront être schedulés sur le nœud remis en service.

Pour forcer le rééquilibrage :

```bash
oc rollout restart deployment/webapp-test
```

</details>
