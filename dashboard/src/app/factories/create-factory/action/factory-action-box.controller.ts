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
 * Defines controller of directive for displaying action box.
 * @ngdoc controller
 * @name factory.directive:FactoryActionBoxController
 * @author Florent Benoit
 */
export class FactoryActionBoxController {


  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog) {
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
  editAction($event, index) {
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

  callbackEditAction(index, newValue) {
    this.factoryObject.ide[this.lifecycle].actions[index].properties = newValue;

    this.onChange();
  }

  addAction() {
    if (!this.factoryObject.ide) {
      this.factoryObject.ide = {};
    }
    if (!this.factoryObject.ide[this.lifecycle]) {
        this.factoryObject.ide[this.lifecycle] = {};
        this.factoryObject.ide[this.lifecycle].actions = [];
    }

    var actionToAdd;
    if ('openfile' === this.selectedAction) {
      actionToAdd = {
        "properties": {
          "file": this.selectedParam
        },
        "id": "openFile"
      };
    } else if ('runcommand' === this.selectedAction) {
      actionToAdd = {
        "properties": {
          "name": this.selectedParam
        },
        "id": "runCommand"
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
  removeAction(index) {
    this.factoryObject.ide[this.lifecycle].actions.splice(index, 1);

    this.onChange();
  }
}
