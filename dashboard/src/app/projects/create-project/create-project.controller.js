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
/*global $:false, window:false */

/**
 * This class is handling the controller for the projects
 * @author Florent Benoit
 */
export class CreateProjectCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI, cheStack, $websocket, $routeParams, $filter, $timeout, $location, $mdDialog, $scope, $rootScope, createProjectSvc, lodash, $q, $log, $document) {
    this.$log = $log;
    this.cheAPI = cheAPI;
    this.cheStack = cheStack;
    this.$websocket = $websocket;
    this.$timeout = $timeout;
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.$scope = $scope;
    this.$rootScope = $rootScope;
    this.createProjectSvc = createProjectSvc;
    this.lodash = lodash;
    this.$q = $q;
    this.$document = $document;

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
    this.workspaceRam = 1000;
    this.websocketReconnect = 50;

    this.generateWorkspaceName();

    this.headerSteps = [
      {
        id: '#create-project-source-id',
        name: 'source',
        link: 'create-project-source'
      },
      {
        id: '#create-project-source-stack',
        name: 'stack',
        link: 'create-project-stack'
      },
      {
        id: '#create-project-workspace',
        name: 'workspace',
        link: 'create-project-workspace'
      },
      {
        id: '#create-project-source-template',
        name: 'template',
        link: 'create-project-template'
      },
      {
        id: '#create-project-source-information',
        name: 'metadata',
        link: 'create-project-information'
      }
    ];

    this.messageBus = null;
    this.recipeUrl = null;

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

    $rootScope.$on('create-project-stacks:initialized', () => {
      this.stacksInitialized = true;
    });

    // sets isReady status after selection
    $rootScope.$on('create-project-github:selected', () => {
      if (!this.isReady && this.currentTab === 'github') {
        this.isReady = true;
      }
    });
    $rootScope.$on('create-project-samples:selected', () => {
      if (!this.isReady && this.currentTab === 'samples') {
        this.isReady = true;
      }
    });

    // channels on which we will subscribe on the workspace bus websocket
    this.listeningChannels = [];

    this.projectName = null;
    this.projectDescription = null;
    this.defaultWorkspaceName = null;
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
    if (this.createProjectSvc.isCreateProjectInProgress() && this.createProjectSvc.getCurrentProgressStep() > 0) {
      let workspaceName = this.createProjectSvc.getWorkspaceOfProject();
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


  startWorkspace(bus, workspace) {
    // then we've to start workspace
    this.createProjectSvc.setCurrentProgressStep(1);
    // get channels
    let environments = workspace.config.environments;
    let envName = workspace.config.defaultEnv;
    let defaultEnvironment = this.lodash.find(environments, (environment) => {
      return environment.name === envName;
    });

    let machineConfigsLinks = defaultEnvironment.machineConfigs[0].links;

    let findStatusLink = this.lodash.find(machineConfigsLinks, (machineConfigsLink) => {
      return machineConfigsLink.rel === 'get machine status channel';
    });

    let findOutputLink = this.lodash.find(machineConfigsLinks, (machineConfigsLink) => {
      return machineConfigsLink.rel === 'get machine logs channel';
    });

    let workspaceId = workspace.id;

    let agentChannel = 'workspace:' + workspace.id + ':ext-server:output';
    let statusChannel = findStatusLink ? findStatusLink.parameters[0].defaultValue : null;
    let outputChannel = findOutputLink ? findOutputLink.parameters[0].defaultValue : null;

    this.listeningChannels.push(agentChannel);
    bus.subscribe(agentChannel, (message) => {
      if (this.createProjectSvc.getCurrentProgressStep() < 2) {
        this.createProjectSvc.setCurrentProgressStep(2);
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
          // need to show the error
          this.$mdDialog.show(
              this.$mdDialog.alert()
                  .title('Error when starting workspace')
                  .content('Unable to start workspace. Error when trying to start the workspace: ' + message.error)
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
        if (this.getCreationSteps()[this.getCurrentProgressStep()].logs.length > 0) {
          this.getCreationSteps()[this.getCurrentProgressStep()].logs = this.getCreationSteps()[this.getCurrentProgressStep()].logs + '\n' + message;
        } else {
          this.getCreationSteps()[this.getCurrentProgressStep()].logs = message;
        }
      });
    }
    
    this.cheAPI.getWorkspace().startWorkspace(workspace.id, workspace.config.defaultEnv);
  }

  createProjectInWorkspace(workspaceId, projectName, projectData, bus, websocketStream, workspaceBus) {
    this.createProjectSvc.setCurrentProgressStep(3);

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

      let deferredImport = this.$q.defer();
      let deferredImportPromise = deferredImport.promise;
      let deferredAddCommand = this.$q.defer();
      let deferredAddCommandPromise = deferredAddCommand.promise;
      let deferredResolve = this.$q.defer();
      let deferredResolvePromise = deferredResolve.promise;

      let importPromise = this.cheAPI.getProject().importProject(workspaceId, projectName, projectData.source);

      importPromise.then(() => {
        // add commands if there are some that have been defined
        let commands = projectData.project.commands;
        if (commands && commands.length > 0) {
          this.addCommand(workspaceId, projectName, commands, 0, deferredAddCommand);
        } else {
          deferredAddCommand.resolve('no commands to add');
        }
        deferredImport.resolve();
      }, (error) => {
        deferredImport.reject(error);
      });

      // now, resolve the project
      deferredImportPromise.then(() => {
        this.resolveProjectType(workspaceId, projectName, projectData, deferredResolve);
      });
      promise = this.$q.all([deferredImportPromise, deferredAddCommandPromise, deferredResolvePromise]);
    }
    promise.then(() => {
      this.cleanupChannels(websocketStream, workspaceBus, bus, channel);
      this.createProjectSvc.setCurrentProgressStep(4);
    }, (error) => {
      this.cleanupChannels(websocketStream, workspaceBus, bus, channel);
      this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
      //if we have a SSH error
      if (error.data.errorCode === 32068) {
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

  resolveProjectType(workspaceId, projectName, projectData, deferredResolve) {
    let projectDetails = projectData.project;
    if (!projectDetails.attributes) {
      projectDetails.source = projectData.source;
      projectDetails.attributes = {};
    }

    if (projectDetails.type) {
      let updateProjectPromise = this.cheAPI.getProject().updateProject(workspaceId, projectName, projectDetails);
      updateProjectPromise.then(() => {
        deferredResolve.resolve();
      });
      return;
    }

    let resolvePromise = this.cheAPI.getProject().fetchResolve(workspaceId, projectName);
    resolvePromise.then(() => {
      let resultResolve = this.cheAPI.getProject().getResolve(workspaceId, projectName);
      // get project-types
      let fetchTypePromise = this.cheAPI.getProjectType().fetchTypes(workspaceId);
      fetchTypePromise.then(() => {
        let projectTypesByCategory = this.cheAPI.getProjectType().getProjectTypesIDs(workspaceId);
        // now try the estimate for each source
        let deferredEstimate = this.$q.defer();
        let deferredEstimatePromise = deferredResolve.promise;


        let estimatePromises = [];
        let estimateTypes = [];
        resultResolve.forEach((sourceResolve) => {
          // add attributes if any
          if (sourceResolve.attributes && Object.keys(sourceResolve.attributes).length > 0) {
            for (let attributeKey in sourceResolve.attributes) {
              projectDetails.attributes[attributeKey] = sourceResolve.attributes[attributeKey];
            }
          }
          let projectType = projectTypesByCategory.get(sourceResolve.type);
          if (projectType.primaryable) {
            // call estimate
            let estimatePromise = this.cheAPI.getProject().fetchEstimate(workspaceId, projectName, sourceResolve.type);
            estimatePromises.push(estimatePromise);
            estimateTypes.push(sourceResolve.type);
          }
        });

        if (estimateTypes.length > 0) {
          // wait estimate are all finished
          let waitEstimate = this.$q.all(estimatePromises);

          waitEstimate.then(() => {
            var firstMatchingType;
            var firstMatchingResult;
            estimateTypes.forEach((type) => {
              let resultEstimate = this.cheAPI.getProject().getEstimate(workspaceId, projectName, type);
              // add attributes
              // there is a matching estimate
              if (Object.keys(resultEstimate.attributes).length > 0 && 'java' !== type && !firstMatchingType) {
                firstMatchingType = type;
                firstMatchingResult = resultEstimate.attributes;
              }
            });

          if (firstMatchingType) {
            projectDetails.attributes = firstMatchingResult;
            projectDetails.type = firstMatchingType;
            let updateProjectPromise = this.cheAPI.getProject().updateProject(workspaceId, projectName, projectDetails);
            updateProjectPromise.then(() => {
              deferredResolve.resolve();
            });
          } else {
            deferredResolve.resolve();
          }
          });
        } else {
          deferredResolve.resolve();
        }
      });

    }, (error) => {
      deferredResolve.reject(error);
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


  /**
   * Add commands sequentially by iterating on the number of the commands.
   * Wait the ack of remote addCommand before adding a new command to avoid concurrent access
   * @param workspaceId the ID of the workspace to use for adding commands
   * @param projectName the name that will be used to prefix the commands inserted
   * @param commands the array to follow
   * @param index the index of the array of commands to register
   */
  addCommand(workspaceId, projectName, commands, index, deferred) {
    if (index < commands.length) {
      let newCommand = angular.copy(commands[index]);

      // Update project command lines using current.project.path with actual path based on workspace runtime configuration
      // so adding the same project twice allow to use commands for each project without first selecting project in tree
      let workspace = this.cheAPI.getWorkspace().getWorkspaceById(workspaceId);
      if (workspace && workspace.runtime) {
        let runtime = workspace.runtime.devMachine.runtime;
        if (runtime) {
          let envVar = runtime.envVariables;
          if (envVar) {
            let cheProjectsRoot = envVar['CHE_PROJECTS_ROOT'];
            if (cheProjectsRoot) {
              // replace current project path by the full path of the project
              let projectPath = cheProjectsRoot + '/' + projectName;
              newCommand.commandLine = newCommand.commandLine.replace(/\$\{current.project.path\}/g, projectPath);
            }
          }
        }
      }
      newCommand.name = projectName + ': ' + newCommand.name;
      var addPromise = this.cheAPI.getWorkspace().addCommand(workspaceId, newCommand);
      addPromise.then(() => {
        // call the method again
        this.addCommand(workspaceId, projectName, commands, ++index, deferred);
      }, (error) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve('All commands added');
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
    } else if ('recipe' === recipeSource.type) {

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
      permissions: {
        groups: [
          {
            name: 'public',
            acl: [
              'read'
            ]
          }
        ],
        users: {}
      },
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
    this.createProjectSvc.setProject(this.projectName);

    if (this.templatesChoice === 'templates-wizard') {
      this.createProjectSvc.setIDEAction('createProject:projectName=' + this.projectName);
    }

    // reset logs and errors
    this.resetCreateProgress();
    this.setCreateProjectInProgress();

    this.createProjectSvc.createPopup();

    // logic to decide if we create workspace based on a stack or reuse existing workspace
    let option;

    if (this.workspaceResource === 'existing-workspace') {
      option = 'reuse-workspace';
      this.recipeUrl = null;
      this.stack = null;
    } else {
      switch (this.stackTab) {
        case 'ready-to-go':
          option = 'create-workspace';
          this.stack = this.readyToGoStack;
          break;
        case 'stack-library':
          option = 'create-workspace';
          this.stack = this.stackLibraryUser;
          break;
        case 'custom-stack':
          option = 'create-workspace';
          this.stack = null;
          break;
      }
    }
    // check workspace is selected
    if (option === 'create-workspace') {
      if (this.stack) {
        // needs to get recipe URL from stack
        let promise = this.computeRecipeForStack(this.stack);
        promise.then((recipe) => {
          let findLink = this.lodash.find(recipe.links, (link) => {
            return link.rel === 'get recipe script';
          });
          if (findLink) {
            this.recipeUrl = findLink.href;
            this.createWorkspace();
          }
        });
      } else {
        if (this.recipeUrl && this.recipeUrl.length > 0) {
          this.createWorkspace();
        } else {
          let recipeName = 'rcp-' + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4)); // jshint ignore:line
          // needs to get recipe URL from custom recipe
          let promise = this.submitRecipe(recipeName, this.recipeScript);
          promise.then((recipe) => {
            let findLink = this.lodash.find(recipe.links, (link) => {
              return link.rel === 'get recipe script';
            });
            if (findLink) {
              this.recipeUrl = findLink.href;
              this.createWorkspace();
            }
          });
        }
      }
    } else {
      this.createProjectSvc.setWorkspaceOfProject(this.workspaceSelected.config.name);
      // Now that the container is started, wait for the extension server. For this, needs to get runtime details
      let promiseWorkspace = this.cheAPI.getWorkspace().fetchWorkspaceDetails(this.workspaceSelected.id);
      promiseWorkspace.then(() => {
        let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(this.workspaceSelected.id);
        // Get bus
        let websocketStream = this.$websocket(websocketUrl);
        // on success, create project
        websocketStream.onOpen(() => {
          let bus = this.cheAPI.getWebsocket().getExistingBus(websocketStream);
          // mode
          this.createProjectInWorkspace(this.workspaceSelected.id, this.projectName, this.importProjectData, bus);
        });
      }, (error) => {
        if (error.data.message) {
          this.getCreationSteps()[this.getCurrentProgressStep()].logs = error.data.message;
        }
        this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
      });
    }
    // do we have projects ?
    let projects = this.cheAPI.getProject().getAllProjects();
    if (projects.length > 1) {
      // we have projects, show notification first and redirect to the list of projects
      this.createProjectSvc.showPopup();
      this.$location.path('/projects');
    }
  }

  /**
   * Create a new workspace from current workspace name, recipe url and workspace ram
   */
  createWorkspace() {
    this.createProjectSvc.setWorkspaceOfProject(this.workspaceName);
    let attributes = this.stack ? {stackId: this.stack.id} : {};
    //TODO: no account in che ? it's null when testing on localhost
    let creationPromise = this.cheAPI.getWorkspace().createWorkspace(null, this.workspaceName, this.recipeUrl, this.workspaceRam, attributes);
    creationPromise.then((workspace) => {
      // init message bus if not there
      if (this.workspaces.length === 0) {
        this.messageBus = this.cheAPI.getWebsocket().getBus(workspace.id);
      }
      // recipe url
      let bus = this.cheAPI.getWebsocket().getBus(workspace.id);
      // subscribe to workspace events
      let workspaceChannel = 'workspace:' + workspace.id;
      this.listeningChannels.push(workspaceChannel);
      bus.subscribe(workspaceChannel, (message) => {

        if (message.eventType === 'ERROR' && message.workspaceId === workspace.id) {
          this.createProjectSvc.setCurrentProgressStep(2);
          this.getCreationSteps()[this.getCurrentProgressStep()].hasError = true;
          // need to show the error
          this.$mdDialog.show(
            this.$mdDialog.alert()
              .title('Error when starting agent')
              .content('Unable to start workspace agent. Error when trying to start the workspace agent: ' + message.error)
              .ariaLabel('Workspace agent start')
              .ok('OK')
          );
        }

        if (message.eventType === 'RUNNING' && message.workspaceId === workspace.id) {
          this.createProjectSvc.setCurrentProgressStep(2);

          this.importProjectData.project.name = this.projectName;

          let promiseWorkspace = this.cheAPI.getWorkspace().fetchWorkspaceDetails(workspace.id);
          promiseWorkspace.then(() => {
            let websocketUrl = this.cheAPI.getWorkspace().getWebsocketUrl(workspace.id);
            // try to connect
            this.websocketReconnect = 10;
            this.connectToExtensionServer(websocketUrl, workspace.id, this.importProjectData.project.name, this.importProjectData, bus);

          });
        }
      });
      this.$timeout(() => {
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
    return !this.isCreateProjectInProgress() && this.isReady;
  }

  resetCreateProgress() {
    this.createProjectSvc.resetCreateProgress();
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
    return this.createProjectSvc.getStepText(stepNumber);
  }

  getCreationSteps() {
    return this.createProjectSvc.getProjectCreationSteps();
  }

  getCurrentProgressStep() {
    return this.createProjectSvc.getCurrentProgressStep();
  }

  isCreateProjectInProgress() {
    return this.createProjectSvc.isCreateProjectInProgress();
  }

  setCreateProjectInProgress() {
    this.createProjectSvc.setCreateProjectInProgress(true);
  }

  hideCreateProjectPanel() {
    this.createProjectSvc.showPopup();
  }

  getWorkspaceOfProject() {
    return this.createProjectSvc.getWorkspaceOfProject();
  }


  getIDELink() {
    return this.createProjectSvc.getIDELink();
  }

  isElementVisible(index) {

    // for each id, check last
    var maxVisibleElement = 0;
    for (var i = 0; i < this.headerSteps.length; i++) {
      var visibleElement = this.isvisible(this.headerSteps[i].id);
      if (visibleElement) {
        maxVisibleElement = i;
      }
    }
    return index <= maxVisibleElement;
  }


  isvisible(elementName) {
    let element = angular.element(elementName);
    var windowElement = $(window);

    var docViewTop = windowElement.scrollTop();
    var docViewBottom = docViewTop + windowElement.height();

    var offset = element.offset();
    if (!offset) {
      return false;
    }

    var elemTop = offset.top;
    var elemBottom = elemTop + element.height();

    // use elemTop if want to see all div or elemBottom if we see partially it
    /*((elemTop <= docViewBottom) && (elemTop >= docViewTop));*/
    return ((elemBottom <= docViewBottom) && (elemTop >= docViewTop));
  }

  setStackTab(stackTab) {
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
    let findEnvironment = this.lodash.find(this.workspaceSelected.config.environments, (environment) => {
      return environment.name === this.workspaceSelected.config.defaultEnv;
    });
    if (findEnvironment) {
      this.workspaceRam = findEnvironment.machineConfigs[0].limits.ram;
    }
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
    window.open('data:text/csv,' + encodeURIComponent(logs));
  }
}
