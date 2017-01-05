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
import {ListComponentsController} from '../list-components.controller';

/**
 * @ngdoc controller
 * @name list.components.controller:EditComponentDialogController
 * @description This class is handling the controller for a dialog box about editing the stack's component.
 * @author Oleksii Orel
 */
export class EditComponentDialogController {
  $mdDialog: ng.material.IDialogService;

  index: number;
  popupTitle: string;
  name: string;
  version: string;
  components: Array<any>;
  usedComponentsName: Array<string>;
  callbackController: ListComponentsController;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    let isAddMode: boolean = (this.index === -1);
    let component: any = isAddMode ? {name: '', version: ''} : this.components[this.index];
    this.name = component.name;
    this.version = component.version;

    this.usedComponentsName = [];
    this.components.forEach((component) => {
      if (this.name !== component.name) {
        this.usedComponentsName.push(component.name);
      }
    });

    this.popupTitle = isAddMode ? 'Add a new component' : 'Edit the component';
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
