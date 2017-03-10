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
  templates: Array<che.IProjectTemplate>;
  selectedTemplateName: string = '';

  currentStackTags: string[];
  projectSampleOnSelect: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheAPI: CheAPI) {
    this.templates = cheAPI.getProjectTemplate().getAllProjectTemplates();
    if (!this.templates.length) {
      cheAPI.getProjectTemplate().fetchTemplates();
    }
  }

  /**
   * Returns filtered list of templates.
   *
   * @return {che.IProjectTemplate[]}
   */
  getFilteredTemplates(): che.IProjectTemplate[] {
    let stackTags = !this.currentStackTags ? [] : this.currentStackTags.map((tag: string) => tag.toLowerCase());

    if (!stackTags.length) {
      return this.templates;
    }

    return this.templates.filter((template: che.IProjectTemplate) => {
      let templateTags = template.tags.map((tag: string) => tag.toLowerCase());
      return stackTags.some((tag: string) => templateTags.indexOf(tag) > -1);
    });
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
