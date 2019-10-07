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
import {ProjectSource} from '../../project-source.enum';
import {AddImportProjectService} from '../add-import-project.service';

/**
 * This class is handling the controller for template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelectorController {

  static $inject = ['$filter', '$scope', 'addImportProjectService', 'templateSelectorSvc', 'cheListHelperFactory'];

  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * Template selector service.
   */
  private templateSelectorSvc: TemplateSelectorSvc;
  /**
   * Service for project adding and importing.
   */
  private addImportProjectService: AddImportProjectService;
  /**
   * Helper for lists.
   */
  private cheListHelper: che.widget.ICheListHelper;

  private devfile: che.IWorkspaceDevfile;
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
    cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$filter = $filter;
    this.templateSelectorSvc = templateSelectorSvc;
    this.addImportProjectService = addImportProjectService;

    const helperId = 'template-selector';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);

    $scope.$watch(() => {
      return this.devfile;
    }, () => {
      if (!this.devfile) {
        return;
      }
      let projects = this.devfile.projects ? this.devfile.projects : [];
      this.allTemplates = this.$filter('orderBy')(projects, ['name']);
      this.filterAndSortTemplates();
    }, true);

    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });


    const actionOnPublish = (source: ProjectSource) => {
      this.onAddImportProjectServicePublish(source);
    };
    this.addImportProjectService.subscribe(actionOnPublish);

    $scope.$on('$destroy', () => {
      this.addImportProjectService.unsubscribe(actionOnPublish);
    });
  }

  $onInit(): void {
    this.selectedTemplates = this.templateSelectorSvc.getTemplates();
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
    this.filteredTemplates = angular.copy(this.allTemplates);
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
