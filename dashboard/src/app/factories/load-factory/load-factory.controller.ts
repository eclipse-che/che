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
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/workspace/che-workspace.factory';
import {LoadFactoryService, FactoryLoadingStep} from './load-factory.service';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {RouteHistory} from '../../../components/routing/route-history.service';
import {CheJsonRpcApi} from '../../../components/api/json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcMasterApi} from '../../../components/api/json-rpc/che-json-rpc-master-api';
import {WorkspaceDataManager} from '../../../components/api/workspace/workspace-data-manager';

const STARTING = WorkspaceStatus[WorkspaceStatus.STARTING];
const RUNNING = WorkspaceStatus[WorkspaceStatus.RUNNING];

const WS_AGENT_STEP: number = 4;

/**
 * This class is handling the controller for the factory loading.
 * @author Ann Shumilova
 */
export class LoadFactoryController {

  static $inject = [
    '$location',
    '$mdDialog',
    '$q',
    '$route',
    '$timeout',
    '$window',
    'cheAPI',
    'cheJsonRpcApi',
    'cheNotification',
    'cheWorkspace',
    'loadFactoryService',
    'lodash',
    'routeHistory',
  ];

  private $location: ng.ILocationService;
  private $mdDialog: ng.material.IDialogService;
  private $q: ng.IQService;
  private $timeout: ng.ITimeoutService;
  private $window: ng.IWindowService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;
  private loadFactoryService: LoadFactoryService;
  private lodash: any;
  private routeHistory: RouteHistory;
  private routeParams: any;

  private workspaceDataManager: WorkspaceDataManager;
  private workspaces: Array<che.IWorkspace>;
  private workspace: che.IWorkspace;
  private projectsToImport: number;

  private factory: che.IFactory;
  private jsonRpcMasterApi: CheJsonRpcMasterApi;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $location: ng.ILocationService,
    $mdDialog: ng.material.IDialogService,
    $q: ng.IQService,
    $route: ng.route.IRouteService,
    $timeout: ng.ITimeoutService,
    $window: ng.IWindowService,
    cheAPI: CheAPI,
    cheJsonRpcApi: CheJsonRpcApi,
    cheNotification: CheNotification,
    cheWorkspace: CheWorkspace,
    loadFactoryService: LoadFactoryService,
    lodash: any,
    routeHistory: RouteHistory,
  ) {
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$timeout = $timeout;
    this.$window = $window;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.loadFactoryService = loadFactoryService;
    this.lodash = lodash;
    this.routeHistory = routeHistory;

    this.workspaceDataManager = new WorkspaceDataManager();

    this.workspaces = [];
    this.workspace = {} as che.IWorkspace;
    this.hideMenuAndFooter();
    this.jsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(cheAPI.getWorkspace().getJsonRpcApiLocation());

    this.loadFactoryService.resetLoadProgress();
    this.loadFactoryService.setLoadFactoryInProgress(true);

    this.routeParams = $route.current.params;
    this.getFactoryData();
  }

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  /**
   * Hides menu and footer to maximize view.
   */
  hideMenuAndFooter(): void {
    angular.element(document.querySelectorAll('[id*=navmenu]')).hide();
    angular.element(document.querySelectorAll('.che-footer')).hide();
  }

  /**
   * Restores the menu and footer.
   */
  restoreMenuAndFooter(): void {
    angular.element(document.querySelectorAll('[id*=navmenu]')).show();
    angular.element(document.querySelectorAll('.che-footer')).show();
  }

  /**
   * Retrieves factory data.
   */
  getFactoryData(): void {
    let promise;
    if (this.routeParams.id) {
      this.factory = this.cheAPI.getFactory().getFactoryById(this.routeParams.id);
      promise = this.cheAPI.getFactory().fetchFactoryById(this.routeParams.id);
    } else if (this.routeParams) {
      promise = this.processFactoryParameters(this.routeParams);
    } else {
      this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Required parameters for loading factory are not there.';
    }
    if (promise) {
      promise.then((factory: che.IFactory) => {
        this.factory = factory;

        // check factory polices:
        if (!this.factory || !this.checkPolicies(this.factory)) {
          return;
        }

        // check factory contains compatible workspace config:
        if (!(Boolean(this.factory.workspace) !== Boolean(this.factory.devfile)) || !this.isSupported()) {
          this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
          this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Factory has no compatible workspace config or devfile.';
        } else {
          this.loadFactoryService.goToNextStep();
          this.$timeout(() => {
            this.processFactorySource();
          }, 1500);
        }
      }, (error: any) => {
        this.handleError(error);
      });
    }
  }

  /**
   * Looks for source of factory (devfile of .factory.json) and prints message to user.
   */
  processFactorySource(): void {
    if (this.factory.source) {
      let sourceString = this.factory.source === 'repo' ?
                   ': devfile.yaml not found in repository root. Default environment will be applied' :
                   ': found ' + this.factory.source + ', applying it';
      this.getLoadingSteps()[this.getCurrentProgressStep()].text += sourceString;
    }
    this.fetchWorkspaces();
  }

  /**
   * Processes factory parameters.
   *
   * @param parameters
   * @returns {any}
   */
  processFactoryParameters(parameters: any): ng.IPromise<any> {
    // user name and factory name should be handled differently:
    if (parameters.name || parameters.user) {
      if (Object.keys(parameters).length === 2) {
        return this.processUser(parameters.user, parameters.name);
      } else {
        let paramName = parameters.name ? 'Factory name' : 'User name';
        this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Invalid factory URL. ' + paramName + ' is missed or misspelled.';
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
        return null;
      }
    }

    return this.cheAPI.getFactory().fetchParameterFactory(parameters);
  }

  /**
   * Processes factory's user. Checks user with such name exists.
   *
   * @param name user name
   * @param factoryName
   * @returns {IPromise<IHttpPromiseCallbackArg<any>>}
   */
  processUser(name: string, factoryName: string): ng.IPromise<any> {
    return this.cheAPI.getUser().fetchUserByName(name).then((user: che.IUser) => {
      return this.cheAPI.getFactory().fetchFactoryByName(factoryName, user.id);
    }, (error: any) => {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Invalid factory URL. User with name ' + name + ' does not exist.';
      this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      return null;
    });
  }

  /**
   * Checks factory's policies.
   *
   * @param factory factory to be checked
   * @returns {boolean} <code>true</code> if factory policies validation has passed
   */
  checkPolicies(factory: che.IFactory): boolean {
    if (!factory.policies || !factory.policies.referer) {
      return true;
    }
    // process referrer:
    let factoryReferrer  = factory.policies.referer;
    let referrer = document.referrer;
    if (referrer && (referrer.indexOf(factoryReferrer) >= 0)) {
      return true;
    } else {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Factory referrer policy does not match the current one.';
      this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      return false;
    }
  }

  /**
   * Handles pointed error - prints it on the proper screen.
   *
   * @param error error to be handled
   */
  handleError(error: any): void {
    if (error && error.data.message) {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = error.data.message;
      this.cheNotification.showError(error.data.message);
    }
    this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
  }

  /**
   * Detect workspace to start: create new one or get created one.
   */
  getWorkspaceToStart(): void {
    const createPolicy = this.factory.policies ? this.factory.policies.create : this.routeParams['policies.create'] || 'perClick';
    let workspace = null;
    switch (createPolicy.toLowerCase()) {
      case 'peruser' :
        workspace = this.lodash.find(this.workspaces, (w: che.IWorkspace) => {
          if (this.factory.id) {
            return this.factory.id === w.attributes.factoryId;
          } else if (this.routeParams.url) {
            const factoryUrl = w.attributes.factoryurl;
            // compare factory URL and route params
            if (angular.isDefined(factoryUrl)) {
              const factoryUrlObj = new (window as any).URL(factoryUrl);
              const isPathCorrect = `${factoryUrlObj.origin}${factoryUrlObj.pathname}` === this.routeParams.url;
              if (isPathCorrect === false) {
                return false;
              }

              let factoryUrlParamsNumber = 0;
              let hasExtraKey = false;
              for (const [key, value] of factoryUrlObj.searchParams) {
                if (hasExtraKey) {
                  return false;
                }
                factoryUrlParamsNumber++;
                hasExtraKey = this.routeParams[key] !== value;
              }
              if (hasExtraKey) {
                return false;
              }

              // `routeParams` contains the `url` param which is not in `factoryUrl`
              const paramsNumber = Object.keys(this.routeParams).length - 1;
              return factoryUrlParamsNumber === paramsNumber;
            }
          }
          return false;
        });
        break;
      case 'peraccount' :
        // TODO when account is ready
        workspace = this.lodash.find(this.workspaces, (w: che.IWorkspace) => {
          return this.factory.workspace.name === this.workspaceDataManager.getName(w);
        });
        break;
      case 'perclick' :
        break;
    }

    if (workspace) {
      this.startWorkspace(workspace);
    } else {
      this.createWorkspace();
    }
  }

  /**
   * Fetches workspaces.
   */
  fetchWorkspaces(): any {
    this.loadFactoryService.goToNextStep();

    let promise = this.cheAPI.getWorkspace().fetchWorkspaces();
    promise.then(() => {
      this.workspaces = this.cheAPI.getWorkspace().getWorkspaces();
      this.getWorkspaceToStart();
    }, () => {
      this.workspaces = this.cheAPI.getWorkspace().getWorkspaces();
      this.getWorkspaceToStart();
    });
  }

  /**
   * Create workspace from factory config.
   */
  createWorkspace(): any {
    const defer = this.$q.defer();
    if (!this.factory.devfile) {
      defer.reject({ data: {message: 'No devfile in factory.' }});
    } else {
      const devfile = this.factory.devfile;
      // set devfile attributes
      let url = '';
      let params = '';
      for (const key in this.routeParams) {
        if (key === 'url') {
          url = this.routeParams[key];
        } else {
          params += `${!params ? '?' : '&'}${key}=${this.routeParams[key]}`;
        }
      }

      const attrs = {factoryurl: `${url}${params}`};
      this.cheAPI.getWorkspace().createWorkspaceFromDevfile(null, devfile, attrs)
        .then((workspace: che.IWorkspace) => defer.resolve(workspace));
    }
    defer.promise.then((workspace: che.IWorkspace) => {
      this.$timeout(() => {
        this.startWorkspace(workspace);
      }, 1000);
    }, (error: any) => {
      this.handleError(error);
    });
  }

  /**
   * Checks workspace status and starts it if necessary,
   *
   * @param workspace workspace to process
   */
  startWorkspace(workspace: che.IWorkspace): void {
    this.workspace = workspace;

    if (workspace.status === RUNNING) {
      this.finish();
      return;
    }

    this.subscribeOnEvents(workspace);

    this.$timeout(() => {
      this.doStartWorkspace(workspace);
    }, 2000);
  }

  /**
   * Performs workspace start.
   *
   * @param workspace
   */
  doStartWorkspace(workspace: che.IWorkspace): void {
    this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id).then(() => {
      const workspaceStatus = this.cheAPI.getWorkspace().getWorkspacesById().get(workspace.id).status;
      if ((workspaceStatus !== RUNNING) && (workspaceStatus !== STARTING)) {
        this.cheAPI.getWorkspace().startWorkspace(workspace.id).then((data: any) => {
          console.log('Workspace started', data);
        }, (error: any) => {
          let errorMessage;

          if (!error || !error.data) {
            errorMessage = 'This factory is unable to start a new workspace.';
          } else if (error.data.errorCode === 10000 && error.data.attributes) {
            let attributes = error.data.attributes;

            errorMessage = 'This factory is unable to start a new workspace.' +
              ' Your running workspaces are consuming ' +
              attributes.used_ram + attributes.ram_unit + ' RAM.' +
              ' Your current RAM limit is ' + attributes.limit_ram + attributes.ram_unit +
              '. This factory requested an additional ' +
              attributes.required_ram + attributes.ram_unit + '.' +
              '  You can stop other workspaces to free resources.';
          } else {
            errorMessage = error.data.message;
          }

          this.handleError({data: {message: errorMessage}});
        });
      }
      this.loadFactoryService.goToNextStep();
    });
  }

  subscribeOnEvents(data: any): void {
    let workspaceId = data.id;

    let environmentStatusHandler = (message: any) => {
      if (message.eventType === 'DESTROYED' && message.workspaceId === data.id) {
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;

        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Unable to start workspace')
            .textContent('Unable to start workspace. It may be linked to OutOfMemory or the container has been destroyed')
            .ariaLabel('Workspace start')
            .ok('OK')
        );
      }
      if (message.eventType === 'FAILED' && message.workspaceId === workspaceId && message.error) {
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Error when starting workspace')
            .textContent('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
            .ariaLabel('Workspace start')
            .ok('OK')
        );
      }
      console.log('Status channel of workspaceID', workspaceId, message);
    };

    this.jsonRpcMasterApi.subscribeEnvironmentStatus(workspaceId, environmentStatusHandler);

    let environmentOutputHandler = (message: any) => {
      if (this.loadFactoryService.getCurrentProgressStep() === WS_AGENT_STEP) {
        return;
      }
      message = this.getDisplayMachineLog(message);
      if (this.getLoadingSteps()[this.getCurrentProgressStep()].logs.length > 0) {
        this.getLoadingSteps()[this.getCurrentProgressStep()].logs = this.getLoadingSteps()[this.getCurrentProgressStep()].logs + '\n' + message;
      } else {
        this.getLoadingSteps()[this.getCurrentProgressStep()].logs = message;
      }
    };

    this.jsonRpcMasterApi.subscribeEnvironmentOutput(workspaceId, environmentOutputHandler);

    let workspaceStatusHandler = (message: any) => {
      if (message.status === 'STOPPED' && message.workspaceId === workspaceId && message.error) {
        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Error when starting workspace')
            .textContent('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
            .ariaLabel('Workspace start')
            .ok('OK')
        );
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      }

      if (message.status === RUNNING && message.workspaceId === workspaceId) {
        this.finish();
      }
    };

    this.jsonRpcMasterApi.subscribeWorkspaceStatus(workspaceId, workspaceStatusHandler);

    let wsAgentHandler = (message: any) => {
      if (this.loadFactoryService.getCurrentProgressStep() < WS_AGENT_STEP) {
        this.loadFactoryService.setCurrentProgressStep(WS_AGENT_STEP);
      }

      if (this.getLoadingSteps()[WS_AGENT_STEP].logs.length > 0) {
        this.getLoadingSteps()[WS_AGENT_STEP].logs = this.getLoadingSteps()[WS_AGENT_STEP].logs + '\n' + message.text;
      } else {
        this.getLoadingSteps()[WS_AGENT_STEP].logs = message.text;
      }
    };

    this.jsonRpcMasterApi.subscribeWsAgentOutput(workspaceId, wsAgentHandler);
  }

  /**
   * Gets the log to be displayed per machine.
   *
   * @param log origin log content
   * @returns {*} parsed log
   */
  getDisplayMachineLog(log: any): string {
    log = angular.fromJson(log);
    if (angular.isObject(log)) {
      return (log.machineName ? '[' + log.machineName + '] ' : '') + log.text;
    } else {
      return log;
    }
  }

  /**
   * Performs operations at the end of accepting factory.
   */
  finish(): void {
    this.loadFactoryService.setCurrentProgressStep(4);

    // people should go back to the dashboard after factory is initialized
    this.routeHistory.pushPath('/');

    const ideParams = [];
    if (this.routeParams && this.workspace.devfile) {
      if (this.routeParams.id || (this.routeParams.name && this.routeParams.user)) {
        ideParams.push('factory-id:' + this.factory.id);
      } else {
        // add every factory parameter by prefix
        Object.keys(this.routeParams).forEach((key: string) => {
          ideParams.push('factory-' + key + ':' + (this.$window as any).encodeURIComponent(this.routeParams[key]));
        });
      }

      // add factory mode
      ideParams.push('factory:' + 'true');
    }
    // add workspace Id
    ideParams.push('workspaceId:' + this.workspace.id);

    this.$location.path(this.getIDELink()).search('ideParams', ideParams);

    // restore elements
    this.restoreMenuAndFooter();
  }

  /**
   * Returns workspace name.
   *
   * @returns {string}
   */
  getWorkspace(): string {
    return this.workspaceDataManager.getName(this.workspace);
  }

  /**
   * Returns the text(logs) of pointed step.
   *
   * @param stepNumber number of step
   * @returns {string} step's text
   */
  getStepText(stepNumber: number): string {
    return this.loadFactoryService.getStepText(stepNumber);
  }

  /**
   * Returns loading steps of the factory.
   *
   * @returns {any}
   */
  getLoadingSteps(): FactoryLoadingStep[] {
    return this.loadFactoryService.getFactoryLoadingSteps();
  }

  /**
   * Returns the current step, which is in progress.
   *
   * @returns {any} the info of current step, which is in progress
   */
  getCurrentProgressStep(): any {
    return this.loadFactoryService.getCurrentProgressStep();
  }

  /**
   * Returns the loading factory in progress state.
   *
   * @returns {boolean}
   */
  isLoadFactoryInProgress(): boolean {
    return this.loadFactoryService.isLoadFactoryInProgress();
  }

  /**
   * Set the loading factory process in progress.
   */
  setLoadFactoryInProgress(): void {
    this.loadFactoryService.setLoadFactoryInProgress(true);
  }

  /**
   * Reset the loading factory process.
   */
  resetLoadFactoryInProgress(): void {
    this.restoreMenuAndFooter();
    let newLocation = this.isResourceProblem() ? '/workspaces' : '/factories';
    this.$location.path(newLocation);
    this.loadFactoryService.resetLoadProgress();
  }

  /**
   * Returns IDE link.
   *
   * @returns {string} IDE application link
   */
  getIDELink() {
    return '/ide/' + this.workspace.namespace + '/' + this.cheWorkspace.getWorkspaceDataManager().getName(this.workspace);
  }

  /**
   * Performs navigation back to dashboard.
   */
  backToDashboard(): void {
    this.restoreMenuAndFooter();
    this.$location.path('/').search({});
  }

  /**
   * Returns `true` if supported version of factory workspace.
   * @returns {boolean}
   */
  isSupported(): boolean {
    return this.loadFactoryService.isSupported(this.factory);
  }

  /**
   * Redirects to create workspace flow.
   */
  redirectToCreateWorkspace(): void {
    this.$location.path('/create-workspace').search({});
  }

  /**
   * Performs downloading of the logs.
   */
  downloadLogs(): void {
    let logs = '';
    this.getLoadingSteps().forEach((step: any) => {
      logs += step.logs + '\n';
    });
    window.open('data:text/csv,' + encodeURIComponent(logs));
  }

  /**
   * Returns whether there was problem with resources.
   *
   * @returns {any|boolean}
   */
  isResourceProblem(): boolean {
    let currentCreationStep = this.getLoadingSteps()[this.getCurrentProgressStep()];
    return currentCreationStep.hasError && currentCreationStep.logs.includes('You can stop other workspaces');
  }
}
