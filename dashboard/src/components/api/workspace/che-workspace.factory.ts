/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheJsonRpcMasterApi} from '../json-rpc/che-json-rpc-master-api';
import {CheJsonRpcApi} from '../json-rpc/che-json-rpc-api.factory';
import {IObservableCallbackFn, Observable} from '../../utils/observable';
import {CheBranding} from '../../branding/che-branding.factory';
import {CheNotification} from '../../notification/che-notification.factory';
import {WorkspaceDataManager} from './workspace-data-manager';

const WS_AGENT_HTTP_LINK: string = 'wsagent/http';
const WS_AGENT_WS_LINK: string = 'wsagent/ws';

interface ICHELicenseResource<T> extends ng.resource.IResourceClass<T> {
  createDevfile: any;
  createDevfileWithNamespace: any;
  deleteWorkspace: any;
  updateWorkspace: any;
  addProject: any;
  deleteProject: any;
  stopWorkspace: any;
  startWorkspace: any;
  startWorkspaceWithNoEnvironment: any;
  startTemporaryWorkspace: any;
  getSettings: () => ng.resource.IResource<che.IWorkspaceSettings>;
}

export enum WorkspaceStatus {
  RUNNING = 1,
  STOPPED,
  PAUSED,
  STARTING,
  STOPPING,
  ERROR
}

/**
 * This class is handling the workspace retrieval
 * It sets to the array workspaces the current workspaces which are not temporary
 * @author Florent Benoit
 */
export class CheWorkspace {

  static $inject = ['$resource', '$http', '$q', 'cheJsonRpcApi', 'cheNotification', '$websocket', '$location', 'proxySettings', 'userDashboardConfig', 'lodash', 'cheBranding'];

  private $resource: ng.resource.IResourceService;
  private $http: ng.IHttpService;
  private $q: ng.IQService;
  private $websocket: any;
  private cheNotification: CheNotification;
  private cheJsonRpcMasterApi: CheJsonRpcMasterApi;
  private listeners: Array<any>;
  private workspaceStatuses: Array<string>;
  private workspaces: Array<che.IWorkspace>;
  private subscribedWorkspacesIds: Array<string>;
  private workspacesByNamespace: Map<string, Array<che.IWorkspace>>;
  private workspacesById: Map<string, che.IWorkspace>;
  private remoteWorkspaceAPI: ICHELicenseResource<any>;
  private lodash: any;
  private statusDefers: Object;
  private workspaceSettings: che.IWorkspaceSettings;
  private jsonRpcApiLocation: string;
  private workspaceLoaderUrl: string;
  /**
   * Map with instance of Observable by workspaceId.
   */
  private observables: Map<string, Observable<che.IWorkspace>> = new Map();
  /**
   * Map with promises.
   */
  private workspacePromises: Map<string, ng.IPromise<any>> = new Map();
  /**
   *
   */
  private workspaceDataManager: WorkspaceDataManager;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              $http: ng.IHttpService,
              $q: ng.IQService,
              cheJsonRpcApi: CheJsonRpcApi,
              cheNotification: CheNotification,
              $websocket: any,
              $location: ng.ILocationService,
              proxySettings: string,
              userDashboardConfig: any,
              lodash: any,
              cheBranding: CheBranding
  ) {
    this.workspaceStatuses = ['RUNNING', 'STOPPED', 'PAUSED', 'STARTING', 'STOPPING', 'ERROR'];
    // keep resource
    this.$q = $q;
    this.$resource = $resource;
    this.$http = $http;
    this.$websocket = $websocket;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.workspaceDataManager = new WorkspaceDataManager();

    // current list of workspaces
    this.workspaces = [];

    // per Id
    this.workspacesById = new Map();

    // per namespace
    this.workspacesByNamespace = new Map();

    // listeners if workspaces are changed/updated
    this.listeners = [];

    // list of subscribed to websocket workspace Ids
    this.subscribedWorkspacesIds = [];
    this.statusDefers = {};

    // remote call
    this.remoteWorkspaceAPI = <ICHELicenseResource<any>>this.$resource('/api/workspace', {}, {
        // having 2 methods for creation to ensure namespace parameter won't be send at all if value is null or undefined
        createDevfile: {method: 'POST', url: '/api/workspace/devfile'},
        createDevfileWithNamespace: {method: 'POST', url: '/api/workspace/devfile?namespace=:namespace'},
        deleteWorkspace: {method: 'DELETE', url: '/api/workspace/:workspaceId'},
        updateWorkspace: {method: 'PUT', url: '/api/workspace/:workspaceId'},
        addProject: {method: 'POST', url: '/api/workspace/:workspaceId/project'},
        deleteProject: {method: 'DELETE', url: '/api/workspace/:workspaceId/project/:path'},
        stopWorkspace: {method: 'DELETE', url: '/api/workspace/:workspaceId/runtime'},
        startWorkspace: {method: 'POST', url: '/api/workspace/:workspaceId/runtime?environment=:envName'},
        startWorkspaceWithNoEnvironment: {method: 'POST', url: '/api/workspace/:workspaceId/runtime'},
        startTemporaryWorkspace: {method: 'POST', url: '/api/workspace/runtime?temporary=true'},
        getSettings: {method: 'GET', url: '/api/workspace/settings'}
      }
    );

    const CONTEXT_FETCHER_ID = 'websocketContextFetcher';
    const callback = () => {
      this.jsonRpcApiLocation = this.formJsonRpcApiLocation($location, proxySettings, userDashboardConfig.developmentMode) + cheBranding.getWebsocketContext();
      this.cheJsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(this.jsonRpcApiLocation);
      cheBranding.unregisterCallback(CONTEXT_FETCHER_ID);
    };
    cheBranding.registerCallback(CONTEXT_FETCHER_ID, callback.bind(this));

    this.checkWorkspaceLoader(userDashboardConfig.developmentMode, proxySettings);
  }

  /**
   * Add callback to the list of on workspace change subscribers.
   *
   * @param {string} workspaceId
   * @param {IObservableCallbackFn<che.IWorkspace>} action the callback
   */
  subscribeOnWorkspaceChange(workspaceId: string, action: IObservableCallbackFn<che.IWorkspace>): void {
    if (!workspaceId || !action) {
      return;
    }
    if (!this.observables.has(workspaceId)) {
      this.observables.set(workspaceId, new Observable());
    }

    const observable = this.observables.get(workspaceId);
    observable.subscribe(action);
  }

  /**
   * Unregister on workspace change callback.
   *
   * @param {string} workspaceId
   * @param {IObservableCallbackFn<che.IWorkspace>} action the callback
   */
  unsubscribeOnWorkspaceChange(workspaceId: string, action: IObservableCallbackFn<che.IWorkspace>): void {
    const observable = this.observables.get(workspaceId);
    if (!observable) {
      return;
    }
    observable.unsubscribe(action);
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
      return workspace.namespace === namespace && this.workspaceDataManager.getName(workspace) === name;
    });
  }

  /**
   * Fetches workspaces by provided namespace.
   *
   * @param namespace namespace
   */
  fetchWorkspacesByNamespace(namespace: string): ng.IPromise<void> {
    let promise = this.$http.get('/api/workspace/namespace/' + namespace);
    let resultPromise = promise.then((response: ng.IHttpResponse<che.IWorkspace[]>) => {
      const workspaces = this.getWorkspacesByNamespace(namespace);

      workspaces.length = 0;
      response.data.forEach((workspace: che.IWorkspace) => {
        workspaces.push(workspace);
      });
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.$q.when();
      }
      return this.$q.reject(error);
    });

    return resultPromise;
  }

  getWorkspacesByNamespace(namespace: string): Array<che.IWorkspace> {
    if (!this.workspacesByNamespace.has(namespace)) {
      this.workspacesByNamespace.set(namespace, []);
    }

    return this.workspacesByNamespace.get(namespace);
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
   * @returns {ng.IPromise<Array<che.IWorkspace>>}
   */
  fetchWorkspaces(): ng.IPromise<Array<che.IWorkspace>> {
    let promise = this.remoteWorkspaceAPI.query().$promise;
    let updatedPromise = promise.then((data: Array<che.IWorkspace>) => {
      this.workspaces.length = 0;
      this.workspacesById.clear();
      // add workspace if not temporary
      data.forEach((workspace: che.IWorkspace) => {
        this.updateWorkspacesList(workspace);
      });
      return this.workspaces;
    }, (error: any) => {
      if (error && error.status === 304) {
        return this.workspaces;
      }
      return this.$q.reject(error);
    });

    let callbackPromises = updatedPromise.then((data: Array<che.IWorkspace>) => {
      let promises = [];
      promises.push(updatedPromise);

      this.listeners.forEach((listener: any) => {
        let promise = listener.onChangeWorkspaces(data);
        promises.push(promise);
      });
      return this.$q.all(promises).then(() => data);
    }, (error: any) => {
      return this.$q.reject(error);
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
    const workspacePromisesKey = 'fetchWorkspaceDetails' + workspaceKey;
    if (this.workspacePromises.has(workspacePromisesKey)) {
      return this.workspacePromises.get(workspacePromisesKey);
    }
    const defer = this.$q.defer();
    const promise: ng.IHttpPromise<any> = this.$http.get('/api/workspace/' + workspaceKey);
    this.workspacePromises.set(workspacePromisesKey, defer.promise);

    promise.then((response: ng.IHttpPromiseCallbackArg<che.IWorkspace>) => {
      const workspace = response.data;
      this.workspacesById.set(workspace.id, workspace);
      this.updateWorkspacesList(workspace);
      defer.resolve();
    }, (error: any) => {
      if (error && error.status === 304) {
        defer.resolve();
        return;
      }
      defer.reject(error);
    }).finally(() => {
      this.workspacePromises.delete(workspacePromisesKey);
    });

    return defer.promise;
  }

  /**
   * Validates machine token for the workspace
   *
   * @param workspaceId workspace ID
   * @param token Che machine token
   */
  validateMachineToken(workspaceId: string, token: string): ng.IPromise<void> {
    const defer = this.$q.defer<void>();

    const promise: ng.IHttpPromise<che.IWorkspace> = this.$http.get(`/api/workspace/${workspaceId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    promise.then((response: ng.IHttpPromiseCallbackArg<che.IWorkspace>) => {
      defer.resolve();
    }, (error: any) => {
      if (error && error.status === 304) {
        defer.resolve();
        return;
      }
      defer.reject(error);
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

  createWorkspaceFromDevfile(namespace: string, devfile: che.IWorkspaceDevfile, attributes: any): ng.IPromise<che.IWorkspace> {
    let attrs = this.lodash.map(this.lodash.pairs(attributes || {}), (item: any) => {
      return item[0] + ':' + item[1];
    });
    return namespace ? this.remoteWorkspaceAPI.createDevfileWithNamespace({
      namespace: namespace,
      attribute: attrs
    }, devfile).$promise :
      this.remoteWorkspaceAPI.createDevfile({attribute: attrs}, devfile).$promise;
  }

  /**
   * Starts the given workspace by specifying the ID and the environment name
   * @param workspaceId the workspace ID
   * @param envName the name of the environment (optional)
   * @returns {ng.IPromise<any>} promise
   */
  startWorkspace(workspaceId: string, envName?: string): ng.IPromise<any> {
    this.notifyIfEphemeral(workspaceId);
    const workspacePromisesKey = 'startWorkspace' + workspaceId;
    if (this.workspacePromises.has(workspacePromisesKey)) {
      return this.workspacePromises.get(workspacePromisesKey);
    }

    const promise = envName ? this.remoteWorkspaceAPI.startWorkspace({workspaceId: workspaceId, envName: envName}, {}).$promise : this.remoteWorkspaceAPI.startWorkspaceWithNoEnvironment({workspaceId: workspaceId}, {}).$promise;
    this.workspacePromises.set(workspacePromisesKey, promise);
    promise.finally(() => {
      this.workspacePromises.delete(workspacePromisesKey);
    });

    return promise;
  }

  /**
   * Notify user if workspace is ephemeral.
   *
   * @param workspaceId
   */
  notifyIfEphemeral(workspaceId: string): void {
    let workspace = this.workspacesById.get(workspaceId);
    let isEphemeral = workspace && workspace.devfile && workspace.devfile.attributes && workspace.devfile.attributes.persistVolumes ? !JSON.parse(workspace.devfile.attributes.persistVolumes) : false;
    if (isEphemeral) {
      this.cheNotification.showWarning('Your are starting an ephemeral workspace. All changes to the source code will be lost when the workspace is stopped unless they are pushed to a source code repository.');
    }
  }

  /**
   * Starts a temporary workspace by specifying configuration
   * @param workspaceConfig {che.IWorkspaceDevfile}
   * @returns {ng.IPromise<any>} promise
   */
  startTemporaryWorkspace(workspaceConfig: che.IWorkspaceDevfile): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.startTemporaryWorkspace({}, workspaceConfig).$promise;
  }

  /**
   * Stop workspace
   * @param workspaceId {string}
   * @returns {ng.IPromise<any>} promise
   */
  stopWorkspace(workspaceId: string): ng.IPromise<any> {
    const workspacePromisesKey = 'stopWorkspace' + workspaceId;
    if (this.workspacePromises.has(workspacePromisesKey)) {
      return this.workspacePromises.get(workspacePromisesKey);
    }
    const promise = this.remoteWorkspaceAPI.stopWorkspace({
      workspaceId: workspaceId
    }, {}).$promise;
    this.workspacePromises.set(workspacePromisesKey, promise);
    promise.finally(() => {
      this.workspacePromises.delete(workspacePromisesKey);
    });

    return promise;
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
      this.updateWorkspacesList(data);
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
  deleteWorkspace(workspaceId: string): ng.IPromise<any> {
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
      const projects = this.workspaceDataManager.getProjects(workspace);
      projects.forEach((project: che.IProject) => {
        project.workspaceId = workspace.id;
        project.workspaceName = this.workspaceDataManager.getName(workspace);
      });

      workspaceProjects[workspace.id] = projects;
    });

    return workspaceProjects;
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

  getWorkspaceLoaderUrl(namespace: string, workspaceName: string): string {
    return this.workspaceLoaderUrl ? this.workspaceLoaderUrl + namespace + '/' + workspaceName : null;
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
    const workspace = this.getWorkspaceById(workspaceId);
    if (workspace && workspace.status === status) {
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
      this.subscribedWorkspacesIds.push(workspaceId);
      this.cheJsonRpcMasterApi.subscribeWorkspaceStatus(workspaceId, (message: any) => {
        let status = message.error ? 'ERROR' : message.status;

        if (this.workspaceStatuses.indexOf(status) >= 0) {
          this.getWorkspaceById(workspaceId).status = status;
        }

        if (!this.statusDefers[workspaceId] || !this.statusDefers[workspaceId][status]) {
          return;
        }

        this.statusDefers[workspaceId][status].forEach((defer: any) => {
          defer.resolve(message);
        });

        this.statusDefers[workspaceId][status].length = 0;
      });
    }
  }

  /**
   * Fetches the system settings for workspaces.
   *
   * @returns {IPromise<any>}
   */
  fetchWorkspaceSettings(): ng.IPromise<any> {
    const promise = this.remoteWorkspaceAPI.getSettings().$promise;
    return promise.then((settings: che.IWorkspaceSettings) => {
      this.workspaceSettings = settings;
      return this.workspaceSettings;
    }, (error: any) => {
      if (error.status === 304) {
        return this.workspaceSettings;
      }
      return this.$q.reject(error);
    });
  }
  /**
   * Returns the system settings for workspaces.
   *
   * @returns {any} the system settings for workspaces
   */
  getWorkspaceSettings(): che.IWorkspaceSettings {
    return this.workspaceSettings;
  }

  getJsonRpcApiLocation(): string {
    return this.jsonRpcApiLocation;
  }

  getWorkspaceDataManager(): WorkspaceDataManager {
    return this.workspaceDataManager;
  }

  private updateWorkspacesList(workspace: che.IWorkspace): void {
    if (workspace.temporary) {
      this.workspacesById.set(workspace.id, workspace);
      return;
    }

    const workspaceDetails = this.getWorkspaceById(workspace.id);

    if (!workspaceDetails) {
      this.workspacesById.set(workspace.id, workspace);
    }
    if (workspaceDetails && WorkspaceStatus[workspaceDetails.status] === WorkspaceStatus.RUNNING && workspaceDetails.runtime && !workspace.runtime) {
      workspace.runtime = angular.copy(workspaceDetails.runtime);
    }
    this.lodash.remove(this.workspaces, (_workspace: che.IWorkspace) => {
      return _workspace.id === workspace.id;
    });
    this.workspaces.push(workspace);
    // publish change
    if (this.observables.has(workspace.id)) {
      this.observables.get(workspace.id).publish(workspace);
    }
    if (!angular.equals(workspaceDetails, workspace)) {
      this.fetchWorkspaceDetails(workspace.id);
      return;
    }

    this.startUpdateWorkspaceStatus(workspace.id);

    const controlStatuses = [WorkspaceStatus.RUNNING, WorkspaceStatus.STOPPED];
    controlStatuses.forEach((statusIndex: number) => {
      if (workspace.status !== WorkspaceStatus[statusIndex]) {
        this.fetchStatusChange(workspace.id, WorkspaceStatus[statusIndex]).then(() => {
          return this.fetchWorkspaceDetails(workspace.id);
        });
      }
    });
  }

  private formJsonRpcApiLocation($location: ng.ILocationService, proxySettings: string, devmode: boolean): string {
    let wsUrl;

    if (devmode) {
      // it handle then http and https
      wsUrl = proxySettings.replace('http', 'ws');
    } else {
      let wsProtocol;
      wsProtocol = 'http' === $location.protocol() ? 'ws' : 'wss';
      wsUrl = wsProtocol + '://' + $location.host() + ':' + $location.port();
    }
    return wsUrl;
  }

  private checkWorkspaceLoader(devmode: boolean, proxySettings: string): void {
    let url = '/workspace-loader/';

    let promise = this.$http.get(url);
    promise.then((response: {data: any}) => {
      this.workspaceLoaderUrl = devmode ? proxySettings + url : url;
    }, (error: any) => {
      if (error.status !== 304) {
        this.workspaceLoaderUrl = null;
      }
    });
  }
}
