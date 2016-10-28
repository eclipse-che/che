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
 * @ngdoc controller
 * @name list.environment.variables.controller:AddPortDialogController
 * @description This class is handling the controller for the dialog box about adding the port.
 * @author Oleksii Kurinnyi
 */
export class AddPortDialogController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, lodash) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.usedPorts = [];

    this.portMin = 1024;
    this.portMax = 65535;
    this.protocol = 'http';

    this.fillInUsedPorts();
    this.port = this.getLowestFreePort();
  }

  isUnique(port) {
    return !this.usedPorts.includes(port);
  }

  fillInUsedPorts() {
   this.lodash.forEach(this.servers, (server) => {
      this.usedPorts.push(parseInt(server.port, 10));
    });
  }

  getLowestFreePort() {
    let port;
    for (port=this.portMin; port<=this.portMax; port++) {
      if (!this.usedPorts.includes(port)) {
        break;
      }
    }
    return port;
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Adds new port
   */
  addPort() {
    this.callbackController.addPort(this.port, this.protocol);
    this.hide();
  }

}
