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
 * @ngdoc directive
 * @name components.directive:toggleSingleButton
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<toggle-single-button>` defines button with two states.
 *
 * @param {string} che-title button's title
 * @param {expression=} che-multiline-title allows multi line title if attr exists
 * @param {string=} che-font-icon button's icon CSS class
 * @param {expression=} che-state expression which defines state of button.
 * @param {Function} che-on-change callback on model change
 * @usage
 *   <toggle-single-button che-title="Filter"
 *                         che-state="ctrl.filterInitState"
 *                         che-on-change="ctrl.filterStateOnChange(state)"></toggle-single-button>
 *
 * @author Oleksii Kurinnyi
 */

interface IToggleSingleButtonAttrs extends ng.IAttributes {
  cheMultilineTitle?: boolean;
}

interface IToggleSingleButtonScope extends ng.IScope {
  state: boolean;
  changeState: () => void;
  onChange: (data: {state: boolean}) => void;
  multilineTitle?: string;
}

/**
 * Defines a directive for the button which can toggle its state.
 * @author Oleksii Kurinnyi
 */
export class ToggleSingleButton {
  restrict: string = 'E';
  transclude: boolean = true;
  templateUrl: string = 'components/widget/toggle-button/toggle-single-button.html';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {
      state: '=?cheState',
      title: '@cheTitle',
      multilineTitle: '=?cheMultilineTitle',
      fontIcon: '@?cheFontIcon',
      onChange: '&?cheOnChange'
    };

  }

  link($scope: IToggleSingleButtonScope, $element: ng.IAugmentedJQuery, $attrs: IToggleSingleButtonAttrs): void {

    if (angular.isDefined($attrs.cheMultilineTitle)) {
      $scope.multilineTitle = 'true';
    }

    const watcher = $scope.$watch(() => { return $scope.state; }, (newValue: boolean, oldValue: boolean) => {
      if (newValue === oldValue) {
        return;
      }
      $scope.onChange({state: $scope.state});
    });

    $scope.$on('$destroy', () => {
      watcher();
    });
  }

}
