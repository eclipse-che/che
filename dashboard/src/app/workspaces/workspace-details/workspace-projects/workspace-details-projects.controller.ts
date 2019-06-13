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
import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheAPI} from '../../../../components/api/che-api.factory';
import {StackSelectorSvc} from '../../create-workspace/stack-selector/stack-selector.service';
import {RandomSvc} from '../../../../components/utils/random.service';
import {WorkspaceDetailsProjectsService} from './workspace-details-projects.service';
import {WorkspaceDetailsService} from '../workspace-details.service';
import {CreateWorkspaceSvc} from '../../create-workspace/create-workspace.service';
import {WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';
import {WorkspaceDataManager} from '../../../../components/api/workspace/workspace-data-manager';

/**
 * @ngdoc controller
 * @name workspace.details.controller:WorkspaceDetailsProjectsCtrl
 * @description This class is handling the controller for details of workspace : section projects
 * @author Ann Shumilova
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
export class WorkspaceDetailsProjectsCtrl {

  static $inject = ['cheAPI', '$mdDialog', 'confirmDialogService', '$scope', 'cheListHelperFactory', 'stackSelectorSvc', 'randomSvc', 'createWorkspaceSvc', 'workspaceDetailsService', 'workspaceDetailsProjectsService'];

  /**
   * Material design Dialog service.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: ConfirmDialogService;
  /**
   * List helper.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * Stack selector service.
   */
  private stackSelectorSvc: StackSelectorSvc;
  /**
   * Generator for random strings.
   */
  private randomSvc: RandomSvc;
  /**
   * Service for Project's tab on Workspace Details page.
   */
  private workspaceDetailsProjectsService: WorkspaceDetailsProjectsService;
  /**
   * Workspace creation service.
   */
  private createWorkspaceSvc: CreateWorkspaceSvc;
  /**
   * Workspace details service.
   */
  private workspaceDetailsService: WorkspaceDetailsService;
  private workspaceDataManager: WorkspaceDataManager;
  private projects: Array <any>;
  private projectFilter: any;
  private profileCreationDate: any;
  /**
   * Current workspace.
   */
  private workspaceDetails: che.IWorkspace;
  /**
   * Callback which is called on new templates added.
   * Provided by parent controller.
   */
  private projectsOnChange: () => void;

  /**
   * Default constructor that is using resource
   */
  constructor(cheAPI: CheAPI,
              $mdDialog: ng.material.IDialogService,
              confirmDialogService: ConfirmDialogService,
              $scope: ng.IScope,
              cheListHelperFactory: che.widget.ICheListHelperFactory,
              stackSelectorSvc: StackSelectorSvc,
              randomSvc: RandomSvc,
              createWorkspaceSvc: CreateWorkspaceSvc,
              workspaceDetailsService: WorkspaceDetailsService,
              workspaceDetailsProjectsService: WorkspaceDetailsProjectsService) {
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;
    this.stackSelectorSvc = stackSelectorSvc;
    this.randomSvc = randomSvc;
    this.workspaceDetailsProjectsService = workspaceDetailsProjectsService;
    this.createWorkspaceSvc = createWorkspaceSvc;
    this.workspaceDetailsService = workspaceDetailsService;

    const helperId = 'workspace-details-projects';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    const preferences = cheAPI.getPreferences().getPreferences();
    this.workspaceDataManager = cheAPI.getWorkspace().getWorkspaceDataManager();
    this.profileCreationDate = preferences['che:created'];

    this.projectFilter = {name: ''};

    const workspaceEditWatcher = $scope.$on('edit-workspace-details', (event: ng.IAngularEvent, data: {status: string}) => {
      if (data.status === 'saved' || data.status === 'cancelled') {
        this.workspaceDetailsProjectsService.clearProjectTemplates();
        this.workspaceDetailsProjectsService.clearProjectNamesToDelete();
      }
    });

    this.updateProjectsData(this.workspaceDetails);
    const action = this.updateProjectsData.bind(this);
    workspaceDetailsService.subscribeOnWorkspaceChange(action);

    $scope.$on('$destroy', () => {
      workspaceDetailsService.unsubscribeOnWorkspaceChange(action);
      this.workspaceDetailsProjectsService.clearProjectTemplates();

      // unregister watcher
      workspaceEditWatcher();
    });
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter projects names
   */
  onSearchChanged(str: string): void {
    this.projectFilter.name = str;
    this.cheListHelper.applyFilter('name', this.projectFilter);
  }

  /**
   * Creates list of existing projects and not imported ones.
   *
   * @param {che.IWorkspace} workspaceDetails
   */
  updateProjectsData(workspaceDetails: che.IWorkspace): void {
    if (!workspaceDetails) {
      return;
    }
    this.workspaceDetails = workspaceDetails;
    this.stackSelectorSvc.setStackId(this.workspaceDetails.attributes.stackId);
    this.projects = this.workspaceDataManager.getProjects(this.workspaceDetails);
    this.cheListHelper.setList(this.projects, 'name');
  }

  /**
   * Returns <code>true</code> if project is not imported yet.
   *
   * @param {string} projectName
   * @return {boolean}
   */
  isNewProject(projectName: string): boolean {
    return this.workspaceDetailsProjectsService.isNewProject(projectName);
  }

  /**
   * Adds selected template(s) to the list.
   *
   * @param {Array<che.IProjectTemplate>} projectTemplates
   */
  projectTemplateOnAdd(projectTemplates: Array<che.IProjectTemplate>): void {
    if (!projectTemplates || projectTemplates.length === 0) {
      return;
    }

    projectTemplates.forEach((projectTemplate: che.IProjectTemplate) => {
      const origName = projectTemplate.name;

      if (this.isProjectNameUnique(origName) === false) {
        // update name, displayName and path
        const newName = this.getUniqueName(origName);
        projectTemplate.name = newName;
        projectTemplate.displayName = newName;
        projectTemplate.path = '/' +  newName.replace(/[^\w-_]/g, '_');
      }

      if (!projectTemplate.type && projectTemplate.projectType) {
        projectTemplate.type = projectTemplate.projectType;
      }
      this.workspaceDetailsProjectsService.addProjectTemplate(projectTemplate);
      this.workspaceDataManager.addProject(this.workspaceDetails, projectTemplate);
    });
    //TODO waits for fix https://github.com/eclipse/che/issues/13514 to enable for devfile
    if (this.workspaceDetails.config) {
      this.createWorkspaceSvc.addProjectCommands(this.workspaceDetails, projectTemplates);
    }
    this.projectsOnChange();
  }

  /**
   * Returns <code>true</code> if project name is unique.
   *
   * @param {string} name the project name
   * @return {boolean}
   */
  isProjectNameUnique(name: string): boolean {
    return this.projects.every((project: che.IProject) => {
      return project.name !== name;
    });
  }

  /**
   * Adds increment or random string to the name
   *
   * @param {string} name
   */
  getUniqueName(name: string): string {
    const limit = 100;
    for (let i = 1; i < limit + 1; i++) {
      const newName = name + '-' + i;
      if (this.isProjectNameUnique(newName)) {
        return newName;
      }
    }

    return this.randomSvc.getRandString({prefix: name + '-'});
  }

  /**
   * Delete all selected projects
   */
  deleteSelectedProjects(): void {
    const selectedProjects = this.cheListHelper.getSelectedItems(),
          selectedProjectsNames = selectedProjects.map((project: che.IProject) => {
            return project.name;
          });

    this.showDeleteProjectsConfirmation(selectedProjects.length).then(() => {
      this.projects = this.projects.filter((project: che.IProject) => {
        return selectedProjectsNames.indexOf(project.name) === -1;
      });
      this.workspaceDataManager.setProjects(this.workspaceDetails, this.projects);
      this.workspaceDetailsProjectsService.addProjectNamesToDelete(selectedProjectsNames);

      this.projectsOnChange();
    });
  }

  /**
   * Show confirmation popup before projects to delete
   * @param numberToDelete{number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteProjectsConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' projects?';
    } else {
      content += 'this selected project?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove projects', content, 'Delete');
  }

  workspaceIsRunning(): boolean {
    return this.workspaceDetailsService.getWorkspaceStatus(this.workspaceDetails.id) === WorkspaceStatus[WorkspaceStatus.RUNNING];
  }

}
