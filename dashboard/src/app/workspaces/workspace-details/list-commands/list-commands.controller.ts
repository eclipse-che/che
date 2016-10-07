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
 * @name workspace.details.controller:ListCommandsController
 * @description This class is handling the controller for list of workspace's commands
 * @author Oleksii Orel
 */
export class ListCommandsController {
  $mdDialog: ng.material.IDialogService;

  commands: Array<any>;
  isNoSelected: boolean;
  isBulkChecked: boolean;
  commandsOrderBy: string;
  selectedCommandsNumber: number;
  commandsSelectedStatus: Object;

  commandsOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.commandsSelectedStatus = {};
    this.selectedCommandsNumber = 0;
    this.commandsOrderBy = 'name';
  }

  /**
   * Update command selected status
   */
  updateSelectedStatus(): void {
    this.selectedCommandsNumber = 0;
    this.isBulkChecked = this.commands.length > 0;
    this.commands.forEach((command) => {
      if (this.commandsSelectedStatus[command.name]) {
        this.selectedCommandsNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  changeCommandSelection(name): void {
    this.commandsSelectedStatus[name] = !this.commandsSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllCommands();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllCommands();
    this.isBulkChecked = true;
  }

  /**
   * Check all commands in list
   */
  selectAllCommands(): void {
    this.selectedCommandsNumber = this.commands.length;
    this.commands.forEach((command) => {
      this.commandsSelectedStatus[command.name] = true;
    })
  }

  /**
   * Uncheck all commands in list
   */
  deselectAllCommands(): void {
    this.commandsSelectedStatus = {};
    this.selectedCommandsNumber = 0;
    this.isBulkChecked = false;
  }

  /**
   * Add a new command
   */
  addCommand(name: string, commandLine: string, previewUrl: string): void {
    let command: any = {};
    command.name = name;
    command.type = "custom";
    command.commandLine = commandLine;
    command.attributes = {};
    if (previewUrl) {
      command.attributes.previewUrl = previewUrl;
    }
    this.commands.push(command);
    this.updateSelectedStatus();
    this.commandsOnChange();
  }

  /**
   * Update the command
   */
  updateCommand(index: number, name: string, commandLine: string, previewUrl: string): void {
    this.commands[index].name = name;
    this.commands[index].commandLine = commandLine;
    if (!this.commands[index].attributes) {
      this.commands[index].attributes = {};
    }
    this.commands[index].previewUrl = previewUrl;
    this.updateSelectedStatus();
    this.commandsOnChange();
  }

  /**
   * Show dialog to add a new command
   * @param $event
   */
  showAddDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'AddCommandDialogController',
      controllerAs: 'addCommandDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        commands: this.commands,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/list-commands/add-command-dialog/add-command-dialog.html'
    });
  }

  /**
   * Show dialog to edit the existing command
   * @param $event
   * @param index {number} index of selected command
   */
  showEditDialog($event: MouseEvent, index: number): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditCommandDialogController',
      controllerAs: 'editCommandDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        index: index,
        commands: this.commands,
        callbackController: this
      },
      templateUrl: 'app/workspaces/workspace-details/list-commands/edit-command-dialog/edit-command-dialog.html'
    });
  }

  /**
   * Removes selected commands
   */
  deleteSelectedCommands(): void {
    this.showDeleteConfirmation(this.selectedCommandsNumber).then(() => {
      this.commands.reduceRight((previousValue, currentValue, index, array) => {
        if (this.commandsSelectedStatus[currentValue.name]) {
          array.splice(index, 1);
        }
      }, []);
      this.deselectAllCommands();
      this.commandsOnChange();
    })
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteConfirmation(numberToDelete): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' commands?';
    } else {
      confirmTitle += 'this selected command?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove command')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }

}
