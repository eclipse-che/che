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
 * This class is handling the controller for the add registry
 * @author Oleksii Orel
 */
export class AddRegistryController {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, chePreferences, cheNotification) {
    this.$mdDialog = $mdDialog;
    this.chePreferences = chePreferences;
    this.cheNotification = cheNotification;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the add button of the dialog(add registry).
   */
  createRegistry() {
    if(!this.registryUrl) {
      return;
    }

    let promise = this.chePreferences.addRegistry(this.registryUrl, this.registryUserName, this.registryUserPassword);

    promise.then(() => {
      this.$mdDialog.hide();
      this.cheNotification.showInfo('Registry successfully added.');
    }, (error) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Add registry error.');
    });
  }

}
