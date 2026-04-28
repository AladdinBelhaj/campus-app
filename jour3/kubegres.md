# Guide - Installer Kubegres sur le cluster avec `oc`

Ce guide couvre le nécessaire pour :

- installer l'opérateur Kubegres dans le cluster ;
- verifier que le CRD `Kubegres` est bien disponible ;
- deployer un premier cluster PostgreSQL gere par Kubegres avec les manifests du depot ;
- tenir compte du comportement OpenShift si les pods PostgreSQL sont bloques par la SCC.

## Ce que Kubegres installe

Kubegres installe :

- un namespace `kubegres-system`
- le controller `kubegres-controller-manager`
- le CRD `kubegres.reactive-tech.io`

Une fois installe, vous pouvez créer des ressources `kind: Kubegres` dans vos namespaces applicatifs.

## Prérequis

Avant de commencer :

- vous avez un acces `oc` fonctionnel au cluster
- vous etes connecte avec un compte ayant les droits d'installation cluster-wide
- vous avez au moins une `StorageClass`

Verification rapide :

```powershell
oc whoami
oc get nodes
oc get sc
```

## Etape 1 - Installer l'opérateur Kubegres

Kubegres documente officiellement l'installation suivante. Ici on la fait avec `oc` :

```powershell
oc apply -f https://raw.githubusercontent.com/reactive-tech/kubegres/v1.19/kubegres.yaml
```

## Etape 2 - Verifier l'installation

Vérifiez que le namespace et les composants existent :

```powershell
oc get namespace kubegres-system
oc get all -n kubegres-system
```

Vérifiez que le controller est bien `Running` :

```powershell
oc get deployment kubegres-controller-manager -n kubegres-system
oc get pods -n kubegres-system
```

Verifiez que le CRD est disponible :

```powershell
oc get crd | Select-String kubegres
oc api-resources | Select-String kubegres
```

Vous pouvez aussi verifier les logs du controller :

```powershell
oc logs deployment/kubegres-controller-manager -c manager -n kubegres-system --tail=100
```

## Etape 3 - Verifier le stockage disponible

Kubegres a besoin d'une `StorageClass`.

```powershell
oc get sc
```

Sur votre cluster ROSA Classic actuel :

- si vous voulez rester sur le stockage AWS de base, vous pouvez utiliser `gp3-csi`
- si vous voulez adosser Kubegres a ODF/Ceph, utilisez plutot :
  - `ocs-storagecluster-ceph-rbd` pour la base PostgreSQL
  - `ocs-storagecluster-cephfs` pour un PVC de backup partage
