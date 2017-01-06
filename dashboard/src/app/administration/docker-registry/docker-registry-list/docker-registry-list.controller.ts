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
 * @ngdoc controller
 * @name administration.docker-registry.docker-registry-list.controller:DockerRegistryListController
 * @description This class is handling the controller for the docker registry's list
 * @author Oleksii Orel
 */
export class DockerRegistryListController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q, $mdDialog, $document, chePreferences, cheNotification) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;

    this.registries = chePreferences.getRegistries();
    this.isLoading = true;

    this.registryOrderBy = 'registry.username';
    this.registryFilter = {username: ''};
    this.registriesSelectedStatus = {};
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;

    let promise = chePreferences.fetchPreferences();
    promise.then(() => {
      this.isLoading = false;
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Load registry failed.');
    });
  }

  /**
   * Check all registries in list
   */
  selectAllRegistries() {
    this.registries.forEach((registry) => {
      this.registriesSelectedStatus[registry.url] = true;
    });
  }

  /**
   * Uncheck all registries in list
   */
  deselectAllRegistries() {
    this.registries.forEach((registry) => {
      this.registriesSelectedStatus[registry.url] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection() {
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
  updateSelectedStatus() {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.registries.forEach((registry) => {
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
  deleteSelectedRegistries() {
    let registriesSelectedStatusKeys = Object.keys(this.registriesSelectedStatus);
    let checkedRegistriesKeys = [];

    if (!registriesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such registries.');
      return;
    }

    registriesSelectedStatusKeys.forEach((key) => {
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

      checkedRegistriesKeys.forEach((registryAddress) => {
        currentRegistryAddress = registryAddress;
        this.registriesSelectedStatus[registryAddress] = false;

        let promise = this.chePreferences.removeRegistry(registryAddress);
        promise.then(() => {
          queueLength--;
        }, (error) => {
          isError = true;
          this.$log.error('Cannot delete registry: ', error);
        });
        deleteRegistryPromises.push(promise);
      });

      this.$q.all(deleteRegistryPromises).finally(() => {
        this.chePreferences.fetchPreferences().then(() => {
          this.updateSelectedStatus();
        }, (error) => {
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
   * @returns {*}
   */
  showDeleteRegistriesConfirmation(numberToDelete) {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' registries?';
    } else {
      confirmTitle += 'this selected registry?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove registries')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

  /**
   * Add docker registry. Show the dialog
   * @param  event - the $event
   */
  showAddRegistryDialog(event) {
    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AddRegistryController',
      controllerAs: 'addRegistryController',
      parent: angular.element(this.$document.body),
      templateUrl: 'app/administration/docker-registry/docker-registry-list/add-registry/add-registry.html'
    });
  }

  /**
   * Edit docker registry. Show the dialog
   * @param  event - the $event
   * @param  registry - the selected registry
   */
  showEditRegistryDialog(event, registry) {
    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'EditRegistryController',
      controllerAs: 'editRegistryController',
      parent: angular.element(this.$document.body),
      locals: {registry: registry},
      templateUrl: 'app/administration/docker-registry/docker-registry-list/edit-registry/edit-registry.html'
    });
  }
}
