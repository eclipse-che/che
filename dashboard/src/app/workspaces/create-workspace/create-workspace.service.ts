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

import {CheWorkspace} from '../../../components/api/che-workspace.factory';
import IdeSvc from '../../ide/ide.service';
import {NamespaceSelectorSvc} from './namespace-selector/namespace-selector.service';
import {StackSelectorSvc} from './stack-selector/stack-selector.service';
import {ProjectSourceSelectorService} from './project-source-selector/project-source-selector.service';
import {CheNotification} from '../../../components/notification/che-notification.factory';
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';

/**
 * This class is handling the service for workspace creation.
 *
 * @author Oleksii Kurinnyi
 */
export class CreateWorkspaceSvc {
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
   * IDE service.
   */
  private ideSvc: IdeSvc;
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
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $log: ng.ILogService, $q: ng.IQService, cheWorkspace: CheWorkspace, ideSvc: IdeSvc, namespaceSelectorSvc: NamespaceSelectorSvc, stackSelectorSvc: StackSelectorSvc, projectSourceSelectorService: ProjectSourceSelectorService, cheNotification: CheNotification, confirmDialogService: ConfirmDialogService) {
    this.$location = $location;
    this.$log = $log;
    this.$q = $q;
    this.cheWorkspace = cheWorkspace;
    this.ideSvc = ideSvc;
    this.namespaceSelectorSvc = namespaceSelectorSvc;
    this.stackSelectorSvc = stackSelectorSvc;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

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
   * @return {IPromise<any>}
   */
  createWorkspace(workspaceConfig: che.IWorkspaceConfig, attributes?: any): ng.IPromise<any> {
    const namespaceId = this.namespaceSelectorSvc.getNamespaceId(),
          projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    return this.checkEditingProgress().then(() => {
      return this.cheWorkspace.createWorkspaceFromConfig(namespaceId, workspaceConfig, attributes).then((workspace: che.IWorkspace) => {

        return this.cheWorkspace.startWorkspace(workspace.id, workspace.config.defaultEnv).then(() => {
          this.redirectToIde(namespaceId, workspace);
          this.projectSourceSelectorService.clearAllSources();

          this.cheWorkspace.getWorkspacesById().set(workspace.id, workspace);
          this.cheWorkspace.startUpdateWorkspaceStatus(workspace.id);
          return this.cheWorkspace.fetchStatusChange(workspace.id, 'RUNNING');
        }).then(() => {
          return this.cheWorkspace.fetchWorkspaceDetails(workspace.id);
        }).then(() => {
          return this.createProjects(workspace.id, projectTemplates);
        }).then(() => {
          this.getIDE().ProjectExplorer.refresh();
          return this.importProjects(workspace.id, projectTemplates);
        }).then(() => {
          let IDE = this.getIDE();
          IDE.ProjectExplorer.refresh();
          IDE.CommandManager.refresh();
        });
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
   * @param {string} namespaceId the namespace ID
   * @param {che.IWorkspace} workspace the workspace to open in IDE
   */
  redirectToIde(namespaceId: string, workspace: che.IWorkspace): void {
    const path = `/ide/${namespaceId}/${workspace.config.name}`;
    this.$location.path(path);
  }

  /**
   * Creates bunch of projects.
   *
   * @param {string} workspaceId the workspace ID
   * @param {Array<che.IProjectTemplate>} projectTemplates the list of project templates to create
   * @return {IPromise<any>}
   */
  createProjects(workspaceId: string, projectTemplates: Array<che.IProjectTemplate>): ng.IPromise<any> {
    if (projectTemplates.length === 0) {
      return this.$q.reject();
    }

    const workspaceAgent = this.cheWorkspace.getWorkspaceAgent(workspaceId);
    return workspaceAgent.getProject().createProjects(projectTemplates);
  }

  /**
   * Imports bunch of projects in row.
   * Returns resolved promise if all project are imported properly, otherwise returns rejected promise with list of names of failed projects.
   *
   * @param {string} workspaceId the workspace ID
   * @param {Array<che.IProjectTemplate>} projectTemplates the list of project templates to import
   * @return {IPromise<any>}
   */
  importProjects(workspaceId: string, projectTemplates: Array<che.IProjectTemplate>): ng.IPromise<any> {
    const defer = this.$q.defer();
    defer.resolve();
    let accumulatorPromise = defer.promise;

    const projectTypeResolverService = this.cheWorkspace.getWorkspaceAgent(workspaceId).getProjectTypeResolver();

    const failedProjects = [];

    accumulatorPromise = projectTemplates.reduce((_accumulatorPromise: ng.IPromise<any>, project: che.IProjectTemplate) => {
      return _accumulatorPromise.then(() => {
        return this.addCommands(workspaceId, project.name, project.commands).catch(() => {
          // adding commands errors, ignore them here
          return this.$q.when();
        }).then(() => {
          return projectTypeResolverService.resolveProjectType(project as any);
        }).catch((error: any) => {
          failedProjects.push(project.name);
          if (error && error.message) {
            this.$log.error(`Importing of project ${project.name} failed with error: ${error.message}`);
          }
        });
      });
    }, accumulatorPromise);

    return accumulatorPromise.then(() => {
      if (failedProjects.length) {
        return this.$q.reject(failedProjects);
      }
      return this.$q.when();
    });
  }

  /**
   * Adds bunch of commands for project in row.
   * Returns resolved promise if all commands are imported properly, otherwise returns rejected promise with list of names of failed commands.
   *
   * @param {string} workspaceId the workspace ID
   * @param {string} projectName the name of project
   * @param {any[]} projectCommands the list of commands
   * @return {IPromise<any>}
   */
  addCommands(workspaceId: string, projectName: string, projectCommands: any[]): ng.IPromise<any> {
    const defer = this.$q.defer();
    defer.resolve();
    let accumulatorPromise = defer.promise;

    if (projectCommands.length === 0) {
      return accumulatorPromise;
    }

    const failedCommands = [];

    accumulatorPromise = projectCommands.reduce((_accumulatorPromise: ng.IPromise<any>, command: any) => {
      command.name = projectName + ':' + command.name;
      return _accumulatorPromise.then(() => {
        return this.cheWorkspace.addCommand(workspaceId, command);
      }, (error: any) => {
        failedCommands.push(command.name);
        if (error && error.message) {
          this.$log.error(`Adding of command "${command.name}" failed for project "${projectName}" with error: ${error}`);
        }
      });
    }, accumulatorPromise);

    return accumulatorPromise.then(() => {
      if (failedCommands.length) {
        return this.$q.reject(failedCommands);
      }
      return this.$q.when();
    });
  }

  private getIDE(): any {
    return (window.frames[0] as any).IDE;
  }

}
