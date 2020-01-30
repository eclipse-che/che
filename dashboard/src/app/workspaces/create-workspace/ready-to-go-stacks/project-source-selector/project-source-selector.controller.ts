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
import {ProjectSourceSelectorService} from './project-source-selector.service';
import {IProjectSourceSelectorScope, IProjectSourceSelectorScopeBindings} from './project-source-selector.directive';
import {ActionType} from './project-source-selector-action-type.enum';

/**
 * This class is handling the controller for the project selector.
 *
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class ProjectSourceSelectorController implements IProjectSourceSelectorScopeBindings {

  static $inject = [
    '$scope',
    'projectSourceSelectorService'
  ];

  /**
   * Directive scope bindings.
   */
  devfile: che.IWorkspaceDevfile;
  onChange: () => {};

  /**
   * Injected dependencies.
   */
  private $scope: IProjectSourceSelectorScope;
  private projectSourceSelectorService: ProjectSourceSelectorService;

  private devfileProjectsCopy: Array<any>;
  private devfileProjects: Array<any>;
  /**
   * State of a button.
   */
  private buttonState: { [buttonId: string]: boolean } = {};
  /**
   * Selected action's type.
   */
  private activeActionType: ActionType;
  /**
   * List of project templates which are ready to be imported.
   */
  private projectTemplates: Array<che.IProjectTemplate>;
  /**
   * Selected template.
   */
  private selectedProjectTemplate: che.IProjectTemplate;
  /**
   * Defines which content should be shown in popover in project's section.
   */
  private actionType: Object;
  /**
   * ID of active button.
   */
  private activeButtonId: string;
  /**
   * <code>true</code> if content has to be scrolled to bottom.
   * @type {boolean}
   */
  private scrollToBottom: boolean = false;

  /**
   * Default constructor that is using resource injection
   */
  constructor($scope: IProjectSourceSelectorScope, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.$scope = $scope;
    this.projectSourceSelectorService = projectSourceSelectorService;
    this.actionType = ActionType;
    this.devfileProjectsCopy = null;

    $scope.$watch(() => {
      return this.devfile;
    }, () => {
      if (!this.devfile) {
        return;
      }
      this.devfileProjects = angular.copy(this.devfile.projects);
      if (this.devfileProjectsCopy && this.devfileProjectsCopy !== this.devfileProjects) {
        this.devfileProjectsCopy.forEach((project: any) => {
          this.projectSourceSelectorService.removeProjectTemplate(project.name);
        });
        this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();
        this.updateWidget({buttonState: true, actionType: ActionType.ADD_PROJECT});
      }

      if (this.devfileProjects && this.devfileProjects.length > 0) {
        this.devfileProjects.forEach((project: any) => {
          this.projectSourceSelectorService.addProjectTemplate(project);
        });

        // update list of templates to redraw the projects section
        this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();
        this.updateWidget({buttonState: true, actionType: ActionType.EDIT_PROJECT, template: this.devfileProjects[0]});
      }

      this.devfileProjectsCopy = angular.copy(this.devfileProjects);
      this.onChange();
    }, true);

    $scope.$watch('$destroy', () => {
      this.projectSourceSelectorService.clearTemplatesList();
    });
  }

  $onInit(): void { }

  /**
   * Add project templates from selected source to the list.
   *
   * @param {Array<che.IProjectTemplate>} projectTemplates list of templates
   */
  projectTemplateOnAdd(projectTemplates: Array<che.IProjectTemplate>): void {
    if (!projectTemplates || !projectTemplates.length) {
      return;
    }

    let projectTemplate: che.IProjectTemplate;
    projectTemplates.forEach((_projectTemplate: che.IProjectTemplate) => {
      projectTemplate = _projectTemplate;
      this.projectSourceSelectorService.addProjectTemplate(_projectTemplate);
    });

    // update list of templates to redraw the projects section
    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();
    this.updateWidget({buttonState: true, actionType: ActionType.EDIT_PROJECT, template: projectTemplate});

    this.onChange();
  }

  /**
   * Removes selected template from ready-to-import templates.
   */
  projectTemplateOnRemove(): void {
    this.projectSourceSelectorService.removeProjectTemplate(this.selectedProjectTemplate.name);

    // update list of templates to redraw the projects section
    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();
    this.updateWidget({buttonState: true, actionType: ActionType.ADD_PROJECT});

    this.onChange();
  }

  /**
   * Updates selected template's metadata.
   */
  projectTemplateOnEdit(projectTemplate: che.IProjectTemplate): void {
    this.projectSourceSelectorService.updateProjectTemplate(this.selectedProjectTemplate.name, projectTemplate);

    // update list of templates to redraw the projects section
    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateWidget({buttonState: true, actionType: ActionType.EDIT_PROJECT, template: projectTemplate});

    this.onChange();
  }

  /**
   * Updates widget data.
   *
   * @param buttonState {boolean} toggle button state
   * @param actionType {ActionType} current action type
   * @param template {che.IProjectTemplate} the project's template
   */
  updateWidget({buttonState, actionType, template = null}: { buttonState: boolean, actionType: ActionType, template?: che.IProjectTemplate }): void {

    const buttonId = template
      ? template.name
      : ActionType[ActionType.ADD_PROJECT];

    if (!buttonState && this.activeButtonId && this.activeButtonId !== buttonId) {
      return;
    }

    this.activeButtonId = buttonId;
    this.projectSourceSelectorService.setActionType(actionType);

    // leave only one selected button
    this.buttonState = {
      [buttonId]: true
    };

    this.activeActionType = actionType;
    this.selectedProjectTemplate = angular.copy(template);

    this.$scope.updateWidget(this.activeButtonId, this.scrollToBottom);
    this.scrollToBottom = false;
  }

  /**
   * Returns <code>true</code> if project project name is unique.
   *
   * @param {string} name the project template name.
   * @return {boolean}
   */
  isProjectNameUnique(name: string): boolean {
    return this.projectSourceSelectorService.isProjectTemplateNameUnique(name);
  }

}
