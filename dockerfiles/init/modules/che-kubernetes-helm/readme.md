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

### Perform the Actual Deployment
The context of the commands below is the directory in which this readme file resides

- Override default values by changing the values.yaml file and then typing:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> ./
  ```
- Or, you can override default values during installation, using the `--set` flag:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> --set cheDomain=<my-hostname> --set cheImage=<my-image> ./
  ```

- Local Host-Based (wildcard DNS) installation:
  ```bash
  helm upgrade --install <my-che-installation> --namespace che --set cheDomain=che.<minikube-ip>.xip.io ./
  ```
  master url: ``http://master.che.<minikube-ip>.xip.io``
- Local Path-Based installation:
  ```bash
  helm upgrade --install <my-che-installation> --namespace che --set cheDomain=<minikube-ip> --set isHostBased=false ./ 
  ```
  master url: ``http://<minikube-ip>``
  
## Deleting a Deployment
You can delete a deployment using the following command:
``` bash
helm delete <my-che-installation>
```
