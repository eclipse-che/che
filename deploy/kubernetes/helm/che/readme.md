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

In case you're specifying a hostname, simply pass it as the value of the `cheDomain` parameter below.

If you must use an ip address (e.g. your corporate policy prevents you from using nip.io), you would also have to set `isHostBased` to `false`.

### Deploying with Helm
The context of the commands below is the directory in which this readme file resides

- Override default values by changing the values.yaml file and then typing:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> ./
  ```
- Or, you can override default values during installation, using the `--set` flag:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> --set global.cheDomain=<my-hostname> --set cheImage=<my-image> ./
  ```

#### Deployment types
Currenty, only minikube deployment is supported.

##### Single User
Only Che will be deployed.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.cheDomain=<domain> ./
  ```

##### Multi User
Che, KeyCloak and Postgres will be deployed.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.multiuser=true --set global.cheDomain=<domain> ./
  ```

##### No Host:
 Ingress will serve requests on minikube-ip.
 Path based routing to Che, Secondary servers (KeyCloak) and Workspace servers.

  ```bash
  helm upgrade --install <che-release> --namespace <che-namespace> --set global.isHostbased=false --set global.cheDomain=<minikube-ip> ./
   Master: http://<minikube-ip>/
   Workspaces: http://<minikube-ip>/<path-to-server>
   Keycloak (if multiuser) : http://<minikube-ip>/auth/
  ```

##### Host (partial):
 WS Master Ingress will serve requests on provided domain
 Workspaces: Ingress will serve requests on minikube-ip, Path Based routing to workspaces.
 KeyCloak : dedicated hostname

   ```bash
   helm upgrade --install <che-release> --namespace <che-namespace> --set global.cheDomain=<minikube-ip>.xip.io ./
   Master: http://master.<minikube-ip>.xip.io
   Workspaces: http://<minikube-ip>/<path-to-server>
   Keycloak (if multiuser): http://keycloak.<minikube-ip>.xip.io/
   ```


##### Future options:
- Path Based: single hostname for all components (che, keycloak, WS servers)
- Host Based: unique host for each component
- TLS

## Deleting a Deployment
You can delete a deployment using the following command:
``` bash
helm delete <my-che-installation>
```
