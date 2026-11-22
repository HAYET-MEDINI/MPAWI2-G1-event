# Déploiement de l’application sur AWS EKS

Ce dépôt contient le pipeline Jenkins et les fichiers de l’application. Le déploiement Kubernetes est vérifié sur un cluster Amazon EKS.

## Vérifier les ressources Kubernetes

### Services

```bash
kubectl get services
```

```text
NAME                  TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
kubernetes            ClusterIP   10.100.0.1     <none>        443/TCP          78m
spring-boot-service   NodePort    10.100.21.31   <none>        8083:30000/TCP   30m
```

Le service `spring-boot-service` reçoit le trafic sur le port NodePort `30000` et le transmet à l’application sur le port `8083`.

### Pods

```bash
kubectl get pods
```

```text
NAME                                  READY   STATUS    RESTARTS   AGE
my-spring-boot-app-5554566ccf-qsfrc   1/1     Running   0          31m
my-spring-boot-app-5554566ccf-vnqnh   1/1     Running   0          31m
```

### Nœuds

```bash
kubectl get nodes -o wide
```

```text
NAME                            STATUS   ROLES    AGE   VERSION                INTERNAL-IP     EXTERNAL-IP
ip-172-31-28-85.ec2.internal    Ready    <none>   75m   v1.33.13-eks-9adbfea   172.31.28.85   [masquée]
ip-172-31-70-240.ec2.internal   Ready    <none>   75m   v1.33.13-eks-9adbfea   172.31.70.240  [masquée]
```

Les adresses IP publiques sont masquées dans ce document. Pour tester l’application, utiliser l’adresse publique actuelle d’un nœud avec le port `30000`.

## Capture du résultat

![Application Spring Boot déployée sur EKS](docs/images/eks-deployment.png)
