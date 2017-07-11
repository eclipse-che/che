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
import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheFactoryTemplate} from '../../../../components/api/che-factory-template.factory';

/**
 * Controller for creating factory from a template.
 * @author Oleksii Orel
 */
export class FactoryFromTemplateController {
  editorState: {isValid: boolean; errors: Array<string>} = {isValid: true, errors: []};
  private $filter: ng.IFilterService;
  private cheFactoryTemplate: CheFactoryTemplate;
  private cheNotification: CheNotification;
  private isImporting: boolean;
  private factoryContent: any;
  private templateName: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
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
    this.isImporting = true;
    this.cheFactoryTemplate.fetchFactoryTemplate(this.templateName).then((factory: che.IFactory) => {
      this.factoryContent = this.$filter('json')(factory, 2);
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Fail to get factory template.');
    }).finally(() => {
      this.isImporting = false;
    });
  }

}
