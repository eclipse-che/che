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
import {ProjectSourceSelectorService} from './project-source-selector.service';
import {ProjectSource} from './project-source.enum';
import {IProjectSourceSelectorScope} from './project-source-selector.directive';
import {ActionType} from './project-source-selector-action-type.enum';

/**
 * This class is handling the controller for the project selector.
 *
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class ProjectSourceSelectorController {
  /**
   * Directive's scope.
   */
  private $scope: IProjectSourceSelectorScope;
  /**
   * Project selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Available project sources.
   */
  private projectSource: Object;
  /**
   * Active project's source.
   */
  private activeProjectSource: ProjectSource;
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
  private projectTemplate: che.IProjectTemplate;
  /**
   * Defines which content should be shown in popover in project's section.
   */
  private actionType: Object;
  /**
   * ID of active button.
   */
  private activeButtonId: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope: IProjectSourceSelectorScope, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.$scope = $scope;
    this.projectSourceSelectorService = projectSourceSelectorService;

    this.actionType = ActionType;
    this.projectSource = ProjectSource;

    this.activeProjectSource = ProjectSource.SAMPLES;
    this.sourceChanged();

    this.$scope.$on('$destroy', () => {
      this.projectSourceSelectorService.clearAllSources();
    });
  }

  /**
   * Set source of project which is going to be added.
   */
  sourceChanged(): void {
    this.projectSourceSelectorService.setProjectSource(this.activeProjectSource);
  }

  /**
   * Add project template from selected source to the list.
   */
  addProjectTemplate(): void {
    const projectTemplate = this.projectSourceSelectorService.addProjectTemplateFromSource(this.activeProjectSource);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateData({buttonState: true, actionType: ActionType.EDIT_PROJECT, template: projectTemplate});
  }

  /**
   * Resets input fields and checkboxes for selected source.
   */
  cancelProjectTemplate(): void {
    this.projectSourceSelectorService.clearSource(this.activeProjectSource);
  }

  /**
   * Removes selected template from ready-to-import templates.
   */
  removeTemplate(): void {
    this.projectSourceSelectorService.removeProjectTemplate(this.projectTemplate.name);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateData({buttonState: true, actionType: ActionType.ADD_PROJECT});
  }

  /**
   * Updates selected template's metadata.
   */
  saveMetadata(): void {
    const projectTemplateName = this.projectTemplate.name;
    const projectTemplate = this.projectSourceSelectorService.updateProjectTemplateMetadata(projectTemplateName);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateData({buttonState: true, actionType: ActionType.EDIT_PROJECT, template: projectTemplate});
  }

  /**
   * Restores template's metadata.
   */
  restoreMetadata(): void {
    this.projectSourceSelectorService.clearSource(null);
  }

  /**
   * Returns <code>true</code> if "Save" button should be disabled.
   *
   * @return {boolean}
   */
  disableSaveAndCancelButtons(): boolean {
    return !this.projectSourceSelectorService.getEditingProgress();
  }

  /**
   * Updates widget data.
   *
   * @param buttonState {boolean} toggle button state
   * @param actionType {ActionType} current action type
   * @param template {che.IProjectTemplate} the project's template
   */
  updateData({buttonState, actionType, template = null}: { buttonState: boolean, actionType: ActionType, template?: che.IProjectTemplate }): void {

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
    this.projectTemplate = angular.copy(template);

    this.$scope.updateWidget(this.activeButtonId);
  }

}
