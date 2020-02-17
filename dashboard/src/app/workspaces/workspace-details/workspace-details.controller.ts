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
import { CheWorkspace, WorkspaceStatus } from '../../../components/api/workspace/che-workspace.factory';
import { CheNotification } from '../../../components/notification/che-notification.factory';
import { WorkspaceDetailsService } from './workspace-details.service';
import { WorkspacesService } from '../workspaces.service';
import { ICheEditModeOverlayConfig } from '../../../components/widget/edit-mode-overlay/che-edit-mode-overlay.directive';
import { CheBranding } from '../../../components/branding/che-branding';
import { WorkspaceDataManager } from '../../../components/api/workspace/workspace-data-manager';

export interface IInitData {
  namespaceId: string;
  workspaceName: string;
  workspaceDetails: che.IWorkspace;
}

/**
 * @ngdoc controller
 * @name workspaces.workspace.details.controller:WorkspaceDetailsController
 * @description This class is handling the controller for workspace to create and edit.
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class WorkspaceDetailsController {

  static $inject = [
    '$location',
    '$q',
    '$sce',
    '$scope',
    '$timeout',
    'cheNotification',
    'cheWorkspace',
    'workspaceDetailsService',
    'initData',
    'cheBranding',
    'workspacesService'
  ];

  /**
   * Overlay panel configuration.
   */
  private editOverlayConfig: ICheEditModeOverlayConfig;
  private workspaceDetails: che.IWorkspace;
  private workspacesService: WorkspacesService;
  private $q: ng.IQService;
  private $scope: ng.IScope;
  private $sce: ng.ISCEService;
  private $location: ng.ILocationService;
  private $timeout: ng.ITimeoutService;
  private cheNotification: CheNotification;
  private cheWorkspace: CheWorkspace;
  private workspaceDetailsService: WorkspaceDetailsService;
  private loading: boolean = false;
  private selectedTabIndex: number;
  private namespaceId: string = '';
  private workspaceId: string = '';
  private workspaceName: string = '';
  private newName: string = '';
  private initialWorkspaceDetails: che.IWorkspace = {};
  private forms: Map<string, ng.IFormController> = new Map();
  private tab: { [key: string]: string } = {};
  private errorMessage: string = '';
  private failedTabs: string[] = [];
  private tabsValidationTimeout: ng.IPromise<any>;
  private pluginRegistry: string;
  private TAB: Array<string>;
  private cheBranding: CheBranding;
  private workspaceDataManager: WorkspaceDataManager;

  /**
   * There is selected deprecated editor when it's <code>true</code>.
   */
  private hasSelectedDeprecatedEditor: boolean;
  /**
   * There are selected deprecated plugins when it's <code>true</code>.
   */
  private hasSelectedDeprecatedPlugins: boolean;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    $location: ng.ILocationService,
    $q: ng.IQService,
    $sce: ng.ISCEService,
    $scope: ng.IScope,
    $timeout: ng.ITimeoutService,
    cheNotification: CheNotification,
    cheWorkspace: CheWorkspace,
    workspaceDetailsService: WorkspaceDetailsService,
    initData: IInitData,
    cheBranding: CheBranding,
    workspacesService: WorkspacesService
  ) {
    this.$location = $location;
    this.$q = $q;
    this.$sce = $sce;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.cheNotification = cheNotification;
    this.cheWorkspace = cheWorkspace;
    this.workspaceDetailsService = workspaceDetailsService;
    this.workspacesService = workspacesService;
    this.cheBranding = cheBranding;
    this.pluginRegistry = cheWorkspace.getWorkspaceSettings() != null ? cheWorkspace.getWorkspaceSettings().cheWorkspacePluginRegistryUrl : null;
    this.workspaceDataManager = this.cheWorkspace.getWorkspaceDataManager();

    if (!initData.workspaceDetails) {
      cheNotification.showError(`There is no workspace with name ${initData.workspaceName}`);
      $location.path('/workspaces').search({});
      return;
    }

    this.namespaceId = initData.namespaceId;
    this.workspaceName = initData.workspaceName;
    this.workspaceId = initData.workspaceDetails.id;

    const action = (newWorkspaceDetails: che.IWorkspace) => {
      if (this.initialWorkspaceDetails.devfile && angular.equals(newWorkspaceDetails.devfile, this.initialWorkspaceDetails.devfile)) {
        return;
      }

      this.initialWorkspaceDetails = angular.copy(newWorkspaceDetails);
      if (this.workspaceDetailsService.isWorkspaceModified(this.workspaceId)) {
        const newName = this.workspaceDataManager.getName(newWorkspaceDetails);
        if (this.workspaceName !== newName) {
          this.$location.path(`workspace/${this.workspaceDetails.namespace}/${newName}`);
          return;
        }
        this.workspaceDetails = angular.copy(newWorkspaceDetails);
      }
      this.checkEditMode();
      this.updateDeprecatedInfo();
    };
    this.cheWorkspace.subscribeOnWorkspaceChange(initData.workspaceDetails.id, action);

    this.initialWorkspaceDetails = angular.copy(initData.workspaceDetails);
    this.workspaceDetails = angular.copy(initData.workspaceDetails);
    this.updateDeprecatedInfo();
    this.TAB = ['Overview', 'Projects', 'Plugins', 'Editors', 'Devfile'];
    this.updateTabs();

    this.updateSelectedTab(this.$location.search().tab);
    const searchDeRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (angular.isDefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);
    const failedTabsDeregistrationFn = $scope.$watch(() => {
      return this.checkForFailedTabs();
    }, () => {
      const isSaved = this.workspaceDetailsService.isWorkspaceConfigSaved(this.workspaceId);
      this.updateEditModeOverlayConfig(isSaved === false);
    }, true);
    $scope.$on('$destroy', () => {
      this.cheWorkspace.unsubscribeOnWorkspaceChange(this.workspaceId, action);
      searchDeRegistrationFn();
      failedTabsDeregistrationFn();
    });

    this.editOverlayConfig = {
      visible: false,
      disabled: false,
      message: {
        content: '',
        visible: false
      },
      applyButton: {
        action: () => {
          this.applyChanges();
        },
        disabled: true,
        title: 'Apply'
      },
      saveButton: {
        action: () => {
          this.saveChanges();
        },
        title: 'Save',
        disabled: true
      },
      cancelButton: {
        action: () => {
          this.cancelConfigChanges();
        }
      },
      preventPageLeave: false,
      onChangesDiscard: (): ng.IPromise<void> => {
        this.cancelConfigChanges();
        return this.$q.when();
      }
    };
  }

  $onInit(): void {
    // this method won't be called here
    // place all initialization code in constructor
  }

  /**
   * Returns `true` if supported.
   *
   * @returns {boolean}
   */
  get isSupported(): boolean {
    return this.workspacesService.isSupported(this.workspaceDetails);
  }

  /**
   * Update tabs.
   */
  updateTabs(): void {
    this.tab = {};
    this.TAB.forEach((tab: string, $index: number) => {
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
    const tabIndex = parseInt(this.tab[tab], 10);
    this.selectedTabIndex = isNaN(tabIndex) ? 0 : tabIndex;
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
   * This method checks form validity on each tab and returns <code>true</code> if
   * all forms are valid.
   *
   * @returns {string[]} list of names of failed tabs.
   */
  checkForFailedTabs(): string[] {
    const tabs = Object.keys(this.tab).filter((tabKey: string) => {
      return !isNaN(parseInt(tabKey, 10));
    });
    tabs.forEach((tabKey: string) => {
      const tabNotValid = this.checkFormsNotValid(tabKey);
      const tabName = this.tab[tabKey];
      const index = this.failedTabs.indexOf(tabName);
      if (tabNotValid && index === -1) {
        this.failedTabs.push(tabName);
      }
      if (tabNotValid === false && index !== -1) {
        this.failedTabs.splice(index, 1);
      }
    });
    return this.failedTabs;
  }

  /**
   * Builds and returns message for edit-mode-overlay component.
   *
   * @returns {string}
   */
  getOverlayMessage(): string {
    if (!this.isSupported) {
      return `This workspace is using old definition format which is not compatible anymore.`;
    }

    if (this.failedTabs.length > 0) {
      const url = this.$location.absUrl().split('?')[0];
      let message = `<i class="error fa fa-exclamation-circle"
          aria-hidden="true"></i>&nbsp;Impossible to save and apply the configuration. Errors in `;
      message += this.failedTabs.map((tab: string) => {
        return `<a href='${url}?tab=${tab}'>${tab}</a>`;
      }).join(', ');

      return message;
    }

    return 'Your workspace will be restarted if you click Apply button.';
  }

  /**
   * Updates config of edit-mode-overlay component.
   */
  updateEditModeOverlayConfig(workspaceIsModified: boolean): void {
    // check for failed tabs
    const formIsValid = !this.failedTabs || this.failedTabs.length === 0;

    // panel
    this.editOverlayConfig.disabled = !formIsValid || this.loading;
    this.editOverlayConfig.visible = workspaceIsModified || this.workspaceDetailsService.doesWorkspaceConfigNeedRestart(this.workspaceId) === true;

    // 'save' button
    const saveButtonDisabled = !this.isSupported || !workspaceIsModified;
    this.editOverlayConfig.saveButton.disabled = saveButtonDisabled;

    // 'apply' button
    this.editOverlayConfig.applyButton.disabled = !this.isSupported
      || (this.workspaceDetailsService.doesWorkspaceConfigNeedRestart(this.workspaceId) === false);

    // 'cancel' button
    this.editOverlayConfig.cancelButton.disabled = !workspaceIsModified;

    // message content
    this.editOverlayConfig.message.content = this.getOverlayMessage();

    // message visibility
    this.editOverlayConfig.message.visible = !this.isSupported
      || this.failedTabs.length > 0
      || this.workspaceDetailsService.doesWorkspaceConfigNeedRestart(this.workspaceId) === true;

    this.editOverlayConfig.preventPageLeave = saveButtonDisabled === false;
  }

  /**
   * Checks editing mode for workspace config.
   */
  checkEditMode(): ng.IPromise<void> {
    if (!this.initialWorkspaceDetails || !this.workspaceDetails) {
      return;
    }

    if (this.tabsValidationTimeout) {
      this.$timeout.cancel(this.tabsValidationTimeout);
    }

    return this.tabsValidationTimeout = this.$timeout(() => {
      this.onWorkspaceChanged();
    }, 500);
  }

  onWorkspaceChanged(): void {
    let { isModified, needRestart } = this.isModifiedDevfile();

    if (this.getWorkspaceStatus() === WorkspaceStatus[WorkspaceStatus.STARTING]
      || this.getWorkspaceStatus() === WorkspaceStatus[WorkspaceStatus.RUNNING]) {
      needRestart = needRestart || this.workspaceDetailsService.doesWorkspaceConfigNeedRestart(this.workspaceId);
    } else {
      needRestart = false;
    }

    if (isModified || needRestart) {
      this.workspaceDetailsService.setModified(this.workspaceId, { isSaved: isModified === false, needRestart });
    } else {
      this.workspaceDetailsService.removeModified(this.workspaceId);
    }

    // update overlay
    this.updateEditModeOverlayConfig(isModified);

    // update info(editor and plugins)
    this.updateDeprecatedInfo();

    // publish changes
    this.workspaceDetailsService.publishWorkspaceChange(this.workspaceDetails);
  }

  /**
   * Applies workspace config changes and restarts the workspace.
   */
  applyChanges(): void {
    this.editOverlayConfig.disabled = true;

    this.loading = true;
    this.$scope.$broadcast('edit-workspace-details', { status: 'saving' });

    this.workspaceDetailsService.applyChanges(this.workspaceDetails)
      .then(() => {
        this.workspaceDetailsService.removeModified(this.workspaceId);
        this.cheNotification.showInfo('Workspace updated.');
        this.$scope.$broadcast('edit-workspace-details', { status: 'saved' });
      })
      .catch((error: any) => {
        this.$scope.$broadcast('edit-workspace-details', { status: 'failed' });
        this.cheNotification.showError('Update workspace failed.', error);
      })
      .then(() => {
        return this.cheWorkspace.fetchWorkspaceDetails(this.initialWorkspaceDetails.id);
      })
      .then(() => {
        this.$location.path('/workspace/' + this.namespaceId + '/' + this.workspaceDataManager.getName(this.workspaceDetails)).search({ tab: this.tab[this.selectedTabIndex] });
      })
      .finally(() => {
        this.loading = false;
        return this.onWorkspaceChanged();
      });
  }

  /**
   * Updates workspace with new config.
   *
   */
  saveChanges(): void {
    const notifyRestart = this.getWorkspaceStatus() === WorkspaceStatus[WorkspaceStatus.STARTING]
      || this.getWorkspaceStatus() === WorkspaceStatus[WorkspaceStatus.RUNNING];

    this.editOverlayConfig.disabled = true;

    this.loading = true;
    this.$scope.$broadcast('edit-workspace-details', { status: 'saving' });

    this.workspaceDetailsService.saveChanges(this.workspaceDetails)
      .then(() => {
        this.workspaceDetailsService.setModified(this.workspaceId, { isSaved: true });

        let message = 'Workspace updated.';
        message += notifyRestart ? '<br/>To apply changes in running workspace - need to restart it.' : '';
        this.cheNotification.showInfo(message);

        this.$scope.$broadcast('edit-workspace-details', { status: 'saved' });
      })
      .catch((error: any) => {
        this.$scope.$broadcast('edit-workspace-details', { status: 'failed' });
        const errorMessage = 'Cannot retrieve workspace configuration.';
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : errorMessage);
      })
      .finally(() => {
        this.loading = false;
        return this.onWorkspaceChanged();
      });
  }

  /**
   * Cancels workspace config changes that weren't stored
   */
  cancelConfigChanges(): void {
    this.workspaceDetailsService.removeModified(this.workspaceId);
    this.workspaceDetails = angular.copy(this.initialWorkspaceDetails);
    this.onWorkspaceChanged();
    this.$scope.$broadcast('edit-workspace-details', { status: 'cancelled' });
  }

  runWorkspace(): ng.IPromise<void> {
    this.errorMessage = '';

    if (this.workspaceDetailsService.isWorkspaceModified(this.workspaceId)) {
      return this.workspaceDetailsService.notifyUnsavedChangesDialog();
    }
    return this.workspaceDetailsService.runWorkspace(this.workspaceDetails).catch((error: any) => {
      this.errorMessage = error.message;
    });
  }

  stopWorkspace(): ng.IPromise<void> {
    if (this.workspaceDetailsService.isWorkspaceModified(this.workspaceId)) {
      return this.workspaceDetailsService.notifyUnsavedChangesDialog();
    }
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

  private isModifiedDevfile(): { isModified: boolean, needRestart: boolean } {
    const isEqual = angular.equals(this.initialWorkspaceDetails.devfile, this.workspaceDetails.devfile);
    if (isEqual) {
      return {
        isModified: false,
        needRestart: false
      };
    }

    const tmpDevfile = angular.extend({}, this.initialWorkspaceDetails.devfile, { metadata: { name: this.workspaceDetails.devfile.metadata.name } });
    const needRestart = false === angular.equals(tmpDevfile, this.workspaceDetails.devfile);
    return {
      isModified: true,
      needRestart
    };
  }

  /**
   * Builds and returns the warning message for che-description(warning info) component.
   *
   * @returns {string}
   */
  get warningMessage(): any {
    let message = '';

    if (!this.isSupported) {
      message += `This workspace is using old definition format which is not compatible anymore.
      Please follow the <a href="${this.cheBranding.getDocs().converting}" target="_blank">documentation</a>
      to update the definition of the workspace and benefits from the latest capabilities.`;
    } else if (this.hasSelectedDeprecatedPlugins) {
      message += `The workspace uses deprecated plugins${this.hasSelectedDeprecatedEditor ? ' and editor' : ''}.`;
    } else if (this.hasSelectedDeprecatedEditor) {
      message += `The workspace uses deprecated editor.`
    }

    return this.$sce.trustAsHtml(message);
  }

  get hasWarningMessage(): boolean {
    return !this.isSupported || this.hasSelectedDeprecatedEditor || this.hasSelectedDeprecatedPlugins;
  }

  private updateDeprecatedInfo() {
    const deprecatedEditor = this.workspaceDetailsService.getSelectedDeprecatedEditor(this.workspaceDetails);
    this.hasSelectedDeprecatedEditor = deprecatedEditor !== '';

    const deprecatedPlugins = this.workspaceDetailsService.getSelectedDeprecatedPlugins(this.workspaceDetails);
    this.hasSelectedDeprecatedPlugins = deprecatedPlugins.length > 0;
  }

}
