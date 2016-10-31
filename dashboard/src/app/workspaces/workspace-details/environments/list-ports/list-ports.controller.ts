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
 * @name workspace.details.controller:ListPortsController
 * @description This class is handling the controller for list of ports
 * @author Oleksii Kurinnyi
 */
export class ListPortsController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, lodash) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.serversSelectedStatus = {};
    this.serversSelectedNumber = 0;
    this.serversOrderBy = 'name';

    this.buildServersList();
  }

  buildServersList() {
    this.serversList = this.lodash.map(this.servers, (server, name) => {
      server.name = name;
      server.protocol = server.protocol ? server.protocol : 'http';
      return server;
    });
  }

  /**
   * Update port selected status
   */
  updateSelectedStatus() {
    this.serversSelectedNumber = 0;
    this.isBulkChecked = !!this.serversList.length;
    this.serversList.forEach((server) => {
      if (this.serversSelectedStatus[server.name]) {
        this.serversSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  changePortSelection(name) {
    this.serversSelectedStatus[name] = !this.serversSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection() {
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
  selectAllPorts() {
    this.serversSelectedNumber = this.serversList.length;
    this.serversList.forEach((server) => {
      this.serversSelectedStatus[server.name] = true;
    })
  }

  /**
   * Uncheck all ports in list
   */
  deselectAllPorts() {
    this.serversSelectedStatus = {};
    this.serversSelectedNumber = 0;
  }

  addPort(port, protocol) {
    let name = this.buildServerName(port);
    this.servers[name] = {'port': port, 'protocol': protocol};

    this.updateSelectedStatus();
    this.serversOnChange();
    this.buildServersList();
  }

  updatePort(serverName, port, protocol) {
    delete this.servers[serverName];
    delete this.serversSelectedStatus[serverName];
    this.updateSelectedStatus();

    let newName = this.buildServerName(port);
    this.servers[newName] = {'port': port, 'protocol': protocol};

    this.serversOnChange();
    this.buildServersList();
  }

  buildServerName(port) {
    return port + '/tcp';
  }

  /**
   * Show dialog to add new port
   * @param $event
   */
  showAddDialog($event) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'AddPortDialogController',
      controllerAs: 'addPortDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        servers: this.servers,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-ports/add-port-dialog/add-port-dialog.html'
    });
  }

  /**
   * Show dialog to edit existing port
   * @param $event
   * @param serverName {string}
   */
  showEditDialog($event, serverName) {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditPortDialogController',
      controllerAs: 'editPortDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        serverName: serverName,
        servers: this.servers,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/environments/list-ports/edit-port-dialog/edit-port-dialog.html'
    });
  }

  /**
   * Removes selected ports
   */
  deleteSelectedPorts() {
    this.showDeleteConfirmation(this.serversSelectedNumber).then(() => {
      this.lodash.forEach(this.serversSelectedStatus, (server, name) => {
        delete this.servers[name];
      });
      this.deselectAllPorts();
      this.isBulkChecked = false;
      this.serversOnChange();
      this.buildServersList();
    })
  }

  /**
   * Show confirmation popup before port to delete
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteConfirmation(numberToDelete) {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' ports?';
    }
    else {
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
