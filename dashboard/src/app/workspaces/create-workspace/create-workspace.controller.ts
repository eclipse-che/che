/*
 * Copyright (c) 2015-2019 Red Hat, Inc.
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
import {CreateWorkspaceSvc} from './create-workspace.service';
import {NamespaceSelectorSvc} from './ready-to-go-stacks/namespace-selector/namespace-selector.service';
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
   * Selected tab index.
   */
  private selectedTabIndex: number = 0;
  private isImportDevfileActive: boolean = false;
  /**
   * The imported devfile.
   */
  private importedDevfile: che.IWorkspaceDevfile = {
    apiVersion: '1.0.0',
    components: [],
    metadata: {
      name: 'custom-wksp'
    }
  };
  private selectedSource: string;

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

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  updateImportedDevfile(devfile: che.IWorkspaceDevfile): void {
    this.importedDevfile = devfile;
  }

  /**
   * Returns <code>true</code> when 'Create' button should be disabled.
   *
   * @return {boolean}
   */
  // TODO
  isCreateButtonDisabled(): boolean {
    return false;
  }

  /**
   * Creates workspace.
   *
   * @returns {angular.IPromise<che.IWorkspace>}
   */
  // TODO
  createWorkspace(): ng.IPromise<che.IWorkspace> {
    return {} as any;
    // let devfileSource: che.IWorkspaceDevfile;
    // if (this.isImportDevfileActive) {
    //   devfileSource = this.importedDevfile;
    //   this.stackName = `custom-${devfileSource.metadata.name}`
    // } else {
    //   // update workspace name
    //   devfileSource = angular.copy(this.selectedDevfile);
    //   devfileSource.metadata.name = this.workspaceName;
    // }
    // return this.createWorkspaceSvc.createWorkspaceFromDevfile(devfileSource, {stackName: this.stackName});
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
