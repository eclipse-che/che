# Load tests

Load tests should test the behaviour of server when user will run simultaneously several of workspaces. 

## Prerequisites
What do you need to run those tests
- `kubectl` client installed
- Openshift cluster with running Openshift DevSpaces
- test user logged into DevSpaces Dashboard(this quaranies that user namespaces are created)

## Running load tests
1. Log in to Openshift cluster with DevSpaces deployed from terminal
2. Start `load-test.sh` script from `test/e2e/performance/load-tests`. Set number of started workspaces by -c parameter(like ./load-test.sh -c 5).
3. This script gets `cpp` sample devfile.yaml from DevSpaces devfile registry and starts workspaces.
4. As results there are average time of workspace starting and number of failed workspaces.


## Results and logs
If workspace failed to start, logs are saved in current directory.
