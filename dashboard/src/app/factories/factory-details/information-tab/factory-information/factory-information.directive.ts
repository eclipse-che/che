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

/**
 * Defines a directive for displaying factory-information widget.
 * @author Oleksii Orel
 */
export class FactoryInformation {
  private restrict: string;
  private templateUrl: string;
  private replace: boolean;
  private controller: string;
  private controllerAs: string;
  private bindToController: boolean;

  private scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/factory-details/information-tab/factory-information/factory-information.html';
    this.replace = false;

    this.controller = 'FactoryInformationController';
    this.controllerAs = 'factoryInformationController';

    this.bindToController = true;

    // scope values
    this.scope = {
      factory: '=cdvyFactory'
    };
  }

}
