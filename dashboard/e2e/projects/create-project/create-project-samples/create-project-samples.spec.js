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

describe('create workspace with project', () => {
  let createProjectPageObject;
  let createProjectMock;
  let utils;

  beforeEach(() => {
    createProjectPageObject = require('../create-project.po');
    createProjectMock = require('../create-project.mock');
    utils = require('../../../utils');
  });

  describe('bugfix https://github.com/eclipse/che/issues/4247', () => {

    it('should be selected the very first template in samples list', () => {
      browser.addMockModule('userDashboardMock', createProjectMock.noWorkspaces);
      browser.get('/');

      // user is redirected to create-project view
      expect(browser.getCurrentUrl()).toMatch('/create-project');

      // step 1. scroll to the very bottom
      expect(createProjectPageObject.containerElement.isDisplayed()).toBeTruthy();
      utils.scrollToBottom(createProjectPageObject.containerElement);

      // step 2. there is list of templates
      expect(createProjectPageObject.samplesListElement.isDisplayed()).toBeTruthy();

      // step 3. check if first item in list is selected
      let firstListItemElement = createProjectPageObject.getSamplesListItemElementByIndex(0);
      expect(firstListItemElement.isDisplayed()).toBeTruthy();
      let firstListItemIsSelected = createProjectPageObject.isListItemSelected(firstListItemElement);
      expect(firstListItemIsSelected).toBeTruthy();

      // try to get Dashboard page and repeat all 3 steps.
      browser.setLocation('/');

      // user is redirected to create-project view
      expect(browser.getCurrentUrl()).toMatch('/create-project');

      // step 1. scroll to the very bottom
      expect(createProjectPageObject.containerElement.isDisplayed()).toBeTruthy();
      utils.scrollToBottom(createProjectPageObject.containerElement);

      // step 2. there is list of templates
      expect(createProjectPageObject.samplesListElement.isDisplayed()).toBeTruthy();

      // step 3. check if first item in list is selected
      let firstListItemElement2 = createProjectPageObject.getSamplesListItemElementByIndex(0);
      expect(firstListItemElement2.isDisplayed()).toBeTruthy();
      let firstListItemIsSelected2 = createProjectPageObject.isListItemSelected(firstListItemElement);
      expect(firstListItemIsSelected2).toBeTruthy();
    });

  });

});
