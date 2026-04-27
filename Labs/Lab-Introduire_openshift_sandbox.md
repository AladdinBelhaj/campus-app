#  Atelier : OpenShift Sandbox (Free Tier / OKD)

##  Objectifs

-   Accéder à un cluster OpenShift en ligne\
-   Utiliser la console web en mode Developer\
-   Manipuler des ressources avec `oc`\
-   Comprendre les limites d'un environnement sandbox

------------------------------------------------------------------------

##  Prérequis

-   Compte Red Hat Developer\
-   Accès au Developer Sandbox OpenShift\
-   CLI OpenShift (`oc`) installé

------------------------------------------------------------------------

##  Étape 1 --- Accéder au cluster

1.  Se connecter au Developer Sandbox\
2.  Lancer l'environnement

 **Résultat attendu :** - Accès à la console web OpenShift

------------------------------------------------------------------------

##  Étape 2 --- Explorer la Web Console (Developer)

-   Basculer sur la vue **Developer**
-   Explorer :
    -   Topology\
    -   +Add\
    -   Project

Créer un projet si nécessaire :

``` bash
oc new-project mon-projet
```

------------------------------------------------------------------------

##  Étape 3 --- Connexion via CLI

``` bash
oc login --token=XXXX --server=https://api....
```

------------------------------------------------------------------------

##  Étape 4 --- Vérifications

``` bash
oc whoami
oc projects
oc get pods
```

------------------------------------------------------------------------

##  Étape 5 --- Namespaces

``` bash
oc get namespaces
```

------------------------------------------------------------------------

##  Étape 6 --- Déployer une application

``` bash
oc new-app nginx
oc expose svc/nginx
oc get route
```

------------------------------------------------------------------------

##  Étape 7 --- Explorer les ressources API

``` bash
oc api-resources | head -30
```

------------------------------------------------------------------------

##  Étape 8 --- Informations cluster

``` bash
oc version
```

------------------------------------------------------------------------

##  Limites du Sandbox

-   Pas d'accès admin\
-   Pas d'accès aux nodes\
-   Quotas CPU/RAM\
-   Projets temporaires

------------------------------------------------------------------------

##  Conclusion

Sandbox = environnement dev limité, idéal pour pratiquer sans gérer l'infra.
