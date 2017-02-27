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
 * Controller for the factories.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ListFactoriesCtrl {

  private confirmDialogService: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($q, $mdDialog, cheAPI, cheNotification, $rootScope, confirmDialogService: any) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    this.maxItems = 15;
    this.skipCount = 0;

    this.factoriesOrderBy = '';
    this.factoriesFilter = {name: ''};
    this.factoriesSelectedStatus = {};
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;

    this.factoriesSelectedStatus = {};
    this.isLoading = true;
    this.factories = cheAPI.getFactory().getPageFactories();

    this.isLoading = true;
    let promise = cheAPI.getFactory().fetchFactories(this.maxItems, this.skipCount);
    promise.then(() => {
      this.isLoading = false;
    }, (error) => {
      this.isLoading = false;
      if (error.status !== 304) {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve the list of factories.');
      }
    });

    this.pagesInfo = cheAPI.getFactory().getPagesInfo();

    $rootScope.showIDE = false;
  }

  /**
   * Check all factories in list
   */
  selectAllFactories() {
    this.factories.forEach((factory) => {
      this.factoriesSelectedStatus[factory.id] = true;
    });
  }

  /**
   * Uncheck all factories in list
   */
  deselectAllFactories() {
    this.factories.forEach((factory) => {
      this.factoriesSelectedStatus[factory.id] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection() {
    if (this.isBulkChecked) {
      this.deselectAllFactories();
      this.isBulkChecked = false;
    } else {
      this.selectAllFactories();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update factories selected status
   */
  updateSelectedStatus() {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.factories.forEach((factory) => {
      if (this.factoriesSelectedStatus[factory.id]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Delete all selected factories
   */
  deleteSelectedFactories() {
    let factoriesSelectedStatusKeys = Object.keys(this.factoriesSelectedStatus);
    let checkedFactoriesKeys = [];

    if (!factoriesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such factories.');
      return;
    }

    factoriesSelectedStatusKeys.forEach((key) => {
      if (this.factoriesSelectedStatus[key] === true) {
        checkedFactoriesKeys.push(key);
      }
    });

    let numberToDelete = checkedFactoriesKeys.length;
    if (!numberToDelete) {
      this.cheNotification.showError('No such factory.');
      return;
    }

    let confirmationPromise = this.showDeleteFactoriesConfirmation(numberToDelete);

    confirmationPromise.then(() => {
      let isError = false;
      let deleteFactoryPromises = [];

      checkedFactoriesKeys.forEach((factoryId) => {
        this.factoriesSelectedStatus[factoryId] = false;

        let promise = this.cheAPI.getFactory().deleteFactoryById(factoryId);

        promise.then(() => {
        }, (error) => {
          isError = true;
          this.$log.error('Cannot delete factory: ', error);
        });
        deleteFactoryPromises.push(promise);
      });

      this.$q.all(deleteFactoryPromises).finally(() => {
        this.isLoading = true;

        let promise = this.cheAPI.getFactory().fetchFactories(this.maxItems, this.skipCount);

        promise.then(() => {
          this.isLoading = false;
        }, (error) => {
          this.isLoading = false;
          if (error.status !== 304) {
            this.cheNotification.showError(error.data.message ? error.data.message : 'Update information failed.');
          }
        });

        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          this.cheNotification.showInfo('Selected ' + (numberToDelete === 1 ? 'factory' : 'factories') + ' has been removed.');
        }
      });
    });
  }

  /**
   * Ask for loading the users page in asynchronous way
   * @param pageKey - the key of page
   */
  fetchFactoriesPage(pageKey) {
    this.isLoading = true;
    let promise = this.cheAPI.getFactory().fetchFactoryPage(pageKey);

    promise.then(() => {
      this.isLoading = false;
    }, (error) => {
      this.isLoading = false;
      if (error.status !== 304) {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update information failed.');
      }
    });
  }

  /**
   * Returns true if the next page is exist.
   * @returns {boolean}
   */
  hasNextPage() {
    if (this.pagesInfo.countOfPages) {
      return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
    }
    return this.factories.length === this.maxItems;
  }

  /**
   * Returns true if the last page is exist.
   * @returns {boolean}
   */
  hasLastPage() {
    if (this.pagesInfo.countOfPages) {
      return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
    }
    return false;
  }

  /**
   * Returns true if the previous page is exist.
   * @returns {boolean}
   */
  hasPreviousPage() {
    return this.pagesInfo.currentPageNumber > 1;
  }

  /**
   * Returns true if we have more then one page.
   * @returns {boolean}
   */
  isPagination() {
    if (this.pagesInfo.countOfPages) {
      return this.pagesInfo.countOfPages > 1;
    }
    return this.factories.length === this.maxItems || this.pagesInfo.currentPageNumber > 1;
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete {number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteFactoriesConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' factories?';
    } else {
      content += 'this selected factory?';
    }
    return this.confirmDialogService.showConfirmDialog('Remove factories', content, 'Delete');
  }
}
