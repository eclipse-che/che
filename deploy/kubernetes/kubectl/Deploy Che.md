# Deploy single user Che to k8s
Tested on minikube with vm providers Virtualbox and kvm2. Note that Che with workspaces requires quite a lot
of RAM. Initial tests were done with 10GB, but it is definitely more than it is needed to start Che
and couple of workspaces.

IP of VM is supposed to be `192.168.99.100`. `nip.io` is also used for handling hosts resolution.
If you have another IP or DNS replace these values in k8s.yml file.

Services are exposed using ingress controller approach.
We added ingress annotations to customize ingress controller behavior -
not to break websocket connections.
In particular testing environment was setup with NginX ingress controller 0.9.0.
So we added annotations specific to this implementation and version:
- nginx.ingress.kubernetes.io/rewrite-target: /
- nginx.ingress.kubernetes.io/ssl-redirect": "false"
- nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"
- nginx.ingress.kubernetes.io/proxy-connect-timeout: "3600"

If you use another ingress controller implementation or version you need to customize
Che master ingress and value of environment variable `CHE_INFRA_KUBERNETES_INGRESS_ANNOTATIONS__JSON` stored in ConfigMap.
Value of the map should be expressed as a stringified JSON.

And environment variable would be: `'{"nginx.ingress.kubernetes.io/rewrite-target": "/","nginx.ingress.kubernetes.io/ssl-redirect": "false","nginx.ingress.kubernetes.io/proxy-connect-timeout": "3600","nginx.ingress.kubernetes.io/proxy-read-timeout": "3600"}'`

###Prerequisites:
- Ingress controller is running. Note: you can start it on minikube with `minikube addons enable ingress`.
- Currently Che workspaces work with NginX ingress controller only. Note: it is default ingress controller on minikube.
- DNS discovery should be enabled. Note: enabled by default in minikube.
### Deployment process:
Note: despite the fact that it is not necessary to use a separate namespace for Che
we use it to simplify development operations such as cleaning of spoiled environment
and clean redeploy of Che.
- Create namespace `che`: `kubectl create namespace che`
- Deploy Che: `kubectl --namespace=che apply -f che-kubernetes.yaml`
- Check Che pod status until it become `Running`: `kubectl get --namespace=che pods`
