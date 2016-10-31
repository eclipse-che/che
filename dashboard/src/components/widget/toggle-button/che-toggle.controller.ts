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
 * Defines a directive for the toggle button.
 * @author Florent Benoit
 */
export class CheToggleController {
  $scope: ng.IScope;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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
