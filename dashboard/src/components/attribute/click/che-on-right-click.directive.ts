/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
