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
 * This class is handling the controller for the projects
 * @author Florent Benoit
 */
export class CreateProjectController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI, cheStack, $websocket, $routeParams, $filter, $timeout, $location, $mdDialog, $scope, $rootScope,
              createProjectProgressorSvc, createProjectSvc, lodash, cheNotification, $q, $log, $document, $window, cheEnvironmentRegistry) {
    this.$log = $log;
    this.cheAPI = cheAPI;
    this.cheStack = cheStack;
    this.$websocket = $websocket;
    this.$timeout = $timeout;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$scope = $scope;
    this.$rootScope = $rootScope;
    this.createProjectProgressorSvc = createProjectProgressorSvc;
    this.createProjectSvc = createProjectSvc;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.$q = $q;
    this.$document = $document;
    this.$window = $window;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.stackMachines = {};

    this.resetCreateProgress();

    // JSON used for import data
    this.importProjectData = this.getDefaultProjectJson();

    this.stackTab = 'ready-to-go';

    this.enableWizardProject = true;

    this.currentStackTags = null;

    // stacks not yet completed
    this.stacksInitialized = false;

    // keep references on workspaces and projects
    this.workspaces = [];

    // default options
    this.selectSourceOption = 'select-source-new';

    this.templatesChoice = 'templates-samples';

    // default RAM value for workspaces
    this.workspaceRam = 2 * Math.pow(1024,3);
    this.websocketReconnect = 50;

    this.generateWorkspaceName();

    this.messageBus = null;
    this.recipeUrl = null;
    this.recipeFormat = null;

    //search the selected tab
    let routeParams = $routeParams.tabName;
    if (!routeParams) {
      this.selectedTabIndex = 0;
    } else {
      switch (routeParams) {
        case 'blank':
          this.selectedTabIndex = 0;
          break;
        case 'samples':
          this.selectedTabIndex = 1;
          break;
        case 'git':
          this.selectedTabIndex = 2;
          break;
        case 'github':
          this.selectedTabIndex = 3;
          break;
        case 'zip':
          this.selectedTabIndex = 4;
          break;
        case 'config':
          this.selectedTabIndex = 2;
          break;
        default:
          $location.path('/create-project');
      }
    }

    if (cheStack.getStacks().length) {
      this.updateWorkspaces();
    } else {
      cheStack.fetchStacks().then(() => {
        this.updateWorkspaces();
      }, (error) => {
        if (error.status === 304) {
          this.updateWorkspaces();
          return;
        }
        this.state = 'error';
      });
    }

    // selected current tab
    this.currentTab = '';
    // all forms that we have
    this.forms = new Map();

    this.jsonConfig = {};
    this.jsonConfig.content = '{}';
    try {
      this.jsonConfig.content = $filter('json')(angular.fromJson(this.importProjectData), 2);
    } catch (e) {
      // ignore the error
    }

    let deregFunc1 = $rootScope.$on('create-project-stacks:initialized', () => {
      this.stacksInitialized = true;
    });

    // sets isReady status after selection
    let deregFunc2 = $rootScope.$on('create-project-github:selected', () => {
      if (!this.isReady && this.currentTab === 'github') {
        this.isReady = true;
      }
    });
    let deregFunc3 = $rootScope.$on('create-project-samples:selected', () => {
      if (!this.isReady && this.currentTab === 'samples') {
        this.isReady = true;
      }
    });
    $rootScope.$on('$destroy', () => {
      deregFunc1();
      deregFunc2();
      deregFunc3();
    });

    // channels on which we will subscribe on the workspace bus websocket
    this.listeningChannels = [];

    this.projectName = null;
    this.projectDescription = null;
    this.defaultWorkspaceName = null;

    cheAPI.cheWorkspace.getWorkspaces();

    $rootScope.showIDE = false;
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
   * Fetch workspaces when initializing
   */
  updateWorkspaces() {
    this.workspaces = this.cheAPI.getWorkspace().getWorkspaces();
      // fetch workspaces when initializing
    let promise = this.cheAPI.getWorkspace().fetchWorkspaces();
    promise.then(() => {
        this.updateData();
      },
      (error) => {
        // retrieve last data that were fetched before
        if (error.status === 304) {
          // ok
          this.updateData();
          return;
        }
        this.state = 'error';
      });
  }

  /**
   * Gets default project JSON used for import data
   */
  getDefaultProjectJson() {
    return {
      source: {
        location: '',
        parameters: {}
      },
      project: {
        name: '',
        description: ''
      }
    };
  }

  /**
   * Fetching operation has been done, so get workspaces and websocket connection
   */
  updateData() {
    this.workspaceResource = this.workspaces.length > 0 ? 'existing-workspace' : 'from-stack';
    //if create project in progress and workspace have started
    if (this.createProjectProgressorSvc.isCreateProjectInProgress() && this.createProjectProgressorSvc.getCurrentProgressStep() > 0) {
      let workspaceName = this.createProjectProgressorSvc.getWorkspaceOfProject();
      let findWorkspace = this.lodash.find(this.workspaces, (workspace) => {
        return workspace.config.name === workspaceName;
      });
      //check current workspace
      if (findWorkspace) {
        // init WS bus
        this.messageBus = this.cheAPI.getWebsocket().getBus(findWorkspace.id);
      } else {
        this.resetCreateProgress();
      }
    } else {
      let preselectWorkspaceId = this.$location.search().workspaceId;
      if (preselectWorkspaceId) {
        this.workspaceSelected = this.lodash.find(this.workspaces, (workspace) => {
          return workspace.id === preselectWorkspaceId;
        });
      }
      // generate project name
      this.generateProjectName(true);
    }
  }

  /**
   * Force codemirror editor to be refreshed
   */
  refreshCM() {
    // hack to make a refresh of the zone
    this.importProjectData.cm = 'aaa';
    this.$timeout(() => {
      delete this.importProjectData.cm;
    }, 500);
  }

  /**
   * Update internal json data from JSON codemirror editor config file
   */
  update() {
    try {
      this.importProjectData = angular.fromJson(this.jsonConfig.content);
    } catch (e) {
      // invalid JSON, ignore
    }

  }


  /**
   * Select the given github repository
   * @param gitHubRepository the repository selected
   */
  selectGitHubRepository(gitHubRepository) {
    this.setProjectName(gitHubRepository.name);
    this.setProjectDescription(gitHubRepository.description);
    this.importProjectData.source.location = gitHubRepository.clone_url;
  }


  /**
   * Checks if the current forms are being validated
   * @returns {boolean|FormController.$valid|*|ngModel.NgModelController.$valid|context.ctrl.$valid|Ic.$valid}
   */
  checkValidFormState() {
    // check project information form and selected tab form

    if (this.selectSourceOption === 'select-source-new') {
      return this.projectInformationForm && this.projectInformationForm.$valid;
    } else if (this.selectSourceOption === 'select-source-existing') {
      var currentForm = this.forms.get(this.currentTab);
      if (currentForm) {
        return this.projectInformationForm && this.projectInformationForm.$valid && currentForm.$valid;
      }
    }
  }

  /**
   * Defines the project information form
   * @param form
   */
  setProjectInformationForm(form) {
    this.projectInformationForm = form;
  }


  /**
   * Sets the form for a given mode
   * @param form the selected form
   * @param mode the tab selected
   */
  setForm(form, mode) {
    this.forms.set(mode, form);
  }

  /**
   * Sets the current selected tab
   * @param tab the selected tab
   */
  setCurrentTab(tab) {
    this.currentTab = tab;
    this.importProjectData = this.getDefaultProjectJson();

    if ('blank' === tab) {
      this.importProjectData.project.type = 'blank';
    } else if ('git' === tab || 'github' === tab) {
      this.importProjectData.source.type = 'git';
    } else if ('zip' === tab) {
      this.importProjectData.project.type = '';
      this.importProjectData.source.type = 'zip';
    } else if ('config' === tab) {
      this.importProjectData.project.type = 'blank';
      this.importProjectData.source.type = 'git';
      //try to set default values
      this.setProjectDescription(this.importProjectData.project.description);
      this.setProjectName(this.importProjectData.project.name);
      this.refreshCM();
    }
    // github and samples tabs have broadcast selection events for isReady status
    this.isReady = !('github' === tab || 'samples' === tab);
  }

  /**
   * Returns current selected tab
   * @returns {string|*}
   */
  getCurrentTab() {
    return this.currentTab;
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
      this.listeningChannels.push(outputChannel);
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
    return  startWorkspacePromise;
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

  createProjectInWorkspace(workspaceId, projectName, projectData, bus, websocketStream, workspaceBus) {
    projectData.name = projectName;
    this.updateRecentWorkspace(workspaceId);

    this.createProjectProgressorSvc.setCurrentProgressStep(3);

    var promise;
    var channel = null;
    // select mode (create or import)
    if (this.selectSourceOption === 'select-source-new' && this.templatesChoice === 'templates-wizard') {

      // we do not create project as it will be done through wizard
      var deferred = this.$q.defer();
      promise = deferred.promise;
      deferred.resolve(true);

    } else if (projectData.source.location.length > 0) {

      // if it's a user-defined location we need to cleanup commands that may have been configured by templates
      if (this.selectSourceOption === 'select-source-existing') {
        projectData.project.commands = [];
      }

      // websocket channel
      channel = 'importProject:output:' + workspaceId + ':' + projectName;

      // on import
      bus.subscribe(channel, (message) => {
        this.getCreationSteps()[this.getCurrentProgressStep()].logs = message.line;
      });

      promise = this.createProjectSvc.importProject(workspaceId, projectData);
    }

    promise.then(() => {
      this.cheAPI.getWorkspace().fetchWorkspaces();

      this.cleanupChannels(websocketStream, workspaceBus, bus, channel);
      this.createProjectProgressorSvc.setCurrentProgressStep(4);

      // redirect to IDE from crane loader page
      let currentPath = this.$location.path();
      if (/create-project/.test(currentPath)) {
        this.createProjectProgressorSvc.redirectToIDE();
      }
    }, (error) => {
      this.cleanupChannels(websocketStream, workspaceBus, bus, channel);
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

  /**
   * Cleanup the websocket elements after actions are finished
   */
  cleanupChannels(websocketStream, workspaceBus, bus, channel) {
    if (websocketStream != null) {
      websocketStream.close();
    }

    if (workspaceBus != null) {
      this.listeningChannels.forEach((channel) => {
        workspaceBus.unsubscribe(channel);
      });
      this.listeningChannels.length = 0;
    }

    if (channel != null) {
      bus.unsubscribe(channel);
    }


  }

  connectToExtensionServer(websocketURL, workspaceId, projectName, projectData, workspaceBus, bus) {

    // try to connect
    let websocketStream = this.$websocket(websocketURL);

    // on success, create project
    websocketStream.onOpen(() => {
      let bus = this.cheAPI.getWebsocket().getExistingBus(websocketStream);
      this.createProjectInWorkspace(workspaceId, projectName, projectData, bus, websocketStream, workspaceBus);
    });

    // on error, retry to connect or after a delay, abort
    websocketStream.onError((error) => {
      this.websocketReconnect--;
      if (this.websocketReconnect > 0) {
        this.$timeout(() => {
          this.connectToExtensionServer(websocketURL, workspaceId, projectName, projectData, workspaceBus, bus);
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

  /**
   * User has selected a stack. needs to find or add recipe for that stack
   */
  computeRecipeForStack(stack) {
    // look at recipe
    let recipeSource = stack.source;

    let promise;

    // what is type of source ?
    if ('image' === recipeSource.type) {
      // needs to add recipe for that script
      promise = this.submitRecipe('generated-' + stack.name, 'FROM ' + recipeSource.origin);
    } else if ('dockerfile' === recipeSource.type.toLowerCase()) {
      promise = this.submitRecipe('generated-' + stack.name, recipeSource.origin);
    } else {
      throw 'Not implemented';
    }

    return promise;
  }

  submitRecipe(recipeName, recipeScript) {
    let recipe = {
      type: 'docker',
      name: recipeName,
      script: recipeScript
    };

    return this.cheAPI.getRecipe().create(recipe);
  }

  /**
   * Call the create operation that may create or import a project
   */
  create() {
    this.importProjectData.project.description = this.projectDescription;
    this.importProjectData.project.name = this.projectName;
    this.createProjectProgressorSvc.setProject(this.projectName);

    if (this.templatesChoice === 'templates-wizard') {
      this.createProjectProgressorSvc.setIDEAction('createProject:projectName=' + this.projectName);
    }

    // reset logs and errors
    this.resetCreateProgress();
    this.setCreateProjectInProgress();

    let source = {};
    source.type = 'dockerfile';
    // logic to decide if we create workspace based on a stack or reuse existing workspace
    if (this.workspaceResource === 'existing-workspace') {
      // reuse existing workspace
      this.recipeUrl = null;
      this.stack = null;
      this.createProjectProgressorSvc.setWorkspaceOfProject(this.workspaceSelected.config.name);
      this.createProjectProgressorSvc.setWorkspaceNamespace(this.workspaceSelected.namespace);
      this.checkExistingWorkspaceState(this.workspaceSelected);
    } else {
      // create workspace based on a stack
      switch (this.stackTab) {
        case 'ready-to-go':
          source = this.getSourceFromStack(this.readyToGoStack);
          break;
        case 'stack-library':
          source = this.getSourceFromStack(this.stackLibraryUser);
          break;
        case 'custom-stack':
          source.type = 'environment';
          source.format = this.recipeFormat;
          if (this.recipeUrl && this.recipeUrl.length > 0) {
            source.location = this.recipeUrl;
          } else {
            source.content = this.recipeScript;
          }
          break;
      }
      this.createWorkspace(source);
    }
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
   * Check whether existing workspace in running (runtime should be present)
   *
   * @param workspace existing workspace
   */
  checkExistingWorkspaceState(workspace) {
    if (workspace.runtime) {
      let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(workspace.id);
      // Get bus
      let websocketStream = this.$websocket(websocketUrl);
      // on success, create project
      websocketStream.onOpen(() => {
        let bus = this.cheAPI.getWebsocket().getExistingBus(websocketStream);
        this.createProjectInWorkspace(workspace.id, this.projectName, this.importProjectData, bus);
      });
    } else {
      this.subscribeStatusChannel(workspace);
      let bus = this.cheAPI.getWebsocket().getBus(workspace.id);
      this.startWorkspace(bus, workspace);
    }
  }

  /**
   * Subscribe on workspace status channel
   *
   * @param workspace workspace for listening status
   */
  subscribeStatusChannel(workspace) {
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

      this.importProjectData.project.name = this.projectName;

      let promiseWorkspace = this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
      promiseWorkspace.then(() => {
        let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(workspace.id),
          bus = this.cheAPI.getWebsocket().getBus(workspace.id);
        // try to connect
        this.websocketReconnect = 10;
        this.connectToExtensionServer(websocketUrl, workspace.id, this.importProjectData.project.name, this.importProjectData, bus);
      });
    });
  }

  /**
   * Create new workspace with provided machine source.
   *
   * @param source machine source
   */
  createWorkspace(source) {
    this.createProjectProgressorSvc.setWorkspaceOfProject(this.workspaceName);

    let attributes = this.stack ? {stackId: this.stack.id} : {};
    let stackWorkspaceConfig = this.stack ? this.stack.workspaceConfig : {};
    this.setEnvironment(stackWorkspaceConfig);
    let workspaceConfig = this.cheAPI.getWorkspace().formWorkspaceConfig(stackWorkspaceConfig, this.workspaceName, source, this.workspaceRam);

    //TODO: no account in che ? it's null when testing on localhost
    let creationPromise = this.cheAPI.getWorkspace().createWorkspaceFromConfig(null, workspaceConfig, attributes);
    creationPromise.then((workspace) => {
      this.createProjectProgressorSvc.setWorkspaceNamespace(workspace.namespace);
      this.updateRecentWorkspace(workspace.id);

      // init message bus if not there
      if (this.workspaces.length === 0) {
        this.messageBus = this.cheAPI.getWebsocket().getBus(workspace.id);
      }

      this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id).then(() => {
        this.subscribeStatusChannel(workspace);
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
   * Generates a default project name only if user has not entered any data
   * @param firstInit on first init, user do not have yet initialized something
   */
  generateProjectName(firstInit) {
    // name has not been modified by the user
    if (firstInit || (this.projectInformationForm['deskname'].$pristine && this.projectInformationForm.name.$pristine)) {
      // generate a name

      // starts with project
      var name = 'project';

      // type selected
      if (this.importProjectData.project.type) {
        name = this.importProjectData.project.type.replace(/\s/g, '_');
      }

      name = name + '-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line

      this.setProjectName(name);
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

  isImporting() {
    return this.isCreateProjectInProgress();
  }

  isReadyToCreate() {
    let isCreateProjectInProgress = this.isCreateProjectInProgress();

    if (!this.isCustomStack) {
      return !isCreateProjectInProgress && this.isReady
    }

    let isRecipeUrl = this.recipeUrl && this.recipeUrl.length > 0;
    let isRecipeScript = this.recipeScript && this.recipeScript.length > 0;

    return !isCreateProjectInProgress && this.isReady && (isRecipeUrl || isRecipeScript);
  }

  resetCreateProgress() {
    if (this.isResourceProblem()) {
      this.$location.path('/workspaces');
    }
    this.createProjectProgressorSvc.resetCreateProgress();
  }

  resetCreateNewProject() {
    this.resetCreateProgress();
    this.generateWorkspaceName();
    this.generateProjectName(true);
  }

  showIDE() {
    this.$rootScope.showIDE = !this.$rootScope.showIDE;
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

  isResourceProblem() {
    let currentCreationStep = this.getCreationSteps()[this.getCurrentProgressStep()];
    return currentCreationStep.hasError && currentCreationStep.logs.includes('You can stop other workspaces');
  }

  setStackTab(stackTab) {
    this.isCustomStack = stackTab === 'custom-stack';
    this.stackTab = stackTab;
  }

  /**
   * Update data for selected workspace
   */
  onWorkspaceChange() {
    if (!this.workspaceSelected) {
      return;
    }
    this.setWorkspaceName(this.workspaceSelected.config.name);
    let stack = null;
    if (this.workspaceSelected.attributes && this.workspaceSelected.attributes.stackId) {
      stack = this.cheStack.getStackById(this.workspaceSelected.attributes.stackId);
    }
    this.updateCurrentStack(stack);
    let defaultEnvironment = this.workspaceSelected.config.defaultEnv;
    let environment = this.workspaceSelected.config.environments[defaultEnvironment];
   /* TODO not implemented yet if (environment) {
      this.workspaceRam = environment.machines[0].limits.ram;
    }*/
    this.updateWorkspaceStatus(true);
  }

  /**
   * Update creation flow state when source option changes
   */
  onSourceOptionChanged() {
    if ('select-source-existing' === this.selectSourceOption) {
      //Need to call selection of current tab
      this.setCurrentTab(this.currentTab);
    }
  }

  /**
   * Use of an existing stack
   * @param stack the stack to use
   */
  cheStackLibrarySelecter(stack) {
    if (this.workspaceResource === 'existing-workspace') {
      return;
    }
    if (this.stackTab === 'ready-to-go') {
      this.readyToGoStack = angular.copy(stack);
    } else if (this.stackTab === 'stack-library') {
      this.stackLibraryUser = angular.copy(stack);
    }
    this.updateCurrentStack(stack);
    this.updateWorkspaceStatus(false);
  }

  updateWorkspaceStatus(isExistingWorkspace) {
    if (isExistingWorkspace) {
      this.stackLibraryOption = 'existing-workspace';
    } else {
      this.stackLibraryOption = 'new-workspace';
      this.generateWorkspaceName();
    }
    this.$rootScope.$broadcast('chePanel:disabled', {id: 'create-project-workspace', disabled: isExistingWorkspace});
  }

  /**
   * Update current stack
   * @param stack the stack to use
   */
  updateCurrentStack(stack) {
    this.stack = stack;
    this.currentStackTags = stack && stack.tags ? angular.copy(stack.tags) : null;
    if (!stack) {
      return;
    }

    this.templatesChoice = 'templates-samples';
    this.generateProjectName(true);
    // Enable wizard only if
    // - ready-to-go-stack with PT
    // - custom stack
    if (stack === null || 'general' !== stack.scope) {
      this.enableWizardProject = true;
      return;
    }
    this.enableWizardProject  = 'Java' === stack.name;
  }

  selectWizardProject() {
    this.importProjectData.source.location = '';
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
   * Set project name
   * @param name
   */
  setProjectName(name) {
    if (!name) {
      return;
    }
    if (!this.projectName || !this.defaultProjectName || this.defaultProjectName === this.projectName) {
      this.defaultProjectName = name;
      this.projectName = angular.copy(name);
    }
    this.importProjectData.project.name = this.projectName;
  }

  /**
   * Set project description
   * @param description
   */
  setProjectDescription(description) {
    if (!description) {
      return;
    }
    if (!this.projectDescription || !this.defaultProjectDescription || this.defaultProjectDescription === this.projectDescription) {
      this.defaultProjectDescription = description;
      this.projectDescription = angular.copy(description);
    }
    this.importProjectData.project.description = this.projectDescription;
  }

  downloadLogs() {
    let logs = '';
    this.getCreationSteps().forEach((step) => {
      logs += step.logs + '\n';
    });
    this.$window.open('data:text/csv,' + encodeURIComponent(logs));
  }

  /**
   * Returns list of projects of current workspace
   * @returns {*|Array}
   */
  getWorkspaceProjects() {
    if (this.workspaceSelected && this.workspaceResource === 'existing-workspace') {
      let projects = this.cheAPI.getWorkspace().getWorkspaceProjects()[this.workspaceSelected.id];
      return projects;
    }
    return [];
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

  getStackMachines(environment) {
    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
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
    workspace.environments[workspace.defaultEnv] = environmentManager.getEnvironment(environment, this.getStackMachines(environment));
  }
}
