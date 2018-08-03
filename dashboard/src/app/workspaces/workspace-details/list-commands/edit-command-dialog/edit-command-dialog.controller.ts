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
import {ListCommandsController} from '../list-commands.controller';

/**
 * @ngdoc controller
 * @name list.commands.controller:EditCommandDialogController
 * @description This class is handling the controller for a dialog box about editing the workspace's command.
 * @author Oleksii Orel
 */
export class EditCommandDialogController {

  static $inject = ['$mdDialog'];

  $mdDialog: ng.material.IDialogService;
  index: number;
  name: string;
  previewUrl: string;
  commandLine: string;
  commands: Array<any>;
  usedCommandsName: Array<string>;
  callbackController: ListCommandsController;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    let isAddMode: boolean = (this.index === -1);
    let command: any = isAddMode ? {name: '', commandLine: ''} : this.commands[this.index];
    this.name = command.name;
    this.commandLine = command.commandLine;
    if (command.attributes && command.attributes.previewUrl) {
      this.previewUrl = command.attributes.previewUrl;
    }

    this.usedCommandsName = [];
    this.commands.forEach((command: any) => {
      if (isAddMode || this.name !== command.name) {
        this.usedCommandsName.push(command.name);
      }
    });
  }

  /**
   * Check if the name is unique.
   * @param name
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return this.usedCommandsName.indexOf(name) === -1;
  }

  /**
   * It will hide the dialog box.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Update the command
   */
  updateCommand() {
    this.callbackController.updateCommand(this.index, this.name, this.commandLine, this.previewUrl);
    this.hide();
  }
}
