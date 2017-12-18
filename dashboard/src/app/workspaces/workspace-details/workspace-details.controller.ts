/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/workspace/che-workspace.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {WorkspaceDetailsService} from './workspace-details.service';
import IdeSvc from '../../ide/ide.service';

export  interface IInitData {
  namespaceId: string;
  workspaceName: string;
  workspaceDetails: che.IWorkspace;
}

const TAB: Array<string> = ['Overview', 'Projects', 'Machines', 'Installers', 'Servers', 'Env_Variables', 'Volumes', 'Config', 'SSH'];

const STOPPING = WorkspaceStatus[WorkspaceStatus.STOPPING];
const STOPPED = WorkspaceStatus[WorkspaceStatus.STOPPED];

/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class WorkspaceDetailsController {
  private $scope: ng.IScope;
  private $log: ng.ILogService;
  private $location: ng.ILocationService;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;
  private ideSvc: IdeSvc;
  private workspaceDetailsService: WorkspaceDetailsService;
  private loading: boolean = false;
  private selectedTabIndex: number;
  private namespaceId: string = '';
  private workspaceId: string = '';
  private workspaceName: string = '';
  private newName: string = '';
  private workspaceDetails: che.IWorkspace;
  private originWorkspaceDetails: any = {};
  private editMode: boolean = false;
  private showApplyMessage: boolean = false;
  private workspaceImportedRecipe: che.IRecipe;
  private forms: Map<string, ng.IFormController> = new Map();
  private tab: { [key: string]: string } = {};
  private saving: boolean = false;
  private editModeMessage: string = '';
  private errorMessage: string = '';

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $scope: ng.IScope, cheNotification: CheNotification, cheWorkspace: CheWorkspace, ideSvc: IdeSvc, workspaceDetailsService: WorkspaceDetailsService, initData: IInitData) {
    this.$log = $log;
    this.$scope = $scope;
    this.$location = $location;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.workspaceDetailsService = workspaceDetailsService;

    if (!initData.workspaceDetails) {
      cheNotification.showError(`There is no workspace with name ${initData.workspaceName}`);
      $location.path('/workspaces').search({});
      return;
    }

    this.namespaceId = initData.namespaceId;
    this.workspaceName = initData.workspaceName;
    this.workspaceId = initData.workspaceDetails.id;

    const action = (workspaceDetails: che.IWorkspace) => {
      this.workspaceDetails = angular.copy(this.cheWorkspace.getWorkspaceById(this.originWorkspaceDetails.id));
      this.updateWorkspaceData(workspaceDetails);
    };
    this.cheWorkspace.subscribeOnWorkspaceChange(initData.workspaceDetails.id, action);
    this.updateWorkspaceData(initData.workspaceDetails);

    this.updateTabs();

    this.updateSelectedTab(this.$location.search().tab);
    const searchDeRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (angular.isDefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);
    $scope.$on('$destroy', () => {
      this.cheWorkspace.unsubscribeOnWorkspaceChange(this.workspaceId, action);
      searchDeRegistrationFn();
    });
  }

  /**
   * Update tabs.
   */
  updateTabs(): void {
    this.tab = {};
    TAB.forEach((tab: string, $index: number) => {
      const index = $index.toString();
      this.tab[tab] = index;
      this.tab[index] = tab;
    });
  }

  /**
   * Add a new tab.
   *
   * @param tab
   */
  addTab(tab: string): void {
    if (this.tab[tab]) {
      return;
    }
    const pos = (Object.keys(this.tab).length / 2).toString();
    this.tab[tab] = pos;
    this.tab[pos] = tab;
  }

  /**
   * Update selected tab index by search part of URL.
   *
   * @param {string} tab
   */
  updateSelectedTab(tab: string): void {
    const value = this.tab[tab];
    if (angular.isDefined(value)) {
      this.selectedTabIndex = parseInt(value, 10);
    }
  }

  /**
   * Changes search part of URL.
   *
   * @param {number} tabIndex
   */
  onSelectTab(tabIndex?: number): void {
    let param: { tab?: string } = {};
    if (angular.isDefined(tabIndex)) {
      param.tab = this.tab[tabIndex.toString()];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace().search(param);
    } else {
      this.$location.search(param);
    }
  }

  /**
   * Prepare workspaces data.
   *
   * @param workspaceDetails {che.IWorkspace}
   */
  updateWorkspaceData(workspaceDetails: che.IWorkspace): void {
    this.originWorkspaceDetails = angular.copy(workspaceDetails);
    this.checkEditMode();
    if (this.workspaceDetails && this.editMode) {
      return;
    }
    this.workspaceDetails = angular.copy(workspaceDetails);
    this.workspaceDetailsService.publishWorkspaceChange(workspaceDetails);
  }

  /**
   * Returns current status of workspace.
   *
   * @returns {string}
   */
  getWorkspaceStatus(): string {
    return this.workspaceDetailsService.getWorkspaceStatus(this.workspaceId);
  }

  /**
   * Returns workspace details pages (tabs, example - projects)
   *
   * @returns {*}
   */
  getPages(): any {
    return this.workspaceDetailsService.getPages();
  }

  /**
   * Returns workspace details section.
   *
   * @returns {*}
   */
  getSections(): any {
    return this.workspaceDetailsService.getSections();
  }

  /**
   * Callback when workspace config has been changed in editor.
   *
   * @param config {che.IWorkspaceConfig} workspace config
   */
  updateWorkspaceConfigImport(config: che.IWorkspaceConfig): void {
    if (!config) {
      return;
    }

    if (this.newName !== config.name) {
      this.newName = config.name;
    }
    if (!config.environments[config.defaultEnv]) {
      return;
    }
    this.workspaceDetails.config = config;
    this.workspaceImportedRecipe = config.environments[config.defaultEnv].recipe;
    this.checkEditMode();
  }


  checkEditMode(): void {
    if (!this.originWorkspaceDetails || !this.workspaceDetails) {
      return;
    }
    this.editMode = !angular.equals(this.originWorkspaceDetails.config, this.workspaceDetails.config);
    if (this.editMode === false) {
      this.editModeMessage = '';
      this.showApplyMessage = false;
      return;
    }
    this.workspaceDetailsService.publishWorkspaceChange(this.workspaceDetails);
    const failTabs = [];
    const tabs = Object.keys(this.tab).filter((tabKey: string) => {
      return !isNaN(parseInt(tabKey, 10));
    });
    tabs.forEach((tabKey: string) => {
      if (this.checkFormsNotValid(tabKey)) {
        failTabs.push(this.tab[tabKey]);
      }
    });
    if (failTabs.length) {
      const url = this.$location.absUrl().split('?')[0];
      this.editModeMessage = '<i class="error fa fa-exclamation-circle" aria-hidden="true"></i>&nbsp;Impossible to save and apply the configuration. Errors in ';
      this.editModeMessage += failTabs.map((tab: string) => {
        return `<a href='${url}?tab=${tab}'>${tab}</a>`;
      }).join(', ');
      this.showApplyMessage = true;
      return;
    }
    this.editModeMessage = 'Changes will be applied and workspace restarted';
    const needRunningStatus = this.workspaceDetailsService.needRunningToUpdate();
    if (needRunningStatus) {
      this.showApplyMessage = true;
    } else  {
      const statusStr = this.getWorkspaceStatus();
      this.showApplyMessage = [STOPPED, STOPPING].indexOf(statusStr) === -1;
    }
  }

  /**
   * Updates workspace config changes.
   */
  saveWorkspace(): void {
    this.editMode = false;
    this.showApplyMessage = false;

    this.saving = true;
    this.loading = true;
    this.$scope.$broadcast('edit-workspace-details', {status: 'saving'});

    this.workspaceDetailsService.applyChanges(this.originWorkspaceDetails, this.workspaceDetails).then((data: any) => {
      this.cheNotification.showInfo('Workspace updated.');
      this.$scope.$broadcast('edit-workspace-details', {status: 'saved'});
      this.saving = false;
      this.cheWorkspace.fetchWorkspaceDetails(this.originWorkspaceDetails.id).then(() => {
        this.$location.path('/workspace/' + this.namespaceId + '/' + this.workspaceDetails.config.name).search({tab: this.tab[this.selectedTabIndex]});
      }).finally(() => {
        this.loading = false;
      });
    }, (error: any) => {
      this.$scope.$broadcast('edit-workspace-details', {status: 'failed'});
      this.cheNotification.showError('Update workspace failed.', error);
      this.checkEditMode();
      this.saving = false;
      this.loading = false;
    });
  }

  /**
   * Cancels workspace config changes that weren't stored
   */
  cancelConfigChanges(): void {
    this.workspaceDetails = angular.copy(this.originWorkspaceDetails);
    this.workspaceDetailsService.publishWorkspaceChange(this.workspaceDetails);
    this.editMode = false;

    this.$scope.$broadcast('edit-workspace-details', {status: 'cancelled'});
  }

  runWorkspace(): ng.IPromise<any> {
    this.errorMessage = '';

    return this.workspaceDetailsService.runWorkspace(this.workspaceDetails).catch((error: any) => {
      this.errorMessage = error.message;
    });
  }

  stopWorkspace(): ng.IPromise<any> {
    return this.workspaceDetailsService.stopWorkspace(this.workspaceDetails.id);
  }

  /**
   * Register form for corresponding tab.
   *
   * @param tabIndex {string}
   * @param form
   */
  setForm(tabIndex: string, form: ng.IFormController): void {
    this.forms.set(tabIndex, form);
  }

  /**
   * Returns false if all forms from specified tabs are valid
   *
   * @param tabIndex {string}
   * @returns {Boolean}
   */
  checkFormsNotValid(tabIndex: string): boolean {
    const form = this.forms.get(tabIndex);
    return form && form.$invalid;
  }

  /**
   * Returns true when 'Save' button should be disabled
   *
   * @returns {boolean}
   */
  isSaveButtonDisabled(): boolean {
    const tabs = Object.keys(this.tab).filter((tabKey: string) => {
      return !isNaN(parseInt(tabKey, 10));
    });

    return tabs.some((tabKey: string) => this.checkFormsNotValid(tabKey));
  }
}
