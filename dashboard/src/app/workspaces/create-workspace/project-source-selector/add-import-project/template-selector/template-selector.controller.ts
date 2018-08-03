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

import {TemplateSelectorSvc} from './template-selector.service';
import {StackSelectorSvc} from '../../../stack-selector/stack-selector.service';
import {ProjectSource} from '../../project-source.enum';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelectorController {

  static $inject = ['$filter', '$scope', 'addImportProjectService', 'templateSelectorSvc', 'stackSelectorSvc', 'cheListHelperFactory'];

  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * Template selector service.
   */
  private templateSelectorSvc: TemplateSelectorSvc;
  /**
   * Stack selector service.
   */
  private stackSelectorSvc: StackSelectorSvc;
  /**
   * Service for project adding and importing.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Helper for lists.
   */
  private cheListHelper: che.widget.ICheListHelper;
  /**
   * The list of tags of selected stack.
   */
  private stackTags: string[];
  /**
   * Sorted list of all templates.
   */
  private allTemplates: Array<che.IProjectTemplate>;
  /**
   * Filtered and sorted list of templates.
   */
  private filteredTemplates: Array<che.IProjectTemplate>;
  /**
   * List of selected templates.
   */
  private selectedTemplates: Array<che.IProjectTemplate>;

  /**
   * Default constructor that is using resource injection
   */
  constructor($filter: ng.IFilterService, $scope: ng.IScope, addImportProjectService: AddImportProjectService, templateSelectorSvc: TemplateSelectorSvc,
     stackSelectorSvc: StackSelectorSvc, cheListHelperFactory: che.widget.ICheListHelperFactory) {

    this.$filter = $filter;
    this.templateSelectorSvc = templateSelectorSvc;
    this.stackSelectorSvc = stackSelectorSvc;
    this.addImportProjectService = addImportProjectService;

    const helperId = 'template-selector';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.filteredTemplates = [];
    this.selectedTemplates = this.templateSelectorSvc.getTemplates();

    this.allTemplates = this.$filter('orderBy')(this.templateSelectorSvc.getAllTemplates(), ['projectType', 'displayName']);
    this.filterAndSortTemplates();

    const actionOnStackChanged = () => {
      this.onStackChanged();
    };
    this.stackSelectorSvc.subscribe(actionOnStackChanged);
    this.onStackChanged();

    const actionOnPublish = (source: ProjectSource) => {
      this.onAddImportProjectServicePublish(source);
    };
    this.addImportProjectService.subscribe(actionOnPublish);

    $scope.$on('$destroy', () => {
      this.addImportProjectService.unsubscribe(actionOnPublish);
      this.stackSelectorSvc.unsubscribe(actionOnStackChanged);
    });
  }

  /**
   * Callback which is called when stack is selected.
   */
  onStackChanged(): void {
    const stackId = this.stackSelectorSvc.getStackId();
    if (!stackId) {
      return;
    }

    const stack = this.stackSelectorSvc.getStackById(stackId);
    this.stackTags = stack ? stack.tags : [];

    this.filterAndSortTemplates();
  }

  /**
   * Callback which is called when project template is added to the list of ready-to-import projects.
   * Make samples not checked.
   *
   * @param {ProjectSource} source project's source
   */
  onAddImportProjectServicePublish(source: ProjectSource): void {
    if (source !== ProjectSource.SAMPLES) {
      return;
    }

    this.cheListHelper.deselectAllItems();
    this.selectedTemplates = [];

    this.templateSelectorSvc.onTemplateSelected(this.selectedTemplates);
  }

  /**
   * Filters templates by tags and sort them by project type and template name.
   */
  filterAndSortTemplates(): void {
    const stackTags = !this.stackTags ? [] : this.stackTags.map((tag: string) => tag.toLowerCase());

    if (stackTags.length === 0) {
      this.filteredTemplates = angular.copy(this.allTemplates);
    } else {
      this.filteredTemplates = this.allTemplates.filter((template: che.IProjectTemplate) => {
        const templateTags = template.tags.map((tag: string) => tag.toLowerCase());
        return stackTags.some((tag: string) => templateTags.indexOf(tag) > -1);
      });
    }

    this.cheListHelper.setList(this.filteredTemplates, 'name');
    this.selectedTemplates.forEach((template: che.IProjectTemplate) => {
      this.cheListHelper.itemsSelectionStatus[template.name] = true;
    });
  }

  /**
   * Callback which is when the template checkbox is clicked.
   */
  onTemplateChanged(): void {
    this.selectedTemplates = this.cheListHelper.getSelectedItems() as Array<che.IProjectTemplate>;
    this.templateSelectorSvc.onTemplateSelected(this.selectedTemplates);
  }

}
