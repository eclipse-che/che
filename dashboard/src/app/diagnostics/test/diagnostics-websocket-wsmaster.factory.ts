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
import {CheJsonRpcApi} from '../../../components/api/json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcMasterApi} from '../../../components/api/json-rpc/che-json-rpc-master-api';
import {CheAPI} from '../../../components/api/che-api.factory';

/**
 * Test for launching websocket connection to the workspace master
 * @author Florent Benoit
 */
export class DiagnosticsWebsocketWsMaster {

  static $inject = ['cheAPI', 'cheJsonRpcApi', '$timeout'];

  private jsonRpcMasterApi: CheJsonRpcMasterApi;
  private wsMasterLocation: string;

  /**
   * Timeout handling.
   */
  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor
   */
  constructor(cheAPI: CheAPI, cheJsonRpcApi: CheJsonRpcApi, $timeout: ng.ITimeoutService) {
    this.wsMasterLocation = cheAPI.getWorkspace().getJsonRpcApiLocation();
    this.jsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(this.wsMasterLocation);
    this.$timeout = $timeout;
  }

  /**
   * Start the diagnostic and report all progress through the callback
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {ng.IPromise<any>} when test is finished
   */
  start(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {
   try {
      // define callback
      let callback = (message: any) => {
        diagnosticCallback.success('Websocket message received');
      };

     this.jsonRpcMasterApi.connect(this.wsMasterLocation).then(() => {
        this.jsonRpcMasterApi.fetchClientId().then(callback);
       // default fallback if no answer in 5 seconds
       diagnosticCallback.delayError('No reply of websocket test after 5 seconds. Websocket is failing to connect to ' + this.wsMasterLocation, 5000);
     });
    } catch (error) {
      diagnosticCallback.error('Unable to connect with websocket to ' + this.wsMasterLocation + ': ' + error);
    }
    return diagnosticCallback.getPromise();
  }

}
