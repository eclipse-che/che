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
export class CheSearch {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.transclude= true;
    this.templateUrl = 'components/widget/search/che-search.html';

    this.require = ['ngModel'];

    // scope values
    this.scope = {
      placeholder:'@chePlaceholder',
      replaceElement: '@cheReplaceElement',
      valueModel : '=ngModel',
      inputName:'@cheName'
    };
  }

  link($scope, element) {
    $scope.$watch('isShown', (isShown) => {
      if (isShown) {
        if ($scope.replaceElement) {
          let replaceElement = angular.element('#' + $scope.replaceElement);
          replaceElement.addClass('search-replace-element-hidden');
        }
        element.addClass('search-component-flex');
        element.find('input').focus();
      } else {
        $scope.valueModel = '';
        if ($scope.replaceElement) {
          let replaceElement = angular.element('#' + $scope.replaceElement);
          replaceElement.removeClass('search-replace-element-hidden');
        }
        element.removeClass('search-component-flex');
      }
    });
  }
}
