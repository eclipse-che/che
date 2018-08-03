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

interface ICheSearchScope extends ng.IScope {
  replaceElement: any;
  valueModel: any;
}

/**
 * @ngdoc directive
 * @name components.directive:cheSearch
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-search>` defines search component for filtering.
 *
 * @param {string=} che-placeholder the placeholder for search input
 * @param {element=} che-replace-element the optional element which will be replaced by search input
 * @param {expression} ng-model The model!
 * @param {string=} che-name element name
 * @usage
 *   <che-search che-placeholder="Search for.." ng-model="ctrl.filter"></che-search>
 *
 * @author Ann Shumilova
 */
export class CheSearch implements ng.IDirective {
  restrict = 'E';
  transclude = true;
  templateUrl = 'components/widget/search/che-search.html';

  require = ['ngModel'];

  // scope values
  scope = {
    placeholder: '@chePlaceholder',
    replaceElement: '@?cheReplaceElement',
    valueModel : '=ngModel',
    inputName: '@?cheName'
  };

  link($scope: ICheSearchScope, $element: ng.IAugmentedJQuery): void {
    $scope.$watch('isShown', (isShown: boolean) => {
      if (isShown) {
        if ($scope.replaceElement) {
          let replaceElement = angular.element('#' + $scope.replaceElement);
          replaceElement.addClass('search-replace-element-hidden');
        }
        $element.addClass('search-component-flex');
        $scope.$applyAsync(() => {
          $element.find('input').focus();
        });
      } else {
        $scope.valueModel = '';
        if ($scope.replaceElement) {
          let replaceElement = angular.element('#' + $scope.replaceElement);
          replaceElement.removeClass('search-replace-element-hidden');
        }
        $element.removeClass('search-component-flex');
      }
    });
  }
}
