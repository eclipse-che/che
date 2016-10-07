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
import {ListCommandsController} from "../list-commands.controller";

/**
 * @ngdoc controller
 * @name list.commands.controller:AddCommandDialogController
 * @description This class is handling the controller for a dialog box about adding a workspace's command.
 * @author Oleksii Orel
 */
export class AddCommandDialogController {
  $mdDialog: ng.material.IDialogService;

  name: string;
  previewUrl: string;
  commandLine: string;
  commands: Array<any>;
  usedCommandsName: Array<string>;
  callbackController: ListCommandsController;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.usedCommandsName = [];
    this.commands.forEach((command) => {
      this.usedCommandsName.push(command.name);
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
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Add a new command
   */
  addCommand(): void {
    this.callbackController.addCommand(this.name, this.commandLine, this.previewUrl);
    this.hide();
  }
}
