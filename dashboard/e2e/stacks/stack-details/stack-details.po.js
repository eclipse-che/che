'use strict';

let StackDetails = function () {
  let utils = require('../../utils');

  // toobar
  this.toolbarElement = $('.che-toolbar');
  this.toolbarTestButtonElement = this.toolbarElement.$('[che-button-title="Test"]').$('button');
  this.toolbarSaveButtonElement = this.toolbarElement.$('[che-button-title="Save"]').$('button');

  // runtime section
  this.runtimeSectionElement = $('workspace-environments');
  this.runtimeMachineElements = this.runtimeSectionElement.$$('.workspace-machine-config');
  this.runtimeRecipeLocationElement = this.runtimeSectionElement.$('.recipe-location');
  this.runtimeRecipeEditorElement = this.runtimeSectionElement.$('.recipe-editor');
  this.runtimeRecipeShowButtonElement = this.runtimeSectionElement.$('[che-button-title="Show"]');

  // machine subsection
  let _getMachineElements = (elem) => {
    if (!elem) {
      return null;
    }
    return {
      titleTextElement: elem.$('.config-title'),
      titleOpenElement: elem.$('.config-title-action-show'),
      titleEditElement: elem.$('.config-title-action-edit'),
      sourceRowElement: elem.$('.config-machine-source'),
      sourceInputElement: utils.getVisibleInputElement(elem.$('.config-machine-source'))
    };
  };
  this.getMachineElementByName = (name) => {
    let machineConfigElement = this.runtimeMachineElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('.config-title-row', name)).isPresent().then(isPresent => isPresent)
    }).get(0);
    return _getMachineElements(machineConfigElement);
  };
  this.getMachineElementByIndex = (index) => {
    let machineConfigElement = this.runtimeMachineElements.filter((elem, idx) => {
      if (idx === index) {
        return elem;
      }
    }).get(0);
    return _getMachineElements(machineConfigElement);
  };

  // edit machine name popup
  this.editMachineNamePopupElement = $('.edit-machine-name-dialog-content');
  this.editMachineNameInputElement = utils.getVisibleInputElement( $('.edit-machine-name-dialog-input') );
  this.updateMachineNameButtonElement = $('[che-button-title="Update"]').$('button');
  this.closeMachineNameButtonElement = $('[che-button-title="Close"]').$('button');

};

module.exports = new StackDetails();
