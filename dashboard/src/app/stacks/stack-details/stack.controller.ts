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
import {CheStack} from '../../../components/api/che-stack.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheUIElementsInjectorService} from '../../../components/injector/che-ui-elements-injector.service';
import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import {ImportStackService} from './import-stack.service';

const STACK_TEST_POPUP_ID: string = 'stackTestPopup';

/**
 * Controller for stack management - creation or edit.
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class StackController {
  GENERAL_SCOPE: string = 'general';
  ADVANCED_SCOPE: string = 'advanced';
  $q: ng.IQService;
  $log: ng.ILogService;
  $route: ng.route.IRoute;
  $scope: ng.IScope;
  $document: ng.IDocumentService;
  $filter: ng.IFilterService;
  $timeout: ng.ITimeoutService;
  $location: ng.ILocationService;
  $mdDialog: ng.material.IDialogService;
  cheUIElementsInjectorService: CheUIElementsInjectorService;
  importStackService: ImportStackService;
  cheStack: CheStack;
  cheWorkspace: CheWorkspace;
  cheNotification: CheNotification;
  showIDE: boolean;
  loading: boolean;
  isLoading: boolean;
  isImport: boolean;
  isCreation: boolean;
  isStackChange: boolean;
  stackId: string;
  tmpWorkspaceId: string;
  stackName: string;
  stackJson: string;
  invalidStack: string;
  stackTags: Array<string>;
  stack: any;
  copyStack: any;
  editorOptions: any;
  machinesViewStatus: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $timeout: ng.ITimeoutService, $route: ng.route.IRoute, $location: ng.ILocationService, $log: ng.ILogService, $filter: ng.IFilterService, cheStack: CheStack, cheWorkspace: CheWorkspace, $mdDialog: ng.material.IDialogService, cheNotification: CheNotification, $document: ng.IDocumentService, cheUIElementsInjectorService: CheUIElementsInjectorService, $scope: ng.IScope, $window: ng.IWindowService, importStackService: ImportStackService) {
    this.$q = $q;
    this.$timeout = $timeout;
    this.$location = $location;
    this.$log = $log;
    this.$route = $route;
    this.$scope = $scope;
    this.$filter = $filter;
    this.cheStack = cheStack;
    this.cheWorkspace = cheWorkspace;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.$document = $document;
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;
    this.importStackService = importStackService;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      matchBrackets: true,
      mode: 'application/json',
      onLoad: (editor: {refresh: Function}) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    this.machinesViewStatus = {};
    let routeParam: string = ($route as any).current.params.stackId;

    switch (routeParam) {
      case 'import' :
        this.isCreation = true;
        this.isImport = true;
        break;
      case 'create':
        this.isCreation = true;
        this.isImport = false;
        break;
      default:
        this.isCreation = false;
        this.isImport = false;
    }

    this.copyStack = {};
    this.stackTags = [];

    if (!this.cheStack.getStacks().length) {
      this.loading = true;
      this.cheStack.fetchStacks().finally(() => {
        this.loading = false;
        this.updateData();
      });
    } else {
      this.updateData();
    }

    $window.addEventListener('message', (event: {data: string}) => {
      if (!this.showIDE && 'show-ide' === event.data) {
        this.showIDE = true;
        this.$scope.$digest();
      }
    }, false);
  }

  /**
   * Update stack's data
   */
  updateData(): void {
    if (this.isCreation) {
      this.stack = this.getNewStackTemplate();
      this.stackTags = angular.copy(this.stack.tags);
      this.stackName = angular.copy(this.stack.name);
    } else {
      this.stackId = (this.$route as any).current.params.stackId;
      this.fetchStack();
    }
  }

  /**
   * Check if the name is unique.
   * @param name {string}
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    if (this.copyStack && this.copyStack.name === name) {
      return true;
    }
    return this.cheStack.isUniqueName(name);
  }

  /**
   * Cancels stack's changes
   */
  cancelStackChanges(): void {
    if (!this.copyStack) {
      return;
    }
    if (this.isCreation) {
      this.$location.path('/stacks');
    }
    this.stack = angular.copy(this.copyStack);
    this.stackName = angular.copy(this.stack.name);

    if (this.stack.tags && this.stack.tags.isArray) {
      this.stackTags = angular.copy(this.stack.tags);
    } else {
      this.stackTags = [];
    }

    this.stackTags = this.stack.tags ? angular.copy(this.stack.tags) : [];
    this.updateJsonFromStack();
  }

  /**
   * Returns template for the new stack.
   *
   * @returns {che.IStack} new stack template
   */
  getNewStackTemplate(): che.IStack {
    let stack: che.IStack = this.isImport ? this.importStackService.getStack() : this.cheStack.getStackTemplate();
    this.stackName = stack.name;
    return stack;
  }

  /**
   * Fetch the stack details.
   */
  fetchStack(): void {
    this.loading = true;
    this.stack = this.cheStack.getStackById(this.stackId);

    if (this.stack) {
      this.loading = false;
      this.prepareStackData();
      return;
    }

    this.cheStack.fetchStack(this.stackId).then((stack: any) => {
      this.stack = stack;
      this.loading = false;
      this.prepareStackData();
    }, (error: any) => {
      if (error.status === 304) {
        this.loading = false;
        this.stack = this.cheStack.getStackById(this.stackId);
        this.prepareStackData();
      } else {
        this.$log.error(error);
        this.loading = false;
        this.invalidStack = error.statusText + error.status;
      }
    });
  }

  /**
   * Handle stack's tag adding.
   *
   * @param tag {string} stack's tag
   * @returns {string} tag if it is unique one, otherwise null
   */
  handleTagAdding(tag: string): string {
    // prevents mentioning same tags twice:
    if (this.stackTags.indexOf(tag) > -1) {
      return null;
    }

    return tag;
  }

  /**
   * Reset stack's tags.
   */
  resetTags(): void {
    if (!this.stack || !this.stack.tags) {
      return;
    }
    this.stack.tags.length = 0;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack name info.
   * @param isFormValid {Boolean} true if form is valid
   */
  updateStackName(isFormValid: boolean): void {
    if (isFormValid === false) {
      return;
    }
    this.stack.name = this.stackName;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack tags info.
   */
  updateStackTags(): void {
    if (!this.stack.tags) {
      this.stack.tags = [];
    }
    this.stack.tags = angular.copy(this.stackTags);
    this.updateJsonFromStack();
  }

  /**
   * Update stack's editor json from stack.
   */
  updateJsonFromStack(): void {
    this.isStackChange = this.isCreation || !angular.equals(this.stack, this.copyStack);
    this.stackJson = angular.toJson(this.stack, true);
  }

  /**
   * Update stack from stack's editor json.
   */
  updateStackFromJson(): void {
    let stack: any;
    try {
      stack = angular.fromJson(this.stackJson);
    } catch (e) {
      this.isStackChange = false;
      return;
    }
    this.isStackChange = !angular.equals(stack, this.copyStack);
    if (this.isStackChange) {
      this.stack = stack;
      this.stackTags = !stack.tags ? [] : angular.copy(stack.tags);
      this.stackName = !stack.name ? '' : angular.copy(stack.name);
    }
  }

  /**
   * Prepare data to be displayed.
   */
  prepareStackData(): void {
    if (!this.stack.tags) {
      this.stack.tags = [];
    }
    this.stackTags = angular.copy(this.stack.tags);

    delete this.stack.links;
    this.stackName = angular.copy(this.stack.name);
    this.copyStack = angular.copy(this.stack);
    this.updateJsonFromStack();
  }

  /**
   * Saves stack configuration - creates new one or updates existing.
   */
  saveStack(): void {
    this.updateJsonFromStack();
    if (this.isCreation) {
      this.createStack();
      return;
    }
    this.cheStack.updateStack(this.stack.id, this.stackJson).then((stack: any) => {
      this.cheNotification.showInfo('Stack is successfully updated.');
      this.isLoading = false;
      this.stack = stack;
      this.prepareStackData();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update stack failed.');
      this.$log.error(error);
      this.stack = this.cheStack.getStackById(this.stackId);
      this.cancelStackChanges();
    });
  }

  /**
   * Creates new stack.
   */
  createStack(): void {
    this.cheStack.createStack(this.stackJson).then((stack: any) => {
      this.stack = stack;
      this.isLoading = false;
      this.cheStack.fetchStacks();
      this.isStackChange = false;
      this.isCreation = false;
      this.prepareStackData();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Creation stack failed.');
      this.$log.error(error);
    });
  }

  /**
   * Show dialog to select project templates for stack's testing
   * @param $event {MouseEvent}
   */
  showSelectTemplateDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'SelectTemplateController',
      controllerAs: 'selectTemplateController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this,
        stack: this.stack
      },
      templateUrl: 'app/stacks/stack-details/select-template/select-template.html'
    });
  }

  /**
   * Add commands sequentially by iterating on the number of the commands.
   * @param workspaceId{string} - the ID of the workspace to use for adding commands
   * @param projectName{string} - the name that will be used to prefix the commands inserted
   * @param commands{Array<any>} - the array to follow
   * @param index{number} - the index of the array of commands
   * @param deferred{ng.IDeferred<any>}
   */
  addCommands(workspaceId: string, projectName: string, commands: Array<any>, index: number, deferred: ng.IDeferred<any>): void {
    if (index < commands.length) {
      let newCommand = angular.copy(commands[index]);
      newCommand.name = projectName + ': ' + newCommand.name;
      let addPromise = this.cheWorkspace.addCommand(workspaceId, newCommand);
      addPromise.then(() => {
        // call the method again
        this.addCommands(workspaceId, projectName, commands, ++index, deferred);
      }, (error: any) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve();
    }
  }

  /**
   * Update projects sequentially by iterating on the number of the projects.
   * @param workspaceId{string} - the ID of the workspace to use for adding commands
   * @param projects{Array<any>} - the array to follow
   * @param index{number} - the index of the array of commands
   * @param deferred{ng.IDeferred<any>}
   */
  updateProjects(workspaceId: string, projects: Array<any>, index: number, deferred: ng.IDeferred<any>): void {
    if (index < projects.length) {
      let project = angular.copy(projects[index]);
      let projectService = this.cheWorkspace.getWorkspaceAgent(workspaceId).getProject();
      projectService.updateProject(project.name, project).then(() => {
        let resolvePromise = projectService.fetchResolve(project.name);
        resolvePromise.then(() => {
          this.updateProjects(workspaceId, projects, ++index, deferred);
        }, (error: any) => {
          deferred.reject(error);
        });
        if (project.commands && project.commands.length > 0) {
          let deferredAddCommand = this.$q.defer();
          this.addCommands(workspaceId, project.name, project.commands, 0, deferredAddCommand);
          deferredAddCommand.promise.then(() => {
            this.updateProjects(workspaceId, projects, ++index, deferred);
          }, (error: any) => {
            deferred.reject(error);
          });
        } else {
          this.updateProjects(workspaceId, projects, ++index, deferred);
        }
      }, (error: any) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve();
    }
  }

  /**
   * Add projects.
   * @param workspaceId{string} - the ID of the workspace to use for adding projects
   * @param projects{Array<che.IProject>} - the adding projects
   *
   * @returns {ng.IPromise<any>}
   */
  addProjects(workspaceId: string, projects: Array<che.IProject>): ng.IPromise<any> {
    let deferred = this.$q.defer();
    if (projects && projects.length) {
      this.cheWorkspace.getWorkspaceAgent(workspaceId).getProject().createProjects(projects).then(() => {
        this.updateProjects(workspaceId, projects, 0, deferred);
      }, (error: any) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve();
    }

    return deferred.promise;
  }

  /**
   * Show popup for stack's testing
   * @param stack {che.IStack}
   * @param projects {Array<che.IProject>}
   */
  showStackTestPopup(stack: che.IStack, projects: Array<che.IProject>): void {
    this.showIDE = false;
    stack.workspaceConfig.projects = [];
    let deferred = this.$q.defer();
    this.cheWorkspace.startTemporaryWorkspace(stack.workspaceConfig).then((workspace: che.IWorkspace) => {
      this.tmpWorkspaceId = workspace.id;
      this.cheWorkspace.getWorkspacesById().set(workspace.id, workspace);
      this.cheWorkspace.fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        if (projects && projects.length) {
          this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
            this.showIDE = false;
            this.addProjects(workspace.id, projects).then(() => {
              deferred.resolve();
            }, (error: any) => {
              deferred.reject(error);
            });
          }, (error: any) => {
            if (error.status === 304) {
              this.showIDE = false;
              this.addProjects(workspace.id, projects).then(() => {
                deferred.resolve();
              }, (error: any) => {
                deferred.reject(error);
              });
            } else {
              this.$log.error(error);
            }
          });
        }
      }, (error: any) => {
        this.$log.error(error);
      });
      this.cheWorkspace.startUpdateWorkspaceStatus(workspace.id);
      let tmpWorkspaceIdeUrl = '';
      angular.forEach(workspace.links, (link: any) => {
        if (link.rel === 'ide url') {
          tmpWorkspaceIdeUrl = link.href;
          return;
        }
      });
      if (!tmpWorkspaceIdeUrl) {
        this.cheNotification.showError('Testing stack failed.');
        return;
      }
      let bodyEl = this.$document.find('body');
      let testPopupEl: string = '<che-modal-popup id="' + STACK_TEST_POPUP_ID + '" ' +
        'title="Testing Stack: ' + stack.name + '" on-close="stackController.closeStackTestPopup()">' +
        '<div ng-hide="stackController.showIDE" class="main-page-loader">' +
        '<div class="ide-page-loader-content"><img ng-src="{{branding.loaderURL}}"></div></div>' +
        '<iframe ng-show="stackController.showIDE" class="ide-page-frame" ' +
        'src="' + tmpWorkspaceIdeUrl.toString() + '"></iframe></che-modal-popup>';
      this.cheUIElementsInjectorService.injectAdditionalElement(bodyEl, testPopupEl, this.$scope);
      deferred.promise.then(() => {
        this.cheUIElementsInjectorService.injectAdditionalElement(bodyEl, testPopupEl, this.$scope);
      }, (error: any) => {
        this.$log.error(error);
      });
    }, (error: any) => {
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Testing stack failed.');
      this.closeStackTestPopup();
    });
  }

  /**
   * Close stack's test popup
   */
  closeStackTestPopup(): void {
    if (this.tmpWorkspaceId) {
      this.cheWorkspace.stopWorkspace(this.tmpWorkspaceId, false);
      this.tmpWorkspaceId = '';
    }
    this.cheUIElementsInjectorService.deleteElementById(STACK_TEST_POPUP_ID);
  }

  /**
   * Deletes current stack if user confirms.
   */
  deleteStack(): void {
    let confirmTitle = 'Would you like to delete ' + this.stack.name + '?';

    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove stack')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    this.$mdDialog.show(confirm).then(() => {
      this.loading = true;
      this.cheStack.deleteStack(this.stack.id).then(() => {
        this.cheNotification.showInfo('Stack <b>' + this.stack.name + '</b> has been successfully removed.');
        this.$location.path('/stacks');
      }, (error: any) => {
        this.loading = false;
        let message = 'Failed to delete <b>' + this.stack.name + '</b> stack.' + (error && error.message) ? error.message : '';
        this.cheNotification.showError(message);
      });
    });
  }

}
