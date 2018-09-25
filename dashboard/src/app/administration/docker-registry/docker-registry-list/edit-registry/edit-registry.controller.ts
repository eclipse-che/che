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
import {ChePreferences} from '../../../../../components/api/che-preferences.factory';
import {CheNotification} from '../../../../../components/notification/che-notification.factory';

/**
 * This class is handling the edit registry controller.
 * @author Oleksii Orel
 */
export class EditRegistryController {
  static $inject = ['$mdDialog', 'chePreferences', 'cheNotification'];

  registry: {
    url: string,
    username: string,
    password: string;
  };
  private $mdDialog: ng.material.IDialogService;
  private cheNotification: CheNotification;
  private chePreferences: ChePreferences;
  private originRegistryUrl: string;

  /**
   * Default constructor.
   */
  constructor($mdDialog: ng.material.IDialogService, chePreferences: ChePreferences, cheNotification: CheNotification) {
    this.$mdDialog = $mdDialog;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;

    if (this.registry) {
      this.originRegistryUrl = angular.copy(this.registry.url);
    }
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Callback for add/update registry.
   */
  update(): void {
    let defaultErrorMessage =  this.originRegistryUrl ? 'Edit registry error.' : 'Add registry error.';
    this.chePreferences.addRegistry(this.registry.url, this.registry.username, this.registry.password).then(() => {
      if (this.originRegistryUrl !== this.registry.url) {
        this.chePreferences.removeRegistry(this.originRegistryUrl).then(() => {
          this.$mdDialog.hide();
        }, (error: any) => {
          this.cheNotification.showError(error.data && error.data.message ? error.data.message : defaultErrorMessage);
        });
      } else {
        this.$mdDialog.hide();
      }
    }, (error: any) => {
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : defaultErrorMessage);
    });
  }
}
