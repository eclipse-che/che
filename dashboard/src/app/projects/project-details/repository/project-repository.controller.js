/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {gitMixinId, subversionMixinId} from '../repository/project-repository-data';

export class ProjectRepositoryController {

  /**
   * Controller for the project local repository and remote repositories details
   * @ngInject for Dependency injection
   * @author Oleksii Orel
   */
  constructor($route, cheAPI, lodash) {
    this.cheAPI = cheAPI;
    this.lodash = lodash;

    this.remoteGitRepositories = [];
    this.localGitRepository = null;
    this.remoteSvnRepository = null;
    this.isEmptyState = false;

    var namespace = $route.current.params.namespace;
    var workspaceName = $route.current.params.workspaceName;
    var projectPath = '/' + $route.current.params.projectName;

    let workspace = this.cheAPI.getWorkspace().getWorkspaceByName(namespace, workspaceName);
    if (workspace && (workspace.status === 'STARTING' || workspace.status === 'RUNNING')) {
      this.cheAPI.getWorkspace().fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        return this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
      }).then(() => {
        this.wsagent = this.cheAPI.getWorkspace().getWorkspaceAgent(workspace.id);
        if (this.wsagent !== null) {
          if (!this.wsagent.getProject().getProjectDetailsByKey(projectPath)) {
            let promise = this.wsagent.getProject().fetchProjectDetails(workspace.id, projectPath);

            promise.then(() => {
              var projectDetails = this.wsagent.getProject().getProjectDetailsByKey(projectPath);
              this.updateRepositories(projectDetails);
            });
          } else {
            var projectDetails = this.wsagent.getProject().getProjectDetailsByKey(projectPath);
            this.updateRepositories(projectDetails);
          }
        }
      });
    }
  }

  updateRepositories(projectDetails) {
    if (!projectDetails.mixins || !projectDetails.mixins.length) {
      this.isEmptyState = true;
      return;
    }

    if (projectDetails.mixins.indexOf(subversionMixinId) !== -1) {
      //update remote svn url
      if (!this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path)) {
        let promise = this.wsagent.getSvn().fetchRemoteUrl(projectDetails.workspaceId, projectDetails.path);

        promise.then(() => {
          this.remoteSvnRepository = this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path);
        });
      } else {
        this.remoteSvnRepository = this.wsagent.getSvn().getRemoteUrlByKey(projectDetails.workspaceId, projectDetails.path);
      }
    }

    if (projectDetails.mixins.indexOf(gitMixinId) !== -1) {
      //update git local url
      if (!this.wsagent.getGit().getLocalUrlByKey(projectDetails.path)) {
        let promise = this.wsagent.getGit().fetchLocalUrl(projectDetails.path);

        promise.then(() => {
          this.localGitRepository = this.wsagent.getGit().getLocalUrlByKey(projectDetails.path);
        });
      } else {
        this.localGitRepository = this.wsagent.getGit().getLocalUrlByKey(projectDetails.path);
      }

      //update git remote urls
      if (!this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path)) {
        let promise = this.wsagent.getGit().fetchRemoteUrlArray(projectDetails.path);

        promise.then(() => {
          this.remoteGitRepositories = this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path);
        });
      } else {
        this.remoteGitRepositories = this.wsagent.getGit().getRemoteUrlArrayByKey(projectDetails.path);
      }
    }

  }

}
