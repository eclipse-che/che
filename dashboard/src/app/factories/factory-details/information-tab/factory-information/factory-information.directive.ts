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
 * Defines a directive for displaying factory-information widget.
 * @author Oleksii Orel
 */
export class FactoryInformation {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/factory-details/information-tab/factory-information/factory-information.html';
    this.replace = false;

    this.controller = 'FactoryInformationCtrl';
    this.controllerAs = 'factoryInformationCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      factory: '=cdvyFactory'
    };
  }

}
