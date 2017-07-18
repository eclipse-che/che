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

enum ButtonType { ADD_PROJECT = 1, PROJECT_TEMPLATE }

/**
 * This class is handling the controller for the project selector.
 *
 * @author Oleksii Kurinnyi
 * @author Oleksii Orel
 */
export class ProjectSourceSelectorController {
  /**
   * Updates widget function.
   */
  updateWidget: Function;

  /**
   * Scope.
   */
  private $scope: ng.IScope;
  /**
   * Project selector service.
   */
  private projectSourceSelectorService: ProjectSourceSelectorService;
  /**
   * Available project sources.
   */
  private projectSource: Object;
  /**
   * Selected project's source.
   */
  private selectedSource: ProjectSource;
  /**
   * button's values by Id.
   */
  private buttonValues: { [butonId: string]: boolean } = {};
  /**
   * Selected button's type.
   */
  private selectedButtonType: ButtonType;
  /**
   * List of project templates which are ready to be imported.
   */
  private projectTemplates: Array<che.IProjectTemplate>;
  /**
   * Selected template.
   */
  private projectTemplate: che.IProjectTemplate;
  /**
   * The copy of selected template.
   */
  private projectTemplateCopy: che.IProjectTemplate;
  /**
   * Defines which content should be shown in popover in project's section.
   */
  private buttonType: Object;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope: ng.IScope, projectSourceSelectorService: ProjectSourceSelectorService) {
    this.$scope = $scope;
    this.projectSourceSelectorService = projectSourceSelectorService;

    this.projectSource = ProjectSource;
    this.selectedSource = ProjectSource.SAMPLES;

    this.buttonType = ButtonType;
    this.selectedButtonType = ButtonType.ADD_PROJECT;

    this.$scope.$on('$destroy', () => {
      this.projectSourceSelectorService.clearAllSources();
    });
  }

  /**
   * Add project template from selected source to the list.
   */
  addProjectTemplate(): void {
    this.projectSourceSelectorService.addProjectTemplateFromSource(this.selectedSource);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();
  }

  /**
   * Resets input fields and checkboxes for selected source.
   */
  cancelProjectTemplate(): void {
    this.projectSourceSelectorService.clearSource(this.selectedSource);
  }

  /**
   * Removes selected template from ready-to-import templates.
   */
  removeTemplate(): void {
    const projectTemplateName = this.projectTemplate.name;
    this.projectSourceSelectorService.removeProjectTemplate(projectTemplateName);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateData({buttonState: true, buttonType: ButtonType.ADD_PROJECT});
  }

  /**
   * Updates selected template's metadata.
   */
  saveMetadata(): void {
    const projectTemplateName = this.projectTemplate.name;
    const projectTemplate = this.projectSourceSelectorService.updateProjectTemplateMetadata(projectTemplateName);

    this.projectTemplates = this.projectSourceSelectorService.getProjectTemplates();

    this.updateData({buttonState: true, buttonType: ButtonType.PROJECT_TEMPLATE, template: projectTemplate});
  }

  /**
   * Restores selected template's metadata.
   */
  restoreMetadata(): void {
    this.projectTemplateCopy = angular.copy(this.projectTemplate);
  }

  /**
   * Updates widget data.
   *
   * @param buttonState {boolean} toggle button state
   * @param buttonType {ButtonType} toggle button type
   * @param template {che.IProjectTemplate} the project's template
   */
  updateData({buttonState, buttonType, template = null}: { buttonState: boolean, buttonType: ButtonType, template?: che.IProjectTemplate }): void {

    const buttonValue = template
      ? template.name
      : ButtonType[ButtonType.ADD_PROJECT];

    // leave only one selected button
    this.buttonValues = {
      [buttonValue]: true
    };

    if (!buttonState) {
      return;
    }

    this.selectedButtonType = buttonType;
    this.projectTemplate = angular.copy(template);
    this.projectTemplateCopy = angular.copy(template);

    if (angular.isFunction(this.updateWidget)) {
      // update widget
      this.updateWidget();
    }
  }

}
