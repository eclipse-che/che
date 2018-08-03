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
 * Defines a directive for the toggle button.
 * @author Florent Benoit
 */
export class CheToggleController {

  static $inject = ['$scope'];

  $scope: ng.IScope;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope) {
    this.$scope = $scope;
  }

  getSelected(): string {
    return (this.$scope as any).setupModelController.$viewValue;
  }

  getCss(selected: string): string {
    if (this.getSelected() !== selected) {
      return 'che-toggle-button-disabled';
    }
    return 'che-toggle-button-enabled';
  }

  onClick(selected: string): void {
    (this.$scope as any).setupModelController.$setViewValue(selected);
  }
}
