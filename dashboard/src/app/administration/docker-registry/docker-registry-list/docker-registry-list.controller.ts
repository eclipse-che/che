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
  private $q: ng.IQService;
  private $log: ng.ILogService;
  private $mdDialog: ng.material.IDialogService;
  private $document: ng.IDocumentService;
  private chePreferences: ChePreferences;
  private cheNotification: CheNotification;
  private confirmDialogService: ConfirmDialogService;

  private registries: Array<che.IRegistry>;
  private isLoading: boolean;

  private registryOrderBy: string;
  private registryFilter: {username: ''};
  private registriesSelectedStatus: any;
  private isNoSelected: boolean;
  private isAllSelected: boolean;
  private isBulkChecked: boolean;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $document: ng.IDocumentService, chePreferences: ChePreferences, cheNotification: CheNotification, confirmDialogService: ConfirmDialogService) {
    this.$q = $q;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    this.registries = chePreferences.getRegistries();
    this.isLoading = true;

    this.registryOrderBy = 'registry.username';
    this.registryFilter = {username: ''};
    this.registriesSelectedStatus = {};
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;

    const promise = chePreferences.fetchPreferences();
    promise.then(() => {
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Load registry failed.');
    });
  }

  /**
   * Check all registries in list
   */
  selectAllRegistries(): void {
    this.registries.forEach((registry: che.IRegistry) => {
      this.registriesSelectedStatus[registry.url] = true;
    });
  }

  /**
   * Uncheck all registries in list
   */
  deselectAllRegistries(): void {
    this.registries.forEach((registry: che.IRegistry) => {
      this.registriesSelectedStatus[registry.url] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllRegistries();
      this.isBulkChecked = false;
    } else {
      this.selectAllRegistries();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update registries selected status
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.registries.forEach((registry: che.IRegistry) => {
      if (this.registriesSelectedStatus[registry.url]) {
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
   * Delete all selected registries
   */
  deleteSelectedRegistries(): void {
    let registriesSelectedStatusKeys = Object.keys(this.registriesSelectedStatus);
    let checkedRegistriesKeys = [];

    if (!registriesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such registries.');
      return;
    }

    registriesSelectedStatusKeys.forEach((key: string) => {
      if (this.registriesSelectedStatus[key] === true) {
        checkedRegistriesKeys.push(key);
      }
    });

    let queueLength = checkedRegistriesKeys.length;
    if (!queueLength) {
      this.cheNotification.showError('No such registry.');
      return;
    }

    let confirmationPromise = this.showDeleteRegistriesConfirmation(queueLength);

    confirmationPromise.then(() => {
      let numberToDelete = queueLength;
      let isError = false;
      let deleteRegistryPromises = [];
      let currentRegistryAddress;

      checkedRegistriesKeys.forEach((registryAddress: string) => {
        currentRegistryAddress = registryAddress;
        this.registriesSelectedStatus[registryAddress] = false;

        let promise = this.chePreferences.removeRegistry(registryAddress);
        promise.then(() => {
          queueLength--;
        }, (error: any) => {
          isError = true;
          this.$log.error('Cannot delete registry: ', error);
        });
        deleteRegistryPromises.push(promise);
      });

      this.$q.all(deleteRegistryPromises).finally(() => {
        this.chePreferences.fetchPreferences().then(() => {
          this.updateSelectedStatus();
        }, (error: any) => {
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
      locals: {registry: registry},
      templateUrl: 'app/administration/docker-registry/docker-registry-list/edit-registry/edit-registry.html'
    });
  }
}
