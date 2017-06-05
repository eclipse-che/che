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

interface IPopoverAttrs extends ng.IAttributes {
  buttonTitle: string;
  buttonFontIcon: string;
  buttonOnChange: string;
  buttonState?: string;
  buttonValue?: boolean;
  chePopoverTitle?: string;
  chePopoverPlacement?: string;
}

interface IPopoverScope extends ng.IScope {
  onChange: Function;
  isOpenPopover: boolean;
  buttonInitState?: boolean;
  buttonOnChange?: Function;
  buttonOnReset?: Function;
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
 * @param {expression=} button-state expression which defines initial state of button.
 * @param {Function} button-on-change callback on model change
 * @param {boolean} button-value button's state
 * @param {string=} che-popover-title popover's title
 * @param {string=} che-popover-placement popover's placement
 * @usage
 *   <toggle-button-popover button-title="Filter"
 *                          button-state="ctrl.filterInitState"
 *                          button-on-change="ctrl.filterStateOnChange(state)"><div>My popover</div></toggle-button-popover>
 *
 * @author Oleksii Orel
 */
export class CheToggleButtonPopover implements ng.IDirective {
  restrict = 'E';
  transclude = true;
  scope = {
    buttonTitle: '@',
    buttonFontIcon: '@',
    buttonOnChange: '&?buttonOnChange',
    buttonInitState: '=?buttonState',
    buttonValue: '=?',
    chePopoverTitle: '@?',
    chePopoverPlacement: '@?'
  };

  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
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
                                  che-state="buttonInitState ? buttonInitState : false"
                                  che-value="buttonValue"
                                  popover-title="{{chePopoverTitle ? chePopoverTitle : ''}}"
                                  popover-placement="{{chePopoverPlacement ? chePopoverPlacement : 'bottom'}}"
                                  popover-is-open="isOpenPopover"
                                  uib-popover-html="'<div class=\\'che-transclude\\'></div>'"></toggle-single-button>`;
  }

  link($scope: IPopoverScope, $element: ng.IAugmentedJQuery, $attrs: IPopoverAttrs, ctrl: any, $transclude: ng.ITranscludeFunction): void {

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

  }
}
