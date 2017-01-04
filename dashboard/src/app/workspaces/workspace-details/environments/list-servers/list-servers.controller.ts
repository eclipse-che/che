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
import {IServer} from './server';

interface IServerListItem extends IServer {
  reference: string;
}

/**
 * @ngdoc controller
 * @name workspace.details.controller:ListServersController
 * @description This class is handling the controller for list of servers
 * @author Oleksii Kurinnyi
 */
export class ListServersController {
  $mdDialog: ng.material.IDialogService;
  lodash: _.LoDashStatic;

  isNoSelected: boolean = true;
  isBulkChecked: boolean = false;
  serversSelectedStatus: {
    [serverName: string]: boolean
  }  = {};
  serversSelectedNumber: number = 0;
  serversOrderBy: string = 'reference';
  server: IServer;
  servers: {
    [reference: string]: IServer
  };
  serversList: IServerListItem[];

  serversOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, lodash: _.LoDashStatic) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.buildServersList();
  }

  /**
   * Build list of servers
   */
  buildServersList(): void {
    this.serversList = this.lodash.map(this.servers, (server: IServer, reference: string) => {
      let serverItem: IServerListItem = angular.extend({}, {reference: reference}, server);
      serverItem.protocol = serverItem.protocol ? serverItem.protocol : 'http';
      return serverItem;
    });
  }

  /**
   * Update port selected status
   */
  updateSelectedStatus(): void {
    this.serversSelectedNumber = 0;
    this.isBulkChecked = !!this.serversList.length;
    this.serversList.forEach((serverListItem: IServerListItem) => {
      if (this.serversSelectedStatus[serverListItem.reference]) {
        this.serversSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * @param {string} name
   */
  changePortSelection(name: string): void {
    this.serversSelectedStatus[name] = !this.serversSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllPorts();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllPorts();
    this.isBulkChecked = true;
  }

  /**
   * Check all ports in list
   */
  selectAllPorts(): void {
    this.serversSelectedNumber = this.serversList.length;
    this.serversList.forEach((serverListItem: IServerListItem) => {
      this.serversSelectedStatus[serverListItem.reference] = true;
    });
  }

  /**
   * Uncheck all ports in list
   */
  deselectAllPorts(): void {
    this.serversSelectedStatus = {};
    this.serversSelectedNumber = 0;
  }

  /**
   * Add new server.
   *
   * @param {string} reference
   * @param {number} port
   * @param {string} protocol
   */
  addServer(reference: string, port: number, protocol: string): void {
    this.servers[reference] = {'port': port, 'protocol': protocol};

    this.updateSelectedStatus();
    this.serversOnChange();
    this.buildServersList();
  }

  /**
   * Update server
   *
   * @param {string} oldReference
   * @param {string} newReference
   * @param {number} port
   * @param {string} protocol
   */
  updateServer(oldReference: string, newReference: string, port: number, protocol: string): void {
    delete this.servers[oldReference];

    this.addServer(newReference, port, protocol);
  }

  /**
   * Show dialog to add new or edit existing port
   *
   * @param {MouseEvent} $event
   * @param {string=} reference
   */
  showEditDialog($event: MouseEvent, reference?: string): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditServerDialogController',
      controllerAs: 'editServerDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        toEdit: reference,
        servers: this.servers,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-servers/edit-server-dialog/edit-server-dialog.html'
    });
  }

  /**
   * Removes selected ports
   */
  deleteSelectedPorts(): void {
    this.showDeleteConfirmation(this.serversSelectedNumber).then(() => {
      this.lodash.forEach(this.serversSelectedStatus, (server: IServer, name: string) => {
        delete this.servers[name];
      });
      this.deselectAllPorts();
      this.isBulkChecked = false;
      this.serversOnChange();
      this.buildServersList();
    });
  }

  /**
   * Show confirmation popup before port to delete
   * @param {number} numberToDelete
   * @returns {angular.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' ports?';
    } else {
      confirmTitle += 'this selected port?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove port')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

}
