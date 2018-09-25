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
import {CreateWorkspaceSvc} from '../create-workspace/create-workspace.service';
import {IObservable, IObservableCallbackFn, Observable} from '../../../components/utils/observable';
import {WorkspaceDetailsProjectsService} from './workspace-projects/workspace-details-projects.service';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/workspace/che-workspace.factory';
import {CheService} from '../../../components/api/che-service.factory';

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

/**
 * This class is handling the data for workspace details sections (tabs)
 *
 * @author Ann Shumilova
 * @author Oleksii Orel
 */
export class WorkspaceDetailsService {

  static $inject = ['$log', '$q', 'cheWorkspace', 'cheNotification', 'ideSvc', 'createWorkspaceSvc', 'workspaceDetailsProjectsService', 'cheService', 'chePermissions'];

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
   * Workspace creation service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Service for projects of workspace.
   */
  private workspaceDetailsProjectsService: WorkspaceDetailsProjectsService;
  /**
   * Instance of Observable.
   */
  private observable: IObservable<che.IWorkspace>;

  private pages: IPage[];
  private sections: ISection[];
  /**
   * This workspaces should be restarted for new config to be applied.
   */
  private restartToApply: string[] = [];

  /**
   * Default constructor that is using resource
   */
  constructor (
    $log: ng.ILogService,
    $q: ng.IQService,
    cheWorkspace: CheWorkspace,
    cheNotification: CheNotification,
    ideSvc: IdeSvc,
    createWorkspaceSvc: CreateWorkspaceSvc,
    workspaceDetailsProjectsService: WorkspaceDetailsProjectsService,
    cheService: CheService,
    chePermissions: che.api.IChePermissions
  ) {
    this.$log = $log;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
    this.ideSvc = ideSvc;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.workspaceDetailsProjectsService = workspaceDetailsProjectsService;

    this.observable =  new Observable<any>();

    this.pages = [];
    this.sections = [];
    this.observable = new Observable();

    cheService.fetchServices().finally(() => {
      if (cheService.isServiceAvailable(chePermissions.getPermissionsServicePath())) {
        this.addPage('Share', '<share-workspace></share-workspace>', 'icon-ic_folder_shared_24px');
      }
    });
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
  applyConfigChanges(workspace: che.IWorkspace): ng.IPromise<any> {
    return this.$q.when()
      .then(() => {
        if (this.getWorkspaceStatus(workspace.id) !== WorkspaceStatus[WorkspaceStatus.STOPPED]) {
          this.stopWorkspace(workspace.id);
        }
        return this.cheWorkspace.fetchStatusChange(workspace.id, WorkspaceStatus[WorkspaceStatus.STOPPED]);
      })
      .then(() => {
        this.removeRestartToApply(workspace.id);

        return this.saveConfigChanges(workspace);
      })
      .then(() => {
        this.cheWorkspace.startWorkspace(workspace.id, workspace.config.defaultEnv);
        return this.cheWorkspace.fetchStatusChange(workspace.id, WorkspaceStatus[WorkspaceStatus.RUNNING]);
      })
      .catch((error: any) => {
        this.$log.error(error);
        return this.$q.reject(error);
      });
  }

  /**
   * Add workspace ID to the list of workspaces that should be restarted.
   *
   * @param {string} workspaceId
   */
  addRestartToApply(workspaceId: string): void {
    if (this.restartToApply.indexOf(workspaceId) === -1) {
      this.restartToApply.push(workspaceId);
    }
  }

  /**
   * Remove workspace ID from the list of workspaces that should be restarted.
   *
   * @param {string} workspaceId
   */
  removeRestartToApply(workspaceId: string): void {
    const index = this.restartToApply.indexOf(workspaceId);
    if (index === -1) {
      return;
    }
    this.restartToApply.splice(index, 1);
  }

  /**
   * Returns <code>true</code> if workspace ID belongs to the list of
   * workspaces that should be restarted.
   *
   * @param {string} workspaceId
   * @returns {boolean}
   */
  getRestartToApply(workspaceId: string): boolean {
    const index = this.restartToApply.indexOf(workspaceId);
    return index !== -1;
  }

  /**
   * Updates workspace config.
   *
   * @param {che.IWorkspace} workspace new workspace details
   * @return {angular.IPromise<any>}
   */
  saveConfigChanges(workspace: che.IWorkspace): ng.IPromise<any> {
    delete workspace.links;

    const projectNamesToDelete = this.workspaceDetailsProjectsService.getProjectNamesToDelete(),
      hasProjectsToDelete = projectNamesToDelete.length > 0;

    return this.cheWorkspace.updateWorkspace(workspace.id, workspace)
      .then(() => {
        if (!hasProjectsToDelete) {
          return this.$q.when();
        }
        return this.workspaceDetailsProjectsService.deleteSelectedProjects(workspace.id, projectNamesToDelete);
      })
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

}
