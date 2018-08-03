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
import {CheToggleController} from './che-toggle.controller';

/**
 * Defines a directive for the toggle button.
 * @author Florent Benoit
 */
export class CheToggle {
  scope: Object;
  controllerAs: string;
  templateUrl: string;
  controller: string;
  restrict: string;
  require: string;
  transclude: boolean;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'components/widget/toggle-button/che-toggle.html';

    this.transclude = true;
    this.controller = 'CheToggleController';
    this.controllerAs = 'cheToggleController';

    // we require ngModel as we want to use it inside our directive
    this.require = 'ngModel';

    // scope values
    this.scope = {};

  }

  /**
   * Keep reference to the model controller
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attrs: ng.IAttributes, ngModelController: CheToggleController) {
    ($scope as any).setupModelController = ngModelController;
  }
}
