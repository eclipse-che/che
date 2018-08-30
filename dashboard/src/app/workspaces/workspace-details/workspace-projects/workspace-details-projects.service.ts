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
import {CheAPI} from '../../../../components/api/che-api.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheWorkspace} from '../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc service
 * @name workspace.details.controller:WorkspaceDetailsProjectsService
 * @description This class is handling the service for details of workspace : section projects
 * @author Oleksii Kurinnyi
 */
export class WorkspaceDetailsProjectsService {

  static $inject = ['$log', '$q', 'cheAPI', 'cheNotification'];

  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Angular Promise service.
   */
  private $q: ng.IQService;
  /**
   * API entry point.
   */
  private cheAPI: CheAPI;
  /**
   * Notification factory.
   */
  private cheNotification: CheNotification;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: CheWorkspace;
  /**
   * List of project templates that are going to be imported.
   */
  private projectTemplates: Array<che.IProjectTemplate>;
  /**
   * List of names of projects which are going to be deleted.
   */
  private projectNamesToDelete: string[];

  /**
   * Default constructor that is using resource
   */
  constructor($log: ng.ILogService,
              $q: ng.IQService,
              cheAPI: CheAPI,
              cheNotification: CheNotification
  ) {
    this.$log = $log;
    this.$q = $q;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    this.cheWorkspace = this.cheAPI.getWorkspace();

    this.projectTemplates = [];
    this.projectNamesToDelete = [];
  }

  /**
   * Clears project templates list.
   */
  clearProjectTemplates(): void {
    this.projectTemplates = [];
  }

  /**
   * Remove project template from list by its name.
   *
   * @param {string} templateName
   */
  removeProjectTemplate(templateName: string): void {
    this.projectTemplates = this.projectTemplates.filter((projectTemplate: che.IProjectTemplate) => {
      return projectTemplate.name !== templateName;
    });
  }

  /**
   * Adds project template to the list of ready-to-import templates.
   *
   * @param {che.IProjectTemplate} projectTemplate
   */
  addProjectTemplate(projectTemplate: che.IProjectTemplate): void {
    this.projectTemplates.push(projectTemplate);
  }

  /**
   * Returns list of ready-to-import templates.
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getProjectTemplates(): Array<che.IProjectTemplate> {
    return this.projectTemplates;
  }

  /**
   * Returns <code>true</code> if project is not imported yet.
   *
   * @param {string} projectName
   * @return {boolean}
   */
  isNewProject(projectName: string): boolean {
    return this.projectTemplates.some((projectTemplate: che.IProjectTemplate) => {
      return projectTemplate.name === projectName;
    });
  }

  /**
   * Returns list of names of projects which are going to be deleted.
   *
   * @return {string[]}
   */
  getProjectNamesToDelete(): string[] {
    return this.projectNamesToDelete;
  }

  /**
   * Adds project names to the list of ready-to-delete projects.
   *
   * @param {string} projectsName
   */
  addProjectNamesToDelete(projectsName: string[]): void {
    projectsName.forEach((projectName: string) => {
      if (this.isNewProject(projectName)) {
        this.removeProjectTemplate(projectName);
        return;
      }

      if (this.projectNamesToDelete.indexOf(projectName) !== -1) {
        return;
      }
      this.projectNamesToDelete.push(projectName);
    });
  }

  /**
   * Clears list of names of ready-to-delete projects.
   */
  clearProjectNamesToDelete(): void {
    this.projectNamesToDelete = [];
  }

  /**
   * Deletes projects by names.
   *
   * @param {string} workspaceId workspace ID
   * @param {string[]} projectNames list of names of projects to delete
   * @return {angular.IPromise<any>}
   */
  deleteSelectedProjects(workspaceId: string, projectNames: string[]): ng.IPromise<any> {
    const deleteProjectPromises = [];

    const workspaceAgent = this.cheWorkspace.getWorkspaceAgent(workspaceId);

    if (!workspaceAgent) {
      return this.$q.reject({message: 'Workspace isn\'t run. Cannot delete any project.'});
    }
    const projectService = workspaceAgent.getProject();

    projectNames.forEach((projectName: string) => {
      const promise = projectService.remove(projectName);
      deleteProjectPromises.push(promise);
    });

    return this.$q.all(deleteProjectPromises).catch((error: any) => {
      this.$log.error(error);
      return this.$q.reject(error);
    });
  }

}
