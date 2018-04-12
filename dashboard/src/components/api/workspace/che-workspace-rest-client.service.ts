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

import WorkspaceClient, {
  IBackend,
  IRemoteAPI,
  IRequestError,
  IResourceCreateQueryParams as IWorkspaceCreateQueryParams
} from '@eclipse-che/workspace-client';

export {IBackend, IRemoteAPI, IRequestError, IWorkspaceCreateQueryParams};

export class CheWorkspaceRestClientService {
  static $inject = ['$q', '$rootScope', 'keycloakAuth'];

  private $q: ng.IQService;
  private $rootScope: ng.IRootScopeService;

  private _restApi: IRestAPIWrapper;
  private _backend: IBackend;

  constructor($q: ng.IQService,
              $rootScope: ng.IRootScopeService,
              keycloakAuth: any) {
    this.$q = $q;
    this.$rootScope = $rootScope;

    const headers: any = {};
    if (keycloakAuth && keycloakAuth.keycloak) {
      headers.Authorization = 'Bearer ' + keycloakAuth.keycloak.token;
    }
    const restApi = WorkspaceClient.getRestApi({headers});
    this._restApi = new RestAPIWrapper(this.$q, this.$rootScope, restApi);
    this._backend = WorkspaceClient.getRestBackend();
  }

  get restClient(): IRestAPIWrapper {
    return this._restApi;
  }

  get backend(): IBackend {
    return this._backend;
  }

}

export interface IRestAPIWrapper {
  getAll(): ng.IPromise<IRequestError | che.IWorkspace[]>;
  getAllByNamespace(namespace: string): ng.IPromise<IRequestError | che.IWorkspace[]>;
  getById(workspaceKey: string): ng.IPromise<IRequestError | che.IWorkspace>;
  create(config: che.IWorkspaceConfig, params: IWorkspaceCreateQueryParams): ng.IPromise<IRequestError | any>;
  update(workspaceId: string, workspace: che.IWorkspace): ng.IPromise<IRequestError | any>;
  delete(workspaceId: string): ng.IPromise<IRequestError | any>;
  start(workspaceId: string, environmentName: string): ng.IPromise<IRequestError | any>;
  startTemporary(config: che.IWorkspaceConfig): ng.IPromise<IRequestError | any>;
  stop(workspaceId: string): ng.IPromise<IRequestError | any>;
  stop(workspaceId: string): ng.IPromise<IRequestError | any>;
  getSettings(): ng.IPromise<IRequestError | che.IWorkspaceSettings>;
}

/**
 * This class wraps Workspace REST client methods with Angular Promise
 * to run $digest and update the UI.
 */
export class RestAPIWrapper implements IRestAPIWrapper {

  private $q: ng.IQService;
  private $rootScope: ng.IRootScopeService;

  private client: IRemoteAPI;

  constructor($q: ng.IQService,
              $rootScope: ng.IRootScopeService,
              client: IRemoteAPI) {
    this.$q = $q;
    this.$rootScope = $rootScope;
    this.client = client;
  }

  public create(config: che.IWorkspaceConfig, params: IWorkspaceCreateQueryParams): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.create(config, params));
  }

  public delete(workspaceId: string): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.delete(workspaceId));
  }

  public getAll(): ng.IPromise<IRequestError | che.IWorkspace[]> {
    return this.execute(this.client.getAll());
  }

  public getAllByNamespace(namespace: string): ng.IPromise<IRequestError | che.IWorkspace[]> {
    return this.execute(this.client.getAllByNamespace(namespace));
  }

  public getById(workspaceKey: string): ng.IPromise<IRequestError | che.IWorkspace> {
    return this.execute(this.client.getById(workspaceKey));
  }

  public getSettings(): ng.IPromise<IRequestError | che.IWorkspaceSettings> {
    return this.execute(this.client.getSettings());
  }

  public start(workspaceId: string, environmentName: string): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.start(workspaceId, environmentName));
  }

  public startTemporary(config: che.IWorkspaceConfig): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.startTemporary(config));
  }

  public stop(workspaceId: string): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.stop(workspaceId));
  }

  public update(workspaceId: string, workspace: che.IWorkspace): ng.IPromise<IRequestError | any> {
    return this.execute(this.client.update(workspaceId, workspace as any));
  }

  /**
   * Run $digest manually and update the UI
   *
   * @param {Promise<IRequestError | any>} promise
   * @returns {angular.IPromise<IRequestError | any>}
   */
  private execute(promise: Promise<IRequestError | any>): ng.IPromise<IRequestError | any> {
    return this.$q((resolve: (arg: any) => void, reject: (error: IRequestError) => void) => {
      promise.then((data: any) => {
        resolve(data);
        this.$rootScope.$apply();
      }, (error: IRequestError) => {
        reject(error);
        this.$rootScope.$apply();
      });
    });
  }

}
