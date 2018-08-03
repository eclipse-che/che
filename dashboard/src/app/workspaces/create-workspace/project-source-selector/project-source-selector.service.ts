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
import {ActionType} from './project-source-selector-action-type.enum';
import {editingProgress} from './project-source-selector-editing-progress';
import {EditProjectService} from './edit-project/edit-project.service';
import {AddImportProjectService} from './add-import-project/add-import-project.service';
import {RandomSvc} from '../../../../components/utils/random.service';

/**
 * This class is handling the service for the project selector.
 *
 * @author Oleksii Kurinnyi
 */
export class ProjectSourceSelectorService {

  static $inject = ['randomSvc', 'addImportProjectService', 'editProjectService'];

  /**
   * Service for project adding or importing.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Edit project service.
   */
  private editProjectService: EditProjectService;
  /**
   * Generator for random strings.
   */
  private randomSvc: RandomSvc;
  /**
   * Project templates to import.
   */
  private projectTemplates: Array<che.IProjectTemplate>;
  /**
   * Action type.
   */
  private activeActionType: ActionType;

  /**
   * Default constructor that is using resource injection
   */
  constructor(randomSvc: RandomSvc, addImportProjectService: AddImportProjectService, editProjectService: EditProjectService) {
    this.randomSvc = randomSvc;
    this.addImportProjectService = addImportProjectService;
    this.editProjectService = editProjectService;

    this.projectTemplates = [];
  }

  /**
   * Checks if any project's template is adding or editing.
   *
   * @return {editingProgress}
   */
  getEditingProgress(): editingProgress {
    if (this.activeActionType === ActionType.EDIT_PROJECT) {
      return this.editProjectService.checkEditingProgress();
    } else if (this.activeActionType === ActionType.ADD_PROJECT) {
      return this.addImportProjectService.checkEditingProgress();
    }
    return null;
  }

  /**
   * Set active action type.
   *
   * @param {ActionType} actionType
   */
  setActionType(actionType: ActionType): void {
    this.activeActionType = actionType;
  }

  /**
   * Adds project template to the list.
   *
   * @param {che.IProjectTemplate} projectTemplate the project template
   */
  addProjectTemplate(projectTemplate: che.IProjectTemplate): void {
    const origName = projectTemplate.name;

    if (this.isProjectTemplateNameUnique(origName) === false) {
      // update name, displayName and path
      const newName = this.getUniqueName(origName);
      projectTemplate.name = newName;
      projectTemplate.displayName = newName;
      projectTemplate.path = '/' +  newName.replace(/[^\w-_]/g, '_');
    }

    if (!projectTemplate.type && projectTemplate.projectType) {
      projectTemplate.type = projectTemplate.projectType;
    }

    this.projectTemplates.push(projectTemplate);
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
      if (this.isProjectTemplateNameUnique(newName)) {
        return newName;
      }
    }

    return this.randomSvc.getRandString({prefix: name + '-'});
  }

  /**
   * Returns list of project templates.
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getProjectTemplates(): Array<che.IProjectTemplate> {
    return this.projectTemplates;
  }

  /**
   * Returns <code>true</code> if project's template name is unique.
   *
   * @param {string} name the project's template name
   * @param {string=} excluded the project's template name which should be skipped from comparison
   * @return {boolean}
   */
  isProjectTemplateNameUnique(name: string, excluded?: string): boolean {
    return this.projectTemplates.every((projectTemplate: che.IProjectTemplate) => {
      return projectTemplate.name !== name || projectTemplate.name === excluded;
    });
  }

  /**
   * Removes project's template from the list.
   *
   * @param {string} projectTemplateName the template to remove
   */
  removeProjectTemplate(projectTemplateName: string): void {
    let indexToRemove = -1;
    this.projectTemplates.find((projectTemplate: che.IProjectTemplate, index: number) => {
      if (projectTemplate.name === projectTemplateName) {
        indexToRemove = index;
        return true;
      }
      return false;
    });

    if (indexToRemove !== -1) {
      this.projectTemplates.splice(indexToRemove, 1);
    }
  }

  /**
   * Updates project template's metadata.
   *
   * @param {string} oldTemplateName project's template old name
   * @param {che.IProjectTemplate} projectTemplateNew project template which was edited
   */
  updateProjectTemplate(oldTemplateName: string, projectTemplateNew: che.IProjectTemplate) {
    if (!oldTemplateName) {
      return;
    }

    const projectTemplateOld = this.projectTemplates.find((_projectTemplate: che.IProjectTemplate) => {
      return _projectTemplate.name === oldTemplateName;
    });

    if (!projectTemplateOld) {
      return;
    }

    angular.extend(projectTemplateOld, projectTemplateNew);
  }

  /**
   * Clears templates list.
   */
  clearTemplatesList(): void {
    this.projectTemplates = [];
  }

}
