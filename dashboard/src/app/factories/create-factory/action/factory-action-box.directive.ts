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
 * Defines a directive for displaying action box.
 * @author Florent Benoit
 */
export class FactoryActionBox implements ng.IDirective {
  restrict: string;
  templateUrl: string;
  replace: boolean;
  controller: string;
  controllerAs: string;
  bindToController: boolean;

  scope: {
    [propName: string]: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/create-factory/action/factory-action-box.html';
    this.replace = false;

    this.controller = 'FactoryActionBoxController';
    this.controllerAs = 'factoryActionBoxCtrl';

    this.bindToController = true;

    // scope values
    this.scope = {
      lifecycle: '@cdvyLifecycle',
      actionTitle: '@?cdvyActionTitle',
      callbackController: '=cdvyCallbackController',
      factoryObject: '=cdvyFactoryObject',
      onChange: '&cdvyOnChange'
    };
  }


}
