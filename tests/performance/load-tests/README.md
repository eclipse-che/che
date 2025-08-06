# DevWorkspace Load Testing

This script tests the performance of DevWorkspace startup times by creating multiple workspaces simultaneously and measuring their startup performance. It evaluates the system under test by checking the average startup time across all workspaces and identifying any failures that occur during the testing process.

## Overview

The `load-test.sh` script:
- Creates multiple DevWorkspace objects in parallel
- Waits for all workspaces to reach the "Ready" state
- Measures the time between "Started" and "Ready" conditions
- Calculates average startup time and failure statistics
- Provides detailed logs for analysis

## Prerequisites

To run these tests, you will need:
- The `kubectl` client installed and configured
- A Kubernetes/OpenShift cluster with DevWorkspace Operator running
- Access to create DevWorkspace objects in the cluster
- A test DevWorkspace YAML template (provided in `samples/` directory)

## Usage

### Basic Usage

```bash
# Run with default settings (3 workspaces, 120s timeout)
./load-test.sh

# Run with custom number of workspaces
./load-test.sh -c 10

# Run with custom timeout
./load-test.sh -t 300

# Run with both custom count and timeout
./load-test.sh -c 5 -t 180
```

### Advanced Usage

```bash
# Use a custom DevWorkspace template from URL
./load-test.sh -c 5 -l https://raw.githubusercontent.com/eclipse/che/main/tests/performance/load-tests/samples/simple-pvc.yaml

# Start workspaces in separate namespaces (one per namespace)
./load-test.sh -c 5 -s

# Combine multiple options
./load-test.sh -c 10 -t 240 -s -l https://example.com/custom-workspace.yaml
```

## Parameters

| Parameter | Description | Default | Example |
|-----------|-------------|---------|---------|
| `-c <COUNT>` | Number of workspaces to create | 3 | `-c 10` |
| `-t <SECONDS>` | Timeout for waiting workspaces to start | 120 | `-t 300` |
| `-l <URL>` | Link to DevWorkspace YAML template | local `samples/simple-ephemeral.yaml` | `-l https://example.com/workspace.yaml` |
| `-s` | Start workspaces in separate namespaces | false | `-s` |
| `--help` | Display help information | - | `--help` |

## Available Sample Templates

The `samples/` directory contains several DevWorkspace templates:

- `simple-ephemeral.yaml` - Basic ephemeral workspace (default)
- `simple-pvc.yaml` - Workspace with persistent storage
- `simple-with-editor-ephemeral.yaml` - Ephemeral workspace with editor
- `simple-with-editor-pvc.yaml` - PVC workspace with editor

## How It Works

1. **Preparation**: The script validates parameters and downloads/prepares the DevWorkspace template
2. **Creation**: Creates the specified number of DevWorkspace objects in parallel
3. **Waiting**: Waits for all workspaces to reach the "Ready" state with the specified timeout
4. **Measurement**: For each successful workspace, measures the time between "Started" and "Ready" conditions
5. **Cleanup**: Deletes all test workspaces and namespaces
6. **Reporting**: Calculates and displays average startup time and failure statistics

## Results and Logs

### Console Output
The script provides real-time feedback including:
- Workspace creation progress
- Waiting status for each workspace
- Final results with average startup time
- Number of successful vs failed workspaces

### Log Files
If workspaces fail to start, detailed logs are saved in the `logs/` directory:
- `sum.log` - Startup times for successful workspaces
- `events.log` - Kubernetes events during the test
- `{workspace-name}-describe.log` - Detailed workspace information for failed workspaces
- `{workspace-name}-{workspace-id}-events.log` - Events specific to failed workspaces

### Example Output
```
==================== Test results ====================
Average workspace starting time for 8 workspaces from 10 started: 45 seconds
2 workspaces failed. See failed workspace pod logs in the current folder for details.
Elapsed time: 180 seconds
```

## Error Handling

The script includes comprehensive error handling:
- Automatic cleanup on script interruption (Ctrl+C)
- Graceful handling of workspace creation failures
- Detailed logging for troubleshooting
- Timeout handling for stuck workspaces

## Tips for Load Testing

1. **Start Small**: Begin with a small number of workspaces (3-5) to establish baseline performance
2. **Monitor Resources**: Ensure your cluster has sufficient resources for the number of workspaces
3. **Use Appropriate Timeouts**: Set realistic timeouts based on your cluster's performance
4. **Separate Namespaces**: Use the `-s` flag for more isolated testing
5. **Custom Templates**: Use different DevWorkspace templates to test various workspace configurations

## Troubleshooting

### Common Issues

1. **Workspaces not starting**: Check cluster resources and DevWorkspace Operator status
2. **Timeout errors**: Increase the `-t` parameter or check cluster performance
3. **Permission errors**: Ensure you have rights to create DevWorkspace objects
4. **Template errors**: Verify your DevWorkspace YAML template is valid

### Debug Information

The script provides several debugging options:
- Check the `logs/` directory for detailed failure information
- Use `kubectl get events` to see cluster events
- Examine workspace descriptions with `kubectl describe dw <workspace-name>`
