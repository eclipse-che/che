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
import {DiagnosticCallback} from '../diagnostic-callback';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import {CheWebsocket, MessageBus} from '../../../components/api/che-websocket.factory';
import {CheBranding} from '../../../components/branding/che-branding.factory';

/**
 * Ability to tests a running workspace.
 * @author Florent Benoit
 */
export class DiagnosticsRunningWorkspaceCheck {

  /**
   * Q service for creating delayed promises.
   */
  private $q : ng.IQService;

  /**
   * Workspace API used to grab details.
   */
  private cheWorkspace;

  /**
   * Lodash utility.
   */
  private lodash : any;

  /**
   * Resource service used in tests.
   */
  private $resource : ng.resource.IResourceService;

  /**
   * Location service used to get data from browser URL.
   */
  private $location : ng.ILocationService;

  /**
   * Websocket handling.
   */
  private cheWebsocket : CheWebsocket;

  /**
   * Branding info.
   */
  private cheBranding : CheBranding;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor ($q : ng.IQService, lodash : any, cheWebsocket : CheWebsocket, cheWorkspace: CheWorkspace,
               $resource : ng.resource.IResourceService, $location : ng.ILocationService, cheBranding : CheBranding) {
    this.$q =$q;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.cheWebsocket = cheWebsocket;
    this.cheBranding = cheBranding;
    this.$resource = $resource;
    this.$location = $location;

  }

  /**
   * Check WS Agent by using the browser host
   * @param diagnosticCallback
   * @returns {ng.IPromise}
   */
  checkAgentWithBrowserHost(diagnosticCallback : DiagnosticCallback) : ng.IPromise {

    let wsAgentHRef = this.getWsAgentURL(diagnosticCallback);
    let parser = document.createElement('a');
    parser.href = wsAgentHRef;
    wsAgentHRef = parser.protocol + '//' + this.$location.host() + ':' + parser.port + parser.pathname;

    let promise : ng.IPromise = this.callSCM(diagnosticCallback, wsAgentHRef, false);
    promise.then(() => {
      let hint: string;
      if (this.cheBranding.getName() === 'Eclipse Che') {
        hint = 'CHE_DOCKER_IP_EXTERNAL property could be used or '
      }
      diagnosticCallback.notifyHint(this.cheBranding.getCLI().name + '_HOST value in `' + this.cheBranding.getCLI().configName + '`  file should use the hostname ' + this.$location.host() + ' instead of ' + parser.hostname);
    });
    return diagnosticCallback.getPromise();
  }

  /**
   * Utility method used to get Workspace Agent URL from a callback shared data
   * @param diagnosticCallback
   */
  private getWsAgentURL(diagnosticCallback : DiagnosticCallback) : string {
    let workspace : che.IWorkspace = diagnosticCallback.getShared('workspace');

    let errMessage : string = 'Workspace has no runtime: unable to test workspace not started';
    let runtime : any = workspace.runtime;
    if (!runtime) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }
    let devMachine = runtime.devMachine;
    if (!devMachine) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }
    let servers : any = devMachine.runtime.servers;
    if (!servers) {
      diagnosticCallback.error(errMessage);
      throw errMessage;
    }
    let wsAgentServer = this.lodash.find(servers, (server: any) => {
      return server.ref === 'wsagent';
    });


    return wsAgentServer.url;
  }


  /**
   * Check the Workspace Agent by calling REST API.
   * @param diagnosticCallback
   * @returns {ng.IPromise}
   */
  checkWsAgent(diagnosticCallback : DiagnosticCallback, errorInsteadOfFailure : boolean) : ng.IPromise {
    let wsAgentHRef = this.getWsAgentURL(diagnosticCallback);
    let promise = this.callSCM(diagnosticCallback, wsAgentHRef, errorInsteadOfFailure);
    promise.catch((error) => {
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
   * @param diagnosticCallback
   * @returns {IPromise} when test is finished
   */
  checkWebSocketWsAgent(diagnosticCallback : DiagnosticCallback) : ng.IPromise {
    let workspace: che.IWorkspace = diagnosticCallback.getShared('workspace');

    let wsAgentSocketWebLink = this.lodash.find(workspace.runtime.links, (link: any) => {
      return link.rel === 'wsagent.websocket';
    });
    if (!wsAgentSocketWebLink) {
      wsAgentSocketWebLink = this.getWsAgentURL(diagnosticCallback).replace('http', 'ws') + '/ws';
    } else {
      wsAgentSocketWebLink = wsAgentSocketWebLink.href;
    }
    let wsAgentRemoteBus : MessageBus = this.cheWebsocket.getRemoteBus(wsAgentSocketWebLink);
    diagnosticCallback.setMessageBus(wsAgentRemoteBus);

    try {
      // define callback
      let callback = (message: any) => {
        if (!message) {
          diagnosticCallback.getMessageBus().unsubscribe('pong');
          diagnosticCallback.success('Websocket Agent Message received');
          wsAgentRemoteBus.datastream.close(true);
        }
      };

      // subscribe to the event
      diagnosticCallback.subscribeChannel('pong', callback);

      // default fallback if no answer in 5 seconds
      diagnosticCallback.delayError('No reply of websocket test after 5 seconds. Websocket is failing to connect to ' + wsAgentSocketWebLink, 5000);

      // send the message
      diagnosticCallback.getMessageBus().ping();

    } catch (error : any) {
      diagnosticCallback.error('Unable to connect with websocket to ' + wsAgentSocketWebLink + ': ' + error);
    }
    return diagnosticCallback.getPromise();
  }

  /**
   * Get data on API and retrieve SCM revision.
   * @param diagnosticCallback
   * @param wsAgentHRef
   * @returns {Promise}
   */
  callSCM(diagnosticCallback : DiagnosticCallback, wsAgentHRef : string, errorInsteadOfFailure : boolean) : ng.IPromise {
    // connect to the workspace agent
    let resourceAPI : any = this.$resource(wsAgentHRef + '/', {}, {
      getDetails: {method: 'OPTIONS', timeout : 15000}
    }, {
      stripTrailingSlashes: false
    });

    return resourceAPI.getDetails().$promise.then((data) => {
      diagnosticCallback.success(wsAgentHRef + '. Got SCM revision ' + angular.fromJson(data).scmRevision);
    }).catch((error) => {
      let errorMessage : string = 'Unable to perform call on ' + wsAgentHRef + ': Status ' + error.status + ', statusText:' + error.statusText + '/' + error.data;
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

}
