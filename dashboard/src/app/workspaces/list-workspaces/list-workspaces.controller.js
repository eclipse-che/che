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

/**
 * @ngdoc controller
 * @name workspaces.list.controller:ListWorkspacesCtrl
 * @description This class is handling the controller for listing the workspaces
 * @author Ann Shumilova
 */
export class ListWorkspacesCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (cheAPI, $q) {
    this.cheAPI = cheAPI;
    this.$q = $q;
    this.state = 'loading';
    this.isInfoLoading = true;
    this.workspaceFilter = {name: ''};

    //Map of all workspaces with additional info by id:
    this.workspacesById = new Map();
    //Map of workspaces' used resources (consumed GBH):
    this.workspaceUsedResources = new Map();

    this.getUserWorkspaces();
  }


  //Fetch current user's workspaces (where he is a member):
  getUserWorkspaces() {
    // fetch workspaces when initializing
    let promise = this.cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
        this.updateSharedWorkspaces();
      },
      (error) => {
        if (error.status === 304) {
          // ok
          this.updateSharedWorkspaces();
          return;
        }
        this.state = 'error';
        this.isInfoLoading = false;
      });
  }


  //Update the info of all user workspaces:
  updateSharedWorkspaces() {
    this.userWorkspaces = [];
    let workspaces = this.cheAPI.getWorkspace().getWorkspaces();
    if (workspaces.length === 0) {
      this.isInfoLoading = false;
    }
    workspaces.forEach((workspace) => {
      //First check the list of already received workspace info:
      if (!this.workspacesById.get(workspace.id)) {
        this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id).then(() => {
          let userWorkspace = this.cheAPI.getWorkspace().getWorkspacesById().get(workspace.id);
          this.getWorkspaceInfo(userWorkspace);
          this.userWorkspaces.push(userWorkspace);
        });
      } else {
        let userWorkspace = this.workspacesById.get(workspace.workspaceReference.id);
        this.userWorkspaces.push(userWorkspace);
        this.isInfoLoading = false;
      }
    });

    this.state = 'loaded';
  }

  //Represents given account resources as a map with workspace id as a key.
  processUsedResources(resources) {
    resources.forEach((resource) => {
      this.workspaceUsedResources.set(resource.workspaceId, resource.memory.toFixed(2));
    });
  }

  //Gets all necessary workspace info to be displayed.
  getWorkspaceInfo(workspace) {
    let promises = [];
    this.workspacesById.set(workspace.id, workspace);

    workspace.isLocked = false;
    workspace.usedResources = this.workspaceUsedResources.get(workspace.id);

    //No access to runner resources if workspace is locked:
    if (!workspace.isLocked) {
      let promiseRuntimeConfig = this.cheAPI.getWorkspace().fetchRuntimeConfig(workspace.id);
      promises.push(promiseRuntimeConfig);
    }

    this.$q.all(promises).then(() => {
      this.isInfoLoading = false;
    }, (error) => {
      console.log(error);
      this.isInfoLoading = false;
    });
  }
}
