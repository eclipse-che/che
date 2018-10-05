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
import {LoadFactoryService} from './load-factory.service';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {RouteHistory} from '../../../components/routing/route-history.service';
import {CheJsonRpcApi} from '../../../components/api/json-rpc/che-json-rpc-api.factory';
import {CheJsonRpcMasterApi} from '../../../components/api/json-rpc/che-json-rpc-master-api';

const WS_AGENT_STEP: number = 3;

/**
 * This class is handling the controller for the factory loading.
 * @author Ann Shumilova
 */
export class LoadFactoryController {

  static $inject = ['cheAPI', 'cheJsonRpcApi', '$route', '$timeout', '$mdDialog', 'loadFactoryService', 'lodash', 'cheNotification', '$location', 'routeHistory', '$window'];

  private cheAPI: CheAPI;
  private $timeout: ng.ITimeoutService;
  private $mdDialog: ng.material.IDialogService;
  private loadFactoryService: LoadFactoryService;
  private lodash: any;
  private cheNotification: CheNotification;
  private $location: ng.ILocationService;
  private routeHistory: RouteHistory;
  private $window: ng.IWindowService;
  private routeParams: any;

  private workspaces: Array<che.IWorkspace>;
  private workspace: che.IWorkspace;
  private projectsToImport: number;

  private factory: che.IFactory;
  private jsonRpcMasterApi: CheJsonRpcMasterApi;

  /**
   * Default constructor that is using resource
   */
  constructor(cheAPI: CheAPI,
              cheJsonRpcApi: CheJsonRpcApi,
              $route: ng.route.IRouteService,
              $timeout: ng.ITimeoutService,
              $mdDialog: ng.material.IDialogService,
              loadFactoryService: LoadFactoryService,
              lodash: any,
              cheNotification: CheNotification,
              $location: ng.ILocationService,
              routeHistory: RouteHistory,
              $window: ng.IWindowService) {
    this.cheAPI = cheAPI;
    this.$timeout = $timeout;
    this.$mdDialog = $mdDialog;
    this.loadFactoryService = loadFactoryService;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.routeHistory = routeHistory;
    this.$window = $window;

    this.workspaces = [];
    this.workspace = {} as che.IWorkspace;
    this.hideMenuAndFooter();
    this.jsonRpcMasterApi = cheJsonRpcApi.getJsonRpcMasterApi(cheAPI.getWorkspace().getJsonRpcApiLocation());

    this.loadFactoryService.resetLoadProgress();
    this.loadFactoryService.setLoadFactoryInProgress(true);

    this.routeParams = $route.current.params;
    this.getFactoryData();
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
        if (!this.checkPolicies(this.factory)) {
          return;
        }

        // check factory contains workspace config:
        if (!this.factory.workspace) {
          this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
          this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Factory has no workspace config.';
        } else {
          this.fetchWorkspaces();
        }
      }, (error: any) => {
        this.handleError(error);
      });
    }
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
    let createPolicy = (this.factory.policies) ? this.factory.policies.create : 'perClick';
    let workspace = null;
    switch (createPolicy) {
      case 'perUser' :
        workspace = this.lodash.find(this.workspaces, (w: che.IWorkspace) => {
          return this.factory.id === (w.attributes as any).factoryId;
        });
        break;
      case 'perAccount' :
        // todo when account is ready
        workspace = this.lodash.find(this.workspaces, (w: che.IWorkspace) => {
          return this.factory.workspace.name === w.config.name;
        });
        break;
      case 'perClick' :
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
    let config = this.factory.workspace;
    // set factory attribute:
    let attrs = {factoryId: this.factory.id};
    config.name = this.getWorkspaceName(config.name);

    // todo: fix account when ready:
    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, config, attrs);
    creationPromise.then((data: any) => {
      this.$timeout(() => {
        this.startWorkspace(data);
      }, 1000);
    }, (error: any) => {
      this.handleError(error);
    });
  }

  /**
   * Get workspace name by detecting the existing names
   * and generate new name if necessary.
   *
   * @param name workspace name
   * @returns {string} generated name
   */
  getWorkspaceName(name: string): string {
    if (this.workspaces.length === 0) {
      return name;
    }
    let existingNames = this.lodash.pluck(this.workspaces, 'config.name');

    if (existingNames.indexOf(name) < 0) {
      return name;
    }

    let generatedName = name;
    let counter = 1;
    while (existingNames.indexOf(generatedName) >= 0) {
      generatedName = name + '_' + counter++;
    }
    return generatedName;
  }

  /**
   * Checks workspace status and starts it if necessary,
   *
   * @param workspace workspace to process
   */
  startWorkspace(workspace: che.IWorkspace): void {
    this.workspace = workspace;

    if (workspace.status === 'RUNNING') {
      this.loadFactoryService.setCurrentProgressStep(4);
      this.importProjects();
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
    let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(workspace.id, workspace.config.defaultEnv);
    this.loadFactoryService.goToNextStep();

    startWorkspacePromise.then((data:  any) => {
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

  subscribeOnEvents(data: any): void {
    let workspaceId = data.id;

    let environmentStatusHandler = (message: any) => {
      if (message.eventType === 'DESTROYED' && message.workspaceId === data.id) {
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;

        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Unable to start workspace')
            .content('Unable to start workspace. It may be linked to OutOfMemory or the container has been destroyed')
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
            .content('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
            .ariaLabel('Workspace start')
            .ok('OK')
        );
      }
      console.log('Status channel of workspaceID', workspaceId, message);
    };

    this.jsonRpcMasterApi.subscribeEnvironmentStatus(workspaceId, environmentStatusHandler);

    let environmentOutputHandler = (message: any) => {
      // skip displaying machine logs after workspace agent:
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
            .title('Error when starting agent')
            .content('Unable to start workspace agent. Error when trying to start the workspace agent: ' + message.error)
            .ariaLabel('Workspace agent start')
            .ok('OK')
        );
        this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      }

      if (message.status === 'RUNNING' && message.workspaceId === workspaceId) {
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
   * Performs importing projects.
   */
  importProjects(): void {
    let promise = this.cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspace.id);
    promise.then(() => {
      let projects = this.cheAPI.getWorkspace().getWorkspacesById().get(this.workspace.id).config.projects;
      this.detectProjectsToImport(projects);
    }, (error: any) => {
      if (error.status !== 304) {
        let projects = this.cheAPI.getWorkspace().getWorkspacesById().get(this.workspace.id).config.projects;
        this.detectProjectsToImport(projects);
      } else {
        this.handleError(error);
      }
    });
  }

  /**
   * Detects the projects to be imported.
   *
   * @param projects projects list
   */
  detectProjectsToImport(projects: Array<che.IProject>): void {
    this.projectsToImport = 0;

    projects.forEach((project: che.IProjectTemplate) => {
      if (!this.isProjectOnFileSystem(project)) {
        this.projectsToImport++;
        this.importProject(this.workspace.id, project);
      }
    });

    if (this.projectsToImport === 0) {
      this.finish();
    }
  }

  /**
   * Project is on file system if there is no errors except code=9.
   */
  isProjectOnFileSystem(project: che.IProjectTemplate): boolean {
    let problems = project.problems;
    if (!problems || problems.length === 0) {
      return true;
    }

    for (let i = 0; i < problems.length; i++) {
      if (problems[i].code === 10) {
        return false;
      }
    }

    return true;
  }

  /**
   * Performs project import to pointed workspace.
   *
   * @param workspaceId workspace id, where project should be imported to
   * @param project project to be imported
   */
  importProject(workspaceId: string, project: che.IProject): void {
    var promise;
    let wsAgentApi = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getWsAgentApi();

    let projectImportHandler = (message: any) => {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = message.line;
    };
    wsAgentApi.subscribeProjectImport(project.name, projectImportHandler);

    let projectService = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProject();
    promise = projectService.importProject(project.name, project.source);

    // needs to update configuration of the project
    let updatePromise = promise.then(() => {
      projectService.updateProject(project.name, project);
    }, (error: any) => {
      this.handleError(error);
    });

    updatePromise.then(() => {
      this.projectsToImport--;
      if (this.projectsToImport === 0) {
        this.finish();
      }
      wsAgentApi.unSubscribeProjectImport(project.name, projectImportHandler);
    }, (error: any) => {
      wsAgentApi.unSubscribeProjectImport(project.name, projectImportHandler);
      this.handleError(error);

      // need to show the error
      this.$mdDialog.show(
        this.$mdDialog.alert()
          .title('Error while importing project')
          .content(error.statusText + ': ' + error.data.message)
          .ariaLabel('Import project')
          .ok('OK')
      );
    });
  }

  /**
   * Performs operations at the end of accepting factory.
   */
  finish(): void {
    this.loadFactoryService.setCurrentProgressStep(4);

    // people should go back to the dashboard after factory is initialized
    this.routeHistory.pushPath('/');

    var ideParams = [];
    if (this.routeParams) {
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
    return this.workspace.config.name;
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
  getLoadingSteps(): any {
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
    return '/ide/' + this.workspace.namespace + '/' + this.workspace.config.name;
  }

  /**
   * Performs navigation back to dashboard.
   */
  backToDashboard(): void {
    this.restoreMenuAndFooter();
    this.$location.path('/');
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
