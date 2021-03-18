---
name: Bug report 🐞
about: Report a bug found in Eclipse Che
title: ''
labels: 'kind/bug'
assignees: ''

---
<!--
  To make it easier for us to help you, please include as much useful information as possible.

  Useful Links:
  - Documentation: https://www.eclipse.org/che/docs
  - Contributing: https://github.com/eclipse/che/blob/master/CONTRIBUTING.md

  Eclipse Che has public chat on:

  - Mattermost: https://mattermost.eclipse.org/eclipse/channels/eclipse-che

  Before opening a new issue, please search existing issues https://github.com/eclipse/che/issues
-->

### Describe the bug
<!-- A clear and concise description of what the bug is. -->

### Che version
<!-- (if workspace is running, version can be obtained with help/about menu) -->
 - [ ] latest
 - [ ] nightly
 - [ ] other: please specify

### Steps to reproduce

<!--
1. Do '...'
2. Click on '....'
3. See error
-->

### Expected behavior
<!-- A clear and concise description of what you expected to happen. -->

### Runtime
   - [ ] kubernetes (include output of `kubectl version`)
   - [ ] Openshift (include output of `oc version`)
   - [ ] minikube (include output of `minikube version` and `kubectl version`)
   - [ ] minishift (include output of `minishift version` and `oc version`)
   - [ ] docker-desktop + K8S (include output of `docker version` and `kubectl version`)
   - [ ] other: (please specify)

### Screenshots
<!-- If applicable, add screenshots to help explain your problem. -->

### Installation method
   - [ ] chectl
     * provide a full command that was used to deploy Eclipse Che (including the output)
     * provide an output of `chectl version` command
   - [ ] OperatorHub
   - [ ] I don't know

### Environment
   - [ ] my computer
       - [ ] Windows
       - [ ] Linux
       - [ ] macOS
   - [ ] Cloud
       - [ ] Amazon
       - [ ] Azure
       - [ ] GCE
       - [ ] other (please specify)
   - [ ] Dev Sandbox (workspaces.openshift.com)
   - [ ] other: please specify

### Eclipse Che Logs
<!-- https://www.eclipse.org/che/docs/che-7/collecting-logs-using-chectl -->

### Additional context
<!-- Add any other context about the problem here. -->
