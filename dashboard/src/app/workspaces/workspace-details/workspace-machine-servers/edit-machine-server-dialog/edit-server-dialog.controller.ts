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
import {IEnvironmentManagerMachineServer} from '../../../../../components/api/environment/environment-manager-machine';

const PORT_MIN = 1;
const PORT_MAX = 65535;

/**
 * @ngdoc controller
 * @name machine.servers.controller:EditMachineServersDialogController
 * @description This class is handling the controller for the dialog box about adding a new machine server or editing an existing one.
 * @author Oleksii Orel
 */
export class EditMachineServerDialogController {

  static $inject = ['$mdDialog'];

  updateServer: (port: number, protocol: string, reference: string, oldReference?: string) => void;

  private $mdDialog: ng.material.IDialogService;

  private popupTitle: string;
  private toEdit: string;
  private servers: {
    [reference: string]: IEnvironmentManagerMachineServer
  };
  private usedPorts: Array<number>;
  private usedReferences: Array<string>;
  private port: number;
  private protocol: string;
  private reference: string;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    // get used references and ports
    this.usedReferences = Object.keys(this.servers);
    this.usedPorts = [];
    this.usedReferences.forEach((reference: string) => {
      if (reference === this.toEdit) {
        return;
      }
      if (this.servers[reference].port) {
        this.usedPorts.push(parseInt(this.servers[reference].port.toString(), 10));
      }
    });

    if (this.toEdit && this.servers[this.toEdit]) {
      let server = this.servers[this.toEdit];
      this.reference = this.toEdit;
      this.protocol = server.protocol;
      this.port = server.port ? parseInt(server.port.toString(), 10) : null;
      this.popupTitle = 'Edit the server';
    } else {
      this.protocol = 'http';
      this.port = this.getLowestFreePort();
      this.popupTitle = 'Add a new server';
    }
  }

  /**
   * Check if port is unique.
   *
   * @param {number} port port to test
   * @returns {boolean}
   */
  isUniquePort(port: number): boolean {
    return this.usedPorts.indexOf(port) < 0;
  }

  /**
   * Check if reference is unique.
   *
   * @param {string} reference reference name to test
   * @returns {boolean}
   */
  isUniqueReference(reference: string): boolean {
    return this.usedReferences.indexOf(reference) < 0 || reference === this.toEdit;
  }

  /**
   * Returns the lowest free port.
   *
   * @returns {number}
   */
  getLowestFreePort(): number {
    let port: number;
    for (port = PORT_MIN; port <= PORT_MAX; port++) {
      if (this.usedPorts.indexOf(port) < 0) {
        break;
      }
    }
    return port;
  }

  /**
   * Cancel the dialog box.
   */
  hide(): void {
    this.$mdDialog.cancel();
  }

  /**
   * Add new server or update an existing one.
   */
  saveServer(): void {
    this.updateServer(this.port, this.protocol, this.reference, this.toEdit);
    this.hide();
  }
}
