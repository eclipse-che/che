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
 * @name components.directive:toggleSingleButton
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<toggle-single-button>` defines button with two states.
 *
 * @param {string} che-title button's title
 * @param {string=} che-font-icon button's icon CSS class
 * @param {expression=} che-state expression which defines initial state of button.
 * @param {Function} che-on-change callback on model change
 * @usage
 *   <toggle-single-button che-title="Filter"
 *                         che-state="ctrl.filterInitState"
 *                         che-on-change="ctrl.filterStateOnChange(state)"></toggle-single-button>
 *
 * @author Oleksii Kurinnyi
 */

interface IToggleSingleButtonScope extends ng.IScope {
  init?: boolean;
  state?: boolean;
  onChange: (data: {state: boolean}) => void;
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
   * @ngInject for Dependency injection
   */
  constructor() {
    // scope values
    this.scope = {
      init: '=?cheState',
      state: '=?cheValue',
      title: '@cheTitle',
      fontIcon: '@?cheFontIcon',
      onChange: '&cheOnChange'
    };

  }

  link($scope: IToggleSingleButtonScope): void {

    if ($scope.init) {
      $scope.state = $scope.init;
      $scope.onChange({state: true});
    }
    const watcher = $scope.$watch(() => { return $scope.state; }, (newValue: boolean, oldValue: boolean) => {
      if (newValue === oldValue) {
        return;
      }
      $scope.onChange({state: newValue});
    });
    $scope.$on('$destroy', () => {
      watcher();
    });
  }

}
