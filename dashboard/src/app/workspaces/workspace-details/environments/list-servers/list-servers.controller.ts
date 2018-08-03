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
import {ConfirmDialogService} from '../../../../../components/service/confirm-dialog/confirm-dialog.service';

interface IServerListItem extends IEnvironmentManagerMachineServer {
  reference: string;
}

/**
 * @ngdoc controller
 * @name workspace.details.controller:ListServersController
 * @description This class is handling the controller for list of servers
 * @author Oleksii Kurinnyi
 */
export class ListServersController {

  static $inject = ['$mdDialog', 'lodash', 'confirmDialogService'];

  $mdDialog: ng.material.IDialogService;
  lodash: any;

  isNoSelected: boolean = true;
  isBulkChecked: boolean = false;
  serversSelectedStatus: {
    [serverName: string]: boolean
  }  = {};
  serversSelectedNumber: number = 0;
  serversOrderBy: string = 'reference';
  server: IEnvironmentManagerMachineServer;
  servers: {
    [reference: string]: IEnvironmentManagerMachineServer
  };
  serversList: IServerListItem[];

  serversOnChange: Function;
  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService,
              lodash: any,
              confirmDialogService: ConfirmDialogService) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.confirmDialogService = confirmDialogService;

    this.buildServersList();
  }

  /**
   * Build list of servers
   */
  buildServersList(): void {
    this.serversList = this.lodash.map(this.servers, (server: IEnvironmentManagerMachineServer, reference: string) => {
      let serverItem: IServerListItem = angular.extend({}, {reference: reference}, server);
      serverItem.protocol = serverItem.protocol ? serverItem.protocol : '-';
      return serverItem;
    });
  }

  /**
   * Update server selected status
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
  changeServerSelection(name: string): void {
    this.serversSelectedStatus[name] = !this.serversSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllServers();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllServers();
    this.isBulkChecked = true;
  }

  /**
   * Check all servers in list
   */
  selectAllServers(): void {
    this.serversSelectedNumber = 0;
    this.serversList.forEach((serverListItem: IServerListItem) => {
      if (serverListItem.userScope === false) {
        return;
      }
      this.serversSelectedNumber++;
      this.serversSelectedStatus[serverListItem.reference] = true;
    });
  }

  /**
   * Uncheck all servers in list
   */
  deselectAllServers(): void {
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
    this.servers[reference] = {
      port: port,
      protocol: protocol,
      userScope: true
    };

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
   * Show dialog to add new or edit existing server
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
   * Removes selected servers
   */
  deleteSelectedServers(): void {
    this.showDeleteConfirmation(this.serversSelectedNumber).then(() => {
      this.lodash.forEach(this.serversSelectedStatus, (server: IEnvironmentManagerMachineServer, name: string) => {
        delete this.servers[name];
      });
      this.deselectAllServers();
      this.isBulkChecked = false;
      this.serversOnChange();
      this.buildServersList();
    });
  }

  /**
   * Show confirmation popup before server to delete
   * @param {number} numberToDelete
   * @returns {angular.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' servers?';
    } else {
      content += 'this selected server?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove servers', content, 'Delete');
  }

}
