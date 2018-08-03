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

interface ICheLongTouchScope extends ng.IScope {
  longTouch: boolean;
}

interface ICheOnLongTouchAttributes extends ng.IAttributes {
  cheOnLongTouch: any;
}

/**
 * @ngdoc directive
 * @name components.directive:cheOnLongTouch
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-on-long-touch` defines an attribute for adding a callback on long touch or long mouse click
 *
 * @usage
 *   <div che-on-long-touch="callback"></div>
 *
 * @author Oleksii Kurinnyi
 */
export class CheOnLongTouch {

  static $inject = ['$timeout'];

  restrict = 'A';

  $timeout: ng.ITimeoutService;
  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  /**
   * Keep reference to the model controller
   */
  link($scope: ICheLongTouchScope, $element: ng.IAugmentedJQuery, $attrs: ICheOnLongTouchAttributes) {
    $scope.longTouch = false;
    $element.on('touchstart mousedown', (event: JQueryEventObject) => {
      $scope.longTouch = true;

      this.$timeout(() => {
        if ($scope.longTouch && event.which !== 3) {
          $element.mouseup();

          $scope.$apply(() => {
            $scope.$eval($attrs.cheOnLongTouch);
            $scope.longTouch = false;
          });
        }
      }, 500);
    });
    $element.on('touchend mouseup', () => {
      $scope.longTouch = false;
    });
  }

}
