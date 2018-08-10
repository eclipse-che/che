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

interface IPopoverScope extends ng.IScope {
  onChange: Function;
  isOpenPopover: boolean;
  buttonState?: boolean;
  buttonOnChange?: Function;
  buttonOnReset?: Function;
  chePopoverTriggerOutsideClick?: boolean;
}

/**
 * @ngdoc directive
 * @name components.directive:CheToggleSinglePopover
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<toggle-button-popover>` defines two state button's popover.
 *
 * @param {string} button-title button's title
 * @param {string} button-font-icon button's icon CSS class
 * @param {expression=} button-state expression which defines state of button.
 * @param {Function} button-on-change callback on model change
 * @param {string=} che-popover-title popover's title
 * @param {string=} che-popover-placement popover's placement
 * @param {expression=} che-popover-close-outside-click if <code>true</close> then click outside of popover will close the it
 * @usage
 *   <toggle-button-popover button-title="Filter"
 *                          button-state="ctrl.filterState"
 *                          button-on-change="ctrl.filterStateOnChange(state)"><div>My popover</div></toggle-button-popover>
 *
 * @author Oleksii Orel
 */
export class CheToggleButtonPopover implements ng.IDirective {

  static $inject = ['$timeout'];

  restrict = 'E';
  transclude = true;
  scope = {
    buttonTitle: '@',
    buttonFontIcon: '@',
    buttonOnChange: '&?buttonOnChange',
    buttonState: '=?buttonState',
    chePopoverTitle: '@?',
    chePopoverPlacement: '@?',
    chePopoverTriggerOutsideClick: '=?'
  };

  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  /**
   * Template for the toggle-button-popover
   * @returns {string} the template
   */
  template(): string {
    return `<toggle-single-button che-title="{{buttonTitle}}"
                                  che-font-icon="{{buttonFontIcon}}"
                                  che-on-change="onChange(state)"
                                  che-state="buttonState"
                                  popover-title="{{chePopoverTitle ? chePopoverTitle : ''}}"
                                  popover-placement="{{chePopoverPlacement ? chePopoverPlacement : 'bottom'}}"
                                  popover-is-open="isOpenPopover"
                                  popover-trigger="{{chePopoverTriggerOutsideClick ? 'outsideClick' : 'none'}}"
                                  uib-popover-html="'<div class=\\'che-transclude\\'></div>'"></toggle-single-button>`;
  }

  link($scope: IPopoverScope, $element: ng.IAugmentedJQuery, $attrs: ng.IAttributes, ctrl: any, $transclude: ng.ITranscludeFunction): void {

    $scope.onChange = (state: boolean) => {
      this.$timeout(() => {
        $scope.isOpenPopover = state;
      });
      this.$timeout(() => {
        if (angular.isFunction($scope.buttonOnChange)) {
          $scope.buttonOnChange({state: state});
        }
        if (state) {
          $transclude((clonedElement: ng.IAugmentedJQuery) => {
            $element.find('.che-transclude').replaceWith(clonedElement);
          });
        }
      });
    };

    if (!$scope.buttonState) {
      $scope.buttonState = false;
    }
    $scope.onChange($scope.buttonState);

    // close popover on Esc is pressed
    $element.attr('tabindex', 0);
    $element.on('keypress keydown', (event: any) => {
      if (event.which === 27) {
        // on press 'esc'
        $scope.$apply(() => {
          $scope.buttonState = false;
        });
      }
    });

    if ($scope.chePopoverTriggerOutsideClick) {
      // update toggle single button state after popover is closed by outside click
      const watcher = $scope.$watch(() => { return $scope.isOpenPopover; }, (newVal: boolean) => {
        $scope.buttonState = newVal;
      });
      $scope.$on('$destroy', () => {
        watcher();
      });
    }

  }

}
