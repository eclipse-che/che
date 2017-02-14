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

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout) {
    this.$timeout = $timeout;

    this.restrict = 'A';
  }

  /**
   * Keep reference to the model controller
   */
  link($scope, element, attrs) {
    $scope.longTouch = false;
    element.on('touchstart mousedown', (event) => {
      $scope.longTouch = true;

      this.$timeout(() => {
        if ($scope.longTouch && event.which !== 3) {
          element.mouseup();

          $scope.$apply(() => {
            $scope.$eval(attrs.cheOnLongTouch);
            $scope.longTouch = false;
          });
        }
      }, 500);
    });
    element.on('touchend mouseup', () => {
      $scope.longTouch = false;
    });
  }

}
