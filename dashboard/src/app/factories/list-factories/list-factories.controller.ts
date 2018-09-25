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
import {ConfirmDialogService} from '../../../components/service/confirm-dialog/confirm-dialog.service';
import {CheAPI} from '../../../components/api/che-api.factory';
import {CheNotification} from '../../../components/notification/che-notification.factory';

/**
 * Controller for the factories.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class ListFactoriesController {

  static $inject = ['$q', '$log', 'cheAPI', 'cheNotification', 'confirmDialogService', '$scope', 'cheListHelperFactory'];

  private confirmDialogService: ConfirmDialogService;
  private cheAPI: CheAPI;
  private cheNotification: CheNotification;
  private $q: ng.IQService;
  private $log: ng.ILogService;
  private cheListHelper: che.widget.ICheListHelper;

  private maxItems: number;

  private factoriesOrderBy: string;
  private factoriesFilter: any;

  private isLoading: boolean;
  private factories: any;
  private pagesInfo: any;

  /**
   * Default constructor that is using resource injection
   */
  constructor($q: ng.IQService, $log: ng.ILogService, cheAPI: CheAPI, cheNotification: CheNotification,
              confirmDialogService: ConfirmDialogService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.$log = $log;
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    const helperId = 'list-factories';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.maxItems = 15;

    this.factoriesOrderBy = '';
    this.factoriesFilter = {name: ''};

    this.isLoading = true;
    this.factories = cheAPI.getFactory().getPageFactories();

    let promise = cheAPI.getFactory().fetchFactories(this.maxItems);
    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
    }).finally(() => {
      this.updateListHelper();
    });

    this.pagesInfo = cheAPI.getFactory().getPagesInfo();
  }

  /**
   * Provides actual list of factories to helper.
   */
  updateListHelper(): void {
    this.cheListHelper.setList(this.factories, 'id');
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter factories names.
   */
  onSearchChanged(str: string): void {
    this.factoriesFilter.name = str;
    this.cheListHelper.applyFilter('name', this.factoriesFilter);
  }

  /**
   * Delete all selected factories
   */
  deleteSelectedFactories(): void {
    const selectedFactories = this.cheListHelper.getSelectedItems(),
          selectedFactoriesIds = selectedFactories.map((factory: che.IFactory) => {
            return factory.id;
          });

    const numberToDelete = selectedFactoriesIds.length;
    if (!numberToDelete) {
      this.cheNotification.showError('No such factory.');
      return;
    }

    const confirmationPromise = this.showDeleteFactoriesConfirmation(numberToDelete);

    confirmationPromise.then(() => {
      let isError = false;
      const deleteFactoryPromises = [];

      selectedFactoriesIds.forEach((factoryId: string) => {
        this.cheListHelper.itemsSelectionStatus[factoryId] = false;

        const promise = this.cheAPI.getFactory().deleteFactoryById(factoryId);

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
        }).finally(() => {
          this.updateListHelper();
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
