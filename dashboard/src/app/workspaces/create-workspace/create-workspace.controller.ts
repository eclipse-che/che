/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @ngdoc controller
 * @name workspaces.create.workspace.controller:CreateWorkspaceController
 * @description This class is handling the controller for workspace creation
 * @author Ann Shumilova
 * @author Oleksii Orel
 * @author Anatolii Bazko
 */
export class CreateWorkspaceController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($location, $timeout, $log, $mdDialog, $websocket, cheAPI, createProjectSvc, createProjectProgressorSvc,
              cheNotification, lodash, $rootScope, cheEnvironmentRegistry) {
    this.$location = $location;
    this.$timeout = $timeout;
    this.$mdDialog = $mdDialog;
    this.$log = $log;
    this.$websocket = $websocket;
    this.cheAPI = cheAPI;
    this.createProjectProgressorSvc = createProjectProgressorSvc;
    this.createProjectSvc = createProjectSvc;
    this.cheNotification = cheNotification;
    this.lodash = lodash;
    this.$rootScope = $rootScope;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.stackMachines = {};

    this.selectSourceOption = 'select-source-recipe';

    // default RAM value
    this.workspaceRam = 2 * Math.pow(1024,3);

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      matchBrackets: true,
      mode: 'application/json'
    };

    // fetch default recipe if we haven't one
    if (!cheAPI.getRecipeTemplate().getDefaultRecipe()) {
      cheAPI.getRecipeTemplate().fetchDefaultRecipe();
    }

    this.stack = null;
    this.recipeUrl = null;
    this.recipeScript = null;
    this.recipeFormat = null;
    this.importWorkspace = '';
    this.defaultWorkspaceName = null;

    // channels on which we will subscribe on the workspace bus websocket
    this.listeningChannels = [];

    this.resetCreateProgress();

    this.usedNamesList = [];
    cheAPI.cheWorkspace.fetchWorkspaces().then(() => {
      let workspaces = cheAPI.cheWorkspace.getWorkspaces();
      workspaces.forEach((workspace) => {
        this.usedNamesList.push(workspace.config.name);
      });
    });

    $rootScope.showIDE = false;
  }

  /**
   * Callback when tab has been change
   * @param tabName  the select tab name
   */
  setStackTab(tabName) {
    if (tabName === 'custom-stack') {
      this.cheStackLibrarySelecter(null);
      this.isCustomStack = true;
      this.generateWorkspaceName();
    }
  }

  /**
   * Gets object keys from target object.
   *
   * @param targetObject
   * @returns [*]
   */
  getObjectKeys(targetObject) {
    return Object.keys(targetObject);
  }

  /**
   * Callback when stack has been set
   * @param stack  the selected stack
   */
  cheStackLibrarySelecter(stack) {
    if (stack) {
      this.isCustomStack = false;
      this.recipeUrl = null;
    }
    if (this.stack !== stack && stack && stack.workspaceConfig && stack.workspaceConfig.name) {
      let workspaceName = stack.workspaceConfig.name;
      if (this.usedNamesList.includes(workspaceName)) {
        this.generateWorkspaceName();
      } else {
        this.setWorkspaceName(workspaceName);
      }
    } else {
      this.generateWorkspaceName();
    }
    this.stack = stack;
  }

  /**
   * Set workspace name
   * @param name
   */
  setWorkspaceName(name) {
    if (!name) {
      return;
    }
    if (!this.defaultWorkspaceName || this.defaultWorkspaceName === this.workspaceName) {
      this.defaultWorkspaceName = name;
      this.workspaceName = angular.copy(name);
    }
  }

  /**
   * Generates a default workspace name
   */
  generateWorkspaceName() {
    // starts with wksp
    let name = 'wksp';
    name += '-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
    this.setWorkspaceName(name);
  }

  /**
   * Create a new workspace
   */
  createWorkspace() {
    let source = {};
    source.type = 'dockerfile';
    //User provides recipe URL or recipe's content:
    if (this.isCustomStack) {
      this.stack = null;
      source.type = 'environment';
      source.format = this.recipeFormat;
      if (this.recipeUrl && this.recipeUrl.length > 0) {
        source.location = this.recipeUrl;
        this.submitWorkspace(source);
      } else {
        source.content = this.recipeScript;
        this.submitWorkspace(source);
      }
    } else if (this.selectSourceOption === 'select-source-import') {
      this.importWorkspaceConfig.name = this.workspaceName;
      this.setEnvironment(this.importWorkspaceConfig);
      this.resetCreateProgress();
      this.setCreateProjectInProgress();
      this.createWorkspaceFromConfig(this.importWorkspaceConfig);
    } else {
      //check predefined recipe location
      if (this.stack && this.stack.source && this.stack.source.type === 'location') {
        this.recipeUrl = this.stack.source.origin;
        source.location = this.recipeUrl;
        this.submitWorkspace(source);
      } else {
        source = this.getSourceFromStack(this.stack);
        this.submitWorkspace(source);
      }
    }
  }

  /**
   * Perform actions when content of workspace config is changed.
   */
  onWorkspaceSourceChange() {
    this.importWorkspaceConfig = null;
    this.importWorkspaceMachines = null;

    if (this.importWorkspace && this.importWorkspace.length > 0) {
      try {
        this.importWorkspaceConfig = angular.fromJson(this.importWorkspace);
        this.workspaceName = this.importWorkspaceConfig.name;
        let environment = this.importWorkspaceConfig.environments[this.importWorkspaceConfig.defaultEnv];
        let recipeType = environment.recipe.type;
        let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
        this.importWorkspaceMachines = environmentManager.getMachines(environment);
      } catch (error) {

      }
    }
  }

  /**
   * Returns the list of environments to be displayed.
   *
   * @returns {*} list of environments
   */
  getEnvironments() {
    if (this.selectSourceOption === 'select-source-import') {
      return (this.importWorkspaceConfig && this.importWorkspaceConfig.environments) ? this.importWorkspaceConfig.environments : [];
    } else if (this.stack) {
      return this.stack.workspaceConfig.environments;
    }
    return [];
  }

  /**
   * Detects machine source from pointed stack.
   *
   * @param stack to retrieve described source
   * @returns {source} machine source config
   */
  getSourceFromStack(stack) {
    let source = {};
    source.type = 'dockerfile';

    switch (stack.source.type.toLowerCase()) {
      case 'image':
        source.content = 'FROM ' + stack.source.origin;
        break;
      case 'dockerfile':
        source.content = stack.source.origin;
        break;
      default:
        throw 'Not implemented';
    }

    return source;
  }

  /**
   * Submit a new workspace from current workspace name, source and workspace ram
   *
   * @param source machine source
   */
  submitWorkspace(source) {
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let stackWorkspaceConfig = this.stack ? this.stack.workspaceConfig : {};
    this.setEnvironment(stackWorkspaceConfig);
    let workspaceConfig = this.cheAPI.getWorkspace().formWorkspaceConfig(stackWorkspaceConfig, this.workspaceName, source, this.workspaceRam);

    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig, attributes);
    this.redirectAfterSubmitWorkspace(creationPromise);
  }


  /**
   * Handle the redirect for the given promise after workspace has been created
   * @param promise used to gather workspace data
   */
  redirectAfterSubmitWorkspace(promise) {
    promise.then((workspaceData) => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.updateRecentWorkspace(workspaceData.id);

      let infoMessage = 'Workspace ' + workspaceData.config.name + ' successfully created.';
      this.cheNotification.showInfo(infoMessage);
      this.cheAPI.cheWorkspace.fetchWorkspaces().then(() => {
        this.$location.path('/workspace/' + workspaceData.namespace + '/' +  workspaceData.config.name);
      });
    }, (error) => {
      let errorMessage = error.data.message ? error.data.message : 'Error during workspace creation.';
      this.cheNotification.showError(errorMessage);
    });
  }

  /**
   * Emit event to move workspace immediately
   * to top of the recent workspaces list
   *
   * @param workspaceId
   */
  updateRecentWorkspace(workspaceId) {
    this.$rootScope.$broadcast('recent-workspace:set', workspaceId);
  }

  getMachines(environment) {
    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);

    if (this.selectSourceOption === 'select-source-import') {
      return this.importWorkspaceMachines;
    }

    if (!this.stackMachines[this.stack.id]) {
      this.stackMachines[this.stack.id] = environmentManager.getMachines(environment);
    }

    return this.stackMachines[this.stack.id];
  }

  /**
   * Updates the workspace's environment with data entered by user.
   *
   * @param workspace workspace to update
   */
  setEnvironment(workspace) {
    if (!workspace.defaultEnv || !workspace.environments || workspace.environments.length === 0) {
      return;
    }

    let environment = workspace.environments[workspace.defaultEnv];
    if (!environment) {
      return;
    }

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    workspace.environments[workspace.defaultEnv] = environmentManager.getEnvironment(environment, this.getMachines(environment));
  }


  createWorkspaceFromConfig(workspaceConfig) {
    this.createProjectProgressorSvc.setWorkspaceOfProject(workspaceConfig.name);

    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig, {});
    creationPromise.then((workspace) => {
      this.createProjectProgressorSvc.setWorkspaceNamespace(workspace.namespace);
      this.updateRecentWorkspace(workspace.id);

      this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id).then(() => {
        this.subscribeStatusChannel(workspace, workspaceConfig.projects || [], workspaceConfig.commands || []);
      });

      this.$timeout(() => {
        let bus = this.cheAPI.getWebsocket().getBus(workspace.id);
        this.startWorkspace(bus, workspace);
      }, 1000);
    }, (error) => {
      if (error.data.message) {
        this.getCreationSteps()[this.getCurrentProgressStep()].logs = error.data.message;
      }
      this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
    });
  }

  /**
   * Subscribe on workspace status channel
   */
  subscribeStatusChannel(workspace, projects, commands) {
    this.cheAPI.getWorkspace().fetchStatusChange(workspace.id, 'ERROR').then((message) => {
      this.createProjectProgressorSvc.setCurrentProgressStep(2);
      this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
      // need to show the error
      this.$mdDialog.show(
        this.$mdDialog.alert()
          .title('Error when starting agent')
          .content('Unable to start workspace agent. Error when trying to start the workspace agent: ' + message.error)
          .ariaLabel('Workspace agent start')
          .ok('OK')
      );
    });

    this.cheAPI.getWorkspace().fetchStatusChange(workspace.id, 'RUNNING').then(() => {
      this.createProjectProgressorSvc.setCurrentProgressStep(2);
      let promiseWorkspace = this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
      promiseWorkspace.then(() => {
        let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(workspace.id);
        let bus = this.cheAPI.getWebsocket().getBus(workspace.id);
        this.connectToExtensionServer(websocketUrl, workspace.id, projects, commands, bus);
      });
    });
  }

  startWorkspace(bus, workspace) {
    // then we've to start workspace
    this.createProjectProgressorSvc.setCurrentProgressStep(1);

    let statusLink = this.lodash.find(workspace.links, (link) => {
      return link.rel === 'environment.status_channel';
    });

    let outputLink = this.lodash.find(workspace.links, (link) => {
      return link.rel === 'environment.output_channel';
    });

    let workspaceId = workspace.id;

    let agentChannel = 'workspace:' + workspace.id + ':ext-server:output';
    let statusChannel = statusLink ? statusLink.parameters[0].defaultValue : null;
    let outputChannel = outputLink ? outputLink.parameters[0].defaultValue : null;

    this.listeningChannels.push(agentChannel);
    bus.subscribe(agentChannel, (message) => {
      if (this.createProjectProgressorSvc.getCurrentProgressStep() < 2) {
        this.createProjectProgressorSvc.setCurrentProgressStep(2);
      }
      let agentStep = 2;
      if (this.getCreationSteps()[agentStep].logs.length > 0) {
        this.getCreationSteps()[agentStep].logs = this.getCreationSteps()[agentStep].logs + '\n' + message;
      } else {
        this.getCreationSteps()[agentStep].logs = message;
      }
    });

    if (statusChannel) {
      // for now, display log of status channel in case of errors
      this.listeningChannels.push(statusChannel);
      bus.subscribe(statusChannel, (message) => {
        message = this.getDisplayMachineLog(message);
        if (message.eventType === 'DESTROYED' && message.workspaceId === workspace.id) {
          this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;

          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Unable to start workspace')
              .content('Unable to start workspace. It may be linked to OutOfMemory or the container has been destroyed')
              .ariaLabel('Workspace start')
              .ok('OK')
          );
        }
        if (message.eventType === 'ERROR' && message.workspaceId === workspace.id) {
          this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
          let errorMessage = 'Error when trying to start the workspace';
          if (message.error) {
            errorMessage += ': ' + message.error;
          } else {
            errorMessage += '.';
          }
          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Error when starting workspace')
              .content('Unable to start workspace. ' + errorMessage)
              .ariaLabel('Workspace start')
              .ok('OK')
          );
        }
        this.$log.log('Status channel of workspaceID', workspaceId, message);
      });
    }

    if (outputChannel) {
      //this.listeningChannels.push(outputChannel);
      bus.subscribe(outputChannel, (message) => {
        message = this.getDisplayMachineLog(message);
        if (this.getCreationSteps()[this.getCurrentProgressStep()].logs.length > 0) {
          this.getCreationSteps()[this.getCurrentProgressStep()].logs = this.getCreationSteps()[this.getCurrentProgressStep()].logs + '\n' + message;
        } else {
          this.getCreationSteps()[this.getCurrentProgressStep()].logs = message;
        }
      });
    }

    let startWorkspacePromise = this.cheAPI.getWorkspace().startWorkspace(workspace.id, workspace.config.defaultEnv);
    startWorkspacePromise.then(() => {
      // update list of workspaces
      // for new workspace to show in recent workspaces
      this.cheAPI.cheWorkspace.fetchWorkspaces();
    }, (error) => {
      let errorMessage;

      if (!error || !error.data) {
        errorMessage = 'Unable to start this workspace.';
      } else if (error.data.errorCode === 10000 && error.data.attributes) {
        let attributes = error.data.attributes;

        errorMessage = 'Unable to start this workspace.' +
          ' There are ' + attributes.workspaces_count + ' running workspaces consuming ' +
          attributes.used_ram + attributes.ram_unit + ' RAM.' +
          ' Your current RAM limit is ' + attributes.limit_ram + attributes.ram_unit +
          '. This workspace requires an additional ' +
          attributes.required_ram + attributes.ram_unit + '.' +
          '  You can stop other workspaces to free resources.';
      } else {
        errorMessage = error.data.message;
      }

      this.cheNotification.showError(errorMessage);
      this.getCreationSteps()[this.getCurrentProgressStep()].logs = errorMessage;
      this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
    });
    return startWorkspacePromise;
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

  connectToExtensionServer(websocketURL, workspaceId, projects, commands, bus) {
    // try to connect
    let websocketStream = this.$websocket(websocketURL);

    // on success, create project
    websocketStream.onOpen(() => {
      let bus = this.cheAPI.getWebsocket().getExistingBus(websocketStream);
      this.importProjects(websocketStream, workspaceId, projects, commands, bus);
    });

    // on error, retry to connect or after a delay, abort
    let websocketReconnect = 10;
    websocketStream.onError((error) => {
      websocketReconnect--;
      if (this.websocketReconnect > 0) {
        this.$timeout(() => {
          this.connectToExtensionServer(websocketURL, workspaceId, projects, commands, bus);
        }, 1000);
      } else {
        this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
        this.$log.log('error when starting remote extension', error);
        // need to show the error
        this.$mdDialog.show(
          this.$mdDialog.alert()
            .title('Workspace Connection Error')
            .content('It seems that your workspace is running, but we cannot connect your browser to it. This commonly happens when Che was' +
            ' not configured properly. If your browser is connecting to workspaces running remotely, then you must start Che with the ' +
            '--remote:<ip-address> flag where the <ip-address> is the IP address of the node that is running your Docker workspaces.' +
            'Please restart Che with this flag. You can read about what this flag does and why it is essential at: ' +
            'https://eclipse-che.readme.io/docs/configuration#envrionment-variables')
            .ariaLabel('Project creation')
            .ok('OK')
        );
      }
    });
  }

  importProjects(websocketStream, workspaceId, projects, commands, bus) {
    this.updateRecentWorkspace(workspaceId);
    this.createProjectProgressorSvc.setCurrentProgressStep(3);

    projects.forEach((projectData) => {
      let channel = 'importProject:output:' + workspaceId + ':' + projectData.name;
      this.listeningChannels.push(channel);
      // on import
      bus.subscribe(channel, (message) => {
        this.getCreationSteps()[this.getCurrentProgressStep()].logs = message.line;
      });
    });

    let promise = this.createProjectSvc.importProjects(workspaceId, projects, commands);

    promise.then(() => {
      this.cheAPI.getWorkspace().fetchWorkspaces();
      this.cleanupChannels(websocketStream, bus);
      this.createProjectProgressorSvc.setCurrentProgressStep(4);
    }, (error) => {
      this.cleanupChannels(websocketStream, bus);
      this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
      //if we have a SSH error
      if (error.data && error.data.errorCode === 32068) {
        this.showAddSecretKeyDialog(projectData.source.location, workspaceId);
        return;
      }
      this.$mdDialog.show(
        this.$mdDialog.alert()
          .title('Error while creating the project')
          .content(error.statusText + ': ' + error.data.message)
          .ariaLabel('Project creation')
          .ok('OK')
      );
    });
  }

  /**
   * Cleanup the websocket elements after actions are finished
   */
  cleanupChannels(websocketStream, bus) {
    if (websocketStream != null) {
      websocketStream.close();
    }

    if (bus != null) {
      this.listeningChannels.forEach((channel) => {
        bus.unsubscribe(channel);
      });
      this.listeningChannels.length = 0;
    }
  }

  /**
   * Show the add ssh key dialog
   * @param repoURL  the repository URL
   * @param workspaceId  the workspace IDL
   */
  showAddSecretKeyDialog(repoURL, workspaceId) {
    let parentEl = angular.element(this.$document.body);

    this.$mdDialog.show({
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AddSecretKeyNotificationCtrl',
      controllerAs: 'addSecretKeyNotificationCtrl',
      locals: {repoURL: repoURL, workspaceId: workspaceId},
      parent: parentEl,
      templateUrl: 'app/projects/create-project/add-ssh-key-notification/add-ssh-key-notification.html'
    });
  }

  getStepText(stepNumber) {
    return this.createProjectProgressorSvc.getStepText(stepNumber);
  }

  getCreationSteps() {
    return this.createProjectProgressorSvc.getProjectCreationSteps();
  }

  getCurrentProgressStep() {
    return this.createProjectProgressorSvc.getCurrentProgressStep();
  }

  isCreateProjectInProgress() {
    return this.createProjectProgressorSvc.isCreateProjectInProgress();
  }

  setCreateProjectInProgress() {
    this.createProjectProgressorSvc.setCreateProjectInProgress(true);
  }

  getWorkspaceOfProject() {
    return this.createProjectProgressorSvc.getWorkspaceOfProject();
  }

  getIDELink() {
    return this.createProjectProgressorSvc.getIDELink();
  }

  resetCreateProgress() {
    this.createProjectProgressorSvc.resetCreateProgress();
  }

  isResourceProblem() {
    let currentCreationStep = this.getCreationSteps()[this.getCurrentProgressStep()];
    return currentCreationStep.hasError && currentCreationStep.logs.includes('You can stop other workspaces');
  }

  downloadLogs() {
    let logs = '';
    this.getCreationSteps().forEach((step) => {
      logs += step.logs + '\n';
    });
    this.$window.open('data:text/csv,' + encodeURIComponent(logs));
  }
}
