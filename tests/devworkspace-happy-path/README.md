# Che DevWorkspace Happy Path

This folder holds the scripts and resources which allows you to run Che DevWorkspace Happy path with help of Pod on the cluster against already deployed Che (with DevWorkspace engine) on the same cluster.

## Running locally

If you have Che with DevWorkspace enabled on the current cluster, you can just run

```bash
./launch.sh
```

## Running remotely

If you need to run this as PR check, you need to predeploy Che with DevWorkspaces enabled
and then invoke remote-launch, possibly via curl:
```bash
curl -s https://raw.githubusercontent.com/eclipse/che/main/tests/devworkspace-happy-path/remote-launch.sh | bash -s
```

## Cluster environments

The OAuth user provisioning supports two kinds of OpenShift clusters:

- **standalone clusters** — the `dev-htpasswd` identity provider is added by
  patching the `oauth/cluster` CR
- **HyperShift hosted clusters** — the `oauth/cluster` CR is read-only there
  (modifications are denied by the ValidatingAdmissionPolicy), so the identity
  provider is configured by patching the `HostedCluster` object on the
  management cluster instead

The hosted cluster environment is detected automatically by the presence of the
`mgmt_kubeconfig` and `cluster-name` files in `SHARED_DIR` (populated by the
`hypershift-hostedcluster-workflow` ci-operator workflow), so consumers of this
test (che-operator, chectl, devworkspace-operator etc.) do not need any
environment-specific configuration.
