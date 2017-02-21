'use strict';

describe('Stack details >', () => {
  let stackDetailsPageObject;
  let stackDetailsMock;

  beforeEach(() => {
    stackDetailsMock = require('./../stack-details.mock.js');
    stackDetailsPageObject = require('./../stack-details.po.js');
  });

  describe('stack\'s source >', () => {

    it('is able to be changed for stack based on dockerimage', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerimageStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // unfold machine config subsection
      machineElements.titleOpenElement.click();

      expect(machineElements.sourceRowElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineElements.sourceInputElement.getAttribute('value').then((origSource) => {

        expect(stackDetailsPageObject.runtimeRecipeLocationElement.getText()).toEqual(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineElements.sourceInputElement.clear();
      machineElements.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineElements.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeLocationElement.getText()).toEqual(newSource);

    });

    it('is able to be changed for stack based on dokerfile', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerfileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // unfold machine config subsection
      machineElements.titleOpenElement.click();

      expect(machineElements.sourceRowElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineElements.sourceInputElement.getAttribute('value').then((origSource) => {

        // show machine's recipe
        stackDetailsPageObject.runtimeRecipeShowButtonElement.click();

        expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineElements.sourceInputElement.clear();
      machineElements.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineElements.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(newSource);

    });

    it('is able to be changed for stack based on compose file', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineElements = stackDetailsPageObject.getMachineElementByIndex(0);
      expect(machineElements).toBeTruthy();

      // unfold machine config subsection
      machineElements.titleOpenElement.click();

      expect(machineElements.sourceRowElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineElements.sourceInputElement.getAttribute('value').then((origSource) => {

        // show machine's recipe
        stackDetailsPageObject.runtimeRecipeShowButtonElement.click();

        expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineElements.sourceInputElement.clear();
      machineElements.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineElements.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(newSource);

    });

  });

});
