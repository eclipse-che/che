/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';
import {CheToggleController} from "./che-toggle.controller";

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
   * @ngInject for Dependency injection
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
