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

describe('List workspaces >', () => {
  let pageObject;
  let mock;
  let workspacesList;

  beforeEach(() => {
    mock = require('./list-workspaces.mock');
    pageObject = require('./list-workspaces.po');

    workspacesList = mock.buildWorkspacesData();

    browser.addMockModule('userDashboardMock', mock.listWorkspacesMock, workspacesList);
    browser.get('/');
    browser.setLocation('/workspaces');

  });

  describe(`workspaces list >`, () => {

    it(`should be present and visible`, () => {
      const listElement = pageObject.listElement;

      expect(listElement.isPresent()).toBeTruthy();
      expect(listElement.isDisplayed()).toBeTruthy();
    });

    it(`should contain correct number of items`, () => {
      const itemElements = pageObject.listItemElements;

      expect(itemElements.count()).toEqual(workspacesList.length);
    });

  });

  /**
   * https://github.com/eclipse/che/issues/4803
   */
  describe('bulk operation along with filtration items > ', () => {

    it(`should show correct message if only one item is left selected in the list`, () => {
      // check checkboxes for first 5 workspaces
      const numberToCheck = 5;
      for (let i = 0; i < numberToCheck; i++) {
        const workspace = workspacesList[i];

        const itemElement = pageObject.getListItemElementByName(workspace.name);
        const checkboxElem = pageObject.getItemCheckbox(itemElement);
        checkboxElem.click();
      }

      const listItemCheckedElements = pageObject.getListItemCheckedElement();
      expect(listItemCheckedElements.count()).toEqual(numberToCheck);

      // perform filtration by name
      pageObject.searchInputElement.sendKeys('4');

      // there should be left two visible items
      expect(pageObject.listItemElements.count()).toEqual(2);

      // and only one of them should be selected
      expect(pageObject.getListItemCheckedElement().count()).toEqual(1);

      // try to delete selected item
      expect(pageObject.deleteWorkspaceButtonElement.isDisplayed()).toBeTruthy();
      pageObject.deleteWorkspaceButtonElement.click();

      // check confirmation's popup message
      expect(pageObject.popupDeleteButtonElement.isDisplayed()).toBeTruthy();
      const popupMessage = 'Would you like to delete this selected workspace?';
      expect(pageObject.popupElement.getText()).toMatch(popupMessage);
    });

    it(`should show correct message if couple items are left selected in the list`, () => {
      // check checkboxes for 5 workspaces in the middle of the list
      const startIndex = 7;
      const numberToCheck = 5;
      for (let i = startIndex; i < startIndex + numberToCheck; i++) {
        const workspace = workspacesList[i];

        const itemElement = pageObject.getListItemElementByName(workspace.name);
        const checkboxElem = pageObject.getItemCheckbox(itemElement);
        checkboxElem.click();
      }

      const listItemCheckedElements = pageObject.getListItemCheckedElement();
      expect(listItemCheckedElements.count()).toEqual(numberToCheck);

      // perform filtration by name
      pageObject.searchInputElement.sendKeys('1');

      const itemsVisibleLeft = 6;

      // check the number of visible items which are left in the list
      expect(pageObject.listItemElements.count()).toEqual(itemsVisibleLeft);

      const itemsSelectedLeft = 2;

      // check the number of selected items which are left in the list
      expect(pageObject.getListItemCheckedElement().count()).toEqual(itemsSelectedLeft);

      // try to delete selected item
      expect(pageObject.deleteWorkspaceButtonElement.isDisplayed()).toBeTruthy();
      pageObject.deleteWorkspaceButtonElement.click();

      // check confirmation's popup message
      expect(pageObject.popupDeleteButtonElement.isDisplayed()).toBeTruthy();
      const popupMessage = `Would you like to delete these ${itemsSelectedLeft} workspaces?`;
      expect(pageObject.popupElement.getText()).toMatch(popupMessage);
    });

  });

});
