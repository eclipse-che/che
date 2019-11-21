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

interface IShowAreaScope extends ng.IScope {
  toggleVisibility: () => void;
  getButtonTitle: () => string;
  isHide: boolean;
  showTitle?: string;
  hideTitle?: string;
}

/**
 * @ngdoc directive
 * @name components.directive:cheShowArea
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-show-area>` defines area to show/hide content with button
 *
 * @author Oleksii Orel
 */
export class CheShowArea implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'components/widget/show-area/che-show-area.html';

  transclude: boolean = true;

  scope: {
    showTitle: string;
    hideTitle: string;
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      showTitle: '@?',
      hideTitle: '@?'
    };
  }

  link($scope: IShowAreaScope) {
    $scope.isHide = true;
    $scope.toggleVisibility = () => {
      $scope.isHide = !$scope.isHide;
    };

    const showTitle = $scope.showTitle ? $scope.showTitle : 'Show';
    const hideTitle = $scope.hideTitle ? $scope.hideTitle : 'Cancel';
    $scope.getButtonTitle = () => {
      return $scope.isHide ? showTitle : hideTitle;
    }
  }
}
