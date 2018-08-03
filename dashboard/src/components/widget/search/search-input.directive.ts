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
 * @name components.directive:searchInput
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<search-input>` defines search component for filtering.
 *
 * @param {string} search-placeholder the placeholder for search input
 * @param {expression} ng-model The model!
 * @param {Function} search-on-change callback on model change
 * @usage
 *   <che-search search-placeholder="Search for.."
 *               ng-model="ctrl.filter"
 *               search-on-change="ctrl.searchOnChange(search)"></che-search>
 *
 * @author Oleksii Kurinnyi
 */
export class SearchInput {
  restrict: string = 'E';
  replace: boolean = true;
  transclude: boolean = true;
  templateUrl: string = 'components/widget/search/search-input.html';
  required: string = 'ngModel';
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      placeholder: '@searchPlaceholder',
      ngModel: '=',
      onChange: '&searchOnChange'
    };
  }

}

