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
import {DiagnosticsWebsocketWsMaster} from './test/diagnostics-websocket-wsmaster.factory';
import {DiagnosticCallback} from './diagnostic-callback';
import {DiagnosticsWorkspaceStartCheck} from './test/diagnostics-workspace-start-check.factory';
import {DiagnosticsRunningWorkspaceCheck} from './test/diagnostics-workspace-check-workspace.factory';
import {DiagnosticPart} from './diagnostic-part';
import {DiagnosticPartState} from './diagnostic-part-state';
import {CheBranding} from '../../components/branding/che-branding.factory';

/**
 * @ngdoc controller
 * @name diagnostics.controller:DiagnosticsController
 * @description This class is handling the controller for the diagnostics page
 * @author Florent Benoit
 */
export class DiagnosticsController {

  static $inject = ['$log', '$q', 'lodash', '$timeout', 'diagnosticsWebsocketWsMaster', 'cheBranding', 'diagnosticsRunningWorkspaceCheck', 'diagnosticsWorkspaceStartCheck'];

  /**
   * Promise service handling.
   */
  private $q: ng.IQService;

  /**
   * Log service.
   */
  private $log: ng.ILogService;

  /**
   * Lodash utility.
   */
  private lodash: any;

  /**
   * Instance of checker for websockets
   */
  private diagnosticsWebsocketWsMaster: DiagnosticsWebsocketWsMaster;

  /**
   * Instance of checker for workspace
   */
  private diagnosticsWorkspaceStartCheck: DiagnosticsWorkspaceStartCheck;

  /**
   * Angular timeout service.
   */
  private $timeout: ng.ITimeoutService;

  /**
   * Shared Map across all parts.
   */
  private sharedMap: Map<string, any>;

  /**
   * Reference to the diagnostic workspace checker.
   */
  private diagnosticsRunningWorkspaceCheck: DiagnosticsRunningWorkspaceCheck;

  /**
   * List of all parts.
   */
  private parts: Array<DiagnosticPart>;

  /**
   * Link to the workspace master part
   */
  private wsMasterPart: DiagnosticPart;

  /**
   * Link to the workspace agent part
   */
  private wsAgentPart: DiagnosticPart;

  /**
   * Alias for the current part being tested
   */
  private currentPart: DiagnosticPart;

  /**
   * Allow to turn on/off details
   */
  private showDetails: boolean = false;

  /**
   * Global state
   */
  private state: DiagnosticPartState;

  /**
   * Text to be displayed as global status
   */
  private globalStatusText: string;

  /**
   * Branding info.
   */
  private cheBranding: CheBranding;

  /**
   * Show/hide logs
   */
  private isLogDisplayed: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService, $q: ng.IQService, lodash: any,
              $timeout: ng.ITimeoutService,
              diagnosticsWebsocketWsMaster: DiagnosticsWebsocketWsMaster,
              cheBranding: CheBranding,
              diagnosticsRunningWorkspaceCheck: DiagnosticsRunningWorkspaceCheck,
              diagnosticsWorkspaceStartCheck: DiagnosticsWorkspaceStartCheck) {
    this.$q = $q;
    this.$log = $log;
    this.lodash = lodash;
    this.$timeout = $timeout;
    this.diagnosticsWebsocketWsMaster = diagnosticsWebsocketWsMaster;
    this.diagnosticsWorkspaceStartCheck = diagnosticsWorkspaceStartCheck;
    this.diagnosticsRunningWorkspaceCheck = diagnosticsRunningWorkspaceCheck;
    this.parts = new Array<DiagnosticPart>();
    this.sharedMap = new Map<string, any>();
    this.cheBranding = cheBranding;
    this.isLogDisplayed = false;
    this.state = DiagnosticPartState.READY;
    this.globalStatusText = 'Ready To Start';

    this.wsMasterPart = new DiagnosticPart();
    this.wsMasterPart.icon = 'fa fa-cube';
    this.wsMasterPart.title = 'Server Tests';
    this.wsMasterPart.state = DiagnosticPartState.READY;
    this.wsMasterPart.subtitle = 'Connectivity checks to the ' + this.cheBranding.getName() + ' server';

    this.wsAgentPart = new DiagnosticPart();
    this.wsAgentPart.icon = 'fa fa-cubes';
    this.wsAgentPart.title = 'Workspace Tests';
    this.wsAgentPart.state = DiagnosticPartState.READY;
    this.wsAgentPart.subtitle = 'Connectivity checks to Dockerized workspaces';
  }

  /**
   * Start the tests.
   */
  public start(): void {
    this.sharedMap.clear();
    this.globalStatusText = 'Running Tests';
    this.state = DiagnosticPartState.IN_PROGRESS;

    this.parts.length = 0;
    this.parts.push(this.wsMasterPart);
    this.parts.push(this.wsAgentPart);
    this.parts.forEach((part: DiagnosticPart) => {
      part.clear();
    });

    this.currentPart = this.wsMasterPart;

    // first check websocket on workspace master
    this.checkWorkspaceMaster().then(() => {
      return this.checkWorkspaceAgent();
    }).then(() => {
      return this.waitAllCompleted([this.checkWorkspaceCheck(), this.checkWebSocketWsAgent()]);
    }).then(() => {
      this.globalStatusText = 'Completed Diagnostics';
      this.state = DiagnosticPartState.SUCCESS;
    }).catch((error: any) => {
      this.globalStatusText = 'Diagnostics Finished With Error';
      this.state = DiagnosticPartState.ERROR;
    });
  }

  /**
   * Wait for all promises to be terminate and not stop at the first error
   * @param promises an array of promises
   * @returns {ng.IPromise<any>}
   */
  waitAllCompleted(promises: Array<ng.IPromise<any>>): ng.IPromise<any> {
    const allCompletedDefered = this.$q.defer();
    let finished: number = 0;
    let toFinish: number = promises.length;
    let error: boolean = false;
    promises.forEach((promise: ng.IPromise<any>) => {
      promise.catch(() => {
        error = true;
      }).finally(() => {
        finished++;
        if (finished === toFinish) {
          if (error) {
            this.currentPart.state = DiagnosticPartState.ERROR;
            allCompletedDefered.reject('error');
          } else {
            allCompletedDefered.resolve('success');
          }
        }
      });
    });
    return allCompletedDefered.promise;
  }

  /**
   * Build a new callback item
   * @param text the text to set in the callback
   * @param diagnosticPart the diagnostic part
   * @returns {DiagnosticCallback} the newly callback
   */
  public newItem(text: string, diagnosticPart: DiagnosticPart): DiagnosticCallback {
    let callback: DiagnosticCallback = new DiagnosticCallback(this.$q, this.$timeout, text, this.sharedMap, this, diagnosticPart);
    diagnosticPart.addCallback(callback);
    return callback;
  }

  /**
   * Sets the details part.
   * @param part the part to be displayed for the details
   */
  public setDetailsPart(part: DiagnosticPart): void {
    this.currentPart = part;
  }

  /**
   * Checks the workspace master.
   * @returns {ng.IPromise<any>}
   */
  public checkWorkspaceMaster(): ng.IPromise<any> {
    this.currentPart = this.wsMasterPart;

    this.wsMasterPart.state = DiagnosticPartState.IN_PROGRESS;
    let promiseWorkspaceMaster: ng.IPromise<any> = this.diagnosticsWebsocketWsMaster.start(this.newItem('Websockets', this.wsMasterPart));
    promiseWorkspaceMaster.then(() => {
      this.wsMasterPart.state = DiagnosticPartState.SUCCESS;
    }).catch((error: any) => {
      this.wsMasterPart.state = DiagnosticPartState.ERROR;
    });

    return promiseWorkspaceMaster;
  }

  /**
   * Checks the workspace agent.
   * @returns {ng.IPromise<any>}
   */
  public checkWorkspaceAgent(): ng.IPromise<any> {
    this.currentPart = this.wsAgentPart;

    this.wsAgentPart.state = DiagnosticPartState.IN_PROGRESS;
    let promiseWorkspaceAgent: ng.IPromise<any> = this.diagnosticsWorkspaceStartCheck.start(this.newItem('Create Workspace', this.wsAgentPart));
    promiseWorkspaceAgent.then(() => {
      this.wsAgentPart.state = DiagnosticPartState.SUCCESS;
    }).catch((error: any) => {
      this.wsAgentPart.state = DiagnosticPartState.ERROR;
    });

    return promiseWorkspaceAgent;
  }

  /**
   * Check the REST API on ws agent
   * @returns {ng.IPromise<any>}
   */
  public checkWorkspaceCheck(): ng.IPromise<any> {
    return this.diagnosticsRunningWorkspaceCheck.checkWsAgent(this.newItem('REST Call on Workspace Agent', this.wsAgentPart), true);
  }

  /**
   * Check the websockets on ws agent
   * @returns {ng.IPromise<any>}
   */
  public checkWebSocketWsAgent(): ng.IPromise<any> {
    return this.diagnosticsRunningWorkspaceCheck.checkWebSocketWsAgent(this.newItem('Websocket on Workspace Agent', this.wsAgentPart));
  }

  /**
   * Allow to toggle details
   */
  public toggleDetails(): void {
    this.showDetails = !this.showDetails;
  }

  /**
   * Checks the state of the controller
   * @returns {boolean} true if state is READY
   */
  public isReady(): boolean {
    return DiagnosticPartState.READY === this.state;
  }

  /**
   * Checks the state of the controller
   * @returns {boolean} true if state is IN_PROGRESS
   */
  public isInProgress(): boolean {
    return DiagnosticPartState.IN_PROGRESS === this.state;
  }

  /**
   * Checks the state of the controller
   * @returns {boolean} true if state is SUCCESS
   */
  public isSuccess(): boolean {
    return DiagnosticPartState.SUCCESS === this.state;
  }

  /**
   * Checks the state of the controller
   * @returns {boolean} true if state is FAILURE
   */
  public isFailure(): boolean {
    return DiagnosticPartState.FAILURE === this.state;
  }

  /**
   * Checks the state of the controller
   * @returns {boolean} true if state is ERROR
   */
  public isError(): boolean {
    return DiagnosticPartState.ERROR === this.state;
  }

  /**
   * Toggle log display.
   */
  public showLogs(): void {
    this.isLogDisplayed = !this.isLogDisplayed;
  }

}
