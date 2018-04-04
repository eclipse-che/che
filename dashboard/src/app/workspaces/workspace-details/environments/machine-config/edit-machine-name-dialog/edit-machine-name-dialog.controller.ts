/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {WorkspaceMachineConfigController} from '../machine-config.controller';

/**
 * @ngdoc controller
 * @name machine.config.controller:EditMachineNameDialogController
 * @description This class is handling the controller for the dialog box about editing the machine name.
 * @author Oleksii Kurinnyi
 */
export class EditMachineNameDialogController {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;
  private name: string;
  private origName: string;
  private machineNames: string[];
  private machineNamesLowercase: string[];
  private callbackController: WorkspaceMachineConfigController;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.origName = this.name;
    this.machineNamesLowercase = this.machineNames.map((name: string) => name.toLowerCase());
  }

  isUnique(name: string): boolean {
    return this.machineNamesLowercase.indexOf(name.toLowerCase()) < 0;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Update machine name
   */
  updateMachineName(): void {
    this.callbackController.updateMachineName(this.name);
    this.hide();
  }
}
