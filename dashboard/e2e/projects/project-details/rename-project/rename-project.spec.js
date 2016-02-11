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


describe('Rename project', function () {
  var renameProjectsPage;

  var renameProjectMock;

  beforeEach(function () {
    renameProjectsPage = require('./rename-project.po.js');
    renameProjectMock = require('./rename-project-http.mock.js');
  });


  it('launch browser', function () {
    browser.get('/');
    browser.waitForAngular();

  });

  //
  //it('updated project name', function () {
  //  browser.addMockModule('userDashboardMock', renameProjectMock.updatedProjectName);
  //  browser.get('http://localhost:5000/#/project/idOleksii/project-tst');
  //  browser.waitForAngular();
  //
  //
  //  // project input elements
  //  var nameInputElement = renameProjectsPage.projectNameInput;
  //
  //  // check that input elements is valid
  //  expect(nameInputElement.getAttribute('class')).toMatch('ng-valid');
  //  expect(nameInputElement.getAttribute('class')).toContain('ng-valid', 'ng-valid-required');
  //
  //  // we can see the button elements
  //  expect(renameProjectsPage.settingsTab.isDisplayed()).toBe(true);
  //  expect(renameProjectsPage.updateButton.isDisplayed()).toBe(true);
  //
  //  // click on the settings tab
  //  renameProjectsPage.settingsTab.click();
  //
  //  // we can see the input elements
  //  expect(renameProjectsPage.projectNameInput.isDisplayed()).toBe(true);
  //
  //  // check project name
  //  expect(nameInputElement.getAttribute('value')).toEqual('project-tst');
  //
  //  //rename project
  //  nameInputElement.clear();
  //  nameInputElement.sendKeys('rename-tst');
  //  renameProjectsPage.updateButton.click();
  //
  //  // check new project name
  //  expect(nameInputElement.getAttribute('value')).toEqual('rename-tst');
  //
  //});
  //
  //it('updated project description', function () {
  //  browser.addMockModule('userDashboardMock', renameProjectMock.updatedProjectDescription);
  //  browser.get('http://localhost:5000/#/project/idOleksii/project-tst');
  //  browser.waitForAngular();
  //
  //
  //  // project input elements
  //  var descriptionInputElement = renameProjectsPage.projectDescriptionInput;
  //
  //  // check that input elements is valid
  //  expect(descriptionInputElement.getAttribute('class')).toMatch('ng-valid');
  //
  //  // we can see the input elements
  //  expect(renameProjectsPage.projectDescriptionInput.isDisplayed()).toBe(true);
  //
  //  // check project description
  //  expect(descriptionInputElement.getAttribute('value')).toEqual('test description');
  //
  //  //rename project
  //  descriptionInputElement.clear();
  //  descriptionInputElement.sendKeys('rename-description');
  //  renameProjectsPage.updateButton.click();
  //
  //  // check new project description
  //  expect(descriptionInputElement.getAttribute('value')).toEqual('rename-description');
  //
  //
  //});
  //
  //it('updated project visibility', function () {
  //  browser.addMockModule('userDashboardMock', renameProjectMock.updatedProjectVisibility);
  //  browser.get('http://localhost:5000/#/project/idOleksii/project-tst');
  //  browser.waitForAngular();
  //
  //
  //  // project button elements
  //  var visibilityPublicWidgetElement = renameProjectsPage.visibilityPublicWidget;
  //  var visibilityPrivateWidgetElement = renameProjectsPage.visibilityPrivateWidget;
  //
  //  // we can see the button elements
  //  expect(visibilityPublicWidgetElement.isDisplayed()).toBe(true);
  //  expect(visibilityPrivateWidgetElement.isDisplayed()).toBe(true);
  //
  //  // check project visibility
  //  expect(renameProjectsPage.visibilityPublicButton.getAttribute('class')).toContain('cdvy-toggle-button-disabled');
  //  expect(renameProjectsPage.visibilityPrivateButton.getAttribute('class')).not.toContain('cdvy-toggle-button-disabled');
  //
  //  // check public visibility
  //  visibilityPublicWidgetElement.click();
  //
  //  // check new project visibility
  //  expect(renameProjectsPage.visibilityPublicButton.getAttribute('class')).not.toContain('cdvy-toggle-button-disabled');
  //  expect(renameProjectsPage.visibilityPrivateButton.getAttribute('class')).toContain('cdvy-toggle-button-disabled');
  //
  //  // check private visibility
  //  visibilityPrivateWidgetElement.click();
  //
  //  // check new project visibility
  //  expect(renameProjectsPage.visibilityPublicButton.getAttribute('class')).toContain('cdvy-toggle-button-disabled');
  //  expect(renameProjectsPage.visibilityPrivateButton.getAttribute('class')).not.toContain('cdvy-toggle-button-disabled');
  //
  //
  //});

});
