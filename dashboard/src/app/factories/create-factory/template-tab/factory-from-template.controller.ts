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
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheFactoryTemplate} from '../../../../components/api/che-factory-template.factory';

/**
 * Controller for creating factory from a template.
 * @author Oleksii Orel
 */
export class FactoryFromTemplateController {

  static $inject = ['$filter', 'cheFactoryTemplate', 'cheNotification'];

  editorState: {isValid: boolean; errors: Array<string>} = {isValid: true, errors: []};
  private $filter: ng.IFilterService;
  private cheFactoryTemplate: CheFactoryTemplate;
  private cheNotification: CheNotification;
  private isImporting: boolean;
  private factoryContent: any;
  private templateName: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($filter: ng.IFilterService, cheFactoryTemplate: CheFactoryTemplate, cheNotification: CheNotification) {
    this.$filter = $filter;
    this.cheNotification = cheNotification;
    this.cheFactoryTemplate = cheFactoryTemplate;

    this.isImporting = false;
    this.factoryContent = null;
    this.templateName = 'minimal';

    this.updateFactoryContent();
  }

  /**
   * Updates factory content.
   */
  updateFactoryContent(): void {
    let factory = this.cheFactoryTemplate.getFactoryTemplate(this.templateName);
    if (!factory) {
      this.fetchFactoryTemplate();
      return;
    }
    this.factoryContent = this.$filter('json')(factory, 2);
  }

  /**
   * Fetch factory template.
   */
  fetchFactoryTemplate() {
    const factory = this.cheFactoryTemplate.getFactoryTemplate(this.templateName);
    this.factoryContent = this.$filter('json')(factory, 2);
  }

}
