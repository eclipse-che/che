/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

describe('List stacks >', () => {
  let listStacksPageObject;
  let listStacksMock;

  beforeEach(() => {
    listStacksMock = require('./list-stack.mock');
    listStacksPageObject = require('./list-stack.po');

    browser.addMockModule('userDashboardMock', listStacksMock.listStacksTheeEntries);
    browser.get('/');
    browser.setLocation('/stacks');

  });

  describe('click on stack\'s title >', () => {

    it('should redirect to stack details', () => {

      // get stack's row by name
      let listItemElement = listStacksPageObject.getListItemElementByName('testStack2');

      expect(listItemElement).toBeTruthy();
      expect(listItemElement.isDisplayed()).toBeTruthy();

      // click on title redirects to details page
      let listItemElementCells = listStacksPageObject.splitStackItemByCell(listItemElement);
      expect(listItemElementCells.title.isDisplayed()).toBeTruthy();
      listItemElementCells.title.click();

      expect(browser.getCurrentUrl()).toMatch('stack/testStackId2');

    });

  });

});
