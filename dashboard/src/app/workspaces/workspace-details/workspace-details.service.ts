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
import {CheNotification} from '../../../components/notification/che-notification.factory';
import IdeSvc from '../../ide/ide.service';
import {IObservable, IObservableCallbackFn, Observable} from '../../../components/utils/observable';
import {WorkspaceDetailsProjectsService} from './workspace-projects/workspace-details-projects.service';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/workspace/che-workspace.factory';
import {CheService} from '../../../components/api/che-service.factory';
import {PluginRegistry} from '../../../components/api/plugin-registry.factory';
import {WorkspaceDataManager} from '../../../components/api/workspace/workspace-data-manager';
import { ConfirmDialogService } from '../../../components/service/confirm-dialog/confirm-dialog.service';

interface IPage {
  title: string;
  content: string;
  icon?: string;
  index: number;
}

interface ISection {
  title: string;
  description: string;
  content: string;
}

export interface IModifiedWorkspace {
  isSaved: boolean;
  needRestart?: boolean;
}

class ModifiedWorkspaces {
  workspaces: { [id: string]: IModifiedWorkspace } = {};

  set(id: string, attrs: { isSaved?: boolean, needRestart?: boolean }): void {
    if (this.workspaces[id] === undefined) {
      this.workspaces[id] = { isSaved: false };
    }
    if (angular.isDefined(attrs.isSaved)) {
      this.workspaces[id].isSaved = attrs.isSaved;
    }
    if (angular.isDefined(attrs.needRestart)) {
      this.workspaces[id].needRestart = attrs.needRestart;
    }
  }

  isSaved(id: string): boolean {
    if (this.workspaces[id] === undefined) {
      return true;
    }
    return this.workspaces[id].isSaved;
  }

  needRestart(id: string): boolean {
    if (this.workspaces[id] === undefined) {
      return false;
    }
    return this.workspaces[id].needRestart;
  }

  remove(id: string): void {
    delete this.workspaces[id];
  }

}

/**
 * This class is handling the data for workspace details sections (tabs)
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class WorkspaceDetailsService {

  static $inject = [
    '$log',
    '$q',
    'cheWorkspace',
    'cheNotification',
    'ideSvc',
    'workspaceDetailsProjectsService',
    'cheService',
    'chePermissions',
    'confirmDialogService',
    'pluginRegistry'
  ];

  /**
   * Logging service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: CheWorkspace;
  /**
   * Notification factory.
   */
  private cheNotification: CheNotification;
  /**
   * IDE service.
   */
  private ideSvc: IdeSvc;
  /**
   * Service for projects of workspace.
   */
  private workspaceDetailsProjectsService: WorkspaceDetailsProjectsService;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * Instance of Observable.
   */
  private observable: IObservable<che.IWorkspace>;

  private pages: IPage[];
  private sections: ISection[];
  /**
   * These workspaces should be restarted for new config to be applied.
   */
  private modifiedWorkspaces: ModifiedWorkspaces = new ModifiedWorkspaces();
  /**
   * Array of deprecated editors.
   */
  private deprecatedEditors: string[] = [];
  /**
   * Array of deprecated plugins.
   */
  private deprecatedPlugins: string[] = [];
  /**
   * Plugin registry API interaction.
   */
  private pluginRegistry: PluginRegistry;
  /**
   * Workspace data manager.
   */
  private workspaceDataManager: WorkspaceDataManager;

  /**
   * Default constructor that is using resource
   */
  constructor (
    $log: ng.ILogService,
    $q: ng.IQService,
    cheWorkspace: CheWorkspace,
    cheNotification: CheNotification,
    ideSvc: IdeSvc,
    workspaceDetailsProjectsService: WorkspaceDetailsProjectsService,
    cheService: CheService,
    chePermissions: che.api.IChePermissions,
    confirmDialogService: ConfirmDialogService,
    pluginRegistry: PluginRegistry
  ) {
    this.$log = $log;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
    this.ideSvc = ideSvc;
    this.pluginRegistry = pluginRegistry;
    this.workspaceDetailsProjectsService = workspaceDetailsProjectsService;
    this.confirmDialogService = confirmDialogService;

    this.observable =  new Observable<any>();

    this.pages = [];
    this.sections = [];
    this.observable = new Observable();

    cheService.fetchServices().finally(() => {
      if (cheService.isServiceAvailable(chePermissions.getPermissionsServicePath())) {
        this.addPage('Share', '<share-workspace></share-workspace>', 'icon-ic_folder_shared_24px');
      }
    });

    this.cheWorkspace.fetchWorkspaceSettings().then(workspaceSettings => {
      this.pluginRegistry.fetchPlugins(workspaceSettings.cheWorkspacePluginRegistryUrl).then(items => {
        if (angular.isArray(items)) {
          items.filter(item => !!item.deprecate).forEach(item => {
            const target = item.type === PluginRegistry.EDITOR_TYPE ? this.deprecatedEditors : this.deprecatedPlugins;
            target.push(item.id);
          });
        }
      });
    });
    this.workspaceDataManager = this.cheWorkspace.getWorkspaceDataManager();
  }

  /**
   * Returns selected deprecated editor.
   *
   * @param {che.IWorkspace} workspace
   * @returns {string}
   */
  getSelectedDeprecatedEditor(workspace: che.IWorkspace): string {
    if (this.workspaceDataManager && workspace) {
      const editor = this.workspaceDataManager.getEditor(workspace);
      if (this.deprecatedEditors.indexOf(editor) !== -1) {
        return editor;
      }
    }
    return '';
  }

  /**
   * Returns selected deprecated plugins.
   *
   * @param {che.IWorkspace} workspace
   * @returns {Array<string>}
   */
  getSelectedDeprecatedPlugins(workspace: che.IWorkspace): Array<string> {
    if (this.workspaceDataManager && workspace) {
      return this.workspaceDataManager.getPlugins(workspace).filter(plugin => {
        return this.deprecatedPlugins.indexOf(plugin) !== -1
      });
    }
    return [];
  }

  /**
   * Subscribes on workspace change actions.
   *
   * @param {callback} action the action's callback
   */
  subscribeOnWorkspaceChange(action: IObservableCallbackFn<che.IWorkspace>): void {
    this.observable.subscribe(action);
  }

  /**
   * Unsubscribes the workspace change action.
   *
   * @param {callback} action the action's callback.
   */
  unsubscribeOnWorkspaceChange(action: IObservableCallbackFn<che.IWorkspace>): void {
    this.observable.unsubscribe(action);
  }

  /**
   * Publish new workspace details to subscribers.
   *
   * @param {che.IWorkspace} workspaceDetails
   */
  publishWorkspaceChange(workspaceDetails: che.IWorkspace): void {
    this.observable.publish(workspaceDetails);
  }

  /**
   * Add new page(tab) to the workspace details.
   *
   * @param title page title
   * @param content page html content
   * @param icon page icon
   * @param index optional page index (order)
   */
  addPage(title: string, content: string, icon?: string, index?: number): void {
    let page: IPage = {
      title: title,
      content: content,
      index: index || this.pages.length
    };
    if (icon) {
      page.icon = icon;
    }
    this.pages.push(page);
  }

  /**
   * Adds new section in workspace details.
   *
   * @param title title of the section
   * @param description description of teh section (optional)
   * @param content html content of the section
   */
  addSection(title: string, description: string, content: string): void {
    let section: ISection = {
      title: title,
      description: description,
      content: content
    };

    this.sections.push(section);
  }

  /**
   * Returns workspace details pages(tabs).
   *
   * @returns {Array} array of pages(tabs)
   */
  getPages(): IPage[] {
    return this.pages;
  }

  /**
   * Returns the array of workspace details sections.
   *
   * @returns {ISection[]} list of sections
   */
  getSections(): ISection[] {
    return this.sections;
  }

  /**
   * Returns current status of workspace.
   *
   * @param {string} workspaceId workspace ID
   * @return {string}
   */
  getWorkspaceStatus(workspaceId: string): string {
    const unknownStatus = 'unknown';
    const workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
    return workspace ? workspace.status : unknownStatus;
  }

  /**
   * Applies changes to the workspace config with workspace restarting.
   *
   * @param {che.IWorkspace} workspace
   * @returns {ng.IPromise<any>}
   */
  applyChanges(workspace: che.IWorkspace): ng.IPromise<any> {
    return this.$q.when()
      .then(() => {
        if (this.getWorkspaceStatus(workspace.id) !== WorkspaceStatus[WorkspaceStatus.STOPPED]) {
          this.stopWorkspace(workspace.id);
        }
        return this.cheWorkspace.fetchStatusChange(workspace.id, WorkspaceStatus[WorkspaceStatus.STOPPED]);
      })
      .then(() => {
        return this.saveChanges(workspace);
      })
      .then(() => {
        this.cheWorkspace.startWorkspace(workspace.id);
        return this.cheWorkspace.fetchStatusChange(workspace.id, WorkspaceStatus[WorkspaceStatus.RUNNING]);
      })
      .catch((error: any) => {
        this.$log.error(error);
        return this.$q.reject(error);
      });
  }

  /**
   * Keep a workspace as one that is modified and may need restarting to apply changes.
   */
  setModified(id: string, attrs: { isSaved?: boolean, needRestart?: boolean }): void {
    this.modifiedWorkspaces.set(id, attrs);
  }

  removeModified(id: string): void {
    this.modifiedWorkspaces.remove(id);
  }

  /**
   * Returns `true` if workspace configuration has been already saved.
   * @param id a workspace ID
   */
  isWorkspaceConfigSaved(id: string): boolean {
    return this.modifiedWorkspaces.isSaved(id);
  }

  /**
   * Returns `true` if workspace configuration has been already applied.
   * @param id a workspace ID
   */
  doesWorkspaceConfigNeedRestart(id: string): boolean {
    return this.modifiedWorkspaces.needRestart(id);
  }

  /**
   * Returns `true` if workspace configuration was changed.
   * @param id a workspace ID
   */
  isWorkspaceModified(id: string): boolean {
    return this.modifiedWorkspaces.isSaved(id) === false
      || this.modifiedWorkspaces.needRestart(id) === true;
  }

  /**
   * Updates workspace config.
   *
   * @param {che.IWorkspace} workspace new workspace details
   * @return {angular.IPromise<any>}
   */
  saveChanges(workspace: che.IWorkspace): ng.IPromise<any> {
    delete workspace.links;

    const projectNamesToDelete = this.workspaceDetailsProjectsService.getProjectNamesToDelete(),
      hasProjectsToDelete = projectNamesToDelete.length > 0;

    return this.cheWorkspace.updateWorkspace(workspace.id, workspace)
      .catch((error: any) => {
        this.$log.error(error);
        return this.$q.reject(error);
      });
  }

  /**
   * Starts the workspace.
   *
   * @param {che.IWorkspace} workspace
   * @return {angular.IPromise<any>}
   */
  runWorkspace(workspace: che.IWorkspace): ng.IPromise<any> {
    return this.ideSvc.startIde(workspace).catch((error: any) => {
      let errorMessage;

      if (!error || !(error.data || error.error)) {
        errorMessage = 'Unable to start this workspace.';
      } else if (error.error) {
        errorMessage = error.error;
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
      this.$log.error(error);

      return this.$q.reject({message: errorMessage});
    });
  }

  /**
   * Stops the workspace.
   *
   * @param {string} workspaceId
   * @return {angular.IPromise<any>}
   */
  stopWorkspace(workspaceId: string): ng.IPromise<any> {
    return this.cheWorkspace.stopWorkspace(workspaceId).catch((error: any) => {
      this.cheNotification.showError('Stop workspace failed.', error);
      this.$log.error(error);

      return this.$q.reject(error);
    });
  }

  /**
   * Shows modal window with notification about unsaved changes.
   */
  notifyUnsavedChangesDialog(): ng.IPromise<void> {
    return this.confirmDialogService.showConfirmDialog('Unsaved Changes', `You're editing this workspace configuration. Please save or discard changes to be able to run or stop the workspace.`, { reject: 'Close' });
  }

}
