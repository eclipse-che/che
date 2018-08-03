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
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {
  IEnvironmentManagerMachine,
  IEnvironmentManagerMachineServer
} from '../../../../components/api/environment/environment-manager-machine';

interface IServerListItem extends IEnvironmentManagerMachineServer {
  reference: string;
}

/**
 * @ngdoc controller
 * @name machine.servers.controller:MachineServersController
 * @description This class is handling the controller for list of machine servers.
 * @author Oleksii Orel
 */
export class MachineServersController {

  static $inject = ['$scope', '$mdDialog', 'confirmDialogService'];

  serversOrderBy = 'reference';

  private $mdDialog: ng.material.IDialogService;
  private confirmDialogService: ConfirmDialogService;
  private selectedMachine: IEnvironmentManagerMachine;
  private environmentManager: EnvironmentManager;
  private isBulkChecked: boolean = false;
  private servers: { [reference: string]: IEnvironmentManagerMachineServer };
  private serversSelectedStatus: { [serverName: string]: boolean } = {};
  private serversList: Array<IServerListItem> = [];
  private serversSelectedNumber: number = 0;
  private onChange: Function;
  private hasUserScope: boolean;

  /**
   * Default constructor that is using resource.
   */
  constructor($scope: ng.IScope, $mdDialog: ng.material.IDialogService, confirmDialogService: ConfirmDialogService) {
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;

    this.buildServersList(this.selectedMachine);
    const deRegistrationFn = $scope.$watch(() => {
      return this.selectedMachine;
    }, (selectedMachine: IEnvironmentManagerMachine) => {
      this.buildServersList(selectedMachine);
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
    });
  }

  /**
   * Build servers list.
   * @param selectedMachine {IEnvironmentManagerMachine}
   */
  buildServersList(selectedMachine: IEnvironmentManagerMachine): void {
    if (!selectedMachine || !this.environmentManager) {
      return;
    }
    this.servers = this.environmentManager.getServers(selectedMachine);
    if (!angular.isObject(this.servers)) {
      return;
    }
    this.serversList = [];
    this.hasUserScope = false;
    Object.keys(this.servers).forEach((reference: string) => {
      const serverItem = angular.extend({}, {reference: reference}, this.servers[reference]);
      serverItem.protocol = serverItem.protocol ? serverItem.protocol : '-';
      if (!this.hasUserScope && serverItem.userScope) {
        this.hasUserScope = true;
      }
      this.serversList.push(serverItem);
    });
  }

  /**
   * Update environment server selected status.
   */
  updateSelectedStatus(): void {
    this.serversSelectedNumber = 0;
    this.isBulkChecked = true;
    this.serversList.forEach((serverListItem: IServerListItem) => {
      if (this.serversSelectedStatus[serverListItem.reference]) {
        this.serversSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllServers();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllUserScopeServers();
    this.isBulkChecked = true;
  }

  /**
   * Check all user scope servers in list.
   */
  selectAllUserScopeServers(): void {
    this.serversSelectedNumber = 0;
    this.serversList.forEach((serverListItem: IServerListItem) => {
      if (!serverListItem.userScope) {
        return;
      }
      this.serversSelectedNumber++;
      this.serversSelectedStatus[serverListItem.reference] = true;
    });
  }

  /**
   * Uncheck all environment servers in list.
   */
  deselectAllServers(): void {
    this.serversSelectedStatus = {};
    this.serversSelectedNumber = 0;
  }

  /**
   * Updates the existing server or create a new one.
   * @param {string} reference
   * @param {number} port
   * @param {string} protocol
   * @param {string} oldReference
   */
  updateServer(port: number, protocol: string, reference: string, oldReference?: string): void {
    if (oldReference) {
      delete this.servers[oldReference];
    }
    this.servers[reference] = {
      port: port,
      protocol: protocol,
      userScope: true
    };
    this.environmentManager.setServers(this.selectedMachine, this.servers);
    this.onChange();
  }

  /**
   * Show dialog to edit the existing server or add a new one.
   * @param {string}  reference
   */
  showEditDialog(reference?: string): void {
    this.$mdDialog.show({
      controller: 'EditMachineServerDialogController',
      controllerAs: 'editMachineServerDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        toEdit: reference,
        servers: this.servers,
        updateServer: this.updateServer.bind(this)
      },
      templateUrl: 'app/workspaces/workspace-details/workspace-machine-servers/edit-machine-server-dialog/edit-server-dialog.html'
    });
  }

  /**
   * Removes selected environment servers.
   */
  deleteSelectedServers(): void {
    this.showDeleteConfirmation(this.serversSelectedNumber).then(() => {
      Object.keys(this.serversSelectedStatus).forEach((reference: string) => {
        delete this.servers[reference];
      });
      this.deselectAllServers();
      this.isBulkChecked = false;
      this.environmentManager.setServers(this.selectedMachine, this.servers);
      this.onChange();
    });
  }

  /**
   * Shows confirmation popup before delete.
   * @param reference {string}
   */
  deleteServer(reference: string): void {
    const promise = this.confirmDialogService.showConfirmDialog('Remove server', 'Would you like to delete this server?', 'Delete');
    promise.then(() => {
      delete this.servers[reference];
      this.environmentManager.setServers(this.selectedMachine, this.servers);
      this.onChange();
    });
  }

  /**
   * Show confirmation popup before environment server to delete.
   * @param {number} numberToDelete number of environment servers to delete
   * @returns {ng.IPromise<any>}
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
