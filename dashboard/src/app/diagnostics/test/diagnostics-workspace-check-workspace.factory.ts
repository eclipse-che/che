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
import {DiagnosticCallback} from '../diagnostic-callback';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {CheBranding} from '../../../components/branding/che-branding.factory';
import {CheJsonRpcApi} from '../../../components/api/json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcWsagentApi} from '../../../components/api/json-rpc/che-json-rpc-wsagent-api';

/**
 * Ability to tests a running workspace.
 * @author Florent Benoit
 */
export class DiagnosticsRunningWorkspaceCheck {

  static $inject = ['$q', 'lodash', 'cheWorkspace', 'cheJsonRpcApi', '$resource', '$location', 'cheBranding'];

  /**
   * Q service for creating delayed promises.
   */
  private $q: ng.IQService;

  /**
   * Workspace API used to grab details.
   */
  private cheWorkspace;

  /**
   * JSON RPC API factory.
   */
  private cheJsonRpcApi: CheJsonRpcApi;

  /**
   * Lodash utility.
   */
  private lodash: any;

  /**
   * Resource service used in tests.
   */
  private $resource: ng.resource.IResourceService;

  /**
   * Location service used to get data from browser URL.
   */
  private $location: ng.ILocationService;

  /**
   * Branding info.
   */
  private cheBranding: CheBranding;

  /**
   * Default constructor
   */
  constructor($q: ng.IQService, lodash: any, cheWorkspace: CheWorkspace, cheJsonRpcApi: CheJsonRpcApi,
              $resource: ng.resource.IResourceService, $location: ng.ILocationService, cheBranding: CheBranding) {
    this.$q = $q;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.cheJsonRpcApi = cheJsonRpcApi;
    this.cheBranding = cheBranding;
    this.$resource = $resource;
    this.$location = $location;
  }

  /**
   * Check WS Agent by using the browser host
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {ng.IPromise<any>}
   */
  checkAgentWithBrowserHost(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {

    let wsAgentHRef = this.getWsAgentURL(diagnosticCallback);
    let parser = document.createElement('a');
    parser.href = wsAgentHRef;
    wsAgentHRef = parser.protocol + '//' + this.$location.host() + ':' + parser.port + parser.pathname;

    let promise: ng.IPromise<any> = this.callSCM(diagnosticCallback, wsAgentHRef, false);
    promise.then(() => {
      let hint: string;
      if (this.cheBranding.getName() === 'Eclipse Che') {
        hint = 'CHE_DOCKER_IP_EXTERNAL property could be used or ';
      }
      diagnosticCallback.notifyHint(this.cheBranding.getCLI().name + '_HOST value in `' + this.cheBranding.getCLI().configName + '`  file should use the hostname ' + this.$location.host() + ' instead of ' + parser.hostname);
    });
    return diagnosticCallback.getPromise();
  }

  /**
   * Check the Workspace Agent by calling REST API.
   * @param {DiagnosticCallback} diagnosticCallback
   * @param {boolean} errorInsteadOfFailure
   * @returns {ng.IPromise<any>}
   */
  checkWsAgent(diagnosticCallback: DiagnosticCallback, errorInsteadOfFailure: boolean): ng.IPromise<any> {
    let wsAgentHRef = this.getWsAgentURL(diagnosticCallback);
    let promise = this.callSCM(diagnosticCallback, wsAgentHRef, errorInsteadOfFailure);
    promise.catch((error: any) => {
      // try with browser host if different location
      let parser = document.createElement('a');
      parser.href = wsAgentHRef;

      if (parser.hostname !== this.$location.host()) {
        this.checkAgentWithBrowserHost(diagnosticCallback.newCallback('Try WsAgent on browser host'));
      }
    });

    return diagnosticCallback.getPromise();
  }

  /**
   * Start the diagnostic and report all progress through the callback
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {ng.IPromise<any>} when test is finished
   */
  checkWebSocketWsAgent(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {
    let machineToken: string = diagnosticCallback.getShared('machineToken');
    let clientId: string = diagnosticCallback.getShared('clientId');

    let wsAgentSocketWebLink = this.getWsAgentWebsocket(diagnosticCallback);
    if (machineToken) {
      wsAgentSocketWebLink += '?token=' + machineToken;
    }

    let callback = (message: any) => {
      diagnosticCallback.success('Websocket Agent Message received');
    };

    let cheJsonRpcWsagentApi:  CheJsonRpcWsagentApi = this.cheJsonRpcApi.getJsonRpcWsagentApi(wsAgentSocketWebLink);
    try {
      cheJsonRpcWsagentApi.connect(wsAgentSocketWebLink, clientId).then(callback);
      // default fallback if no answer in 5 seconds
      diagnosticCallback.delayError('No reply of websocket test after 5 seconds. Websocket is failing to connect to ' + wsAgentSocketWebLink, 15000);

    } catch (error) {
      diagnosticCallback.error('Unable to connect with websocket to ' + wsAgentSocketWebLink + ': ' + error);
    }
    return diagnosticCallback.getPromise();
  }

  /**
   * Get data on API and retrieve SCM revision.
   * @param {DiagnosticCallback} diagnosticCallback
   * @param {string} wsAgentHRef
   * @param {boolean} errorInsteadOfFailure
   * @returns {Promise}
   */
  callSCM(diagnosticCallback: DiagnosticCallback, wsAgentHRef: string, errorInsteadOfFailure: boolean): ng.IPromise<any> {
    let uriWsAgent: string = wsAgentHRef + '/';
    let machineToken: string = diagnosticCallback.getShared('machineToken');
    if (machineToken) {
      uriWsAgent += '?token=' + machineToken;
    }

    // connect to the workspace agent
    let resourceAPI: any = this.$resource(uriWsAgent, {}, {
      getDetails: {method: 'OPTIONS', timeout: 15000}
    }, {
      stripTrailingSlashes: false
    });

    return resourceAPI.getDetails().$promise.then((data: any) => {
      diagnosticCallback.success(wsAgentHRef + '. Got SCM revision ' + angular.fromJson(data).scmRevision);
    }).catch((error: any) => {
      let errorMessage: string = 'Unable to perform call on ' + wsAgentHRef + ': Status ' + error.status + ', statusText:' + error.statusText + '/' + error.data;
      if (errorInsteadOfFailure) {
        if (this.cheBranding.getName() === 'Eclipse Che') {
          diagnosticCallback.error(errorMessage, 'Workspace Agent is running but browser is unable to connect to it. Please check CHE_HOST and CHE_DOCKER_IP_EXTERNAL in che.env and the firewall settings.');
        } else {
          diagnosticCallback.error(errorMessage, 'Workspace Agent is running but unable to connect. Please check HOST defined in the env file and the firewall settings.');
        }
      } else {
        diagnosticCallback.notifyFailure(errorMessage);
      }
      throw error;
    });
  }

  /**
   * Utility method used to get Workspace Agent URL from a callback shared data
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {string}
   */
  getWsAgentURL(diagnosticCallback: DiagnosticCallback): string {
    let workspace: che.IWorkspace = diagnosticCallback.getShared('workspace');

    let errMessage: string = 'Workspace has no runtime: unable to test workspace not started';
    let runtime: any = workspace.runtime;
    if (!runtime) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }

    const devMachine = Object.keys(runtime.machines).map((machineName: string) => {
      return runtime.machines[machineName];
    }).find((machine: any) => {
      return Object.keys(machine.servers).some((serverName: string) => {
        return serverName === 'wsagent/http';
      });
    });

    if (!devMachine) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }

    return devMachine.servers['wsagent/http'].url;
  }

  /**
   * Utility method used to get Workspace Agent websocket endpoint from a callback shared data
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {string}
   */
  getWsAgentWebsocket(diagnosticCallback: DiagnosticCallback): string {
    let workspace: che.IWorkspace = diagnosticCallback.getShared('workspace');

    let errMessage: string = 'Workspace has no runtime: unable to test workspace not started';
    let runtime: any = workspace.runtime;
    if (!runtime) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }

    const devMachine = Object.keys(runtime.machines).map((machineName: string) => {
      return runtime.machines[machineName];
    }).find((machine: any) => {
      return Object.keys(machine.servers).some((serverName: string) => {
        return serverName === 'wsagent/ws';
      });
    });

    if (!devMachine) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }

    return devMachine.servers['wsagent/ws'].url;
  }

}
