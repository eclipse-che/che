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
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheAPI} from "../../../components/api/che-api.factory";
import {CheNotification} from "../../../components/notification/che-notification.factory";

/**
 * Controller for the factories.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ListFactoriesController {

  private confirmDialogService: ConfirmDialogService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private $q: ng.IQService;
  private $log: ng.ILogService;

  private maxItems: number;

  private factoriesOrderBy: string;
  private factoriesFilter: any;
  private factoriesSelectedStatus: any;
  private isNoSelected: boolean;
  private isAllSelected: boolean;
  private isBulkChecked: boolean;

  private isLoading: boolean;
  private factories: any;
  private pagesInfo: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $log: ng.ILogService, cheAPI: CheAPI, cheNotification: CheNotification, $rootScope: che.IRootScopeService, confirmDialogService: ConfirmDialogService) {
    this.$q = $q;
    this.$log = $log;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    this.factoriesOrderBy = '[name, id]';
    this.factoriesFilter = {name: ''};
    this.factoriesSelectedStatus = {};
    this.maxItems = 15;
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;

    this.isLoading = true;
    this.factories = cheAPI.getFactory().getPageFactories();

    let promise = cheAPI.getFactory().fetchFactories(this.maxItems);
    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
    });

    this.pagesInfo = cheAPI.getFactory().getPagesInfo();

    $rootScope.showIDE = false;
  }

  /**
   * Make all factories in list checked.
   */
  selectAllFactories(): void {
    this.factories.forEach((factory: che.IFactory) => {
      this.factoriesSelectedStatus[factory.id] = true;
    });
  }

  /**
   * Make all factories in list unchecked.
   */
  deselectAllFactories(): any {
    this.factories.forEach((factory: che.IFactory) => {
      this.factoriesSelectedStatus[factory.id] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
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
   * Update factories selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.factories.forEach((factory: che.IFactory) => {
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
  deleteSelectedFactories(): void {
    let factoriesSelectedStatusKeys = Object.keys(this.factoriesSelectedStatus);
    let checkedFactoriesKeys = [];

    if (!factoriesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such factories.');
      return;
    }

    factoriesSelectedStatusKeys.forEach((key: string) => {
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

      checkedFactoriesKeys.forEach((factoryId: string) => {
        this.factoriesSelectedStatus[factoryId] = false;

        let promise = this.cheAPI.getFactory().deleteFactoryById(factoryId);

        promise.catch((error: any) => {
          isError = true;
          this.$log.error('Cannot delete factory: ', error);
        });
        deleteFactoryPromises.push(promise);
      });

      this.$q.all(deleteFactoryPromises).finally(() => {
        this.isLoading = true;

        let promise = this.cheAPI.getFactory().fetchFactories(this.maxItems);

        promise.then(() => {
          this.isLoading = false;
        }, (error: any) => {
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
  fetchFactoriesPage(pageKey: string): void {
    this.isLoading = true;
    let promise = this.cheAPI.getFactory().fetchFactoryPage(pageKey);

    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
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
  hasNextPage(): boolean {
    if (this.pagesInfo.countOfPages) {
      return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
    }
    return this.factories.length === this.maxItems;
  }

  /**
   * Returns true if the last page is exist.
   * @returns {boolean}
   */
  hasLastPage(): boolean {
    if (this.pagesInfo.countOfPages) {
      return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
    }
    return false;
  }

  /**
   * Returns true if the previous page is exist.
   * @returns {boolean}
   */
  hasPreviousPage(): boolean {
    return this.pagesInfo.currentPageNumber > 1;
  }

  /**
   * Returns true if we have more then one page.
   * @returns {boolean}
   */
  isPagination(): boolean {
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
