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
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // click on pencil
      machineElements.titleEditElement.click();

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
      expect(machineElements.titleTextElement.getText()).toMatch(newNameRE);

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
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // click on pencil
      machineElements.titleEditElement.click();

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
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // click on pencil
      machineElements.titleEditElement.click();

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
      let renamedMachineElements = stackDetailsPageObject.getMachineElementByName(newName);
      expect(renamedMachineElements).toBeTruthy();

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
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // click on pencil
      machineElements.titleEditElement.click();

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
      let firstMachineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(firstMachineElements).toBeTruthy();

      // get second machine config subsection
      let secondMachineElements = stackDetailsPageObject.getMachineElementByIndex(1);
      expect(secondMachineElements).toBeTruthy();

      // start editing first machine name
      firstMachineElements.titleEditElement.click();

      // popup should be shown
      expect(stackDetailsPageObject.editMachineNamePopupElement.isDisplayed()).toBeTruthy();

      // get second machine name
      secondMachineElements.titleTextElement.getText().then((secondMachineName) => {
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
