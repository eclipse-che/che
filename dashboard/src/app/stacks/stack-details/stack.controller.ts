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
import {CheStack} from '../../../components/api/che-stack.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {CheUIElementsInjectorService} from '../../../components/service/injector/che-ui-elements-injector.service';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {ImportStackService} from './import-stack.service';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';

export  interface IInitData {
  stackId?: string;
  stack: che.IStack;
}

const GENERAL_SCOPE: string = 'general';
const ADVANCED_SCOPE: string = 'advanced';
const STACK_TEST_POPUP_ID: string = 'stackTestPopup';

/**
 * Controller for stack management.
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class StackController {

  static $inject = ['$q', '$timeout', '$location', '$log', 'cheStack', 'cheWorkspace', '$mdDialog', 'cheNotification',
    '$document', 'cheUIElementsInjectorService', '$scope', '$window', 'importStackService', 'confirmDialogService', 'initData'];
  $q: ng.IQService;
  $log: ng.ILogService;
  $scope: ng.IScope;
  $document: ng.IDocumentService;
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
  isStackChange: boolean;
  stackId: string;
  tmpWorkspaceId: string;
  stackName: string;
  stackDescription: string;
  stackJson: string;
  stackTags: Array<string>;
  stack: che.IStack;
  copyStack: che.IStack;
  editorOptions: any;
  machinesViewStatus: any;

  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource injection
   */
  constructor($q: ng.IQService, $timeout: ng.ITimeoutService, $location: ng.ILocationService,
              $log: ng.ILogService, cheStack: CheStack, cheWorkspace: CheWorkspace, $mdDialog: ng.material.IDialogService,
              cheNotification: CheNotification, $document: ng.IDocumentService, cheUIElementsInjectorService: CheUIElementsInjectorService,
              $scope: ng.IScope, $window: ng.IWindowService, importStackService: ImportStackService, confirmDialogService: ConfirmDialogService, initData: IInitData) {
    this.$q = $q;
    this.$location = $location;
    this.$log = $log;
    this.$scope = $scope;
    this.cheStack = cheStack;
    this.cheWorkspace = cheWorkspace;
    this.$mdDialog = $mdDialog;
    this.cheNotification = cheNotification;
    this.$document = $document;
    this.cheUIElementsInjectorService = cheUIElementsInjectorService;
    this.importStackService = importStackService;
    this.confirmDialogService = confirmDialogService;

    this.editorOptions = {
      lineWrapping: true,
      lineNumbers: false,
      matchBrackets: true,
      mode: 'application/json',
      onLoad: (editor: { refresh: Function }) => {
        $timeout(() => {
          editor.refresh();
        }, 1000);
      }
    };

    this.stackId = initData.stackId;
    this.stack = initData.stack;
    this.machinesViewStatus = {};
    this.stackTags = [];

    this.prepareStackData();

    $window.addEventListener('message', (event: { data: string }) => {
      if (!this.showIDE && 'show-ide' === event.data) {
        this.showIDE = true;
        this.$scope.$digest();
      }
    }, false);
  }

  get GENERAL_SCOPE(): string {
    return GENERAL_SCOPE;
  }

  get ADVANCED_SCOPE(): string {
    return ADVANCED_SCOPE;
  }

  _updateInputVariables(stack: che.IStack) {
    const {tags, name, description} = stack;
    this.stackTags = tags ? tags : [];
    this.stackName = name ? name : '';
    this.stackDescription = description;
    this.stack = angular.copy(stack);
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
    this._updateInputVariables(this.copyStack);
    this.updateJsonFromStack();
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
    this.stackTags.length = 0;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack name.
   */
  updateStackName(name: string): void {
    this.stack.name = name;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack description.
   */
  updateStackDescription(): void {
    this.stack.description = this.stackDescription;
    this.updateJsonFromStack();
  }

  /**
   * Updates stack tags info.
   */
  updateStackTags(): void {
    this.stack.tags = angular.copy(this.stackTags);
    this.updateJsonFromStack();
  }

  /**
   * Update stack's editor json from stack.
   */
  updateJsonFromStack(): void {
    this.isStackChange = this.hasChanges(this.stack);
    this.stackJson = angular.toJson(this.stack, true);
  }

  protected hasChanges(stack: che.IStack): boolean {
    return !angular.equals(stack, this.copyStack);
  }

  /**
   * Update stack from stack's editor json.
   */
  updateStackFromJson(): void {
    let stack: che.IStack;
    try {
      stack = angular.fromJson(this.stackJson);
    } catch (e) {
      this.isStackChange = false;
      return;
    }
    this.isStackChange = this.hasChanges(stack);
    if (this.isStackChange) {
      this._updateInputVariables(stack);
    }
  }

  /**
   * Prepare data to be displayed.
   */
  prepareStackData(): void {
    delete this.stack.links;
    this._updateInputVariables(this.stack);
    this.copyStack = angular.copy(this.stack);
    this.updateJsonFromStack();
  }

  /**
   * Saves stack configuration - creates new one or updates existing.
   */
  saveStack(): void {
    if (!this.stackId) {
      this.cheNotification.showError('Update stack failed.');
      return;
    }
    const stack = angular.fromJson(this.stackJson);
    this.cheStack.updateStack(this.stack.id, stack).then(() => {
      this.cheStack.fetchStacks().finally(() => {
        this.cheNotification.showInfo('Stack has been successfully updated.');
        this.isLoading = false;
        this.stack = this.cheStack.getStackById(this.stackId);
        this.prepareStackData();
      });
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Update stack failed.');
      this.$log.error(error);
      this.stack = this.cheStack.getStackById(this.stackId);
      this.cancelStackChanges();
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
   * @param projects{Array<IProjectTemplate>} - the array to follow
   * @param index{number} - the index of the array of commands
   * @param deferred{ng.IDeferred<any>}
   */
  updateProjects(workspaceId: string, projects: Array<che.IProjectTemplate>, index: number, deferred: ng.IDeferred<any>): void {
    if (index < projects.length) {
      const project = projects[index];
      const projectTypeResolverService = this.cheWorkspace.getWorkspaceAgent(workspaceId).getProjectTypeResolver();

      const deferredAddCommand = this.$q.defer();
      this.addCommands(workspaceId, project.name, project.commands, 0, deferredAddCommand);
      deferredAddCommand.promise.finally(() => {
        projectTypeResolverService.resolveProjectType(project).then(() => {
          this.updateProjects(workspaceId, projects, ++index, deferred);
        }, (error: any) => {
          deferred.reject(error);
        });
      });
    } else {
      deferred.resolve();
    }
  }

  /**
   * Add projects.
   * @param workspaceId{string} - the ID of the workspace to use for adding projects
   * @param projects{Array<che.IProjectTemplate>} - the adding projects
   * @param deferred{ng.IDeferred<any>}
   *
   * @returns {ng.IPromise<any>}
   */
  addProjects(workspaceId: string, projects: Array<che.IProjectTemplate>, deferred: ng.IDeferred<any>): void {
    if (projects && projects.length) {
      let workspaceAgent = this.cheWorkspace.getWorkspaceAgent(workspaceId);
      workspaceAgent.getProject().createProjects(projects).then(() => {
        this.updateProjects(workspaceId, projects, 0, deferred);
      }, (error: any) => {
        deferred.reject(error);
      });
    } else {
      deferred.resolve();
    }
  }

  /**
   * Show popup for stack's testing
   * @param stack {che.IStack}
   * @param projects {Array<che.IProjectTemplate>}
   */
  showStackTestPopup(stack: che.IStack, projects: Array<che.IProjectTemplate>): void {
    this.showIDE = false;
    stack.workspaceConfig.projects = [];
    let deferred = this.$q.defer();
    this.cheWorkspace.startTemporaryWorkspace(stack.workspaceConfig).then((workspace: che.IWorkspace) => {
      if (!workspace || !workspace.id || !workspace.links || !workspace.links.ide) {
        this.cheNotification.showError('Testing stack failed.');
        return;
      }
      this.tmpWorkspaceId = workspace.id;
      this.cheWorkspace.getWorkspacesById().set(workspace.id, workspace);
      this.cheWorkspace.fetchStatusChange(workspace.id, 'RUNNING').then(() => {
        if (projects && projects.length) {
          this.cheWorkspace.fetchWorkspaceDetails(workspace.id).then(() => {
            this.showIDE = false;
            this.addProjects(workspace.id, projects, deferred);
          }, (error: any) => {
            deferred.reject(error);
          });
        }
      }, (error: any) => {
        this.$log.error(error);
      });
      this.cheWorkspace.startUpdateWorkspaceStatus(workspace.id);
      const bodyEl = this.$document.find('body');
      const testPopupEl = `<che-modal-popup id="${STACK_TEST_POPUP_ID}" title="Testing Stack: ${stack.name }" on-close="stackController.closeStackTestPopup()"><iframe ng-show="stackController.showIDE" class="ide-page-frame" src="${workspace.links.ide}"></iframe></che-modal-popup>`;
      this.cheUIElementsInjectorService.injectAdditionalElement(bodyEl, testPopupEl, this.$scope);
      deferred.promise.then(() => {
        this.cheUIElementsInjectorService.injectAdditionalElement(bodyEl, testPopupEl, this.$scope);
      }, (error: any) => {
        this.showIDE = true;
        this.$log.error(error);
      });
    }, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Testing stack failed.');
      this.closeStackTestPopup();
    });
  }

  /**
   * Close stack's test popup
   */
  closeStackTestPopup(): void {
    if (this.tmpWorkspaceId) {
      this.cheWorkspace.stopWorkspace(this.tmpWorkspaceId);
      this.tmpWorkspaceId = '';
    }
    this.cheUIElementsInjectorService.deleteElementById(STACK_TEST_POPUP_ID);
  }

  /**
   * Deletes current stack if user confirms.
   */
  deleteStack(): void {
    const content = `Would you like to delete '${this.stack.name}'?`;

    this.confirmDialogService.showConfirmDialog('Remove stack', content, 'Delete').then(() => {
      this.loading = true;
      this.cheStack.deleteStack(this.stack.id).then(() => {
        this.cheNotification.showInfo(`Stack <b>${this.stack.name}</b> has been successfully removed.`);
        this.$location.path('/stacks');
      }, (error: any) => {
        const errorMessage = error && error.data && error.data.message ? error.data.message : '';
        this.cheNotification.showError(`Failed to delete <b>${this.stack.name}</b> stack. ${errorMessage}`);
      }).finally(() => {
        this.loading = false;
      });
    });
  }

}
