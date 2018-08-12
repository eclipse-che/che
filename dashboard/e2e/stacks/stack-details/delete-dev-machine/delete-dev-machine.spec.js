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
  let utils;

  beforeEach(() => {
    stackDetailsMock = require('./../stack-details.mock.js');
    stackDetailsPageObject = require('./../stack-details.po.js');
    utils = require('../../../utils');
  });

  describe('one machine (dev) in stack >', () => {

    it('cannot be removed', () => {
      browser.addMockModule('userDashboardMock', stackDetailsMock.dockerimageStack);
      browser.get('/');

      // go to stack details page
      let stackId = 'testStackId';
      browser.setLocation('/stack/' + stackId);

      // get the machine configuration elements map
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByIndex(0);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on machine's title to expand its config
      expect(machineConfigParts.titleTextElement.isDisplayed()).toBeTruthy();
      machineConfigParts.titleTextElement.click();

      // check if current machine has ws-agent activated
      expect(machineConfigParts.isDevSwitchElement.getAttribute('class')).toMatch('md-checked');

      // click on 'Delete' button
      expect(machineConfigParts.titleDeleteElement.isDisplayed()).toBeTruthy();
      machineConfigParts.titleDeleteElement.click();

      // 'Remove machine' popup is shown
      expect(stackDetailsPageObject.deleteDevMachinePopupElement.isDisplayed()).toBeTruthy();
      // but "Delete" button is not there
      expect(stackDetailsPageObject.deleteDevMachineDeleteButtonElement.isPresent()).toBeFalsy();
    });

  });

  describe('two machines in stack >', () => {

    it('machine (not dev) can be removed', () => {
      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      const stackId = 'testStackId';
      browser.setLocation('/stack/' + stackId);

      // get machine (not dev)
      const notDevMachineName = 'db';
      let machineConfigElement = stackDetailsPageObject.getMachineConfigByName(notDevMachineName);
      expect(machineConfigElement).toBeTruthy();

      let machineConfigParts = stackDetailsPageObject.splitMachineConfig(machineConfigElement);

      // click on machine's title to expand its config
      expect(machineConfigParts.titleTextElement.isDisplayed()).toBeTruthy();
      machineConfigParts.titleTextElement.click();

      // check if current machine doesn't have ws-agent activated
      expect(machineConfigParts.isDevSwitchElement.getAttribute('class')).not.toMatch('md-checked');

      // try to remove this machine
      expect(machineConfigParts.titleDeleteElement.isDisplayed()).toBeTruthy();
      machineConfigParts.titleDeleteElement.click();

      // 'Remove machine' popup is shown
      expect(stackDetailsPageObject.deleteNotDevMachinePopupElement.isDisplayed()).toBeTruthy();
      // 'Delete' button is visible
      expect(stackDetailsPageObject.deleteNotDevMachineDeleteButtonElement.isDisplayed()).toBeTruthy();

      // click on 'Delete' button
      stackDetailsPageObject.deleteNotDevMachineDeleteButtonElement.click();

      const deletedMachineConfigElement = stackDetailsPageObject.getMachineConfigByName(notDevMachineName);
      expect(deletedMachineConfigElement.isPresent()).toBeFalsy();

    });

    it('machine (dev) can be removed', () => {
      browser.addMockModule('userDashboardMock', stackDetailsMock.composefileStack);
      browser.get('/');

      // go to stack details page
      const stackId = 'testStackId';
      browser.setLocation('/stack/' + stackId);

      // get first machine (dev)
      const firstMachineName = 'dev-machine';
      const firstMachineConfigElement = stackDetailsPageObject.getMachineConfigByName(firstMachineName);
      expect(firstMachineConfigElement).toBeTruthy();

      const firstMachineConfigParts = stackDetailsPageObject.splitMachineConfig(firstMachineConfigElement);

      // click on first machine's title to expand its config
      expect(firstMachineConfigParts.titleTextElement.isDisplayed()).toBeTruthy();
      firstMachineConfigParts.titleTextElement.click();

      // check if first machine has ws-agent activated
      expect(firstMachineConfigParts.isDevSwitchElement.getAttribute('class')).toMatch('md-checked');

      // try to remove this machine
      expect(firstMachineConfigParts.titleDeleteElement.isDisplayed()).toBeTruthy();
      firstMachineConfigParts.titleDeleteElement.click();

      // 'Remove machine' popup is shown
      expect(stackDetailsPageObject.deleteDevMachinePopupElement.isDisplayed()).toBeTruthy();
      // 'Delete' button is visible
      expect(stackDetailsPageObject.deleteDevMachineDeleteButtonElement.isDisplayed()).toBeTruthy();
      // 'Delete' button is disabled
      expect(stackDetailsPageObject.deleteDevMachineDeleteButtonElement.isEnabled()).toBeFalsy();

      // look for radio button for second machine
      const secondMachineName = 'db';
      let notDevMachineRadioButtonElement = utils.getRadioButtonByLabel(stackDetailsPageObject.deleteDevMachinePopupElement, secondMachineName.toUpperCase());
      expect(notDevMachineRadioButtonElement.isDisplayed()).toBeTruthy();

      // check second machine name radio button
      notDevMachineRadioButtonElement.click();

      // 'Delete' button is enabled
      expect(stackDetailsPageObject.deleteDevMachineDeleteButtonElement.isEnabled()).toBeTruthy();

      // click on 'Delete' button
      stackDetailsPageObject.deleteDevMachineDeleteButtonElement.click();

      // first machine config is deleted
      expect(firstMachineConfigElement.isPresent()).toBeFalsy();

      // get second machine config
      const secondMachineConfigElement = stackDetailsPageObject.getMachineConfigByName(secondMachineName);
      expect(secondMachineConfigElement).toBeTruthy();

      const secondMachineConfigParts = stackDetailsPageObject.splitMachineConfig(secondMachineConfigElement);


      // click on second machine's title to expand its config
      expect(secondMachineConfigParts.titleTextElement.isDisplayed()).toBeTruthy();
      secondMachineConfigParts.titleTextElement.click();

      // check if second machine has ws-agent activated
      expect(secondMachineConfigParts.isDevSwitchElement.getAttribute('class')).toMatch('md-checked');

    });

  });

});
