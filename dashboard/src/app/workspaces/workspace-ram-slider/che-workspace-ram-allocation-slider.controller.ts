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
export class CheWorkspaceRamAllocationSliderController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout, $scope) {
    "ngInject";
    this.$timeout = $timeout;

    $scope.$watch(() => {
      return this.ngModel;
    }, () => {
      this.inputVal = this.init(this.ngModel / Math.pow(1024,3));
    });
  }

  /**
   * Rounds value to first decimal
   * @param value original value
   * @returns {number} rounded value
   */
  init(value) {
    var factor = Math.pow(10, 1);
    var tempValue = value * factor;
    var roundedTempValue = Math.round(tempValue);
    return roundedTempValue / factor;
  }

  onChange() {
    if (!this.inputVal) {
      return;
    }
    this.ngModel = this.inputVal * Math.pow(1024,3);

    this.$timeout(() => {
      this.cheOnChange();
    });
  }
}
