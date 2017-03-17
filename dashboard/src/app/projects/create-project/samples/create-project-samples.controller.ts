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
import {CheAPI} from '../../../../components/api/che-api.factory';

/**
 * This class is handling the controller for the samples part
 * @author Florent Benoit
 */
export class CreateProjectSamplesController {
  $filter: ng.IFilterService;

  templates: Array<che.IProjectTemplate>;
  filteredAndSortedTemplates: Array<che.IProjectTemplate>;
  selectedTemplateName: string = '';

  currentStackTags: string[];
  projectSampleOnSelect: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, $filter: ng.IFilterService, cheAPI: CheAPI) {
    this.$filter = $filter;

    this.templates = cheAPI.getProjectTemplate().getAllProjectTemplates();
    if (!this.templates.length) {
      cheAPI.getProjectTemplate().fetchTemplates();
    }

    $scope.$watch(() => {
      return this.currentStackTags;
    }, () => {
      this.filterAndSortTemplates();
    });
  }

  /**
   * Returns list of filtered and sorted templates.
   *
   * @return {che.IProjectTemplate[]}
   */
  getTemplates(): che.IProjectTemplate[] {
    return this.filteredAndSortedTemplates;
  }

  /**
   * Filters templates by tags and sort them by project type and template name.
   */
  filterAndSortTemplates(): void {
    let stackTags = !this.currentStackTags ? [] : this.currentStackTags.map((tag: string) => tag.toLowerCase());

    let filteredTemplates;
    if (!stackTags.length) {
      filteredTemplates = this.templates;
    } else {
      filteredTemplates = this.templates.filter((template: che.IProjectTemplate) => {
        let templateTags = template.tags.map((tag: string) => tag.toLowerCase());
        return stackTags.some((tag: string) => templateTags.indexOf(tag) > -1);
      });
    }

    this.filteredAndSortedTemplates = this.$filter('orderBy')(filteredTemplates, 'projectType', 'displayName');

    if (this.filteredAndSortedTemplates.length) {
      this.initItem(this.filteredAndSortedTemplates[0]);
    }
  }

  /**
   * Callback when a template is selected and also give the controller on which to select the data
   * @param template: che.IProjectTemplate -  the selected template
   */
  selectTemplate(template: che.IProjectTemplate): void {
    if (!template) {
      return;
    }

    // set selected item
    this.selectedTemplateName = template.name;

    this.projectSampleOnSelect({template: template});
  }

  /**
   * Select the first element in the list
   * @param {che.IProjectTemplate} template
   */
  initItem(template: che.IProjectTemplate): void {
    if (!template || this.selectedTemplateName === template.name) {
      return;
    }
    this.selectTemplate(template);
  }

  /**
   * Helper method used to get the length of keys of the given object
   * @param {Array<any>} items
   * @returns {number} length of keys
   */
  getItemsSize(items: Array<any>): number {
    return items.length;
  }
}
