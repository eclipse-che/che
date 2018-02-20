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

import {gitMixinId, subversionMixinId} from './project-repository-data';
import {CheAPI} from '../../../../../../components/api/che-api.factory';
import {WorkspaceStatus} from '../../../../../../components/api/workspace/che-workspace.factory';
import {CheWorkspaceAgent} from '../../../../../../components/api/che-workspace-agent';

export class ProjectRepositoryController {

  static $inject = ['$route', 'cheAPI', 'lodash'];

  private cheAPI: CheAPI;
  private lodash: any;

  private remoteGitRepositories: any[] = [];
  private localGitRepository: any = null;
  private remoteSvnRepository: any = null;
  private isEmptyState: boolean = false;
  private wsagent: CheWorkspaceAgent;

  /**
   * Controller for the project local repository and remote repositories details
   * @author Oleksii Orel
   */
  constructor($route: ng.route.IRouteService,
              cheAPI: CheAPI,
              lodash: any) {
    this.cheAPI = cheAPI;
    this.lodash = lodash;

    const namespace = $route.current.params.namespace;
    const workspaceName = $route.current.params.workspaceName;
    const projectPath = '/' + $route.current.params.projectName;

    const workspace = this.cheAPI.getWorkspace().getWorkspaceByName(namespace, workspaceName);
    if (workspace && (WorkspaceStatus[workspace.status] === WorkspaceStatus.RUNNING || WorkspaceStatus[workspace.status] === 'RUNNING')) {
      this.cheAPI.getWorkspace().fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        return this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
      }).then(() => {
        this.wsagent = this.cheAPI.getWorkspace().getWorkspaceAgent(workspace.id);
        if (this.wsagent !== null) {
          if (!this.wsagent.getProject().getProjectDetailsByKey(projectPath)) {
            let promise = this.wsagent.getProject().fetchProjectDetails(workspace.id, projectPath);

            promise.then(() => {
              const projectDetails = this.wsagent.getProject().getProjectDetailsByKey(projectPath);
              this.updateRepositories(projectDetails);
            });
          } else {
            const projectDetails = this.wsagent.getProject().getProjectDetailsByKey(projectPath);
            this.updateRepositories(projectDetails);
          }
        }
      });
    }
  }

  updateRepositories(projectDetails: che.IProjectTemplate): void {
    if (!projectDetails.mixins || !projectDetails.mixins.length) {
      this.isEmptyState = true;
      return;
    }

    if (projectDetails.mixins.indexOf(subversionMixinId) !== -1) {
      // update remote svn url
      if (!this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path)) {
        const promise = this.wsagent.getSvn().fetchRemoteUrl(projectDetails.workspaceId, projectDetails.path);

        promise.then(() => {
          this.remoteSvnRepository = this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path);
        });
      } else {
        this.remoteSvnRepository = this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path);
      }
    }

    if (projectDetails.mixins.indexOf(gitMixinId) !== -1) {
      // update git local url
      if (!this.wsagent.getGit().getLocalUrlByKey(projectDetails.path)) {
        const promise = this.wsagent.getGit().fetchLocalUrl(projectDetails.path);

        promise.then(() => {
          this.localGitRepository = this.wsagent.getGit().getLocalUrlByKey(projectDetails.path);
        });
      } else {
        this.localGitRepository = this.wsagent.getGit().getLocalUrlByKey(projectDetails.path);
      }

      // update git remote urls
      if (!this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path)) {
        const promise = this.wsagent.getGit().fetchRemoteUrlArray(projectDetails.path);

        promise.then(() => {
          this.remoteGitRepositories = this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path);
        });
      } else {
        this.remoteGitRepositories = this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path);
      }
    }

  }

}
