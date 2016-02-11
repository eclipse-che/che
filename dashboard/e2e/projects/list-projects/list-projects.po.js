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

/**
 * This file uses the Page Object pattern to define the main page for tests
 * https://docs.google.com/presentation/d/1B6manhG0zEXkC-H-tPo2vwU06JhL8w9-XCF9oehXzAQ
 * @author Florent Benoit
 */

var MainPage = function() {
  this.projectsWorkspaceElements = element.all(by.repeater('(workspaceId, projects) in listProjectsCtrl.projectsPerWorkspace'));
  this.projectElements = element.all(by.repeater('project in projects'));
  this.noProjectsLabel = element(by.css('.project-list-empty'));


  // COG for displaying dropdown
  this.cogElement = element(by.css('.projects-list-projects-cog'));

  this.dropDownWorkspaceElement = element(by.cssContainingText('a', 'Filter Workspace'));

  this.filterWorkspacePanelFistCheckBox = element.all(by.css('.projects-list-projects-select-workspace')).first().element(by.css('md-checkbox'));
  this.filterWorkspacePanelAllCheckBoxes = element.all(by.css('.projects-list-projects-select-workspace')).all(by.css('md-checkbox'));

};

module.exports = new MainPage();
