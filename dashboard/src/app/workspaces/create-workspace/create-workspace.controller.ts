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

import {CheEnvironmentRegistry} from '../../../components/api/environment/che-environment-registry.factory';
import {EnvironmentManager} from '../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../components/api/environment/environment-manager-machine';
import {CreateWorkspaceSvc} from './create-workspace.service';
import {NamespaceSelectorSvc} from './namespace-selector/namespace-selector.service';
import {StackSelectorSvc} from './stack-selector/stack-selector.service';
import {RandomSvc} from '../../../components/utils/random.service';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {
  ICheButtonDropdownMainAction,
  ICheButtonDropdownOtherAction
} from '../../../components/widget/button-dropdown/che-button-dropdown.directive';
import {DevfileRegistry} from '../../../components/api/devfile-registry.factory';

/**
 * This class is handling the controller for workspace creation.
 *
 * @author Oleksii Kurinnyi
 */
export class CreateWorkspaceController {

  static $inject = ['$mdDialog', '$timeout', 'cheEnvironmentRegistry', 'createWorkspaceSvc', 'namespaceSelectorSvc',
   'randomSvc', '$log', 'cheNotification', 'devfileRegistry'];

  /**
   * Dropdown button config.
   */
  headerCreateButtonConfig: {
    mainAction: ICheButtonDropdownMainAction,
    otherActions: Array<ICheButtonDropdownOtherAction>
  };
  private $mdDialog: ng.material.IDialogService;
  /**
   * Timeout service.
   */
  private $timeout: ng.ITimeoutService;
  /**
   * The registry of environment managers.
   */
  private cheEnvironmentRegistry: CheEnvironmentRegistry;
  /**
   * Workspace creation service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Namespace selector service.
   */
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  /**
   * Generator for random strings.
   */
  private randomSvc: RandomSvc;
  /**
   * Logging service.
   */
  private $log: ng.ILogService;
  /**
   * Notification factory.
   */
  private cheNotification: CheNotification;
  /**
   * Devfile registry.
   */
  private devfileRegistry: DevfileRegistry;
  /**
   * The environment manager.
   */
  private environmentManager: EnvironmentManager;
  /**
   * The selected devfile.
   */
  private selectedDevfile: che.IWorkspaceDevfile;
  /**
   * The selected namespace ID.
   */
  private namespaceId: string;
  /**
   * The map of forms.
   */
  private forms: Map<string, ng.IFormController>;
  /**
   * The list of names of existing workspaces.
   */
  private usedNamesList: string[];
  /**
   * The name of workspace.
   */
  private workspaceName: string;
  /**
   * Hide progress loader if <code>true</code>.
   */
  private hideLoader: boolean;
  /**
   * Default constructor that is using resource injection
   */
  constructor($mdDialog: ng.material.IDialogService,
              $timeout: ng.ITimeoutService,
              cheEnvironmentRegistry: CheEnvironmentRegistry,
              createWorkspaceSvc: CreateWorkspaceSvc,
              namespaceSelectorSvc: NamespaceSelectorSvc,
              randomSvc: RandomSvc,
              $log: ng.ILogService,
              cheNotification: CheNotification,
              devfileRegistry: DevfileRegistry) {
    this.$mdDialog = $mdDialog;
    this.$timeout = $timeout;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.randomSvc = randomSvc;
    this.$log = $log;
    this.cheNotification = cheNotification;
    this.devfileRegistry = devfileRegistry;

    this.usedNamesList = [];
    this.forms = new Map();

    this.namespaceId = this.namespaceSelectorSvc.getNamespaceId();
    this.buildListOfUsedNames().then(() => {
      this.workspaceName = this.randomSvc.getRandString({prefix: 'wksp-', list: this.usedNamesList});
      this.reValidateName();
    });

    // loader should be hidden and page content shown
    // when stacks selector is rendered
    // and default stack is selected
    this.hideLoader = false;

    // header toolbar
    // dropdown button config
    this.headerCreateButtonConfig = {
      mainAction: {
        title: 'Create & Open',
        type: 'button',
        action: () => {
          this.createWorkspace().then((workspace: che.IWorkspace) => {
            this.createWorkspaceSvc.redirectToIDE(workspace);
          });
        }
      },
      otherActions: [{
        title: 'Create & Proceed Editing',
        type: 'button',
        action: () => {
          this.createWorkspace().then((workspace: che.IWorkspace) => {
            this.createWorkspaceSvc.redirectToDetails(workspace);
          });
        },
        orderNumber: 1
      }]
    };
  }

  /**
   * Callback which is called when stack is selected.
   *
   * @param {string} stackId the stack ID
   */
  onDevfileSelected(devfile: che.IWorkspaceDevfile): void {
    // tiny timeout for templates selector to be rendered
    this.$timeout(() => {
      this.hideLoader = true;
    }, 10);
    this.selectedDevfile = devfile
  }

  /**
   * Callback which is called when namespace is selected.
   *
   * @param {string} namespaceId a namespace ID
   */
  onNamespaceChanged(namespaceId: string) {
    this.namespaceId = namespaceId;

    this.buildListOfUsedNames().then(() => {
      this.reValidateName();
    });
  }

  /**
   * Returns list of namespaces.
   *
   * @return {Array<che.INamespace>}
   */
  getNamespaces(): Array<che.INamespace> {
    return this.namespaceSelectorSvc.getNamespaces();
  }

  /**
   * Returns namespaces empty message if set.
   *
   * @returns {string}
   */
  getNamespaceEmptyMessage(): string {
    return this.namespaceSelectorSvc.getNamespaceEmptyMessage();
  }

  /**
   * Returns namespaces caption.
   *
   * @returns {string}
   */
  getNamespaceCaption(): string {
    return this.namespaceSelectorSvc.getNamespaceCaption();
  }

  /**
   * Returns <code>true</code> when 'Create' button should be disabled.
   *
   * @return {boolean}
   */
  isCreateButtonDisabled(): boolean {
    if (!this.namespaceId || !this.selectedDevfile) {
      return true;
    }

    for (const form of this.forms.values()) {
      if (form.$valid !== true) {
        return true;
      }
    }

    return false;
  }

  /**
   * Stores forms in list.
   *
   * @param {string} inputName
   * @param {ng.IFormController} form
   */
  registerForm(inputName: string, form: ng.IFormController) {
    this.forms.set(inputName, form);
  }

  /**
   * Returns <code>false</code> if workspace's name is not unique in the namespace.
   * Only member with 'manageWorkspaces' permission can definitely know whether
   * name is unique or not.
   *
   * @param {string} name workspace's name
   */
  isNameUnique(name: string): boolean {
    return this.usedNamesList.indexOf(name) === -1;
  }

  /**
   * Filters list of workspaces by current namespace and
   * builds list of names for current namespace.
   *
   * @return {IPromise<any>}
   */
  buildListOfUsedNames(): ng.IPromise<any> {
    return this.createWorkspaceSvc.fetchWorkspacesByNamespace(this.namespaceId).then((workspaces: Array<che.IWorkspace>) => {
      this.usedNamesList = workspaces.filter((workspace: che.IWorkspace) => {
        return workspace.namespace === this.namespaceId;
      }).map((workspace: che.IWorkspace) => {
        return this.createWorkspaceSvc.getWorkspaceName(workspace);
      });
    });
  }

  /**
   * Triggers form validation on Settings tab.
   */
  reValidateName(): void {
    const form: ng.IFormController = this.forms.get('name');

    if (!form) {
      return;
    }

    ['name', 'deskname'].forEach((inputName: string) => {
      const model = form[inputName] as ng.INgModelController;
      if (model) {
        model.$validate();
      }
    });
  }

  /**
   * Creates workspace.
   *
   * @returns {angular.IPromise<che.IWorkspace>}
   */
  createWorkspace(): ng.IPromise<che.IWorkspace> {
    // update workspace name
    this.selectedDevfile.metadata.name = this.workspaceName;
    return this.createWorkspaceSvc.createWorkspaceFromDevfile(this.selectedDevfile, null);
  }

  /**
   * Creates a workspace and shows a dialogue window for a user to select
   * whether to open Workspace Details page or the IDE.
   *
   * @param {MouseEvent} $event
   */
  createWorkspaceAndShowDialog($event: MouseEvent): void {
    this.createWorkspace().then((workspace: che.IWorkspace) => {
      this.$mdDialog.show({
        targetEvent: $event,
        controller: 'AfterCreationDialogController',
        controllerAs: 'afterCreationDialogController',
        bindToController: true,
        clickOutsideToClose: true,
        templateUrl: 'app/workspaces/create-workspace/after-creation-dialog/after-creation-dialog.html'
      }).then(() => {
        // when promise is resolved then open workspace in IDE
        this.createWorkspaceSvc.redirectToIDE(workspace);
      }, () => {
        // when promise is rejected then open Workspace Details page
        this.createWorkspaceSvc.redirectToDetails(workspace);
      });
    });
  }

  /**
   * Creates a workspace and redirects to the IDE.
   */
  createWorkspaceAndOpenIDE(): void {
    this.createWorkspace().then((workspace: che.IWorkspace) => {
      this.createWorkspaceSvc.redirectToIDE(workspace);
    });
  }
}
