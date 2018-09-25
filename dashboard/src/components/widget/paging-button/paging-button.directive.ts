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
import {RemotePageLabels} from '../../api/paging-resource/remote-page-labels';


interface IPagingButtonsScope extends ng.IScope {
  pagesInfo: che.IPageInfo;
  fetchPage(data: { key: string }): void;
  firstPage(): void;
  previousPage(): void;
  nextPage(): void;
  lastPage(): void;
  hasNextPage(): boolean;
  hasPreviousPage(): boolean;
  isPagination(): boolean;
  getCurrentPageNumber(): number;
}

/**
 * @ngdoc directive
 * @name widgets.directive:pagingButtons
 * @restrict E
 * @element
 *
 * @description
 * <che-paging-buttons pages-info="ctrl.pagesInfo" fetch-page="ctrl.fetchPage(key)"></che-paging-buttons>` for adding paging buttons.
 *
 * @usage
 *   <che-paging-buttons pages-info="ctrl.pagesInfo" fetch-page="ctrl.fetchPage"></che-paging-buttons>
 *
 * @author Oleksii Orel
 */
export class PagingButtons implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'components/widget/paging-button/paging-button.html';
  replace = false;
  scope = {
    pagesInfo: '=',
    fetchPage: '&'
  };


  link($scope: IPagingButtonsScope) {
    $scope.firstPage = () => {
      $scope.fetchPage({key: RemotePageLabels.FIRST});
    };
    $scope.previousPage = () => {
      $scope.fetchPage({key: RemotePageLabels.PREVIOUS});
    };
    $scope.nextPage = () => {
      $scope.fetchPage({key: RemotePageLabels.NEXT});
    };
    $scope.lastPage = () => {
      $scope.fetchPage({key: RemotePageLabels.LAST});
    };
    $scope.hasNextPage = () => {
      return $scope.pagesInfo && $scope.pagesInfo.countPages - $scope.pagesInfo.currentPageNumber > 0;
    };
    $scope.hasPreviousPage = () => {
      return $scope.pagesInfo && $scope.pagesInfo.currentPageNumber > 1;
    };
    $scope.isPagination = () => {
      return $scope.pagesInfo && $scope.pagesInfo.countPages > 1;
    };
    $scope.getCurrentPageNumber = () => {
      return $scope.pagesInfo && $scope.pagesInfo.currentPageNumber;
    };
  }
}
