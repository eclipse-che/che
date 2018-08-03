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
 * Defines controller of directive for displaying factory command.
 * @ngdoc controller
 * @name factory.directive:FactoryCommandController
 * @author Florent Benoit
 */
export class FactoryCommandController {

  static $inject = ['$mdDialog'];

  private $mdDialog: ng.material.IDialogService;
  private factoryObject: any;
  private onChange: Function;
  private commandLine: string;
  private commandLineName: string;

  /**
   * Default constructor that is using resource injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * User clicked on the add button to add a new command
   */
  addCommand(): void {
    if (!this.factoryObject) {
      this.factoryObject = {};
    }

    if (!this.factoryObject.workspace) {
      this.factoryObject.workspace = {};
    }

    if (!this.factoryObject.workspace.commands) {
      this.factoryObject.workspace.commands = [];
    }
    let command = {
      'commandLine': this.commandLine,
      'name': this.commandLineName,
      'attributes': {
        'previewUrl': ''
      },
      'type': 'custom'
    };

    this.factoryObject.workspace.commands.push(command);

    this.onChange();
  }

  /**
   * Remove command based on the provided index
   * @param {number} index the index in the array of workspace commands
   */
  removeCommand(index: number): void {
    this.factoryObject.workspace.commands.splice(index, 1);

    this.onChange();
  }

  /**
   * Edit the command based on the provided index
   * @param {MouseEvent} $event the mouse event
   * @param {number} index the index in the array of workspace commands
   */
  editCommand($event: MouseEvent, index: number): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'FactoryCommandDialogEditController',
      controllerAs: 'factoryCommandDialogEditCtrl',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this,
        index: index,
        selectedValue: this.factoryObject.workspace.commands[index].commandLine
      },
      templateUrl: 'app/factories/create-factory/command/factory-command-edit.html'
    });
  }

  /**
   * Callback on edit action.
   *
   * @param {number} index commands index
   * @param {string} newValue value to update with
   */
  callbackEditAction(index: number, newValue: string): void {
    this.factoryObject.workspace.commands[index].commandLine = newValue;

    this.onChange();
  }
}
