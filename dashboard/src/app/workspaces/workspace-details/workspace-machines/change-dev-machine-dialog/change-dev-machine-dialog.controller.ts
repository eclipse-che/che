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
import {IEnvironmentManagerMachine} from '../../../../../components/api/environment/environment-manager-machine';

/**
 * This class is handling the controller for the change machines dialog.
 *
 * @author Oleksii Orel
 */
export class ChangeDevMachineDialogController {
  /**
   * Material design Dialog service.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * Current devMachine name.
   * Passed from parent controller.
   */
  private currentDevMachineName: string;
  /**
   * List of machines.
   * Passed from parent controller.
   */
  private machinesList: Array<IEnvironmentManagerMachine>;
  /**
   * Popup title.
   * Passed from parent controller.
   */
  private popupTitle: string;
  /**
   * Change button title.
   * Passed from parent controller.
   */
  private okButtonTitle: string;
  /**
   * Callback which is called when change DEV machine.
   * Passed from parent controller.
   */
  private changeDevMachine: (machineName: string) => void;
  /**
   * Popup's message.
   */
  private message: string;
  /**
   * Machine name which will be configured as dev-machine
   */
  private newDevMachine: string;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    if (!angular.isArray(this.machinesList)) {
      this.machinesList = [];
    }
    if (!this.popupTitle) {
      this.popupTitle = 'Change DEV machine';
    }
    if (!this.okButtonTitle) {
      this.okButtonTitle = 'OK';
    }

    this.message = this.machinesList.length > 1 ? 'Select the machine to get ws-agent activated:' : 'You can\'t change it without having other machines configured.';
  }

  /**
   * Returns list of machines not including current dev machine.
   *
   * @return {Array<IEnvironmentManagerMachine>}
   */
  getMachinesList(): Array<IEnvironmentManagerMachine> {
    return this.machinesList.filter((machine: IEnvironmentManagerMachine) => {
      return machine.name !== this.currentDevMachineName;
    });
  }

  /**
   * Cancels this dialog.
   */
  cancel(): void {
    this.$mdDialog.cancel();
  }

  /**
   * Changes DEV machine.
   */
  onDevChange(): void {
    if (angular.isFunction(this.changeDevMachine)) {
      this.changeDevMachine(this.newDevMachine);
    }
    this.$mdDialog.hide();
  }

}
