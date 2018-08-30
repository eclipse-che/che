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
import {ListComponentsController} from '../list-components.controller';

interface IComponent {
  name: string;
  version: string;
}

/**
 * @ngdoc controller
 * @name list.components.controller:EditComponentDialogController
 * @description This class is handling the controller for a dialog box about editing the stack's component.
 * @author Oleksii Orel
 */
export class EditComponentDialogController {

  static $inject = ['$mdDialog'];

  $mdDialog: ng.material.IDialogService;

  index: number;
  name: string;
  version: string;
  components: Array<IComponent>;
  usedComponentsName: Array<string>;
  callbackController: ListComponentsController;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    let isAddMode = (this.index === -1);
    let component = isAddMode ? {name: '', version: ''} : this.components[this.index];
    this.name = component.name;
    this.version = component.version;

    this.usedComponentsName = [];
    this.components.forEach((component: IComponent) => {
      if (this.name !== component.name) {
        this.usedComponentsName.push(component.name);
      }
    });
  }

  /**
   * Check if the name is unique.
   * @param name {string}
   * @returns {boolean}
   */
  isUnique(name: string): boolean {
    return this.usedComponentsName.indexOf(name) === -1;
  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Update the component
   */
  updateComponent(): void {
    this.callbackController.updateComponent(this.index, this.name, this.version);
    this.hide();
  }
}
