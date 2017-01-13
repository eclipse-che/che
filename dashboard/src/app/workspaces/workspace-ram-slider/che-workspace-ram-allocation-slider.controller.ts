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
 * Controller for the Workspace Ram slider
 * @author Florent Benoit
 */
export class CheWorkspaceRamAllocationSliderController {
  onChangeTimeoutPromise: ng.IPromise;
  $timeout: ng.ITimeoutService;
  ngModel: number;
  inputVal: number;
  cheOnChange: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout: ng.ITimeoutService, $scope: ng.IScope) {
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
   * @param value: number original value
   * @returns {number} rounded value
   */
  init(value: number): number {
    var factor: number = Math.pow(10, 1);
    var tempValue: number = value * factor;
    var roundedTempValue: number = Math.round(tempValue);
    return roundedTempValue / factor;
  }

  /**
   * Update model value
   */
  onChange(): void {
    if (!this.inputVal) {
      return;
    }
    this.ngModel = this.inputVal * Math.pow(1024, 3);
    if (this.onChangeTimeoutPromise) {
      this.$timeout.cancel(this.onChangeTimeoutPromise);
    }
    this.onChangeTimeoutPromise = this.$timeout(() => {
      this.cheOnChange();
    }, 500);
  }
}
