/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * @ngdoc controller
 * @name environment.variables.controller:EditMachineVolumeDialogController
 * @description This class is handling the controller for the dialog box about adding a new volume or editing an existing one.
 * @author Oleksii Orel
 */
export class EditMachineVolumeDialogController {
  editorState = {isValid: true};
  private $mdDialog: ng.material.IDialogService;
  private popupTitle: string;
  private toEdit: string;
  private machineVolumes: {
    [volumeName: string]: { path: string }
  };
  private name: string;
  private path: string;
  private usedNames: Array<string>;
  private updateMachineVolume: (volumeName: string, volumePath: string, volumeOldName?: string) => void;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    // build list of used names
    this.usedNames = angular.isObject(this.machineVolumes) ? Object.keys(this.machineVolumes) : [];

    if (this.toEdit) {
      this.name = this.toEdit;
      this.popupTitle = 'Edit the volume';
      this.path = this.machineVolumes[this.name] ? this.machineVolumes[this.name].path : '';
    } else {
      this.popupTitle = 'Add a new volume';
    }
  }

  /**
   * Check if name is unique.
   * @param {string} name volume name to test
   * @return {boolean} true if name is unique
   */
  isUniqueName(name: string) {
    return this.usedNames.indexOf(name) < 0 || name === this.toEdit;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Add new or update an existing machine volume.
   */
  saveVolume(): void {
    this.updateMachineVolume(this.name, this.path, this.toEdit);
    this.hide();
  }
}
