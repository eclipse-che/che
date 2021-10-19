# Production ready Theia DevWorkspace

## Summary

This issue is about finalizing Che Theia workflow to be production ready for DevWorkspace Engine.

## Motivation

We know that in long-term perspective we would like to move away from Theia in VSCode direction to use resources we spent on supporting Theia on making Che flow better.
But we need in a short time finalize DevWorkspace support to be based on devfile v2 (which is already used by ODO and OpenShift Console).

## Design

Here are question which should be addressed somehow:

- How Theia based workspace is created with factory flow?
  - Dashboard after resolving Devfile, determinate the custom Editor, is noone is detected, Dashboard with help of Theia Library resolves editor/plugins DevWorkspaceTemplates with help of .vscode/extensions.json and .che/che-theia-plugins.yaml.
  Details in [dashboard-creation-diagram.png](./images/dashboard-workspace-creation.png). Source https://docs.google.com/drawings/d/1BJeQMHu6uDP4gqCVU2kA_voCqgb9KaCmJjAevtc9R78/edit?usp=sharing
- How Theia plugins are defined in DevWorkspace format?
  - Pure VSX: that's an attribute on Che Theia DevWorkspace Template
  - Sidecar current:
    - Plugin's devfile v2 is merged with users devfile and stored together in DevWorkspace CR. Depending on the sidecar policy, user's first container may be modified (provision env vars, endpoints, override entrypoint) or a separate container is provisioned.      
  - Sidecar alternative: that's devfile v2 served by Che Plugin Registry which should live in a separate DevWorkspace Template
- How projects are cloned?
  - Projects cloning are handled by init container injected by DevWorkspaceOperator;
- How VSX are downloaded?
  - They are downloaded with init container from Theia DWT;
- How Che Theia is updated after Che is upgraded?
  - When DevWorkspaceTemplate is resolved, it gets the corresponding annotation/labels for Dashboard and Editor being able to match it with the corresponding URL to plugin registry. Then:
    - Dashboard is able to check and update DevWorkspaceTemplates if newer version is present in plugin registry on start;
    - Editor is able to notify user that newer version is available, ask user to apply changes and restart DevWorkspace;
      - \* How Editor restarts itself? Special annotation on DevWorkspace?
