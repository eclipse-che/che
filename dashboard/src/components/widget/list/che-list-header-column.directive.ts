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
 * Defines a directive for creating header column.
 * @author Oleksii Orel
 */
export class CheListHeaderColumn {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.templateUrl = 'components/widget/list/che-list-header-column.html';

    // scope values
    this.scope = {
      sortValue: '=?cheSortValue',
      sortItem: '@?cheSortItem',
      columnTitle: '@cheColumnTitle'
    };
  }

  link($scope) {
    $scope.updateSortValue = () => {
      if (!$scope.sortItem || $scope.sortItem.length === 0) {
        return;
      }
      if ($scope.sortItem === $scope.sortValue) {
        $scope.sortValue = '-' + $scope.sortValue;
        return;
      }
      $scope.sortValue = $scope.sortItem;
    };
  }
}
