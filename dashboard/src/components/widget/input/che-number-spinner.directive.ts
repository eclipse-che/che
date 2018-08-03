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

interface INumberSpinnerScope extends ng.IScope {
  form: ng.IFormController;
  name: string;
  value: number;
  tofixed: number;
  step: number;
  minvalue: number;
  maxvalue: number;
  decrement: Function;
  increment: Function;
  isChanged: Function;
}

/**
 * @ngdoc directive
 * @name components.directive.cheNumberSpinner
 * @restrict E
 * @function
 * @element
 *
 * @description
 * <che-number-spinner></che-number-spinner>
 *
 * @param {ng.IFormController} che-form
 * @param {string} che-name input name
 * @param {number} ng-model the model
 * @param {number=} che-tofixed number of digits after decimal point
 * @param {string=} unit measure unit
 * @param {number=} che-step step of increment/decrement
 * @param {number=} che-minvalue minimal value
 * @param {number=} che-maxvalue maximal value
 * @param {Function=} isChanged callback
 *
 * @usage
 * <che-number-spinner che-form="myForm"
 *                     che-name="myName"
 *                     ng-model="value"
 *                     che-step="1"
 *                     che-tofixed="2"
 *                     che-minvalue="0"
 *                     che-maxvalue="10"></che-number-spinner>
 *
 * @example
 * <example module="userDashboard">
 *   <file name="index.html">
 *     <ng-form name="myForm">
 *       <che-number-spinner che-form="myForm"
 *                           che-name="myName"
 *                           ng-model="value"
 *                           che-step="1"
 *                           che-tofixed="2"
 *                           che-minvalue="0"
 *                           che-maxvalue="10"></che-number-spinner>
 *     </ng-form>
 *   </file>
 * </example>
 *
 * @author Oleksii Kurinnyi
 */
export class CheNumberSpinner {
  static $inject = ['$timeout', '$interval'];

  $interval: ng.IIntervalService;
  $timeout: ng.ITimeoutService;

  restrict: string = 'AE';
  transclude: boolean = true;
  require: string = 'ngModel';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor ($timeout: ng.ITimeoutService, $interval: ng.IIntervalService) {
    this.$interval = $interval;
    this.$timeout = $timeout;

    this.scope = {
      form: '=cheForm',
      name: '@cheName',
      value: '=ngModel',
      tofixed: '=?cheTofixed',
      unit: '@?cheUnit',
      step: '=?cheStep',
      minvalue: '=?cheMinvalue',
      maxvalue: '=?cheMaxvalue',
      isChanged: '&?ngChange'
    };
  }

  template(element: ng.IAugmentedJQuery, attrs: any) {
    let step = attrs.cheStep ? `step="${attrs.cheStep}"` : `step="1"`,
        minvalue = attrs.cheMinvalue ? `min="${attrs.cheMinvalue}" ng-min="${attrs.cheMinvalue}"` : '',
        maxvalue = attrs.cheMaxvalue ? `max="${attrs.cheMaxvalue}" ng-max="${attrs.cheMaxvalue}"` : '';
    return `<div layout="row" layout-align="start center"
                 flex="initial"
                 class="che-number-spinner">
      <div layout="row" layout-align="start center"
           flex="initial"
           class="spinner-container">
        <!-- decrement button -->
        <md-button aria-label="Decrement ${attrs.cheName}"
                   data-action="decrement">
          <md-icon md-font-icon="material-design icon-ic_remove_24px"
                   layout="column"
                   layout-align="center center"></md-icon>
        </md-button>
        <!-- input -->
        <div class="input-container"
             layout="row" layout-align="center center">
          <input name="${attrs.cheName}"
                 type="number"
                 ng-model="value"
                 ${step} ${minvalue} ${maxvalue}>
          <span class="unit" ng-if="unit">{{unit}}</span>
        </div>
        <!-- increment button -->
        <md-button aria-label="Increment ${attrs.cheName}"
                   data-action="increment">
          <md-icon md-font-icon="material-design icon-ic_add_24px"
                   layout="column"
                   layout-align="center center"></md-icon>
        </md-button>
      </div>
      <div ng-messages="form.${attrs.cheName}.$error" role="alert" ng-transclude></div>
    </div>`;
  }

  compile(element: ng.IAugmentedJQuery, attrs: any) {

    let keys = Object.keys(attrs);

    // search the input field
    let inputElement = element.find('input');

    let tabIndex;

    keys.forEach((key: string) => {

      // don't reapply internal properties
      if (key.indexOf('$') === 0) {
        return;
      }
      // don't reapply internal element properties
      if (key.indexOf('che') === 0) {
        return;
      }
      // avoid model
      if ('ngModel' === key) {
        return;
      }
      // don't reapply ngChange
      if ('ngChange' === key) {
        return;
      }
      let value = attrs[key];

      // remember tabindex
      if (key === 'tabindex') {
        tabIndex = value;
      }

      // handle empty values as boolean
      if (value === '') {
        value = 'true';
      }

      // set the value of the attribute
      inputElement.attr(attrs.$attr[key], value);

      element.removeAttr(attrs.$attr[key]);

    });
  }

  link($scope: INumberSpinnerScope, $element: ng.IAugmentedJQuery) {
    $scope.step = isNaN($scope.step) ? 1 : $scope.step;
    $scope.value = isNaN($scope.value) ? 0 : $scope.value;
    $scope.tofixed = $scope.tofixed ? $scope.tofixed : 0;

    let timeoutPromise, intervalPromise,
        doAction = (action: Function): void => {
          action();
          $scope.$digest();

          timeoutPromise = this.$timeout(() => {
            intervalPromise = this.$interval(() => {
              action();
            }, 50);
          }, 500);
        },
        increment = () => {
          if (angular.isUndefined($scope.maxvalue) || $scope.value + $scope.step <= $scope.maxvalue) {
            $scope.value += $scope.step;
            $scope.value = Number($scope.value.toFixed($scope.tofixed));
          }
        },
        decrement = () => {
          if (angular.isUndefined($scope.minvalue) || $scope.value - $scope.step >= $scope.minvalue) {
            $scope.value -= $scope.step;
            $scope.value = Number($scope.value.toFixed($scope.tofixed));
          }
        };

    // handle events on buttons
    $element.find('.md-button').bind('mousedown', (e: Event) => {
      let action = angular.element(e.currentTarget).data('action') === 'increment' ? increment : decrement;
      doAction(action);
    }).bind('mouseup mouseout', () => {
      this.$timeout.cancel(timeoutPromise);
      this.$interval.cancel(intervalPromise);
    });

    // manage classes
    const numberSpinnerElement = $element.find('.che-number-spinner');
    $scope.$watch('form.' + $scope.name + '.$invalid', (isInvalid: boolean) => {
      if (isInvalid) {
        numberSpinnerElement.addClass('number-invalid');
      } else {
        numberSpinnerElement.removeClass('number-invalid');
      }
    });

    if (!$scope.isChanged) {
      return;
    }
    // trigger value change
    $scope.$watch('value', (value: number) => {
      $scope.isChanged({value: value});
    });
  }

}
