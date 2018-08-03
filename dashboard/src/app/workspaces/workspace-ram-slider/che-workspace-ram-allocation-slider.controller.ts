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
 * Controller for the Workspace Ram slider
 * @author Florent Benoit
 */
export class CheWorkspaceRamAllocationSliderController {

  static $inject = ['$timeout', '$scope'];

  onChangeTimeoutPromise: ng.IPromise<any>;
  $timeout: ng.ITimeoutService;
  ngModel: number;
  inputVal: number;
  cheOnChange: Function;

  /**
   * Default constructor that is using resource
   */
  constructor ($timeout: ng.ITimeoutService, $scope: ng.IScope) {
    this.$timeout = $timeout;

    $scope.$watch(() => {
      return this.ngModel;
    }, () => {
      this.inputVal = this.init(this.ngModel / Math.pow(1024, 3));
    });
  }

  /**
   * Rounds value to first decimal
   * @param value: number original value
   * @returns {number} rounded value
   */
  init(value: number): number {
    let factor: number = Math.pow(10, 1);
    let tempValue: number = value * factor;
    let roundedTempValue: number = Math.round(tempValue);
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
