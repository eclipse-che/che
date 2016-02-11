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


describe('The projects view', function () {
  var listProjectsPage;


  var projectsMock;

  beforeEach(function () {
    listProjectsPage = require('./list-projects.po.js');
    projectsMock = require('./list-projects-http.mock');
  });


  it('should include my 2 workspaces', function() {
    browser.addMockModule('userDashboardMock', projectsMock.projectsList);
    browser.get('/#/projects');
    browser.waitForAngular();
    expect(listProjectsPage.projectsWorkspaceElements.count()).toEqual(2);

  });

  it('should include only 1 workspace', function() {
    browser.addMockModule('userDashboardMock', projectsMock.projectsList2);
    browser.get('/#/projects');
    browser.waitForAngular();
    expect(listProjectsPage.projectsWorkspaceElements.count()).toEqual(1);
    expect(listProjectsPage.projectElements.count()).toEqual(2);
    expect(listProjectsPage.noProjectsLabel.isDisplayed()).toBe(false);
  });

  it('should not have any projects', function() {
    browser.addMockModule('userDashboardMock', projectsMock.emptyProjectsList);
    browser.get('/#/projects');
    browser.waitForAngular();

    expect(listProjectsPage.projectsWorkspaceElements.count()).toEqual(0);
    expect(listProjectsPage.projectElements.count()).toEqual(0);
    //expect(listProjectsPage.noProjectsLabel.isDisplayed()).toBe(false);
  });

});
