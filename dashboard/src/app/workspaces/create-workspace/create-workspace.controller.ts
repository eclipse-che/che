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

import { CreateWorkspaceSvc } from './create-workspace.service';
import {
  ICheButtonDropdownMainAction,
  ICheButtonDropdownOtherAction
} from '../../../components/widget/button-dropdown/che-button-dropdown.directive';

/**
 * View tabs.
 */
enum TABS {
  READY_TO_GO,
  IMPORT_DEVFILE
}

/**
 *
 */
type DevfileChangeEventData = {
  devfile: che.IWorkspaceDevfile,
  attrs?: { [key: string]: string }
};

/**
 * This class is handling the controller for workspace creation.
 *
 * @author Oleksii Kurinnyi
 */
export class CreateWorkspaceController {

  static $inject = [
    '$location',
    '$scope',
    'createWorkspaceSvc',
  ];

  /**
   * Selected tab index.
   */
  selectedTab: number = 0;
  /**
   * View tabs.
   */
  tabs: typeof TABS;

  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Directive scope service.
   */
  private $scope: ng.IScope;
  /**
   * Workspace creation service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Dropdown button config.
   */
  private headerCreateButtonConfig: {
    mainAction: ICheButtonDropdownMainAction,
    otherActions: Array<ICheButtonDropdownOtherAction>
  };
  /**
   * Devfiles by view.
   */
  private devfiles: Map<TABS, DevfileChangeEventData> = new Map();
  /**
   * Forms by view.
   */
  private forms: Map<TABS, ng.IFormController> = new Map();

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    $location: ng.ILocationService,
    $scope: ng.IScope,
    createWorkspaceSvc: CreateWorkspaceSvc
  ) {
    this.$location = $location;
    this.$scope = $scope;
    this.createWorkspaceSvc = createWorkspaceSvc;

    this.tabs = TABS;
    this.updateSelectedTab(this.$location.search().tab);
    const locationWatcherDeregistration = $scope.$watch(() => {
      return $location.search().tab;
    }, (newTab: string, oldTab: string) => {
      if (newTab === oldTab) {
        return;
      }
      if (angular.isDefined(newTab)) {
        this.updateSelectedTab(newTab);
      }
    });
    $scope.$on('$destroy', () => {
      locationWatcherDeregistration();
    });

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

  /**
   * Changes search part of URL.
   * @param index a tab index.
   */
  onSelectTab(index?: number): void {
    let param: { tab?: string } = {};
    if (angular.isDefined(index)) {
      param.tab = TABS[index];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace().search(param);
    } else {
      this.$location.search(param);
    }
  }

  /**
   * Update selected tab index by search part of URL.
   *
   * @param tab a tab name
   */
  updateSelectedTab(tab: string): void {
    const index = parseInt(TABS[tab], 10);
    this.selectedTab = isNaN(index) ? 0 : index;
  }

  /**
   * Stores forms in map.
   *
   * @param {number} tab
   * @param {ng.IFormController} form
   */
  registerForm(tab: number, form: ng.IFormController) {
    this.forms.set(tab, form);
  }

  /**
   * Returns <code>true</code> when 'Create' button should be disabled.
   *
   * @return {boolean}
   */
  isCreateButtonDisabled(): boolean {
    const form = this.forms.get(this.selectedTab);

    return !form || form.$valid !== true;
  }

  /**
   * Creates workspace.
   */
  createWorkspace(): ng.IPromise<che.IWorkspace> {
    const { devfile, attrs } = this.devfiles.get(this.selectedTab);
    return this.createWorkspaceSvc.createWorkspaceFromDevfile(devfile, attrs, this.selectedTab === TABS.IMPORT_DEVFILE);
  }


  /**
   * Creates a workspace and redirects to the IDE.
   */
  createWorkspaceAndOpenIDE(): void {
    this.createWorkspace().then((workspace: che.IWorkspace) => {
      this.createWorkspaceSvc.redirectToIDE(workspace);
    });
  }

  onDevfileChange(tab: number, devfile: che.IWorkspaceDevfile, attrs: { [key: string]: string }): void {
    this.devfiles.set(tab, { devfile, attrs });
  }

}
