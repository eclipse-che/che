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

import {CheWorkspaceAgent} from './che-workspace-agent';
import {ComposeEnvironmentManager} from './environment/compose-environment-manager';
import {DockerFileEnvironmentManager} from './environment/docker-file-environment-manager';
import {DockerImageEnvironmentManager} from './environment/docker-image-environment-manager';

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
  constructor ($resource, $q, cheWebsocket, lodash, cheEnvironmentRegistry, $log) {
    this.workspaceStatuses = ['RUNNING', 'STOPPED', 'PAUSED', 'STARTING', 'STOPPING', 'ERROR'];

    // keep resource
    this.$resource = $resource;
    this.$q = $q;
    this.lodash = lodash;
    this.cheWebsocket = cheWebsocket;

    // current list of workspaces
    this.workspaces = [];

    // per Id
    this.workspacesById = new Map();

    //Workspace agents per workspace id:
    this.workspaceAgents = new Map();

    // listeners if workspaces are changed/updated
    this.listeners = [];

    // list of subscribed to websocket workspace Ids
    this.subscribedWorkspacesIds = [];
    this.statusDefers = {};

    // remote call
    this.remoteWorkspaceAPI = this.$resource('/api/workspace', {}, {
        getDetails: {method: 'GET', url: '/api/workspace/:workspaceKey'},
        //having 2 methods for creation to ensure namespace parameter won't be send at all if value is null or undefined
        create: {method: 'POST', url: '/api/workspace'},
        createWithNamespace: {method: 'POST', url: '/api/workspace?namespace=:namespace'},
        deleteWorkspace: {method: 'DELETE', url: '/api/workspace/:workspaceId'},
        updateWorkspace: {method: 'PUT', url : '/api/workspace/:workspaceId'},
        addProject: {method: 'POST', url : '/api/workspace/:workspaceId/project'},
        deleteProject: {method: 'DELETE', url : '/api/workspace/:workspaceId/project/:path'},
        stopWorkspace: {method: 'DELETE', url : '/api/workspace/:workspaceId/runtime'},
        startWorkspace: {method: 'POST', url : '/api/workspace/:workspaceId/runtime?environment=:envName'},
        startTemporaryWorkspace: {method: 'POST', url : '/api/workspace/runtime?temporary=true'},
        addCommand: {method: 'POST', url: '/api/workspace/:workspaceId/command'}
      }
    );

    cheEnvironmentRegistry.addEnvironmentManager('compose', new ComposeEnvironmentManager($log));
    cheEnvironmentRegistry.addEnvironmentManager('dockerfile', new DockerFileEnvironmentManager($log));
    cheEnvironmentRegistry.addEnvironmentManager('dockerimage', new DockerImageEnvironmentManager($log));
  }

  getWorkspaceAgent(workspaceId) {
    if (this.workspaceAgents.has(workspaceId)) {
      return this.workspaceAgents.get(workspaceId);
    }

    let runtimeConfig = this.getWorkspaceById(workspaceId).runtime;
    if (runtimeConfig) {
      let wsAgentLink = this.lodash.find(runtimeConfig.links, (link) => {
        return link.rel === 'wsagent';
      });

      if (!wsAgentLink) {
        return null;
      }

      let workspaceAgentData = {path : wsAgentLink.href};
      let wsagent = new CheWorkspaceAgent(this.$resource, this.$q, this.cheWebsocket, workspaceAgentData);
      this.workspaceAgents.set(workspaceId, wsagent);
      return wsagent;
    }
    return null;
  }

/**
 * Gets all workspace agents of this remote
 * @returns {Map}
 */
  getWorkspaceAgents() {
    return this.workspaceAgents;
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

  getWorkspaceByName(namespace, name) {
    return this.lodash.find(this.workspaces, (workspace) => {
      return workspace.namespace === namespace && workspace.config.name === name;
    });
  }

  /**
   * Gets the workspace by id
   * @param workspace id
   * @returns {workspace}
   */
  getWorkspaceById(id) {
    return this.workspacesById.get(id);
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

        if (!workspace.temporary) {
          remoteWorkspaces.push(workspace);
          this.workspaces.push(workspace);
        }
        this.workspacesById.set(workspace.id, workspace);
        this.startUpdateWorkspaceStatus(workspace.id);
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

  /**
   * Fetch workspace details by workspace's key.
   *
   * @param workspaceKey workspace key: can be just id or namespace:workspaceName pair
   * @returns {Promise}
   */
  fetchWorkspaceDetails(workspaceKey) {
    var defer = this.$q.defer();

    let promise = this.remoteWorkspaceAPI.getDetails({workspaceKey : workspaceKey}).$promise;
    promise.then((data) => {
      this.workspacesById.set(data.id, data);
      this.lodash.remove(this.workspaces, (workspace) => {
        return workspace.id === data.id;
      });

     this.workspaces.push(data);

      this.startUpdateWorkspaceStatus(data.id);
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

  /**
   * Deletes a project of the workspace by it's path
   * @param workspaceId the workspace ID required to delete a project
   * @param path path to project to be deleted
   * @returns {*}
   */
  deleteProject(workspaceId, path) {
    let promise = this.remoteWorkspaceAPI.deleteProject({workspaceId : workspaceId, path: path}).$promise;
    return promise;
  }

  /**
   * Prepares workspace config using the data in provided one,
   * workspace name, machine source, RAM.
   *
   * @param config provided base workspace config
   * @param workspaceName workspace name
   * @param source machine source
   * @param ram workspace's RAM
   * @returns {config} prepared workspace config
   */
  formWorkspaceConfig(config, workspaceName, source, ram) {
    config = config || {};
    config.name = workspaceName;
    config.projects = [];
    config.defaultEnv = config.defaultEnv || workspaceName;
    config.description = null;
    ram = ram || 2 * Math.pow(1024, 3);

    //Check environments were provided in config:
    config.environments = (config.environments && Object.keys(config.environments).length > 0) ? config.environments : {};

    let defaultEnvironment = config.environments[config.defaultEnv];

    //Check default environment is provided and add if there is no:
    if (!defaultEnvironment) {
      defaultEnvironment = {
        'recipe': null,
        'machines': {'dev-machine': {'attributes': {'memoryLimitBytes': ram}, 'agents': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']}}
      };

      config.environments[config.defaultEnv] = defaultEnvironment;
    }

    if (source && source.type && source.type === 'environment') {
      let contentType = source.format === 'dockerfile' ? 'text/x-dockerfile' : 'application/x-yaml';
      defaultEnvironment.recipe = {
        'type': source.format,
        'contentType': contentType
      };

      defaultEnvironment.recipe.content = source.content || null;
      defaultEnvironment.recipe.location = source.location || null;
    }

    if (defaultEnvironment.recipe && defaultEnvironment.recipe.type === 'compose') {
      return config;
    }

    let devMachine = this.lodash.find(defaultEnvironment.machines, (machine) => {
      return machine.agents.includes('org.eclipse.che.ws-agent');
    });

    // check dev machine is provided and add if there is no:
    if (!devMachine) {
      devMachine = {
        'name': 'ws-machine',
        'attributes': {'memoryLimitBytes': ram},
        'type': 'docker',
        'source': source,
        'agents': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']
      };
      defaultEnvironment.machines[devMachine.name] = devMachine;
    } else {
      if (devMachine.attributes) {
        if (!devMachine.attributes.memoryLimitBytes) {
          devMachine.attributes.memoryLimitBytes = ram;
        }
      } else {
        devMachine.attributes = {'memoryLimitBytes': ram};
      }
      devMachine.source = source;
    }

    return config;
  }

  createWorkspace(namespace, workspaceName, source, ram, attributes) {
    let data = this.formWorkspaceConfig({}, workspaceName, source, ram);
    let attrs = this.lodash.map(this.lodash.pairs(attributes || {}), (item) => { return item[0] + ':' + item[1]});
    let promise = namespace ? this.remoteWorkspaceAPI.createWithNamespace({namespace : namespace, attribute: attrs}, data).$promise :
      this.remoteWorkspaceAPI.create({attribute: attrs}, data).$promise;
    return promise;
  }

  createWorkspaceFromConfig(namespace, workspaceConfig, attributes) {
    let attrs = this.lodash.map(this.lodash.pairs(attributes || {}), (item) => { return item[0] + ':' + item[1]});
    return namespace ? this.remoteWorkspaceAPI.createWithNamespace({namespace : namespace, attribute: attrs}, workspaceConfig).$promise :
      this.remoteWorkspaceAPI.create({attribute: attrs}, workspaceConfig).$promise;
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

  /**
   * Starts a temporary workspace by specifying configuration
   * @param workspaceConfig: che.IWorkspaceConfig - the configuration to start the workspace from
   * @returns {*} promise
   */
  startTemporaryWorkspace(workspaceConfig: che.IWorkspaceConfig): ng.IHttpPromise<any> {
    return this.remoteWorkspaceAPI.startTemporaryWorkspace({}, workspaceConfig).$promise;
  }

  stopWorkspace(workspaceId) {
    let promise = this.remoteWorkspaceAPI.stopWorkspace({workspaceId: workspaceId}, {}).$promise;
    return promise;
  }

  /**
   * Performs workspace config update by the given workspaceId and new data.
   * @param workspaceId the workspace ID
   * @param data the new workspace details
   * @returns {*|promise|n|N}
   */
  updateWorkspace(workspaceId, data) {
    var defer = this.$q.defer();

    let promise = this.remoteWorkspaceAPI.updateWorkspace({workspaceId : workspaceId}, data).$promise;
    promise.then((data) => {
      this.workspacesById.set(data.id, data);
      this.lodash.remove(this.workspaces, (workspace) => {
        return workspace.id === data.id;
      });
      this.workspaces.push(data);
      this.startUpdateWorkspaceStatus(data.id);
      defer.resolve(data);
    }, (error) => {
      defer.reject(error);
    });

    return defer.promise;
  }

  /**
   * Performs workspace deleting by the given workspaceId.
   * @param workspaceId the workspace ID
   * @returns {*|promise|n|N}
   */
  deleteWorkspaceConfig(workspaceId) {
    var defer = this.$q.defer();
    let promise = this.remoteWorkspaceAPI.deleteWorkspace({workspaceId : workspaceId}).$promise;
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
   * Gets the map of projects by workspace id.
   * @returns
   */
  getWorkspaceProjects() {
    let workspaceProjects = {};
    this.workspacesById.forEach((workspace) => {
      let projects = workspace.config.projects;
      projects.forEach((project) => {
        project.workspaceId = workspace.id;
        project.workspaceName = workspace.config.name;
      });

      workspaceProjects[workspace.id] = projects;
    });

    return workspaceProjects;
  }

  getAllProjects() {
    let projects = this.lodash.pluck(this.workspaces, 'config.projects');
    return [].concat.apply([], projects);
  }

  /**
   * Gets websocket for a given workspace. It needs to have fetched first the runtime configuration of the workspace
   * @param workspaceId the id of the workspace
   * @returns {string}
   */
  getWebsocketUrl(workspaceId) {
    let workspace = this.workspacesById.get(workspaceId);
    if (!workspace || !workspace.runtime || !workspace.runtime.devMachine) {
      return '';
    }
    let websocketLink = this.lodash.find(workspace.runtime.devMachine.links, l => l.rel === "wsagent.websocket");
    return websocketLink ? websocketLink.href : '';
  }

  getIdeUrl(namespace, workspaceName) {
    return '/ide/' + namespace + '/' + workspaceName;
  }

  /**
   * Creates deferred object which will be resolved
   * when workspace change it's status to given
   * @param workspaceId
   * @param status needed to resolve deferred object
     */
  fetchStatusChange(workspaceId, status) {
    let defer = this.$q.defer();
    if (status === this.getWorkspaceById(workspaceId).status) {
      defer.resolve();
    } else {
      if (!this.statusDefers[workspaceId]) {
        this.statusDefers[workspaceId] = {};
      }
      if (!this.statusDefers[workspaceId][status]) {
        this.statusDefers[workspaceId][status] = [];
      }
      this.statusDefers[workspaceId][status].push(defer);
    }
    return defer.promise;
  }

  /**
   * Add subscribe to websocket channel for specified workspaceId
   * to handle workspace's status changes.
   * @param workspaceId
     */
  startUpdateWorkspaceStatus(workspaceId) {
    if (!this.subscribedWorkspacesIds.includes(workspaceId)) {
      let bus = this.cheWebsocket.getBus();
      this.subscribedWorkspacesIds.push(workspaceId);

      bus.subscribe('workspace:' + workspaceId, (message) => {

        //Filter workspace events, which really indicate the status change:
        if (this.workspaceStatuses.indexOf(message.eventType) >= 0) {
          this.getWorkspaceById(workspaceId).status = message.eventType;
        } else if (message.eventType === 'SNAPSHOT_CREATING') {
          this.getWorkspaceById(workspaceId).status = 'SNAPSHOTTING';
        } else if (message.eventType === 'SNAPSHOT_CREATED') {
          //Snapshot can be created for RUNNING workspace only. As far as snapshot creation is only the events, not the state,
          //we introduced SNAPSHOT_CREATING status to be handled by UI, though it is fake one, and end of it is indicated by SNAPSHOT_CREATED.
          this.getWorkspaceById(workspaceId).status = 'RUNNING';
        }

        if (!this.statusDefers[workspaceId] || !this.statusDefers[workspaceId][message.eventType]) {
          return;
        }

        this.statusDefers[workspaceId][message.eventType].forEach((defer) => {defer.resolve(message)});
        this.statusDefers[workspaceId][message.eventType].length = 0;
      });
    }
  }
}
