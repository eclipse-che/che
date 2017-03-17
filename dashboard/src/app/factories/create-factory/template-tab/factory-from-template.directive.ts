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

/**
 * Defines a directive for displaying factory from template widget.
 * @author Oleksii Orel
 */
export class FactoryFromTemplate {
  private restrict: string;
  private templateUrl: string;
  private controller: string;
  private controllerAs: string;
  private bindToController: boolean;
  private replace: boolean;

  private scope: {
    [propName: string]: string;
  };


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/template-tab/factory-from-template.html';
    this.replace = false;

    this.controller = 'FactoryFromTemplateCtrl';
    this.controllerAs = 'factoryFromTemplateCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      factoryContent: '=cdvyFactoryContent',
      isImporting: '=cdvyIsImporting'
    };
  }

}
