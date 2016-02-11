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

/**
 * Controller for the Workspace Ram slider
 * @author Florent Benoit
 */
export class CheWorkspaceRamAllocationSliderCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($scope) {
    "ngInject";

    $scope.$watch(() => {return this.inputVal;}, (newVal, oldVal) => {
      if (!newVal || newVal===oldVal) {
        // do not change ngModel if input contains incorrect value
        return;
      }
      this.ngModel = newVal * 1000;
    });
  }

}
