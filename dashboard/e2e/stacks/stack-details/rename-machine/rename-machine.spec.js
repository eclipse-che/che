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

describe('Stack details >', () => {
  let stackDetailsPageObject;
  let stackDetailsMock;

  beforeEach(() => {
    stackDetailsMock = require('./../stack-details.mock.js');
    stackDetailsPageObject = require('./../stack-details.po.js');
  });

  // todo
  describe('machine\'s name >', () => {

    it('can be replaced with valid name, single machine stack', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerimageStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on pencil
      machineConfigParts.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // set new name
      let newName = 'new-title-12345';
      stackDetailsPageObject.editMachineNameInputElement.clear();
      stackDetailsPageObject.editMachineNameInputElement.sendKeys(newName);
      expect(stackDetailsPageObject.editMachineNameInputElement.getAttribute('value')).toEqual(newName);

      // check if "Update" button is enabled
      expect(stackDetailsPageObject.updateMachineNameButtonElement.isEnabled()).toBeTruthy();

      // update machine's name
      stackDetailsPageObject.updateMachineNameButtonElement.click();

      // check if machine name is updated
      // note: title is in upper case
      let newNameRE = new RegExp(newName, 'i');
      expect(machineConfigParts.titleTextElement.getText()).toMatch(newNameRE);

      // check if stack can be saved
      expect(stackDetailsPageObject.toolbarSaveButtonElement.isEnabled()).toBeTruthy();
    });

    it('cannot be replaced with invalid name, single machine stack', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerimageStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on pencil
      machineConfigParts.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // set new name
      let newName = 'new-title-!@#$%';
      stackDetailsPageObject.editMachineNameInputElement.clear();
      stackDetailsPageObject.editMachineNameInputElement.sendKeys(newName);
      expect(stackDetailsPageObject.editMachineNameInputElement.getAttribute('value')).toEqual(newName);

      // check if "Update" button is disabled
      expect(stackDetailsPageObject.updateMachineNameButtonElement.isEnabled()).toBeFalsy();
    });

    it('can be replaced with valid unique name, multi machines stack', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on pencil
      machineConfigParts.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // set new name
      let newName = 'new-title-12345';
      stackDetailsPageObject.editMachineNameInputElement.clear();
      stackDetailsPageObject.editMachineNameInputElement.sendKeys(newName);
      expect(stackDetailsPageObject.editMachineNameInputElement.getAttribute('value')).toEqual(newName);

      // check if "Update" button is enabled
      expect(stackDetailsPageObject.updateMachineNameButtonElement.isEnabled()).toBeTruthy();

      // update machine's name
      stackDetailsPageObject.updateMachineNameButtonElement.click();

      // look for machine with new name
      let renamedMachineConfigElement = stackDetailsPageObject.getMachineConfigByName(newName);
      expect(renamedMachineConfigElement).toBeTruthy();

      // check if stack can be saved
      expect(stackDetailsPageObject.toolbarSaveButtonElement.isEnabled()).toBeTruthy();
    });

    it('cannot be replaced with invalid name, multi machines stack', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on pencil
      machineConfigParts.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // set new name
      let newName = 'new-title-!@#$%';
      stackDetailsPageObject.editMachineNameInputElement.clear();
      stackDetailsPageObject.editMachineNameInputElement.sendKeys(newName);
      expect(stackDetailsPageObject.editMachineNameInputElement.getAttribute('value')).toEqual(newName);

      // check if "Update" button is disabled
      expect(stackDetailsPageObject.updateMachineNameButtonElement.isEnabled()).toBeFalsy();
    });

    it('cannot be replaced with not unique name, multi machines stack', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let firstMachineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(firstMachineConfigElement).toBeTruthy();

      let firstMachineConfigParts = stackDetailsPageObject.splitMachineConfig(firstMachineConfigElement);

      // get second machine config subsection
      let secondMachineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(1);
      expect(secondMachineConfigElement).toBeTruthy();

      let secondMachineConfigParts = stackDetailsPageObject.splitMachineConfig(secondMachineConfigElement);

      // start editing first machine name
      firstMachineConfigParts.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // get second machine name
      secondMachineConfigParts.titleTextElement.getText().then((secondMachineName) => {
        // set same name to the first machine
        stackDetailsPageObject.editMachineNameInputElement.clear();
        stackDetailsPageObject.editMachineNameInputElement.sendKeys(secondMachineName);

        expect(stackDetailsPageObject.editMachineNameInputElement.getAttribute('value')).toEqual(secondMachineName);

        // check if "Update" button is disabled
        expect(stackDetailsPageObject.updateMachineNameButtonElement.isEnabled()).toBeFalsy();
      });
    });

  });

});
