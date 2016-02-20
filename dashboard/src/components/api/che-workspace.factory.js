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
 * This class is handling the workspace retrieval
 * It sets to the array workspaces the current workspaces which are not temporary
 * @author Florent Benoit
 */
export class CheWorkspace {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource, $q, cheUser) {
    // keep resource
    this.$resource = $resource;

    this.$q = $q;

    this.cheUser = cheUser;

    // current list of workspaces
    this.workspaces = [];

    // per Id
    this.workspacesById = new Map();

    // per workspace
    this.runtimeConfig = new Map();

    // listeners if workspaces are changed/updated
    this.listeners = [];

    // remote call
    this.remoteWorkspaceAPI = this.$resource('/api/workspace', {}, {
        getDetails: {method: 'GET', url: '/api/workspace/:workspaceId'},
        create: {method: 'POST', url: '/api/workspace/config?account=:accountId'},
        deleteConfig: {method: 'DELETE', url: '/api/workspace/:workspaceId/config'},
        updateConfig: {method: 'PUT', url : '/api/workspace/:workspaceId/config'},
        addProject: {method: 'POST', url : '/api/workspace/:workspaceId/project'},
        getRuntime: {method: 'GET', url : '/api/workspace/:workspaceId/runtime'},
        deleteRuntime: {method: 'DELETE', url : '/api/workspace/:workspaceId/runtime'},
        startWorkspace: {method: 'POST', url : '/api/workspace/:workspaceId/runtime?environment=:envName'},
        addCommand: {method: 'POST', url: '/api/workspace/:workspaceId/command'}
      }
    );
  }

  /**
   * Add a listener that need to have the onChangeWorkspaces(workspaces: Array) method
   * @param listener a changing listener
   */
  addListener(listener) {
    this.listeners.push(listener);
  }


  /**
   * Gets the workspaces of this remote
   * @returns {Array}
   */
  getWorkspaces() {
    return this.workspaces;
  }

  /**
   * Gets the workspaces per id
   * @returns {Map}
   */
  getWorkspacesById() {
    return this.workspacesById;
  }


  /**
   * Ask for loading the workspaces in asynchronous way
   * If there are no changes, it's not updated
   */
  fetchWorkspaces() {
    let query = this.remoteWorkspaceAPI.query();
    let promise = query.$promise;
    let updatedPromise = promise.then((data) => {
      var remoteWorkspaces = [];
      this.workspaces.length = 0;
      //TODO It's a fix used not to loose account ID of the workspace.
      //Can be removed, when API will return accountId in the list of user workspaces response:
      var copyWorkspaceById = new Map();
      angular.copy(this.workspacesById, copyWorkspaceById);

      this.workspacesById.clear();
      // add workspace if not temporary
      data.forEach((workspace) => {

        if (!workspace.config.temporary) {
          remoteWorkspaces.push(workspace);
          this.workspaces.push(workspace);
          this.workspacesById.set(workspace.id, workspace);
        }
      });
      return this.workspaces;
    });

    let callbackPromises = updatedPromise.then((data) => {
      var promises = [];
      promises.push(updatedPromise);

      this.listeners.forEach((listener) => {
        let promise = listener.onChangeWorkspaces(data);
        promises.push(promise);
      });
      return this.$q.all(promises);
    });

    return callbackPromises;
  }

  fetchWorkspaceDetails(workspaceId) {
    var defer = this.$q.defer();

    let promise = this.remoteWorkspaceAPI.getDetails({workspaceId : workspaceId}).$promise;
    promise.then((data) => {
      this.workspacesById.set(workspaceId, data);
      defer.resolve();
    }, (error) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });

    return defer.promise;
  }

  /**
   * Adds a project on the workspace
   * @param workspaceId the workspace ID required to add a project
   * @param project the project JSON entry to add
   * @returns {*}
   */
  addProject(workspaceId, project) {
    let promise = this.remoteWorkspaceAPI.addProject({workspaceId : workspaceId}, project).$promise;
    return promise;
  }


  createWorkspace(accountId, workspaceName, recipeUrl, ram) {
    // /api/workspace/config?account=accountId


    let data = {
      'environments': [],
      'name': workspaceName,
      'attributes': {},
      'projects': [],
      'defaultEnv': workspaceName,
      'description': null,
      'commands': [],
      'links': []
    };

    var memory = 2048;
    if (ram) {
      memory = ram;
    }


    let envEntry = {
        'name': workspaceName,
        'recipe': null,
        'machineConfigs': [{
          'name': 'ws-machine',
          'limits': {'ram': memory},
          'type': 'docker',
          'source': {'location': recipeUrl, 'type': 'recipe'},
          'dev': true
        }]
      };

    data.environments.push(envEntry);

    let promise = this.remoteWorkspaceAPI.create({accountId : accountId}, data).$promise;
    return promise;
  }

  createWorkspaceFromConfig(accountId, workspaceConfig) {
    return this.remoteWorkspaceAPI.create({accountId : accountId}, workspaceConfig).$promise;
  }

  /**
   * Add a command into the workspace
   * @param workspaceId the id of the workspace on which we want to add the command
   * @param command the command object that contains attribute like name, type, etc.
   * @returns promise
   */
  addCommand(workspaceId, command) {
    return this.remoteWorkspaceAPI.addCommand({workspaceId : workspaceId}, command).$promise;
  }

  /**
   * Starts the given workspace by specifying the ID and the environment name
   * @param workspaceId the workspace ID
   * @param envName the name of the environment
   * @returns {*} promise
   */
  startWorkspace(workspaceId, envName) {
    let promise = this.remoteWorkspaceAPI.startWorkspace({workspaceId: workspaceId, envName : envName}, {}).$promise;
    return promise;
  }


  stopWorkspace(workspaceId) {
    let promise = this.remoteWorkspaceAPI.deleteRuntime({workspaceId: workspaceId}, {}).$promise;
    return promise;
  }

  /**
   * Performs workspace config update by the given workspaceId and new data.
   * @param workspaceId the workspace ID
   * @param data the new workspace details
   * @returns {*|promise|n|N}
   */
  updateWorkspace(workspaceId, data) {
    let promise = this.remoteWorkspaceAPI.updateConfig({workspaceId : workspaceId}, data).$promise;
    promise.then(() => {this.fetchWorkspaceDetails(workspaceId);});
    return promise;
  }

  /**
   * Performs workspace deleting by the given workspaceId.
   * @param workspaceId the workspace ID
   * @returns {*|promise|n|N}
   */
  deleteWorkspaceConfig(workspaceId) {
    var defer = this.$q.defer();
    let promise = this.remoteWorkspaceAPI.deleteConfig({workspaceId : workspaceId}).$promise;
    promise.then(() => {
      this.listeners.forEach((listener) => {
        listener.onDeleteWorkspace(workspaceId);
      });
      defer.resolve();
    }, (error) => {
        defer.reject(error);
      });

    return defer.promise;
  }

  /**
   * Get Runtimeconfig of a workspace
   * @param workspaceId the workspace ID
   * @returns {*|promise|n|N}
   */
  fetchRuntimeConfig(workspaceId) {
    var defer = this.$q.defer();
    let promise = this.remoteWorkspaceAPI.getRuntime({workspaceId: workspaceId}).$promise;
    let updatedPromise = promise.then((data) => {
      this.runtimeConfig.set(workspaceId, data);
      defer.resolve();
    }, (error) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });
    return defer.promise;
  }

  /**
   * Get Runtime Config of a workspace.
   * @param workspaceId the workspace ID
   * @returns {Object}
   */
  getRuntimeConfig(workspaceId) {
    return this.runtimeConfig.get(workspaceId);
  }

  /**
   * Gets websocket for a given workspace. It needs to have fetched first the runtime configuration of the workspace
   * @param workspaceId the id of the workspace
   * @returns {string}
   */
  getWebsocketUrl(workspaceId) {
    let runtimeData = this.getRuntimeConfig(workspaceId);
    if (!runtimeData) {
      return '';
    }
    // extract the Websocket URL of the runtime
    let servers = runtimeData.devMachine.metadata.servers;

    var extensionServerAddress;
    for (var key in servers) {
      let server = servers[key];
      if ('extensions' === server.ref) {
        extensionServerAddress = server.address;
      }
    }

    let endpoint = runtimeData.devMachine.metadata.envVariables.CHE_API_ENDPOINT;

    var contextPath;
    if (endpoint.endsWith('/ide/api')) {
      contextPath = 'ide';
    } else {
      contextPath = 'api';
    }

    return 'ws://' + extensionServerAddress + '/' + contextPath + '/ext/ws/' + workspaceId;

  }

}
