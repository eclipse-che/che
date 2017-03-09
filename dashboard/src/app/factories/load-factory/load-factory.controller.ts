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

/**
 * This class is handling the controller for the factory loading.
 * @author Ann Shumilova
 */
export class LoadFactoryCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI, $websocket, $route, $timeout, $mdDialog, loadFactoryService, lodash, cheNotification, $location, routeHistory, $window) {
    this.cheAPI = cheAPI;
    this.$websocket = $websocket;
    this.$timeout = $timeout;
    this.$mdDialog = $mdDialog;
    this.loadFactoryService = loadFactoryService;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.routeHistory = routeHistory;
    this.$window = $window;

    this.workspaces = [];
    this.workspace = {};

    this.websocketReconnect = 50;

    this.hideMenuAndFooter();

    this.loadFactoryService.resetLoadProgress();
    this.loadFactoryService.setLoadFactoryInProgress(true);

    this.routeParams = $route.current.params;
    this.getFactoryData();
  }

  hideMenuAndFooter() {
    angular.element('#chenavmenu').hide();
    angular.element(document.querySelectorAll('.che-footer')).hide();
  }

  restoreMenuAndFooter() {
    angular.element('#chenavmenu').show();
    angular.element(document.querySelectorAll('.che-footer')).show();
  }

  /**
   * Retrieve factory data.
   */
  getFactoryData() {
    var promise;
    if (this.routeParams.id) {
      this.factory = this.cheAPI.getFactory().getFactoryById(this.routeParams.id);
      promise = this.cheAPI.getFactory().fetchFactoryById(this.routeParams.id);
    } else if (this.routeParams) {
      promise = this.cheAPI.getFactory().fetchParameterFactory(this.routeParams);
    } else {
      this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Required parameters for loading factory are not there.';
    }
    if (promise) {
      promise.then((factory) => {
        this.factory = factory;

        //Check factory contains workspace config:
        if (!this.factory.workspace) {
          this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
          this.getLoadingSteps()[this.getCurrentProgressStep()].logs = 'Factory has no workspace config.';
        } else {
          this.fetchWorkspaces();
        }
      }, (error) => {
        this.handleError(error);
      });
    }
  }

  handleError(error) {
    if (error.data.message) {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = error.data.message;
      this.cheNotification.showError(error.data.message);
    }
    this.getLoadingSteps()[this.getCurrentProgressStep()].hasError = true;
  }

  /**
   * Detect workspace to start: create new one or get created one.
   */
  getWorkspaceToStart() {
    let createPolicy = (this.factory.policies) ? this.factory.policies.create : 'perClick';
    var workspace = null;
    switch (createPolicy) {
      case 'perUser' :
        workspace = this.lodash.find(this.workspaces, (w) => {
          return this.factory.id === w.attributes.factoryId;
        });
        break;
      case 'perAccount' :
        //TODO when account is ready
        workspace = this.lodash.find(this.workspaces, (w) => {
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

  fetchWorkspaces() {
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
  createWorkspace() {
    let config = this.factory.workspace;
    //set factory attribute:
    let attrs = {factoryId: this.factory.id};
    config.name = this.getWorkspaceName(config.name);

    //TODO: fix account when ready:
    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, config, attrs);
    creationPromise.then((data) => {
      this.$timeout(() => {
        this.startWorkspace(data);
      }, 1000);
    }, (error) => {
      this.handleError(error);
    });
  }

  /**
   * Get workspace name by detecting the existing names
   * and generate new name if necessary.
   */
  getWorkspaceName(name) {
    if (this.workspaces.size === 0) {
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
   * Start workspace.
   */
  startWorkspace(workspace) {
    this.workspace = workspace;
    var bus = this.cheAPI.getWebsocket().getBus();

    if (workspace.status === 'RUNNING') {
      this.loadFactoryService.setCurrentProgressStep(4);
      this.importProjects(bus);
      return;
    }

    this.subscribeOnEvents(workspace, bus);

    this.$timeout(() => {
      this.doStartWorkspace(workspace);
    }, 2000);
  }

  doStartWorkspace(workspace) {
    let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(workspace.id, workspace.config.defaultEnv);
    this.loadFactoryService.goToNextStep();

    startWorkspacePromise.then((data) => {
      console.log('Workspace started', data);
    }, (error) => {
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

  subscribeOnEvents(data, bus) {
    // get channels
    let statusLink = this.lodash.find(data.links, (link) => {
      return link.rel === 'environment.status_channel';
    });

    let outputLink = this.lodash.find(data.links, (link) => {
      return link.rel === 'environment.output_channel';
    });

    let workspaceId = data.id;

    let agentChannel = 'workspace:' + data.id + ':ext-server:output';
    let statusChannel = statusLink ? statusLink.parameters[0].defaultValue : null;
    let outputChannel = outputLink ? outputLink.parameters[0].defaultValue : null;

    bus.subscribe(outputChannel, (message) => {
      message = this.getDisplayMachineLog(message);
      if (this.getLoadingSteps()[this.getCurrentProgressStep()].logs.length > 0) {
        this.getLoadingSteps()[this.getCurrentProgressStep()].logs = this.getLoadingSteps()[this.getCurrentProgressStep()].logs + '\n' + message;
      } else {
        this.getLoadingSteps()[this.getCurrentProgressStep()].logs = message;
      }
    });

    // for now, display log of status channel in case of errors
    bus.subscribe(statusChannel, (message) => {
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
      if (message.eventType === 'ERROR' && message.workspaceId === data.id) {
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
    });

    // subscribe to workspace events
    bus.subscribe('workspace:' + workspaceId, (message) => {

      if (message.eventType === 'ERROR' && message.workspaceId === workspaceId) {
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

      if (message.eventType === 'RUNNING' && message.workspaceId === workspaceId) {
        this.finish();
      }
    });

    bus.subscribe(agentChannel, (message) => {
      let agentStep = 3;
      if (this.loadFactoryService.getCurrentProgressStep() < agentStep) {
        this.loadFactoryService.setCurrentProgressStep(agentStep);
      }

      if (this.getLoadingSteps()[agentStep].logs.length > 0) {
        this.getLoadingSteps()[agentStep].logs = this.getLoadingSteps()[agentStep].logs + '\n' + message;
      } else {
        this.getLoadingSteps()[agentStep].logs = message;
      }
    });

  }

  /**
   * Gets the log to be displayed per machine.
   *
   * @param log origin log content
   * @returns {*} parsed log
   */
  getDisplayMachineLog(log) {
    log = angular.fromJson(log);
    if (angular.isObject(log)) {
      return '[' + log.machineName + '] ' + log.content;
    } else {
      return log;
    }
  }

  importProjects(bus) {
    let promise = this.cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspace.id);
    promise.then(() => {
      let projects = this.cheAPI.getWorkspace().getWorkspacesById().get(this.workspace.id).config.projects;
      this.detectProjectsToImport(projects, bus);
    }, (error) => {
      if (error.status !== 304) {
        let projects = this.cheAPI.getWorkspace().getWorkspacesById().get(this.workspace.id).config.projects;
        this.detectProjectsToImport(projects, bus);
      } else {
        this.handleError(error);
      }
    });
  }

  /**
   * Detect projects to import by their existence on file system.
   */
  detectProjectsToImport(projects, bus) {
    this.projectsToImport = 0;

    projects.forEach((project) => {
      if (!this.isProjectOnFileSystem(project)) {
        this.projectsToImport++;
        this.importProject(this.workspace.id, project, bus);
      }
    });

    if (this.projectsToImport === 0) {
      this.finish();
    }
  }

  /**
   * Project is on file system if there is no errors except code=9.
   */
  isProjectOnFileSystem(project) {
    let problems = project.problems;
    if (!problems || problems.length === 0) {
      return true;
    }

    for (var i = 0; i < problems.length; i++) {
      if (problems[i].code === 9) {
        return true;
      }
    }

    return false;
  }

  /**
   * Perform import project
   */
  importProject(workspaceId, project, bus) {
    var promise;
    // websocket channel
    var channel = 'importProject:output';

    // on import
    bus.subscribe(channel, (message) => {
      this.getLoadingSteps()[this.getCurrentProgressStep()].logs = message.line;
    });

    let projectService = this.cheAPI.getWorkspace().getWorkspaceAgent(workspaceId).getProject();
    promise = projectService.importProject(project.name, project.source);

    // needs to update configuration of the project
    let updatePromise = promise.then(() => {
      projectService.updateProject(project.name, project).$promise;
    }, (error) => {
      this.handleError(error);
    });

    updatePromise.then(() => {
      this.projectsToImport--;
      if (this.projectsToImport === 0) {
        this.finish();
      }
      bus.unsubscribe(channel);
    }, (error) => {
      bus.unsubscribe(channel);
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

  finish() {
    this.loadFactoryService.setCurrentProgressStep(4);

    // people should go back to the dashboard after factory is initialized
    this.routeHistory.pushPath('/');

    var ideParams = [];
    if (this.routeParams) {
      // add every factory parameter by prefix
      Object.keys(this.routeParams).forEach((key) => {
        ideParams.push('factory-' + key + ':' + this.$window.encodeURIComponent(this.routeParams[key]));
      });
      // add factory mode
      ideParams.push('factory:' + 'true');
    }
    // add workspace Id
    ideParams.push('workspaceId:' + this.workspace.id);

    this.$location.path(this.getIDELink()).search('ideParams', ideParams);

    // restore elements
    angular.element('#chenavmenu').show();
    angular.element(document.querySelectorAll('.che-footer')).show();

  }

  getWorkspace() {
    return this.workspace.config.name;
  }

  getStepText(stepNumber) {
    return this.loadFactoryService.getStepText(stepNumber);
  }

  getLoadingSteps() {
    return this.loadFactoryService.getFactoryLoadingSteps();
  }

  getCurrentProgressStep() {
    return this.loadFactoryService.getCurrentProgressStep();
  }

  isLoadFactoryInProgress() {
    return this.loadFactoryService.isLoadFactoryInProgress();
  }

  setLoadFactoryInProgress() {
    this.loadFactoryService.setLoadFactoryInProgress(true);
  }

  resetLoadFactoryInProgress() {
    this.restoreMenuAndFooter();
    let newLocation = this.isResourceProblem() ? '/workspaces' : '/factories';
    this.$location.path(newLocation);
    this.loadFactoryService.resetLoadProgress();
  }

  getIDELink() {
    return '/ide/' + this.workspace.namespace + '/' + this.workspace.config.name;
  }

  backToDashboard() {
    this.restoreMenuAndFooter();
    this.$location.path('/');
  }

  downloadLogs() {
    let logs = '';
    this.getLoadingSteps().forEach((step) => {
      logs += step.logs + '\n';
    });
    window.open('data:text/csv,' + encodeURIComponent(logs));
  }

  isResourceProblem() {
    let currentCreationStep = this.getLoadingSteps()[this.getCurrentProgressStep()];
    return currentCreationStep.hasError && currentCreationStep.logs.includes('You can stop other workspaces');
  }
}
