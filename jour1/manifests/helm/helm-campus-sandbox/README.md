# Correction Helm - Campus Sandbox

Cette correction répond à la partie Helm du lab `02-builder-et-deployer-dans-le-sandbox.md`.

Elle fournit une chart minimale, cohérente avec le sujet :

- `backend-deployment.yaml`
- `backend-service.yaml`
- `frontend-deployment.yaml`
- `frontend-service.yaml`
- `route.yaml`
- `values.yaml`

## Structure

```text
helm-campus-sandbox/
  Chart.yaml
  values.yaml
  templates/
    backend-deployment.yaml
    backend-service.yaml
    frontend-deployment.yaml
    frontend-service.yaml
    route.yaml
```

## Commandes utiles

Windows / PowerShell :

```powershell
helm lint .\training\labs\jour-1-sandbox\correction\helm-campus-sandbox
helm template campus-sandbox .\training\labs\jour-1-sandbox\correction\helm-campus-sandbox
```

Linux / bash :

```bash
helm lint ./training/labs/jour-1-sandbox/correction/helm-campus-sandbox
helm template campus-sandbox ./training/labs/jour-1-sandbox/correction/helm-campus-sandbox
```
