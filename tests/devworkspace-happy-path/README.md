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
