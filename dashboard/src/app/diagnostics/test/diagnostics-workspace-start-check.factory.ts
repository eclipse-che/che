/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {DiagnosticCallback} from '../diagnostic-callback';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {DiagnosticsRunningWorkspaceCheck} from './diagnostics-workspace-check-workspace.factory';
import {CheBranding} from '../../../components/branding/che-branding.factory';
import {CheJsonRpcApi} from '../../../components/api/json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcMasterApi} from '../../../components/api/json-rpc/che-json-rpc-master-api';

/**
 * Test the start of a workspace
 * @author Florent Benoit
 */
export class DiagnosticsWorkspaceStartCheck {

  /**
   * Q service for creating delayed promises.
   */
  private $q: ng.IQService;

  /**
   * Workspace API used to grab details.
   */
  private cheWorkspace;

  /**
   * Lodash utility.
   */
  private lodash: any;

  /**
   * Other checker used to spawn new tests.
   */
  private diagnosticsRunningWorkspaceCheck: DiagnosticsRunningWorkspaceCheck;

  /**
   * Keep a reference to the workspace Agent callback
   */
  private wsAgentCallback: DiagnosticCallback;

  /**
   * Keep a reference to the workspace callback
   */
  private workspaceCallback: DiagnosticCallback;

  /**
   * Keep a reference to the machine callback
   */
  private machineCallback: DiagnosticCallback;

  /**
   * Keep a reference to the exec agent callback
   */
  private execAgentCallback: DiagnosticCallback;

  /**
   * Branding info.
   */
  private cheBranding: CheBranding;

  /**
   * Location service.
   */
  private $location: ng.ILocationService;

  /**
   * RPC API location string.
   */
  private jsonRpcApiLocation: string;

  /**
   * Workspace master interceptions.
   */
  private cheJsonRpcMasterApi: CheJsonRpcMasterApi;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService,
              lodash: any,
              cheWorkspace: CheWorkspace,
              diagnosticsRunningWorkspaceCheck: DiagnosticsRunningWorkspaceCheck,
              cheBranding: CheBranding,
              $location: ng.ILocationService,
              cheJsonRpcApi: CheJsonRpcApi,
              userDashboardConfig: any,
              keycloakAuth: any,
              proxySettings: string) {
    this.$q = $q;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.cheBranding = cheBranding;
    this.$location = $location;
    this.diagnosticsRunningWorkspaceCheck = diagnosticsRunningWorkspaceCheck;

    const keycloakToken = keycloakAuth.isPresent ? '?token=' + keycloakAuth.keycloak.token : '';
    this.jsonRpcApiLocation = this.cheWorkspace.formJsonRpcApiLocation($location, proxySettings, userDashboardConfig.developmentMode) + cheBranding.getWebsocketContext();
    this.jsonRpcApiLocation += keycloakToken;
    this.cheJsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(this.jsonRpcApiLocation);
  }

  /**
   * Delete the diagnostic workspace (by stopping it first) if it's already running
   * @param {DiagnosticCallback} diagnosticCallback the callback used to send response
   * @returns {ng.IPromise<any>}
   */
  deleteDiagnosticWorkspaceIfPresent(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {
    let defered = this.$q.defer();
    this.cheWorkspace.fetchWorkspaces().finally(() => {
      let workspaces: Array<che.IWorkspace> = this.cheWorkspace.getWorkspaces();
      let workspace: any = this.lodash.find(workspaces, (workspace: che.IWorkspace) => {
        return workspace.config.name === 'diagnostics';
      });
      // need to delete it
      if (workspace) {
        // first stop
        if (workspace.status === 'RUNNING' || workspace.status === 'STARTING') {
          // listen on the events
          const callback = (message: any) => {
            if (message.status === 'STOPPED') {
              this.cheWorkspace.deleteWorkspaceConfig(workspace.id).finally(() => {
                defered.resolve(true);
              });
            } else if ('ERROR' === message.status) {
              defered.reject(message.content);
            }
          };
          this.cheJsonRpcMasterApi.subscribeWorkspaceStatus(workspace.id, callback);
          defered.promise.then((wsIsStopped: boolean) => {
            if (wsIsStopped) {
              this.cheJsonRpcMasterApi.unSubscribeWorkspaceStatus(workspace.id, callback);
            }
          });
          this.cheWorkspace.stopWorkspace(workspace.id);
        } else {
          this.cheWorkspace.deleteWorkspaceConfig(workspace.id).finally(() => {
            defered.resolve(true);
          });
        }
      } else {
        defered.resolve(true);
      }
    }).catch((error: any) => {
      defered.reject(error);
    });
    return defered.promise;
  }

  /**
   * Always create a fresh workspace (by removing the old one if it exists)
   * @param {DiagnosticCallback} diagnosticCallback
   * @returns {ng.IPromise<che.IWorkspace>}
   */
  recreateDiagnosticWorkspace(diagnosticCallback: DiagnosticCallback): ng.IPromise<che.IWorkspace> {
    let defered = this.$q.defer();

    // delete if present
    this.deleteDiagnosticWorkspaceIfPresent(diagnosticCallback).then(() => {
      // now create workspace config
      let workspaceConfig: che.IWorkspaceConfig = {
        'projects': [],
        'environments': {
          'diagnostics': {
            'machines': {
              'dev-machine': {
                'installers': ['org.eclipse.che.ws-agent'],
                'servers': {},
                'attributes': {'memoryLimitBytes': '1147483648'}
              }
            },
            'recipe': {
              'content': 'FROM openjdk:8-jre-alpine\nCMD tail -f /dev/null\n',
              'contentType': 'text/x-dockerfile',
              'type': 'dockerfile'
            }
          }
        },
        'name': 'diagnostics',
        'defaultEnv': 'diagnostics',
        'commands': []
      };
      return this.cheWorkspace.createWorkspaceFromConfig(null, workspaceConfig);
    }).then((workspace: che.IWorkspace) => {
      defered.resolve(workspace);
    }).catch((error: any) => {
        defered.reject(error);
      }
    );
    return defered.promise;
  }

  /**
   * Starts the test by adding new callbacks after this one
   * @param {DiagnosticCallback} diagnosticCallback the original check
   * @returns {ng.IPromise<any>}
   */
  start(diagnosticCallback: DiagnosticCallback): ng.IPromise<any> {
    this.workspaceCallback = diagnosticCallback.newCallback('Workspace State');
    this.wsAgentCallback = diagnosticCallback.newCallback('Workspace Agent State');
    this.machineCallback = diagnosticCallback.newCallback('Workspace Runtime State');
    this.execAgentCallback = diagnosticCallback.newCallback('Workspace Exec Agent State');

    let workspaceIsStarted: boolean = false;
    this.recreateDiagnosticWorkspace(diagnosticCallback).then((workspace: che.IWorkspace) => {

      diagnosticCallback.shared('workspace', workspace);
      this.cheJsonRpcMasterApi.subscribeWorkspaceStatus(workspace.id, (message: any) => {
        diagnosticCallback.addContent('EventChannel : ' + JSON.stringify(message));
        if (message.status === 'RUNNING') {
          workspaceIsStarted = true;
          this.workspaceCallback.stateRunning('RUNNING');
          this.cheWorkspace.fetchWorkspaces().then(() => {
            let workspace = diagnosticCallback.getShared('workspace');
            let workspaceId = workspace.id;
            this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
              let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
              diagnosticCallback.shared('workspace', workspace);
              diagnosticCallback.shared('machineToken', workspace.runtime.machineToken);
              diagnosticCallback.success('Starting workspace OK');
            });
          });
        }
      });

      this.cheJsonRpcMasterApi.subscribeEnvironmentStatus(workspace.id, (message: any) => {
        if (message.eventType === 'DESTROYED' && message.identity.workspaceId === workspace.id) {
          diagnosticCallback.error('Error while starting the workspace : Workspace has been destroyed', 'Please check the diagnostic logs.');
        }
        if (message.eventType === 'ERROR' && message.identity.workspaceId === workspace.id) {
          diagnosticCallback.error('Error while starting the workspace : ' + JSON.stringify(message));
        }

        if (message.eventType === 'RUNNING' && message.identity.workspaceId === workspace.id && message.machineName === 'dev-machine') {
          this.machineCallback.stateRunning('RUNNING');
        }

        diagnosticCallback.addContent('StatusChannel : ' + JSON.stringify(message));

      });

      this.cheJsonRpcMasterApi.subscribeWsAgentOutput(workspace.id, (message: any) => {
        diagnosticCallback.addContent('agent channel :' + message);

        if (message.text.indexOf(' Server startup') > 0) {
          this.wsAgentCallback.stateRunning('RUNNING');

          // server has been startup in the workspace agent and tries to reach workspace agent but is unable to do it
          // try with the browser ip
          diagnosticCallback.delayFunction(() => {

            let hint: string = 'The workspace agent has started in the workspace, but the ' + this.cheBranding.getName() + ' server cannot verify this. There is a failure for ' + this.cheBranding.getName() + ' server to connect to your workspace\'s agent. Either your firewall is blocking essential ports or you can change ';
            if (this.cheBranding.getName() === 'Eclipse Che') {
              hint += 'CHE_DOCKER_IP and DOCKER_HOST to values ';
            }
            hint += 'specific to your environment. See the `' + this.cheBranding.getCLI().configName + '` file for specifics.';
            diagnosticCallback.notifyFailure('The workspace started, but ' + this.cheBranding.getName() + ' <--> Workspace connection not established', hint);
            let workspaceId = workspace.id;
            this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
              let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
              diagnosticCallback.shared('workspace', workspace);
              diagnosticCallback.shared('machineToken', workspace.runtime.machineToken);
              let newCallback: DiagnosticCallback = diagnosticCallback.newCallback('Test connection from browser to workspace agent by using Workspace Agent IP');
              this.diagnosticsRunningWorkspaceCheck.checkWsAgent(newCallback, false);
              let websocketCallback: DiagnosticCallback = diagnosticCallback.newCallback('Test connection from browser to workspace agent with websocket');
              this.diagnosticsRunningWorkspaceCheck.checkWebSocketWsAgent(websocketCallback);
            });

          }, 7000);
        }

        diagnosticCallback.addContent(message);
      });

      this.cheJsonRpcMasterApi.subscribeWsAgentOutput(workspace.id, (message: any) => {
        const content = message.text;
        diagnosticCallback.addContent(content);

        // check if connected (always pull)
        if (content.indexOf('Client.Timeout exceeded while awaiting headers') > 0) {
          diagnosticCallback.error('Network connection issue', 'Docker was unable to pull the right Docker image for the workspace. Either networking is not working from your Docker daemon or try disabling CHE_DOCKER_ALWAYSE__PULL__IMAGE in `che.env` to avoid pulling images over the network.');
        }

        if (content.indexOf('dial tcp: lookup') > 0 && content.indexOf('server misbehaving') > 0) {
          diagnosticCallback.error('Network connection issue', 'Docker is trying to connect to a Docker registry but the connection is failing. Check Docker\'s DNS settings and network connectivity.');
        }

        if (content.indexOf('Exec-agent configuration') > 0) {
          this.execAgentCallback.stateRunning('RUNNING');
        }
        diagnosticCallback.addContent('output channel message :' + JSON.stringify(message));
      });

      diagnosticCallback.delayError('Test limit is for up to 5minutes. Time has exceed.', 5 * 60 * 1000);

      let startWorkspacePromise = this.cheWorkspace.startWorkspace(workspace.id, workspace.config.defaultEnv);
      startWorkspacePromise.then((workspaceData: che.IWorkspace) => {
        diagnosticCallback.shared('workspace', workspaceData);
      });

    }).catch((error: any) => {
      diagnosticCallback.error('Unable to start workspace: ' + error);
    });

    return diagnosticCallback.getPromise();
  }

}
