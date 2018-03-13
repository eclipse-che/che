/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheWorkspaceClientService, IBackend} from '../workspace/che-workspace-client.service';

/**
 * todo
 */
export class CheWorkspaceClientBackend {

  static $inject = ['cheWorkspaceClient'];

  private cheWorkspaceClient: CheWorkspaceClientService;
  private workspaceClientBackend: IBackend;

  private projectsPerWorkspace: Map<string, che.IProject[]> = new Map();
  private workspaceAgentMap: Map<string, string> = new Map();
  private workspaces: Map<string, che.IWorkspace> = new Map();

  constructor(cheWorkspaceClient: CheWorkspaceClientService) {
    this.cheWorkspaceClient = cheWorkspaceClient;
    this.workspaceClientBackend = cheWorkspaceClient.backend;
  }

  public getBackend(): IBackend {
    return this.workspaceClientBackend;
  }

  public setup(): void {
    const workspacesToReturn = [];
    const workspaceIds = this.workspaces.keys();
    for (const id of workspaceIds) {
      let tmpWorkspace = this.workspaces.get(id);
      workspacesToReturn.push(tmpWorkspace);
      this.addWorkspaceAgent(id, tmpWorkspace.runtime);

      // get by ID
      this.workspaceClientBackend.stubRequest('GET', '/workspace/' + id, {
        status: 200,
        response: tmpWorkspace
      });
      // get by namespace/workspaceName
      this.workspaceClientBackend.stubRequest('GET', `/workspace/${tmpWorkspace.namespace}/${tmpWorkspace.config.name}`, {
        status: 200,
        response: tmpWorkspace
      });

      this.workspaceClientBackend.stubRequest('DELETE', '/workspace/' + id, {
        response: 200
      });
    }

    // this.workspaceClientBackend.stubRequest('GET', '/workspace/settings', {});

    this.workspaceClientBackend.stubRequest('GET', '/workspace', {
      status: 200,
      response: workspacesToReturn
    });

    this.workspaceClientBackend.stubRequest('GET', '/workspace/settings', {});
  }

  /**
   * Add the given project types
   * @param workspaceId the workspaceId of the runt
   * @param runtime runtime to add
   */
  addWorkspaceAgent(workspaceId: string, runtime: che.IWorkspaceRuntime): void {
    if (runtime && runtime.links) {
      runtime.links.forEach((link: any) => {
        if (link.rel === 'wsagent') {
          this.workspaceAgentMap.set(workspaceId, link.href);
        }
      });
    }
  }

  /**
   * Add the given workspaces on this backend
   * @param workspaces an array of workspaces
   */
  addWorkspaces(workspaces: che.IWorkspace[]): void {
    workspaces.forEach((workspace: che.IWorkspace) => {
      // if there is a workspace ID, add empty projects
      if (workspace.id) {
        this.projectsPerWorkspace.set(workspace.id, []);
      }
      this.workspaces.set(workspace.id, workspace);
    });
  }

}
