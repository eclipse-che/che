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

import {editingProgress, IEditingProgress} from '../../project-source-selector-editing-progress';

/**
 * Service for template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelectorSvc implements IEditingProgress {

  static $inject = ['$filter', '$q'];

  /**
   * Filter service.
   */
  $filter: ng.IFilterService;
  /**
   * Promises service.
   */
  $q: ng.IQService;
  /**
   * The list of selected templates.
   */
  templates: Array<che.IProjectTemplate>;

  /**
   * Default constructor that is using resource injection
   */
  constructor($filter: ng.IFilterService, $q: ng.IQService) {
    this.$filter = $filter;
    this.$q = $q;

    this.templates = [];
  }

  /**
   * Returns projects' adding progress.
   *
   * @return {editingProgress}
   */
  checkEditingProgress(): editingProgress {
    if (this.templates.length === 0) {
      return null;
    }

    const number = this.templates.length;
    return {
      message: `There ${number === 1 ? 'is' : 'are'} ${number} ${number === 1 ? 'sample' : 'samples'} selected but not added.`,
      number: number
    };
  }

  /**
   * Callback which is called when template is checked or unchecked.
   *
   * @param {Array<che.IProjectTemplate>} templates the list of selected templates
   */
  onTemplateSelected(templates: Array<che.IProjectTemplate>): void {
    this.templates = templates;
  }

  /**
   * Returns selected templates.
   *
   * @return {che.IProjectTemplate[]}
   */
  getTemplates(): Array<che.IProjectTemplate> {
    return angular.copy(this.templates);
  }

}
