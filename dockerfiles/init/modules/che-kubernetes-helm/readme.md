# Deploy single user Che to Kubernetes using Helm

### Prerequisites
- Install the [Helm](https://github.com/kubernetes/helm/blob/master/docs/install.md) CLI
- Set your default Kubernetes context (this is required to use helm):
  - In Minikube this is set for you automatically
  - Otherwise, you may have to modify the KUBECONFIG environment variable and then type `kubectl config use-context <my-context>`
- Install tiller on your cluster:
  - Create a tiller serviceAccount and bind it to the almighty cluster-admin role: `kubectl apply -f ./tiller-rbac.yaml`
  - Install tiller itself: `helm init --service-acount tiller`
- Ensure that you have an NGINX-based ingress controller. Note: This is the default ingress controller on Minikube. You can start it with `minikube addons enable ingress`
- DNS discovery should be enabled. Note: It is enabled by default in minikube.
### Deployment Process
- Obtain the address of your Kubernetes cluser:
  - If your cluster is running on Minikube, simply type `minikube ip` at your terminal
  - If you're on the cloud, obtain the hostname or ip address from your cloud provider
- The context of the commands below is the directory in which this readme file resides
- Override default values by changing the values.yaml file and then typing:

  ```bash
  helm upgrade --install <my-che-installation> ./
  ```
- Or, you can override default values during installation, using the `--set` flag:

  ```bash
  helm upgrade --install <my-che-installation> --namespace <my-che-namespace> --set ingress.cheDomain=<my-hostname> --set cheImage=<my-image> ./
  ```
