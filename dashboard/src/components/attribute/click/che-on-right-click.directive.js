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
 * @ngdoc directive
 * @name components.directive:cheOnRightClick
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-on-right-click` defines an attribute for adding a callback on right mouse click
 *
 * @usage
 *   <div che-on-right-click="callback"></div>
 *
 * @author Oleksii Kurinnyi
 */
export class CheOnRightClick {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'A';
  }

  /**
   * Keep reference to the model controller
   */
  link($scope, $element, attrs) {
    $element.bind('contextmenu', (event) => {
      event.stopPropagation();
      event.preventDefault();

      $scope.$apply(() => {
        $scope.$eval(attrs.cheOnRightClick);
        $scope.longTouch = false;
      });
    });
  }

}
