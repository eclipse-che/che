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

      // click on title redirects to details page
      listItemElement.title.click();
      expect(browser.getCurrentUrl()).toMatch('stack/testStackId2');

    });

  });

});
