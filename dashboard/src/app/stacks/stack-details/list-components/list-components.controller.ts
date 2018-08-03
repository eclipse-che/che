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

interface IComponent {
  name: string;
  version: string;
}

/**
 * @ngdoc controller
 * @name stacks.details.controller:ListComponentsController
 * @description This class is handling the controller for list of stack's components
 * @author Oleksii Orel
 */
export class ListComponentsController {

  static $inject = ['$mdDialog', 'confirmDialogService'];

  $mdDialog: ng.material.IDialogService;

  components: Array<any>;
  isNoSelected: boolean;
  isBulkChecked: boolean;
  componentsOrderBy: string;
  selectedComponentsNumber: number;
  componentsSelectedStatus: Object;

  componentsOnChange: Function;

  private confirmDialogService: ConfirmDialogService;

  /**
   * Default constructor that is using resource
   */
  constructor($mdDialog: ng.material.IDialogService, confirmDialogService: ConfirmDialogService) {
    this.$mdDialog = $mdDialog;
    this.confirmDialogService = confirmDialogService;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.componentsSelectedStatus = {};
    this.selectedComponentsNumber = 0;
    this.componentsOrderBy = 'name';
  }

  /**
   * Update component selected status
   */
  updateSelectedStatus(): void {
    this.selectedComponentsNumber = 0;
    this.isBulkChecked = this.components.length > 0;
    this.components.forEach((component: IComponent) => {
      if (this.componentsSelectedStatus[component.name]) {
        this.selectedComponentsNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * Change component selection
   * @param name: string
   */
  changeComponentSelection(name: string): void {
    this.componentsSelectedStatus[name] = !this.componentsSelectedStatus[name];
    this.updateSelectedStatus();
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllComponents();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllComponents();
    this.isBulkChecked = true;
  }

  /**
   * Check all components in list
   */
  selectAllComponents(): void {
    this.selectedComponentsNumber = this.components.length;
    this.components.forEach((component: IComponent) => {
      this.componentsSelectedStatus[component.name] = true;
    });
  }

  /**
   * Uncheck all components in list
   */
  deselectAllComponents(): void {
    this.componentsSelectedStatus = {};
    this.selectedComponentsNumber = 0;
    this.isBulkChecked = false;
  }

  /**
   * Add a new component
   * @param name: string
   * @param version: string
   */
  addComponent(name: string, version: string): void {
    let component: IComponent = {
      name: name,
      version: version ? version : '---'
    };
    this.components.push(component);
  }

  /**
   * Update the component
   * @param index: number
   * @param name: string
   * @param version: string
   */
  updateComponent(index: number, name: string, version: string): void {
    if (index === -1) {
      this.addComponent(name, version);
    } else {
      this.components[index].name = name;
      this.components[index].version = version ? version : '---';
    }
    this.updateSelectedStatus();
    this.componentsOnChange();
  }

  /**
   * Show dialog to add a new component
   * @param $event: MouseEvent
   */
  showAddDialog($event: MouseEvent): void {
    this.showEditDialog($event, -1);
  }

  /**
   * Show dialog to edit the existing component
   * @param $event: MouseEvent
   * @param index: number - index of selected component
   */
  showEditDialog($event: MouseEvent, index: number): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'EditComponentDialogController',
      controllerAs: 'editComponentDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        index: index,
        components: this.components,
        callbackController: this
      },
      templateUrl: 'app/stacks/stack-details/list-components/edit-component-dialog/edit-component-dialog.html'
    });
  }

  /**
   * Removes selected components
   */
  deleteSelectedComponents(): void {
    this.showDeleteConfirmation(this.selectedComponentsNumber).then(() => {
      this.components.reduceRight((previousValue: IComponent, currentValue: IComponent, index: number, array: Array<IComponent>) => {
        if (this.componentsSelectedStatus[currentValue.name]) {
          array.splice(index, 1);
        }
      }, []);
      this.deselectAllComponents();
      this.componentsOnChange();
    });
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' components?';
    } else {
      content += 'this selected component?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove components', content, 'Delete');
  }
}
