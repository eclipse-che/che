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

/**
 * @ngdoc controller
 * @name stacks.details.controller:ListComponentsController
 * @description This class is handling the controller for list of stack's components
 * @author Oleksii Orel
 */
export class ListComponentsController {
  $mdDialog: ng.material.IDialogService;

  components: Array<any>;
  isNoSelected: boolean;
  isBulkChecked: boolean;
  componentsOrderBy: string;
  selectedComponentsNumber: number;
  componentsSelectedStatus: Object;

  componentsOnChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

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
    this.components.forEach((component) => {
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
  changeComponentSelection(name): void {
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
    this.components.forEach((component) => {
      this.componentsSelectedStatus[component.name] = true;
    })
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
    let component: any = {};
    component.name = name;
    component.version = version ? version : '---';
    this.components.push(component);
  }

  /**
   * Update the component
   * @param index: number
   * @param name: string
   * @param version: string
   */
  updateComponent(index: number, name: string, version: string): void {
    if(index === -1){
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
      this.components.reduceRight((previousValue, currentValue, index, array) => {
        if (this.componentsSelectedStatus[currentValue.name]) {
          array.splice(index, 1);
        }
      }, []);
      this.deselectAllComponents();
      this.componentsOnChange();
    })
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete
   * @returns {ng.IPromise<any>}
   */
  showDeleteConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' components?';
    } else {
      confirmTitle += 'this selected component?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove component')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }
}
