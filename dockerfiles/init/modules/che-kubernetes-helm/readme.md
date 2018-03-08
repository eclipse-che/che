# Deploy single user Che to Kubernetes using Helm

## Prerequisites
- Install the [Helm](https://github.com/kubernetes/helm/blob/master/docs/install.md) CLI
- Set your default Kubernetes context (this is required to use helm):
  - In Minikube this is set for you automatically
  - Otherwise, you may have to modify the KUBECONFIG environment variable and then type `kubectl config use-context <my-context>`
- Install tiller on your cluster:
  - Create a [tiller serviceAccount](https://github.com/kubernetes/helm/blob/master/docs/rbac.md) and bind it to the almighty cluster-admin role: `kubectl apply -f ./tiller-rbac.yaml`
  - Install tiller itself: `helm init --service-account tiller`
- Ensure that you have an NGINX-based ingress controller. Note: This is the default ingress controller on Minikube. You can start it with `minikube addons enable ingress`
- DNS discovery should be enabled. Note: It is enabled by default in minikube.
## Deployment Process
### Obtain the Address of your Kubernetes Cluser
- If your cluster is running on Minikube, simply type `minikube ip` at your terminal
- If yourr cluster is in the cloud, obtain the hostname or ip address from your cloud provider

In production, you should specify a hostname (see [here](https://github.com/eclipse/che/issues/8694) why). In case you don't have a hostname (e.g. during development), and would still want to use a host-based configuration, you can use services such as nip.io or xip.io.

In case you're specifying a hostname, simply pass it as the value of the `ingressDomain` parameter below.

If you must use an ip address (e.g. your corporate policy prevents you from using nip.io), you would also have to set `isHostBased` to `false`.

### Deploying with Helm
The context of the commands below is the directory in which this readme file resides

- Override default values by changing the values.yaml file and then typing:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> ./
  ```
- Or, you can override default values during installation, using the `--set` flag:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> --set global.ingressDomain=<my-hostname> --set cheImage=<my-image> ./
  ```

#### Deployment Options

##### Single User 
Only Che will be deployed.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.ingressDomain=<domain> ./
  ```
  
##### Multi User 
Che, KeyCloak and Postgres will be deployed.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.multiuser=true --set global.ingressDomain=<domain> ./
  ```

##### Routing

- No Hostname 

All Ingress specs are created without a host attribute (defaults to *).   

  ```bash
  helm upgrade --install <my-che-installation> --namespace che --set global.ingressDomain=<minikube-ip>  --set global.serverStrategy=default-host ./
      Master: http://<minikube-ip>/
      Workspace servers: http://<minikube-ip>/<path-to-server>
      Keycloak: http://<minikube-ip>/auth/
  ```
- Single Host

All Ingress specs are created with the same host. Path based routing.
Can be used in conjunction with cert-manager for TLS, with a single certificate.
Useful for development and testing in cloud environments.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.ingressDomain=<minikube-ip>.xip.io --set global.serverStrategy=single-host ./
    Master: http://che.<minikube-ip>.xip.io/
    Workspaces servers: http://che.<minikube-ip>.xip.io/<path-to-server>
    Keycloak: http://che.<minikube-ip>.xip.io/auth/
  ```

- Multiple Hosts
helm upgrade --install che --namespace che --set global.ingressDomain=che.192.168.99.100.xip.io --set global.serverStrategy=multi-host --set cheImage=guydaich/che-server:tls ./
All Ingress specs are created with a unique host. 
Host based routing. 

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.ingressDomain=<minikube-ip>.xip.io --set global.serverStrategy=multi-host ./
    Master: http://master.<minikube-ip>.xip.io
    Workspaces: http://<server-hostname>.<minikube-ip>.xip.io
    Keycloak: http://keycloak.<minikube-ip>.xip.io/
  ```

##### TLS

- Cert-Manager

Currently, limited to Single Host routing.   

helm upgrade --install che --namespace che --set global.ingressDomain=<mydomain> --set global.serverStrategy=single-host --set global.cheNamespace=che --set global.tls.enabled=true --set global.tls.useStaging=false --set cheImage=guydaich/che-server:tls ./

  ```bash
  helm install --name <cert-manager-release-name> stable/cert-manager
  helm upgrade --install che --namespace che --set global.ingressDomain=<domain> --set global.tls.enabled=true --set global.serverStrategy=single-host ./
    Master: https://che.domain/
    Workspaces servers: https://che.domain/<path-to-server>
    Keycloak: https://che.domain/auth/
  ```    

## Deleting a Deployment
You can delete a deployment using the following command:
``` bash
helm delete <my-che-installation>
```
