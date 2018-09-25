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

interface ICheListHeaderColumnScope extends ng.IScope {
  sortItem: string;
  sortValue: string;
  updateSortValue: () => void;
  onSortChange: (data: {sortValue: string}) => void;
}

/**
 * Defines a directive for creating header column.
 * @author Oleksii Orel
 */
export class CheListHeaderColumn implements ng.IDirective {

  static $inject = ['$timeout'];

  restrict = 'E';
  replace = true;
  templateUrl = 'components/widget/list/che-list-header-column.html';

  // scope values
  scope = {
    sortValue: '=?cheSortValue',
    onSortChange: '&?',
    sortItem: '@?cheSortItem',
    columnTitle: '@cheColumnTitle'
  };

  private $timeout: ng.ITimeoutService;

  /**
   * Default constructor that is using resource
   */
  constructor($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;
  }

  link($scope: ICheListHeaderColumnScope): void {
    $scope.updateSortValue = () => {
      if (!$scope.sortItem || $scope.sortItem.length === 0) {
        return;
      }
      const sortValue = angular.equals($scope.sortItem, $scope.sortValue) ? '-' + $scope.sortItem : $scope.sortItem;
      $scope.sortValue = sortValue;
      if (angular.isFunction($scope.onSortChange)) {
        this.$timeout(() => {
          $scope.onSortChange({sortValue: sortValue});
        });
      }
    };
  }
}
