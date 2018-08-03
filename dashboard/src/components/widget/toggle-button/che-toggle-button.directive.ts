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
 * Defines a directive for a toggle button.
 * @author Florent Benoit
 */
export class CheToggleButton {
  restrict: string;
  templateUrl: string;
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'components/widget/toggle-button/che-toggle-button.html';

    // scope values
    this.scope = {
      title: '@cheTitle',
      value: '=?cheValue',
      fontIcon: '@cheFontIcon',
      ngDisabled: '@ngDisabled'
    };
  }

  link($scope: ng.IScope): void {
    ($scope as any).controller = ($scope.$parent.$parent as any).cheToggleController || ($scope.$parent.$parent.$parent as any).cheToggleController;
  }
}
