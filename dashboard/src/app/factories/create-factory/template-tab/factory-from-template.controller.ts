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
import {CheNotification} from '../../../../components/notification/che-notification.factory';

/**
 * Controller for creating factory from a template.
 * @author Oleksii Orel
 */
export class FactoryFromTemplateCtrl {
  private $filter: ng.IFilterService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private isImporting: boolean;
  private factoryContent: any;
  private editorOptions: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter: ng.IFilterService, cheAPI: CheAPI, cheNotification: CheNotification) {
    this.$filter = $filter;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    this.isImporting = false;
    this.factoryContent = null;

    this.editorOptions = {
      mode: 'application/json'
    };
  }

  // gets factory template.
  getFactoryTemplate(templateName: string) {
    let factoryContent = this.cheAPI.getFactoryTemplate().getFactoryTemplate(templateName);

    if (factoryContent) {
      this.factoryContent = this.$filter('json')(factoryContent, 2);
      return;
    }

    this.isImporting = true;

    // fetch it:
    let promise = this.cheAPI.getFactoryTemplate().fetchFactoryTemplate(templateName);

    promise.then((factoryContent: any) => {
      this.isImporting = false;
      this.factoryContent = this.$filter('json')(factoryContent, 2);
    }, (error: any) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Fail to get factory template.');
    });
  }

}
