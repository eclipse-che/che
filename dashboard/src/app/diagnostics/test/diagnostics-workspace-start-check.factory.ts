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
import {DiagnosticsRunningWorkspaceCheck} from './diagnostics-workspace-check-workspace.factory';
import {CheBranding} from '../../../components/branding/che-branding.factory';

/**
 * Test the start of a workspace
 * @author Florent Benoit
 */
export class DiagnosticsWorkspaceStartCheck {

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
   * Other checker used to spawn new tests.
   */
  private diagnosticsRunningWorkspaceCheck : DiagnosticsRunningWorkspaceCheck;

  /**
   * Keep a reference to the workspace Agent callback
   */
  private wsAgentCallback : DiagnosticCallback;

  /**
   * Keep a reference to the workspace callback
   */
  private workspaceCallback : DiagnosticCallback;

  /**
   * Keep a reference to the machine callback
   */
  private machineCallback : DiagnosticCallback;

  /**
   * Keep a reference to the exec agent callback
   */
  private execAgentCallback : DiagnosticCallback;

  /**
   * Branding info.
   */
  private cheBranding : CheBranding;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor ($q : ng.IQService, lodash : any, cheWorkspace: CheWorkspace, diagnosticsRunningWorkspaceCheck : DiagnosticsRunningWorkspaceCheck, cheBranding : CheBranding) {
    this.$q =$q;
    this.lodash = lodash;
    this.cheWorkspace = cheWorkspace;
    this.cheBranding = cheBranding;
    this.diagnosticsRunningWorkspaceCheck = diagnosticsRunningWorkspaceCheck;
  }

  /**
   * Delete the diagnostic workspace (by stopping it first) if it's already running
   * @param diagnosticCallback the callback used to send response
   * @returns {IPromise}
   */
  deleteDiagnosticWorkspaceIfPresent(diagnosticCallback : DiagnosticCallback) : ng.IPromise {
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
          let eventChannelLink = this.lodash.find(workspace.links, (link: any) => {
            return link.rel === 'get workspace events channel';
          });
          let eventChannel = eventChannelLink ? eventChannelLink.parameters[0].defaultValue : null;
          diagnosticCallback.subscribeChannel(eventChannel, (message: any) => {
            if (message.eventType === 'STOPPED') {
              diagnosticCallback.unsubscribeChannel(eventChannel);
              this.cheWorkspace.deleteWorkspaceConfig(workspace.id).finally(() => {
                defered.resolve(true);
              });
            } else if ('ERROR' === message.eventType) {
              defered.reject(message.content);
            }
          });
          this.cheWorkspace.stopWorkspace(workspace.id, false);
        } else {
          this.cheWorkspace.deleteWorkspaceConfig(workspace.id).finally(() => {
            defered.resolve(true);
          });
        }
      } else {
        defered.resolve(true);
      }
    }).catch((error) => {
      defered.reject(error);
    });
    return defered.promise;
  }

  /**
   * Always create a fresh workspace (by removing the old one if it exists)
   * @param diagnosticCallback
   * @returns {IPromise<T>}
   */
  recreateDiagnosticWorkspace(diagnosticCallback : DiagnosticCallback) : ng.IPromise<che.IWorkspace> {
    let defered = this.$q.defer();

    // delete if present
    this.deleteDiagnosticWorkspaceIfPresent(diagnosticCallback).then(() => {
      // now create workspace config
      let workspaceConfig: che.IWorkspaceConfig = {
        "projects": [],
        "environments": {
          "diagnostics": {
            "machines": {
              "dev-machine": {
                "agents": ["org.eclipse.che.ws-agent"],
                "servers": {},
                "attributes": {"memoryLimitBytes": "1147483648"}
              }
            },
            "recipe": {
              "content": "FROM openjdk:8-jre-alpine\nCMD tail -f /dev/null\n",
              "contentType": "text/x-dockerfile",
              "type": "dockerfile"
            }
          }
        },
        "name": "diagnostics",
        "defaultEnv": "diagnostics",
        "description": "Diagnostics Workspace",
        "commands": []
      };
      return this.cheWorkspace.createWorkspaceFromConfig(null, workspaceConfig);
    }).then((workspace) => {
      defered.resolve(workspace);
    }).catch((error) => {
        defered.reject(error);
      }
    );
    return defered.promise;
  }

  /**
   * Starts the test by adding new callbacks after this one
   * @param diagnosticCallback the original check
   * @returns {ng.IPromise}
   */
  start(diagnosticCallback : DiagnosticCallback) : ng.IPromise {
    this.workspaceCallback = diagnosticCallback.newCallback('Workspace State');
    this.wsAgentCallback = diagnosticCallback.newCallback('Workspace Agent State');
    this.machineCallback = diagnosticCallback.newCallback('Workspace Runtime State');
    this.execAgentCallback = diagnosticCallback.newCallback('Workspace Exec Agent State');

    let workspaceIsStarted : boolean = false;
    this.recreateDiagnosticWorkspace(diagnosticCallback).then((workspace : che.IWorkspace) => {

      let statusLink = this.lodash.find(workspace.links, (link: any) => {
        return link.rel === 'environment.status_channel';
      });

      let eventChannelLink = this.lodash.find(workspace.links, (link: any) => {
        return link.rel === 'get workspace events channel';
      });

      let outputLink = this.lodash.find(workspace.links, (link: any) => {
        return link.rel === 'environment.output_channel';
      });

      let eventChannel = eventChannelLink ? eventChannelLink.parameters[0].defaultValue : null;
      let agentChannel = eventChannel + ':ext-server:output';
      let statusChannel = statusLink ? statusLink.parameters[0].defaultValue : null;
      let outputChannel = outputLink ? outputLink.parameters[0].defaultValue : null;

      diagnosticCallback.shared('workspace', workspace);
      diagnosticCallback.subscribeChannel(eventChannel, (message: any) => {
        diagnosticCallback.addContent('EventChannel : ' + JSON.stringify(message));
        if (message.eventType === 'RUNNING') {
          workspaceIsStarted = true;
          this.workspaceCallback.stateRunning('RUNNING');
          this.cheWorkspace.fetchWorkspaces().then(() => {
            let workspace = diagnosticCallback.getShared('workspace');
            this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
              diagnosticCallback.shared('workspace', this.cheWorkspace.getWorkspaceById(workspace.id));
              diagnosticCallback.success('Starting workspace OK');
            })
          });
        }
      });

      if (statusChannel) {
        diagnosticCallback.subscribeChannel(statusChannel, (message: any) => {
          if (message.eventType === 'DESTROYED' && message.workspaceId === workspace.id) {
            diagnosticCallback.error('Error while starting the workspace : Workspace has been destroyed', 'Please check the diagnostic logs.');
          }
          if (message.eventType === 'ERROR' && message.workspaceId === workspace.id) {
            diagnosticCallback.error('Error while starting the workspace : ' + JSON.stringify(message));
          }

          if (message.eventType === 'RUNNING' && message.workspaceId === workspace.id && message.machineName === 'dev-machine') {
            this.machineCallback.stateRunning('RUNNING');
          }

          diagnosticCallback.addContent('StatusChannel : ' + JSON.stringify(message));

        });
      }

      diagnosticCallback.subscribeChannel(agentChannel, (message: any) => {
        diagnosticCallback.addContent('agent channel :' + message);

        if (message.indexOf(' Server startup') > 0) {
          this.wsAgentCallback.stateRunning('RUNNING');

          // Server has been startup in the workspace agent and tries to reach workspace agent but is unable to do it
          // try with the browser ip
          diagnosticCallback.delayFunction(() => {

            let hint : string = 'The workspace agent has started in the workspace, but the ' + this.cheBranding.getName() + ' server cannot verify this. There is a failure for ' + this.cheBranding.getName() + ' server to connect to your workspace\'s agent. Either your firewall is blocking essential ports or you can change ';
            if (this.cheBranding.getName() === 'Eclipse Che') {
              hint += 'CHE_DOCKER_IP and DOCKER_HOST to values ';
            }
            hint += 'specific to your environment. See the `' + this.cheBranding.getCLI().configName + '` file for specifics.';
            diagnosticCallback.notifyFailure('The workspace started, but ' + this.cheBranding.getName() + ' <--> Workspace connection not established', hint);
            this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
              diagnosticCallback.shared('workspace', this.cheWorkspace.getWorkspaceById(workspace.id));
              let newCallback : DiagnosticCallback = diagnosticCallback.newCallback('Test connection from browser to workspace agent by using Workspace Agent IP');
              this.diagnosticsRunningWorkspaceCheck.checkWsAgent(newCallback, false);
              let websocketCallback : DiagnosticCallback = diagnosticCallback.newCallback('Test connection from browser to workspace agent with websocket');
              this.diagnosticsRunningWorkspaceCheck.checkWebSocketWsAgent(websocketCallback);
            });

          }, 7000)
        }

        diagnosticCallback.addContent(message);
      });

      if (outputChannel) {
        diagnosticCallback.subscribeChannel(outputChannel, (message: any) => {
          let content : string = angular.fromJson(message).content;
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
      }

      diagnosticCallback.delayError('Test limit is for up to 5minutes. Time has exceed.', 5 * 60 * 1000);

      let startWorkspacePromise = this.cheWorkspace.startWorkspace(workspace.id, workspace.config.defaultEnv);
      startWorkspacePromise.then((workspaceData) => {
        diagnosticCallback.shared('workspace', workspaceData);
      });

    }).catch((error) => {
      diagnosticCallback.error('Unable to start workspace: ' + error);
    });


    return diagnosticCallback.getPromise();

  }


}
