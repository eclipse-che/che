/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
import {CheEnvironmentRegistry} from './environment/che-environment-registry.factory';
import {CheWebsocket} from './che-websocket.factory';

interface ICHELicenseResource<T> extends ng.resource.IResourceClass<T> {
  getDetails: any;
  create: any;
  createWithNamespace: any;
  deleteWorkspace: any;
  updateWorkspace: any;
  addProject: any;
  deleteProject: any;
  stopWorkspace: any;
  startWorkspace: any;
  startTemporaryWorkspace: any;
  addCommand: any;
  getSettings: any;
}

/**
 * This class is handling the workspace retrieval
 * It sets to the array workspaces the current workspaces which are not temporary
 * @author Florent Benoit
 */
export class CheWorkspace {
  $resource: ng.resource.IResourceService;
  $q: ng.IQService;
  listeners: Array<any>;
  workspaceStatuses: Array<string>;
  workspaces: Array<che.IWorkspace>;
  subscribedWorkspacesIds: Array<string>;
  workspaceAgents: Map<string, CheWorkspaceAgent>;
  workspacesById: Map<string, che.IWorkspace>;
  remoteWorkspaceAPI: ICHELicenseResource<any>;
  lodash: any;
  cheWebsocket: CheWebsocket;
  statusDefers: Object;
  workspaceSettings: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, cheWebsocket: CheWebsocket, lodash: any, cheEnvironmentRegistry: CheEnvironmentRegistry, $log: ng.ILogService) {
    this.workspaceStatuses = ['RUNNING', 'STOPPED', 'PAUSED', 'STARTING', 'STOPPING', 'ERROR'];

    // keep resource
    this.$q = $q;
    this.$resource = $resource;
    this.lodash = lodash;
    this.cheWebsocket = cheWebsocket;

    // current list of workspaces
    this.workspaces = [];

    // per Id
    this.workspacesById = new Map();

    // workspace agents per workspace id:
    this.workspaceAgents = new Map();

    // listeners if workspaces are changed/updated
    this.listeners = [];

    // list of subscribed to websocket workspace Ids
    this.subscribedWorkspacesIds = [];
    this.statusDefers = {};

    // remote call
    this.remoteWorkspaceAPI = <ICHELicenseResource<any>>this.$resource('/api/workspace', {}, {
        getDetails: {method: 'GET', url: '/api/workspace/:workspaceKey'},
        // having 2 methods for creation to ensure namespace parameter won't be send at all if value is null or undefined
        create: {method: 'POST', url: '/api/workspace'},
        createWithNamespace: {method: 'POST', url: '/api/workspace?namespace=:namespace'},
        deleteWorkspace: {method: 'DELETE', url: '/api/workspace/:workspaceId'},
        updateWorkspace: {method: 'PUT', url: '/api/workspace/:workspaceId'},
        addProject: {method: 'POST', url: '/api/workspace/:workspaceId/project'},
        deleteProject: {method: 'DELETE', url: '/api/workspace/:workspaceId/project/:path'},
        stopWorkspace: {method: 'DELETE', url: '/api/workspace/:workspaceId/runtime?create-snapshot=:createSnapshot'},
        startWorkspace: {method: 'POST', url: '/api/workspace/:workspaceId/runtime?environment=:envName'},
        startTemporaryWorkspace: {method: 'POST', url: '/api/workspace/runtime?temporary=true'},
        addCommand: {method: 'POST', url: '/api/workspace/:workspaceId/command'},
        getSettings: {method: 'GET', url: '/api/workspace/settings'}
      }
    );

    cheEnvironmentRegistry.addEnvironmentManager('compose', new ComposeEnvironmentManager($log));
    cheEnvironmentRegistry.addEnvironmentManager('dockerfile', new DockerFileEnvironmentManager($log));
    cheEnvironmentRegistry.addEnvironmentManager('dockerimage', new DockerImageEnvironmentManager($log));

    this.fetchWorkspaceSettings();
  }

  /**
   * Gets workspace agent
   * @param workspaceId {string}
   * @returns {CheWorkspaceAgent}
   */
  getWorkspaceAgent(workspaceId: string): CheWorkspaceAgent {
    if (this.workspaceAgents.has(workspaceId)) {
      return this.workspaceAgents.get(workspaceId);
    }

    let runtimeConfig = this.getWorkspaceById(workspaceId).runtime;
    if (runtimeConfig) {
      let wsAgentLink = this.lodash.find(runtimeConfig.links, (link: any) => {
        return link.rel === 'wsagent';
      });

      if (!wsAgentLink) {
        return null;
      }

      let workspaceAgentData = {path: wsAgentLink.href};
      let wsagent: CheWorkspaceAgent = new CheWorkspaceAgent(this.$resource, this.$q, this.cheWebsocket, workspaceAgentData);
      this.workspaceAgents.set(workspaceId, wsagent);
      return wsagent;
    }
    return null;
  }

  /**
   * Gets all workspace agents of this remote
   * @returns {Map<string, CheWorkspaceAgent>}
   */
  getWorkspaceAgents(): Map<string, CheWorkspaceAgent> {
    return this.workspaceAgents;
  }

  /**
   * Add a listener that need to have the onChangeWorkspaces(workspaces: Array) method
   * @param listener {Function} a changing listener
   */
  addListener(listener: Function): void {
    this.listeners.push(listener);
  }


  /**
   * Gets the workspaces of this remote
   * @returns {Array}
   */
  getWorkspaces(): Array<che.IWorkspace> {
    return this.workspaces;
  }

  /**
   * Gets the workspaces per id
   * @returns {Map}
   */
  getWorkspacesById(): Map<string, che.IWorkspace> {
    return this.workspacesById;
  }

  getWorkspaceByName(namespace: string, name: string): che.IWorkspace {
    return this.lodash.find(this.workspaces, (workspace: che.IWorkspace) => {
      return workspace.namespace === namespace && workspace.config.name === name;
    });
  }

  /**
   * Gets the workspace by id
   * @param id {string} - workspace id
   * @returns {che.IWorkspace}
   */
  getWorkspaceById(id: string): che.IWorkspace {
    return this.workspacesById.get(id);
  }


  /**
   * Ask for loading the workspaces in asynchronous way
   * If there are no changes, it's not updated
   * @returns {ng.IPromise<any>}
   */
  fetchWorkspaces(): ng.IPromise<any> {
    let promise = this.remoteWorkspaceAPI.query().$promise;
    let updatedPromise = promise.then((data: Array<che.IWorkspace>) => {
      let remoteWorkspaces = [];
      this.workspaces.length = 0;
      // todo It's a fix used not to loose account ID of the workspace.
      // can be removed, when API will return accountId in the list of user workspaces response:
      let copyWorkspaceById = new Map();
      angular.copy(this.workspacesById, copyWorkspaceById);

      this.workspacesById.clear();
      // add workspace if not temporary
      data.forEach((workspace: che.IWorkspace) => {

        if (!workspace.temporary) {
          remoteWorkspaces.push(workspace);
          this.workspaces.push(workspace);
        }
        this.workspacesById.set(workspace.id, workspace);
        this.startUpdateWorkspaceStatus(workspace.id);
      });
      return this.workspaces;
    });

    let callbackPromises = updatedPromise.then((data: any) => {
      let promises = [];
      promises.push(updatedPromise);

      this.listeners.forEach((listener: any) => {
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
   * @param workspaceKey {string} workspace key: can be just id or namespace:workspaceName pair
   * @returns {ng.IPromise<any>}
   */
  fetchWorkspaceDetails(workspaceKey: string): ng.IPromise<any> {
    let defer = this.$q.defer();

    let promise = this.remoteWorkspaceAPI.getDetails({workspaceKey: workspaceKey}).$promise;
    promise.then((data: che.IWorkspace) => {
      this.workspacesById.set(data.id, data);
      if (!data.temporary) {
        this.lodash.remove(this.workspaces, (workspace: che.IWorkspace) => {
          return workspace.id === data.id;
        });
        this.workspaces.push(data);
        this.startUpdateWorkspaceStatus(data.id);
      }
      defer.resolve();
    }, (error: any) => {
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
   * @param workspaceId {string} the workspace ID required to add a project
   * @param project {che.IProject} the project JSON entry to add
   * @returns {ng.IPromise<any>}
   */
  addProject(workspaceId: string, project: che.IProject): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.addProject({workspaceId: workspaceId}, project).$promise;
  }

  /**
   * Deletes a project of the workspace by it's path
   * @param workspaceId {string} the workspace ID required to delete a project
   * @param path {string} path to project to be deleted
   * @returns {ng.IPromise<any>}
   */
  deleteProject(workspaceId: string, path: string): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.deleteProject({workspaceId: workspaceId, path: path}).$promise;
  }

  /**
   * Prepares workspace config using the data in provided one,
   * workspace name, machine source, RAM.
   *
   * @param config {any} provided base workspace config
   * @param workspaceName {string} workspace name
   * @param source {any} machine source
   * @param ram {number} workspace's RAM
   * @returns {any} prepared workspace config
   */
  formWorkspaceConfig(config: any, workspaceName: string, source: any, ram: number): any {
    config = config || {};
    config.name = workspaceName;
    config.projects = [];
    config.defaultEnv = config.defaultEnv || workspaceName;
    config.description = null;
    ram = ram || 2 * Math.pow(1024, 3);

    // check environments were provided in config:
    config.environments = (config.environments && Object.keys(config.environments).length > 0) ? config.environments : {};

    let defaultEnvironment = config.environments[config.defaultEnv];

    // check default environment is provided and add if there is no:
    if (!defaultEnvironment) {
      defaultEnvironment = {
        'recipe': null,
        'machines': {
          'dev-machine': {
            'attributes': {'memoryLimitBytes': ram},
            'agents': ['org.eclipse.che.ws-agent', 'org.eclipse.che.terminal', 'org.eclipse.che.ssh']
          }
        }
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

    let devMachine = this.lodash.find(defaultEnvironment.machines, (machine: any) => {
      return machine.agents.indexOf('org.eclipse.che.ws-agent') >= 0;
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

  createWorkspace(namespace: string, workspaceName: string, source: any, ram: number, attributes: any): ng.IPromise<any> {
    let data = this.formWorkspaceConfig({}, workspaceName, source, ram);
    let attrs = this.lodash.map(this.lodash.pairs(attributes || {}), (item: any) => {
      return item[0] + ':' + item[1];
    });
    let promise = namespace ? this.remoteWorkspaceAPI.createWithNamespace({
      namespace: namespace,
      attribute: attrs
    }, data).$promise :
      this.remoteWorkspaceAPI.create({attribute: attrs}, data).$promise;
    return promise;
  }

  createWorkspaceFromConfig(namespace: string, workspaceConfig: che.IWorkspaceConfig, attributes: any): ng.IPromise<any> {
    let attrs = this.lodash.map(this.lodash.pairs(attributes || {}), (item: any) => {
      return item[0] + ':' + item[1];
    });
    return namespace ? this.remoteWorkspaceAPI.createWithNamespace({
      namespace: namespace,
      attribute: attrs
    }, workspaceConfig).$promise :
      this.remoteWorkspaceAPI.create({attribute: attrs}, workspaceConfig).$promise;
  }

  /**
   * Add a command into the workspace
   * @param workspaceId {string} the id of the workspace on which we want to add the command
   * @param command {any} the command object that contains attribute like name, type, etc.
   * @returns {ng.IPromise<any>}
   */
  addCommand(workspaceId: string, command: any): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.addCommand({workspaceId: workspaceId}, command).$promise;
  }

  /**
   * Starts the given workspace by specifying the ID and the environment name
   * @param workspaceId the workspace ID
   * @param envName the name of the environment
   * @returns {ng.IPromise<any>} promise
   */
  startWorkspace(workspaceId: string, envName: string): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.startWorkspace({workspaceId: workspaceId, envName: envName}, {}).$promise;
  }

  /**
   * Starts a temporary workspace by specifying configuration
   * @param workspaceConfig {che.IWorkspaceConfig}
   * @returns {ng.IPromise<any>} promise
   */
  startTemporaryWorkspace(workspaceConfig: che.IWorkspaceConfig): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.startTemporaryWorkspace({}, workspaceConfig).$promise;
  }

  /**
   * Stop workspace
   * @param workspaceId {string}
   * @returns {ng.IPromise<any>} promise
   */
  stopWorkspace(workspaceId: string, createSnapshot: boolean): ng.IPromise<any> {
    createSnapshot = createSnapshot === undefined ? this.getAutoSnapshotSettings() : createSnapshot;
    return this.remoteWorkspaceAPI.stopWorkspace({workspaceId: workspaceId, createSnapshot: createSnapshot}, {}).$promise;
  }

  /**
   * Performs workspace config update by the given workspaceId and new data.
   * @param workspaceId {string} the workspace ID
   * @param data {che.IWorkspace} the new workspace details
   * @returns {ng.IPromise<any>}
   */
  updateWorkspace(workspaceId: string, data: che.IWorkspace): ng.IPromise<any> {
    let defer = this.$q.defer();

    let promise = this.remoteWorkspaceAPI.updateWorkspace({workspaceId: workspaceId}, data).$promise;
    promise.then((data: che.IWorkspace) => {
      this.workspacesById.set(data.id, data);
      this.lodash.remove(this.workspaces, (workspace: che.IWorkspace) => {
        return workspace.id === data.id;
      });
      this.workspaces.push(data);
      this.startUpdateWorkspaceStatus(data.id);
      defer.resolve(data);
    }, (error: any) => {
      defer.reject(error);
    });

    return defer.promise;
  }

  /**
   * Performs workspace deleting by the given workspaceId.
   * @param workspaceId {string} the workspace ID
   * @returns {ng.IPromise<any>}
   */
  deleteWorkspaceConfig(workspaceId: string): ng.IPromise<any> {
    let defer = this.$q.defer();
    let promise = this.remoteWorkspaceAPI.deleteWorkspace({workspaceId: workspaceId}).$promise;
    promise.then(() => {
      this.listeners.forEach((listener: any) => {
        listener.onDeleteWorkspace(workspaceId);
      });
      defer.resolve();
    }, (error: any) => {
      defer.reject(error);
    });

    return defer.promise;
  }

  /**
   * Gets the map of projects by workspace id.
   * @returns {che.IWorkspaceProjects}
   */
  getWorkspaceProjects(): che.IWorkspaceProjects {
    let workspaceProjects: che.IWorkspaceProjects = {};
    this.workspacesById.forEach((workspace: che.IWorkspace) => {
      let projects = workspace.config.projects;
      projects.forEach((project: che.IProject) => {
        project.workspaceId = workspace.id;
        project.workspaceName = workspace.config.name;
      });

      workspaceProjects[workspace.id] = projects;
    });

    return workspaceProjects;
  }

  getAllProjects(): Array<che.IProject> {
    let projects = this.lodash.pluck(this.workspaces, 'config.projects');
    return [].concat.apply([], projects);
  }

  /**
   * Gets websocket for a given workspace. It needs to have fetched first the runtime configuration of the workspace
   * @param workspaceId {string} the id of the workspace
   * @returns {string}
   */
  getWebsocketUrl(workspaceId: string): string {
    let workspace = this.workspacesById.get(workspaceId);
    if (!workspace || !workspace.runtime || !workspace.runtime.devMachine) {
      return '';
    }
    let websocketLink = this.lodash.find(workspace.runtime.devMachine.links, (link: any) => {
      return link.rel === 'wsagent.websocket';
    });
    return websocketLink ? websocketLink.href : '';
  }

  /**
   * Gets IDE Url
   * @param namespace {string}
   * @param workspaceName {string}
   * @returns {string}
   */
  getIdeUrl(namespace: string, workspaceName: string): string {
    return '/ide/' + namespace + '/' + workspaceName;
  }

  /**
   * Creates deferred object which will be resolved
   * when workspace change it's status to given
   * @param workspaceId {string}
   * @param status {string} needed to resolve deferred object
   * @returns {ng.IPromise<any>}
   */
  fetchStatusChange(workspaceId: string, status: string): ng.IPromise<any> {
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
   * @param workspaceId {string}
   */
  startUpdateWorkspaceStatus(workspaceId: string): void {
    if (this.subscribedWorkspacesIds.indexOf(workspaceId) < 0) {
      let bus = this.cheWebsocket.getBus();
      this.subscribedWorkspacesIds.push(workspaceId);

      bus.subscribe('workspace:' + workspaceId, (message: any) => {

        // filter workspace events, which really indicate the status change:
        if (this.workspaceStatuses.indexOf(message.eventType) >= 0) {
          this.getWorkspaceById(workspaceId).status = message.eventType;
        } else if (message.eventType === 'SNAPSHOT_CREATING') {
          this.getWorkspaceById(workspaceId).status = 'SNAPSHOTTING';
        } else if (message.eventType === 'SNAPSHOT_CREATED') {
          // snapshot can be created for RUNNING workspace only.
          this.getWorkspaceById(workspaceId).status = 'RUNNING';
        }

        if (!this.statusDefers[workspaceId] || !this.statusDefers[workspaceId][message.eventType]) {
          return;
        }

        this.statusDefers[workspaceId][message.eventType].forEach((defer: any) => {
          defer.resolve(message);
        });

        this.statusDefers[workspaceId][message.eventType].length = 0;
      });
    }
  }

  /**
   * Fetches the system settings for workspaces.
   *
   * @returns {IPromise<TResult>}
   */
  fetchWorkspaceSettings(): ng.IPromise {
    let promise = this.remoteWorkspaceAPI.getSettings().$promise;
    let resultPromise = promise.then((settings: any) => {
      this.workspaceSettings = settings;
    });

    return resultPromise;
  }

  /**
   * Returns the system settings for workspaces.
   *
   * @returns {any} the system settings for workspaces
   */
  getWorkspaceSettings(): any {
    return this.workspaceSettings;
  }

  /**
   * Returns the value of autosnapshot system property.
   *
   * @returns {boolean} 'che.workspace.auto_snapshot' property value
   */
  getAutoSnapshotSettings(): boolean {
    return this.workspaceSettings ? this.workspaceSettings['che.workspace.auto_snapshot'] : true;
  }
}
