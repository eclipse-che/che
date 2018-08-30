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

/**
 * Defines controller of directive for displaying action box.
 * @ngdoc controller
 * @name factory.directive:FactoryActionBoxController
 * @author Florent Benoit
 */
export class FactoryActionBoxController {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;
  private actions: Array<any>;
  private selectedAction: string;
  private factoryObject: any;
  private lifecycle: any;
  private onChange: Function;
  private selectedParam: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    this.actions = [];
    this.actions.push({name : 'RunCommand', id: 'runcommand'});
    this.actions.push({name : 'openFile', id: 'openfile'});
    this.selectedAction = this.actions[0].id;
  }

  /**
   * Edit the action based on the provided index
   * @param $event the mouse event
   * @param index the index in the array of factory actions
   */
  editAction($event: any, index: number): void {
    let action = this.factoryObject.ide[this.lifecycle].actions[index];
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'FactoryActionDialogEditController',
      controllerAs: 'factoryActionDialogEditCtrl',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this,
        index: index,
        // selectedAction: action
        selectedValue: action.properties
      },
      templateUrl: 'app/factories/create-factory/action/factory-action-edit.html'
    });
  }

  /**
   * Edit action callback.
   *
   * @param index the index in the array of factory actions
   * @param newValue new value
   */
  callbackEditAction(index: number, newValue: any): void {
    this.factoryObject.ide[this.lifecycle].actions[index].properties = newValue;

    this.onChange();
  }

  addAction(): void {
    if (!this.factoryObject.ide) {
      this.factoryObject.ide = {};
    }
    if (!this.factoryObject.ide[this.lifecycle]) {
        this.factoryObject.ide[this.lifecycle] = {};
        this.factoryObject.ide[this.lifecycle].actions = [];
    }

    let actionToAdd;
    if ('openfile' === this.selectedAction) {
      actionToAdd = {
        'properties': {
          'file': this.selectedParam
        },
        'id': 'openFile'
      };
    } else if ('runcommand' === this.selectedAction) {
      actionToAdd = {
        'properties': {
          'name': this.selectedParam
        },
        'id': 'runCommand'
      };
    }
    if (actionToAdd) {
      this.factoryObject.ide[this.lifecycle].actions.push(actionToAdd);
    }

    this.onChange();
  }

  /**
   * Remove action based on the provided index
   * @param index the index in the array of factory actions
   */
  removeAction(index: number): void {
    this.factoryObject.ide[this.lifecycle].actions.splice(index, 1);

    this.onChange();
  }
}
