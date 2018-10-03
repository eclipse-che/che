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

import {NamespaceSelectorSvc} from './namespace-selector/namespace-selector.service';
import {StackSelectorSvc} from './stack-selector/stack-selector.service';
import {ProjectSourceSelectorService} from './project-source-selector/project-source-selector.service';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';

/**
 * This class is handling the service for workspace creation.
 *
 * @author Oleksii Kurinnyi
 */
export class CreateWorkspaceSvc {

  static $inject = ['$location', '$log', '$q', 'cheWorkspace', 'namespaceSelectorSvc', 'stackSelectorSvc', 'projectSourceSelectorService', 'cheNotification', 'confirmDialogService', '$document'];

  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Log service.
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
   * Namespace selector service.
   */
  private namespaceSelectorSvc: NamespaceSelectorSvc;
  /**
   * Project selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Stack selector service.
   */
  private stackSelectorSvc: StackSelectorSvc;
  /**
   * The list of workspaces by namespace.
   */
  private workspacesByNamespace: {
    [namespaceId: string]: Array<che.IWorkspace>
  };
  /**
   * Notification factory.
   */
  private cheNotification: CheNotification;
  /**
   * Confirmation dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * Document service.
   */
  private $document: ng.IDocumentService;

  /**
   * Default constructor that is using resource injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $q: ng.IQService, cheWorkspace: CheWorkspace, namespaceSelectorSvc: NamespaceSelectorSvc, stackSelectorSvc: StackSelectorSvc, projectSourceSelectorService: ProjectSourceSelectorService, cheNotification: CheNotification, confirmDialogService: ConfirmDialogService, $document: ng.IDocumentService) {
    this.$location = $location;
    this.$log = $log;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.stackSelectorSvc = stackSelectorSvc;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
    this.$document = $document;

    this.workspacesByNamespace = {};
  }

  /**
   * Fills in list of workspace's name in current namespace,
   * and triggers validation of entered workspace's name
   *
   * @param {string} namespaceId a namespace ID
   * @return {IPromise<any>}
   */
  fetchWorkspacesByNamespace(namespaceId: string): ng.IPromise<any> {
    return this.getOrFetchWorkspacesByNamespace(namespaceId).then((workspaces: Array<che.IWorkspace>) => {
      return this.$q.when(workspaces);
    }, (error: any) => {
      // user is not authorized to get workspaces by namespace
      return this.getOrFetchWorkspaces();
    }).then((workspaces: Array<che.IWorkspace>) => {
      const filteredWorkspaces = workspaces.filter((workspace: che.IWorkspace) => {
        return workspace.namespace === namespaceId;
      });
      this.workspacesByNamespace[namespaceId] = filteredWorkspaces;
      return this.$q.when(filteredWorkspaces);
    });
  }

  /**
   * Returns promise for getting list of workspaces by namespace.
   *
   * @param {string} namespaceId a namespace ID
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspacesByNamespace(namespaceId: string): ng.IPromise<any> {
    const defer = this.$q.defer();

    this.cheWorkspace.fetchWorkspacesByNamespace(namespaceId).then(() => {
      defer.resolve(this.cheWorkspace.getWorkspacesByNamespace(namespaceId) || []);
    }, (error: any) => {
      if (error.status === 304) {
        defer.resolve(this.cheWorkspace.getWorkspacesByNamespace(namespaceId) || []);
      } else {
        // not authorized
        defer.reject(error);
      }
    });

    return defer.promise;
  }

  /**
   * Returns promise for getting list of workspaces owned by user
   *
   * @return {ng.IPromise<any>}
   */
  getOrFetchWorkspaces(): ng.IPromise<any> {
    const defer = this.$q.defer();
    const workspacesList = this.cheWorkspace.getWorkspaces();
    if (workspacesList.length) {
      defer.resolve(workspacesList);
    } else {
      this.cheWorkspace.fetchWorkspaces().finally(() => {
        defer.resolve(this.cheWorkspace.getWorkspaces());
      });
    }

    return defer.promise;
  }

  /**
   * Creates a workspace from config.
   *
   * @param {che.IWorkspaceConfig} workspaceConfig the config of workspace which will be created
   * @param {any} attributes the attributes of the workspace
   * @returns {ng.IPromise<che.IWorkspace>}
   */
  createWorkspace(workspaceConfig: che.IWorkspaceConfig, attributes: any): ng.IPromise<che.IWorkspace> {
    const namespaceId = this.namespaceSelectorSvc.getNamespaceId(),
          projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    return this.checkEditingProgress().then(() => {
      workspaceConfig.projects = projectTemplates;
      this.addProjectCommands(workspaceConfig, projectTemplates);
      return this.cheWorkspace.createWorkspaceFromConfig(namespaceId, workspaceConfig, attributes).then((workspace: che.IWorkspace) => {
        return this.cheWorkspace.fetchWorkspaces().then(() => this.cheWorkspace.getWorkspaceById(workspace.id));
      })
      .then((workspace: che.IWorkspace) => {
        this.projectSourceSelectorService.clearTemplatesList();
        const workspaces = this.cheWorkspace.getWorkspaces();
        if (workspaces.findIndex((_workspace: che.IWorkspace) => {
            return _workspace.id === workspace.id;
          }) === -1) {
          workspaces.push(workspace);
        }
        this.cheWorkspace.startUpdateWorkspaceStatus(workspace.id);

        return workspace;
      }, (error: any) => {
        let errorMessage = 'Creation workspace failed.';
        if (error && error.data && error.data.message) {
          errorMessage = error.data.message;
        }
        this.cheNotification.showError(errorMessage);

        return this.$q.reject(error);
      });
    });
  }

  /**
   * Show confirmation dialog when project editing is not completed.
   *
   * @return {angular.IPromise<any>}
   */
  checkEditingProgress(): ng.IPromise<any> {
    const editingProgress = this.projectSourceSelectorService.getEditingProgress();
    if (editingProgress === null) {
      return this.$q.when();
    }

    const title = 'Warning',
          content = `You have project editing, that is not completed. Would you like to proceed to workspace creation without these changes?`;
    return this.confirmDialogService.showConfirmDialog(title, content, 'Continue');
  }

  /**
   * Redirects to IDE with specified workspace.
   *
   * @param {che.IWorkspace} workspace the workspace to open in IDE
   */
  redirectToIDE(workspace: che.IWorkspace): void {
    const path = `/ide/${workspace.namespace}/${workspace.config.name}`;
    this.$location.path(path);
  }

  /**
   * Redirects to the details page of the workspace.
   *
   * @param {che.IWorkspace} workspace the workspace to open in IDE
   */
  redirectToDetails(workspace: che.IWorkspace): void {
    const path = `/workspace/${workspace.namespace}/${workspace.config.name}`;
    this.$location.path(path);
  }

  /**
   * Adds commands from the bunch of project templates to provided workspace config.
   *
   * @param {che.IWorkspaceConfig} workspaceConfig workspace config
   * @param {Array<che.IProjectTemplate>} projectTemplates the list of project templates
   */
  addProjectCommands(workspaceConfig: che.IWorkspaceConfig, projectTemplates: Array<che.IProjectTemplate>): void {
    workspaceConfig.commands = workspaceConfig.commands || [];

    projectTemplates.forEach((template: che.IProjectTemplate) => {
      let projectName = template.name;
      template.commands.forEach((command: any) => {
        command.name = projectName + ':' + command.name;
        workspaceConfig.commands.push(command);
      });
    });
  }

  /**
   * Returns the location of the plugin registry.
   *
   * @returns {string} the location of the plugin registry if exists
   */
  getPluginRegistryLocation(): string {
    return this.cheWorkspace.getWorkspaceSettings() != null ? this.cheWorkspace.getWorkspaceSettings().cheWorkspacePluginRegistryUrl : null;
  }
}
