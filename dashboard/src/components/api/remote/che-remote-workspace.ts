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

interface IRemoteWorkspaceResource<T> extends ng.resource.IResourceClass<T> {
  create: any;
  startWorkspace: any;
  getMachineToken: any;
  getDetails: any;
}

/**
 * This class is handling the call to remote API
 * @author Florent Benoit
 */
export class CheRemoteWorkspace {
  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private remoteWorkspaceAPI: IRemoteWorkspaceResource<any>;
  private cheJsonRpcMasterApi: CheJsonRpcMasterApi;
  private authData: any;
  private cheJsonRpcApi: CheJsonRpcApi;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, cheJsonRpcApi: CheJsonRpcApi, authData: any) {
    this.$resource = $resource;
    this.$q = $q;
    this.cheJsonRpcApi = cheJsonRpcApi;
    this.authData = authData;

    // remote call
    this.remoteWorkspaceAPI = <IRemoteWorkspaceResource<any>>this.$resource('', {}, {
        getDetails: {method: 'GET', url: authData.url + '/api/workspace/:workspaceId?token=' + authData.token},
        getMachineToken: {method: 'GET', url: authData.url + '/api/machine/token/:workspaceId?token=' + authData.token},
        create: {method: 'POST', url: authData.url + '/api/workspace?token=' + authData.token},
        startWorkspace: {method: 'POST', url : authData.url + '/api/workspace/:workspaceId/runtime?environment=:envName&token=' + authData.token}
      }
    );
  }

  createWorkspaceFromConfig(workspaceConfig: any): ng.IPromise<any> {
    return this.remoteWorkspaceAPI.create(workspaceConfig).$promise;
  }

  /**
   * Provides machine token for given workspace
   * @param workspaceId the ID of the workspace
   * @returns {*}
   */
  getMachineToken(workspaceId: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let deferredPromise = deferred.promise;

    let promise = this.remoteWorkspaceAPI.getMachineToken({workspaceId: workspaceId}, {}).$promise;
    promise.then((workspace: any) => {
      deferred.resolve(workspace);
    }, (error: any) => {
      deferred.reject(error);
    });

    return deferredPromise;
  }

  /**
   * Starts the given workspace by specifying the ID and the environment name
   * @param workspaceId the workspace ID
   * @param envName the name of the environment
   * @returns {*} promise
   */
  startWorkspace(remoteWsURL: string, workspaceId: string, envName: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let deferredPromise = deferred.promise;
    this.cheJsonRpcMasterApi = this.cheJsonRpcApi.getJsonRpcMasterApi(remoteWsURL);
    this.cheJsonRpcMasterApi.subscribeWorkspaceStatus(workspaceId, (message: any) => {
      if (message.status === 'RUNNING' && message.workspaceId === workspaceId) {
        let promise = this.remoteWorkspaceAPI.getDetails({workspaceId: workspaceId}, {}).$promise;
        promise.then((workspace: any) => {
          deferred.resolve(workspace);
        }, (error: any) => {
          deferred.reject(error);
        });
      }
    });

    this.remoteWorkspaceAPI.startWorkspace({workspaceId: workspaceId, envName : envName}, {}).$promise;

    return deferredPromise;
  }
}
