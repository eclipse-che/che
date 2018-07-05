# Deploy single user Che to Kubernetes using Helm

## Prerequisites
- Start minikube with sufficient resources and RBAC enabled
  - for minikube 0.26.0 and greater `minikube start --cpus 2 --memory 4096 --extra-config=apiserver.authorization-mode=RBAC`
  - for minikube 0.25.2 and lower `minikube start --cpus 2 --memory 4096 --extra-config=apiserver.Authorization.Mode=RBAC`
- Add cluster-admin role for `kube-system:default` account
  - `kubectl create clusterrolebinding add-on-cluster-admin --clusterrole=cluster-admin --serviceaccount=kube-system:default`
- Install the [Helm](https://github.com/kubernetes/helm/blob/master/docs/install.md) CLI
- Set your default Kubernetes context (this is required to use helm):
  - In Minikube this is set for you automatically
  - Otherwise, you may have to modify the KUBECONFIG environment variable and then type `kubectl config use-context <my-context>`
- Install tiller on your cluster:
  - Create a [tiller serviceAccount](https://github.com/kubernetes/helm/blob/master/docs/rbac.md): `kubectl create serviceaccount tiller --namespace kube-system`
   - Bind it to the almighty cluster-admin role: `kubectl apply -f ./tiller-rbac.yaml`
  - Install tiller itself: `helm init --service-account tiller`
- Ensure that you have an NGINX-based ingress controller. Note: This is the default ingress controller on Minikube. You can start it with `minikube addons enable ingress`
- DNS discovery should be enabled. Note: It is enabled by default in minikube.

## Deployment Process
### Obtain the Address of your Kubernetes Cluster
- If your cluster is running on Minikube, simply type `minikube ip` at your terminal
- If your cluster is in the cloud, obtain the hostname or ip address from your cloud provider

### Routing Strategies
In production, you should specify an ingress domain and use the `multi-host` routing strategy, where agents and servers are served from a unique subdomain of the ingress domain (see [here](https://github.com/eclipse/che/issues/8694) why). To set an ingress domain, pass the value of the `global.ingressDomain` parameter below. 

In case you don't have an ingress domain (e.g. during local development), and would still want to use a host-based configuration, you can use services such as nip.io or xip.io, in conjunction with `multi-host` routing. If you must use an ip address (e.g. your corporate policy prevents you from using nip.io), you should use the `default-host` routing strategy, that provides limited path-based routing to workspace agents and servers. 

In some cases, you may be limited to a single host name (for example, when using the default cert-manager integration, that issues only a single, non-wildcard, certificate). You can utilize the `single-host` routing strategy, which relies on path based routing. 


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
  helm upgrade --install <che-release-name> --namespace <che-namespace> --set global.ingressDomain=<domain> ./
  ```
* Master: `https://che-<che-namespace>.domain`
* Workspaces servers: `https://server-host.domain`
  
##### Multi User 
Che, KeyCloak and Postgres will be deployed.

  ```bash
  helm upgrade --install <che-release-name> --namespace <che-namespace> -f ./values/multi-user.yaml --set global.ingressDomain=<domain> ./
  ```

* Master: `https://che-<che-namespace>.domain`
* Keycloak:  `https://keycloak-<che-namespace>.domain`
* Workspaces servers: `https://server-host.domain`

##### Default Host
All Ingress specs are created without a host attribute (defaults to *).
Path based routing to all components.
Multi User configuration is enabled. 
 
  ```bash
  helm upgrade --install <che-release-name> --namespace <che-namespace> -f ./values/default-host.yaml --set global.ingressDomain=<domain> ./
  ```
 
* Master: `http://<domain>/`
* Keycloak:  `http://<domain>/auth/`
* Workspaces servers: `http://<domain>/<path-to-server>`

##### TLS-enabled
Cert-Manager is used to issue LetsEncrypt certificates.
To avoid rate-limit issues, we use a single hostname for all ingresses.
Path based routing to all components.
Multi User configuration is enabled. 

  ```bash
  helm install --name <cert-manager-release-name> stable/cert-manager
  helm upgrade --install <che-release-name> --namespace <che-namespace> -f ./values/tls.yaml --set global.ingressDomain=<domain> ./
  ```

* Master: `https://che-<che-namespace>.domain/`
* Keycloak:  `https://che-<che-namespace>.domain/auth/`
* Workspaces servers: `https://che-<che-namespace>.domain/<path-to-server>`

## Deleting a Deployment
You can delete a deployment using the following command:

``` bash
helm delete <che-release-name>
```
