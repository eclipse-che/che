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

import {ConfirmDialogService} from '../../../../components/service/confirm-dialog/confirm-dialog.service';
import {ChePreferences} from '../../../../components/api/che-preferences.factory';
import {CheNotification} from '../../../../components/notification/che-notification.factory';

/**
 * @ngdoc controller
 * @name administration.docker-registry.docker-registry-list.controller:DockerRegistryListController
 * @description This class is handling the controller for the docker registry's list
 * @author Oleksii Orel
 */
export class DockerRegistryListController {
  static $inject = ['$q', '$log', '$mdDialog', '$document', 'chePreferences', 'cheNotification', 'confirmDialogService', '$scope', 'cheListHelperFactory'];

  private $q: ng.IQService;
  private $log: ng.ILogService;
  private $scope: ng.IScope;
  private $mdDialog: ng.material.IDialogService;
  private $document: ng.IDocumentService;
  private chePreferences: ChePreferences;
  private cheNotification: CheNotification;
  private confirmDialogService: ConfirmDialogService;
  private cheListHelper: che.widget.ICheListHelper;

  private registries: Array<che.IRegistry>;
  private isLoading: boolean;

  private registryOrderBy: string;
  private registryFilter: {username: string};

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $document: ng.IDocumentService,
    chePreferences: ChePreferences, cheNotification: CheNotification, confirmDialogService: ConfirmDialogService, $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.$log = $log;
    this.$scope = $scope;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    this.isLoading = true;
    this.registries = [];

    this.registryOrderBy = 'registry.username';
    this.registryFilter = {username: ''};

    const helperId = 'docker-registry-list';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.getRegistries();
  }

  /**
   * Gets the list of registries.
   *
   * @return {IPromise<any>}
   */
  getRegistries(): ng.IPromise<any> {
    const promise = this.chePreferences.fetchPreferences();
    return promise.then(() => {
      this.isLoading = false;
      this.registries = this.chePreferences.getRegistries();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Load registry failed.');
    }).finally(() => {
      this.cheListHelper.setList(this.registries, 'url');
    });
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter user names.
   */
  onSearchChanged(str: string): void {
    this.registryFilter.username = str;
    this.cheListHelper.applyFilter('username', this.registryFilter);
  }

  /**
   * Delete all selected registries
   */
  deleteSelectedRegistries(): void {
    const selectedRegistries = this.cheListHelper.getSelectedItems(),
          selectedRegistriesUrls = selectedRegistries.map((registry: any) => {
            return registry.url;
          });

    const queueLength = selectedRegistriesUrls.length;
    if (!queueLength) {
      this.cheNotification.showError('No such registry.');
      return;
    }

    let confirmationPromise = this.showDeleteRegistriesConfirmation(queueLength);

    confirmationPromise.then(() => {
      const numberToDelete = queueLength;
      const deleteRegistryPromises = [];
      let isError = false;
      let currentRegistryAddress;

      selectedRegistriesUrls.forEach((registryAddress: string) => {
        currentRegistryAddress = registryAddress;
        this.cheListHelper.itemsSelectionStatus[registryAddress] = false;

        const promise = this.chePreferences.removeRegistry(registryAddress);
        promise.catch((error: any) => {
          isError = true;
          this.$log.error('Cannot delete registry: ', error);
        });
        deleteRegistryPromises.push(promise);
      });

      this.$q.all(deleteRegistryPromises).finally(() => {
        this.getRegistries().catch((error: any) => {
          this.$log.error(error);
        });
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(currentRegistryAddress + ' has been removed.');
          } else {
            this.cheNotification.showInfo('Selected registries have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before delete registries
   * @param numberToDelete
   * @returns {ng.IPromise<any>}
   */
  showDeleteRegistriesConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' registries?';
    } else {
      content += 'this selected registry?';
    }
    return this.confirmDialogService.showConfirmDialog('Remove registries', content, 'Delete');
  }

  /**
   * Edit docker registry. Show the dialog
   * @param  event {MouseEvent} the $event
   * @param  registry {che.IRegistry} the selected registry
   */
  showEditRegistryDialog(event: MouseEvent, registry: che.IRegistry): void {
    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'EditRegistryController',
      controllerAs: 'editRegistryController',
      locals: {
        callbackController: this,
        registry: registry
      },
      templateUrl: 'app/administration/docker-registry/docker-registry-list/edit-registry/edit-registry.html'
    }).finally(() => {
      this.getRegistries().then(() => {
        this.cheListHelper.updateBulkSelectionStatus();
      });
    });
  }
}
