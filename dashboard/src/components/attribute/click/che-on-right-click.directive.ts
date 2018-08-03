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

interface ICheOnRightClickAttributes extends ng.IAttributes {
  cheOnRightClick: () => void;
}

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
export class CheOnRightClick implements ng.IDirective {
  restrict = 'A';

  /**
   * Keep reference to the model controller
   */
  link($scope: ng.IScope, $element: ng.IAugmentedJQuery, $attrs: ICheOnRightClickAttributes): void {
    $element.on('contextmenu', (event: JQueryEventObject) => {
      event.stopPropagation();
      event.preventDefault();

      $scope.$apply(() => {
        $scope.$eval($attrs.cheOnRightClick);
      });
    });
  }

}
