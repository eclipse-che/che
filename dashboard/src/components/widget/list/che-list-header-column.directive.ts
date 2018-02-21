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
