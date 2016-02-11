/*******************************************************************************
 * Copyright (c) 2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

'use strict';

describe('Manage access of a project', function () {
  var accessProjectsPage;

  var accessProjectMock;

  beforeEach(function () {
    accessProjectsPage = require('./project-details-permissions.po.js');
    accessProjectMock = require('./project-details-permissions.mock.js');
  });


  it('launch browser', function() {
    browser.get('/');
    browser.waitForAngular();

  });

  //
  //it('check permissions name', function() {
  //  browser.addMockModule('userDashboardMock', accessProjectMock.listPermissions);
  //  browser.get('http://localhost:5000/#/project/idFlorent/project-wk1-1');
  //  browser.waitForAngular();
  //
  //  expect(accessProjectsPage.accessTab.isDisplayed()).toBe(true);
  //
  //
  //  // click on the access tab
  //  accessProjectsPage.accessTab.click();
  //
  //  // check that we have expected permissions number
  //  expect(accessProjectsPage.permissionsElement.count()).toEqual(3);
  //
  //});


});
