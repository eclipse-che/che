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
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'components/widget/toggle-button/che-toggle-button.html';

    // scope values
    this.scope = {
      title: '@cheTitle',
      value: '@?cheValue',
      fontIcon: '@cheFontIcon',
      ngDisabled: '@ngDisabled'
    };
  }

  link($scope: ng.IScope): void {
    ($scope as any).controller = ($scope.$parent.$parent as any).cheToggleController || ($scope.$parent.$parent.$parent as any).cheToggleController;
  }
}
