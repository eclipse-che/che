/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * This class is handling the controller for the edit registry
 * @author Oleksii Orel
 */
export class EditRegistryController {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, chePreferences, cheNotification) {
    this.$mdDialog = $mdDialog;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;

    this.originRegistryUrl = angular.copy(this.registry.url);
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the edit button.
   */
  editRegistry() {
    if(!this.registry) {
      return;
    }

    let promise = this.chePreferences.addRegistry(this.registry.url, this.registry.username, this.registry.password);

    promise.then(() => {
      this.$mdDialog.hide();
      if(this.originRegistryUrl !== this.registry.url) {
        this.chePreferences.removeRegistry(this.originRegistryUrl).then(() => {

          this.cheNotification.showInfo('Registry successfully edited.');
        }, (error) => {
          this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Edit registry error.');
        });
      } else {
        this.cheNotification.showInfo('Registry successfully edited.');
      }
    }, (error) => {
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Edit registry error.');
    });
  }

}
