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

let StackDetails = function () {
  let utils = require('../../utils');

  // toobar
  this.toolbarElement = $('.che-toolbar');
  this.toolbarTestButtonElement = this.toolbarElement.$('[che-button-title="Test"]').$('button');
  this.toolbarSaveButtonElement = this.toolbarElement.$('[che-button-title="Save"]').$('button');

  // runtime section
  this.runtimeSectionElement = $('workspace-environments');
  this.runtimeMachineElements = this.runtimeSectionElement.all(by.repeater('machine in workspaceEnvironmentsController.machines'));
  this.runtimeRecipeLocationElement = this.runtimeSectionElement.$('.recipe-location');
  this.runtimeRecipeEditorElement = this.runtimeSectionElement.$('.recipe-editor');
  this.runtimeRecipeShowButtonElement = this.runtimeSectionElement.$('[che-button-title="Show"]').$('button');

  // machine subsection
  this.getMachineConfigByName = (name) => {
    return this.runtimeMachineElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.config-title-row', name)).isPresent().then(isPresent => isPresent);
    }).first();
  };
  this.getMachineConfigByIndex = (index) => {
    return this.runtimeMachineElements.filter((elem, idx) => {
      return idx === index;
    }).first();
  };
  this.splitMachineConfig = (elem) => {
    return {
      titleTextElement: elem.$('.config-title'),
      titleOpenElement: elem.$('.config-title-action-show'),
      titleEditElement: elem.$('.config-title-action-edit'),
      titleDeleteElement: elem.$('.config-title-action-delete'),
      // source
      sourceFormElement: elem.$('.config-machine-source'),
      sourceInputElement: utils.getVisibleInputElement(elem.$('.config-machine-source')),
      // is dev
      isDevSwitchElement: elem.$('.config-dev-machine-switch md-switch')
    };
  };

  // edit machine name popup
  this.editMachineNamePopupElement = $('.edit-machine-name-dialog-content');
  this.editMachineNameInputElement = utils.getVisibleInputElement( $('.edit-machine-name-dialog-input') );
  this.updateMachineNameButtonElement = $('[che-button-title="Update"]').$('button');
  this.closeMachineNameButtonElement = $('[che-button-title="Close"]').$('button');

  // remove machine (not dev) popup
  this.deleteNotDevMachinePopupElement = element(by.cssContainingText('.che-confirm-dialog-notification', 'Would you like to delete this machine?'));
  this.deleteNotDevMachineDeleteButtonElement = this.deleteNotDevMachinePopupElement.$('[che-button-title="Delete"]').$('button');

  // remove machine (dev) popup
  this.deleteDevMachinePopupElement = $('.delete-dev-machine-dialog');
  this.deleteDevMachineDeleteButtonElement = this.deleteDevMachinePopupElement.$('[che-button-title="Delete"]').$('button');
};

module.exports = new StackDetails();
