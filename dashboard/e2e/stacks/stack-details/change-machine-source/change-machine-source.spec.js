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

  describe('stack\'s source >', () => {

    it('is able to be changed for stack based on dockerimage', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerimageStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement.isDisplayed()).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // unfold machine config subsection
      machineConfigParts.titleOpenElement.click();

      expect(machineConfigParts.sourceFormElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineConfigParts.sourceInputElement.getAttribute('value').then((origSource) => {

        expect(stackDetailsPageObject.runtimeRecipeLocationElement.getText()).toEqual(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineConfigParts.sourceInputElement.clear();
      machineConfigParts.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineConfigParts.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeLocationElement.getText()).toEqual(newSource);

    });

    it('is able to be changed for stack based on dokerfile', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerfileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // unfold machine config subsection
      machineConfigParts.titleOpenElement.click();

      expect(machineConfigParts.sourceFormElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineConfigParts.sourceInputElement.getAttribute('value').then((origSource) => {

        // show machine's recipe
        stackDetailsPageObject.runtimeRecipeShowButtonElement.click();

        expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineConfigParts.sourceInputElement.clear();
      machineConfigParts.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineConfigParts.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(newSource);

    });

    it('is able to be changed for stack based on compose file', () => {
      let stackId = 'testStackId';

      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      browser.setLocation('/stack/' + stackId);

      // get first machine config subsection
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // unfold machine config subsection
      machineConfigParts.titleOpenElement.click();

      expect(machineConfigParts.sourceFormElement.isDisplayed()).toBeTruthy();

      // get original machine's source
      machineConfigParts.sourceInputElement.getAttribute('value').then((origSource) => {

        // show machine's recipe
        stackDetailsPageObject.runtimeRecipeShowButtonElement.click();

        expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(origSource);
      });

      // set new source
      let newSource = 'codenvy/node';
      machineConfigParts.sourceInputElement.clear();
      machineConfigParts.sourceInputElement.sendKeys(newSource);

      // check whether it is applied successfully
      expect(machineConfigParts.sourceInputElement.getAttribute('value')).toEqual(newSource);
      expect(stackDetailsPageObject.runtimeRecipeEditorElement.getText()).toMatch(newSource);

    });

  });

});
