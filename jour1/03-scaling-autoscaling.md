# Lab 3 - Scaling et autoscaling

## Objectif pédagogique

Dans ce lab, vous allez vous concentrer sur le comportement dynamique de votre application :

* comprendre ce qui se passe quand plusieurs pods tournent ;
* manipuler le scaling manuel ;
* découvrir comment OpenShift ajuste automatiquement le nombre de pods.

👉 L’objectif est d’observer **le comportement réel du système**, pas seulement sa configuration.

---

## Prérequis

Application déjà déployée.

```powershell
oc get pods
oc get deploy
```

---

## Étape 1 - Observer le comportement avec plusieurs pods

Jusqu’ici, votre frontend tourne avec un seul pod.
On va maintenant simuler une montée en charge en augmentant le nombre d’instances.

### À faire

```powershell
oc scale deployment/campus-frontend --replicas=2
oc get pods -l app=campus-frontend -w
```

Attendez que les deux pods soient en `Running`, puis :

```powershell
oc get pods -l app=campus-frontend
```

Rechargez plusieurs fois l’application dans le navigateur.

---

### Ce que vous devez comprendre

* plusieurs pods exécutent la même application ;
* ils sont interchangeables ;
* l’utilisateur ne voit aucune différence ;
* le trafic est automatiquement réparti.

👉 Cela améliore la **capacité** et la **résilience**.

---

### Revenir à l’état initial

```powershell
oc scale deployment/campus-frontend --replicas=1
```

---

## Étape 2 - Comprendre le HPA

Le `HPA` (Horizontal Pod Autoscaler) permet d’ajuster automatiquement le nombre de pods.

Principe :

* définir un minimum de pods ;
* définir un maximum ;
* utiliser une métrique (souvent CPU) pour décider.

👉 Contrairement au scaling manuel, ici c’est le cluster qui décide.

---

## Étape 3 - Créer un HPA

```powershell
oc autoscale deployment/campus-backend --min=1 --max=3 --cpu=1%
```

---

### ⚠️ Important

> Le seuil CPU est volontairement configuré très bas (`1%`) dans ce lab.
>
> En production, on utiliserait plutôt une valeur autour de 60–70%.
>
> Ici, le but est d’observabilité :
> un seuil très faible permet de déclencher plus facilement le scaling et de voir concrètement la création de pods.

---

Puis :

```powershell
oc get hpa
oc describe hpa campus-backend
```

---

### Ce que vous devez observer

* le `Deployment` ciblé ;
* les bornes min/max ;
* la métrique CPU utilisée.

---

## Étape 4 - Observer le comportement

Surveillez en temps réel :

```powershell
oc get hpa -w
oc get pods -l app=campus-backend -w
```

---

### Ce qu’il faut comprendre

* le HPA surveille la charge ;
* il décide du nombre de pods ;
* le Deployment applique automatiquement.

👉 Séparation claire :

* décision → HPA
* exécution → Deployment

---

## Étape 5 - Nettoyage

```powershell
oc delete hpa campus-backend
oc scale deployment/campus-backend --replicas=1
```

---

## Ce qu’il faut retenir

* plusieurs pods permettent de gérer la charge et les pannes ;
* le scaling manuel est simple mais limité ;
* le HPA automatise le scaling ;
* le comportement reste transparent côté utilisateur.
