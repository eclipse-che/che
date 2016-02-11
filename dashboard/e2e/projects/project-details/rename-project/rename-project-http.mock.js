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

exports.updatedProjectName = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', function($httpBackend, cheAPIBuilder, cheHttpBackendProvider) {

      // setup tests objects
      var newProjectName = 'rename-tst';

      var projectDetails = {
        name: 'project-tst',
        description: 'test description',
        workspaceName: 'oleksii',
        workspaceId: 'idOleksii',
        visibility: 'private',
        typeName: 'Maven Project'
      };

      var newProjectDetails = {
        name: newProjectName,
        description: projectDetails.description,
        workspaceName: projectDetails.workspaceName,
        workspaceId: projectDetails.workspaceId,
        visibility: projectDetails.visibility,
        typeName: projectDetails.typeName
      };

      // create backend
      var cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      // setup it
      cheBackend.addProjectDetails(projectDetails);
      cheBackend.addProjectDetails(newProjectDetails);
      cheBackend.addUpdatedProjectName(projectDetails.workspaceId, projectDetails.name, newProjectDetails.name);
      cheBackend.setup();

    }]);
};

exports.updatedProjectDescription = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', function($httpBackend, cheAPIBuilder, cheHttpBackendProvider) {

      // setup tests objects
      var newProjectDescription = 'rename-description';

      var projectDetails = {
        name: 'project-tst',
        description: 'test description',
        workspaceName: 'oleksii',
        workspaceId: 'idOleksii',
        visibility: 'private',
        typeName: 'Maven Project'
      };

      var newProjectDetails = {
        name: projectDetails.name,
        description: newProjectDescription,
        workspaceName: projectDetails.workspaceName,
        workspaceId: projectDetails.workspaceId,
        visibility: projectDetails.visibility,
        typeName: projectDetails.typeName
      };

      // create backend
      var cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      // setup it
      cheBackend.addProjectDetails(projectDetails);
      cheBackend.addFetchProjectDetails(projectDetails.workspaceId, projectDetails.name);
      cheBackend.addUpdatedProjectDetails(projectDetails.workspaceId, projectDetails.name, newProjectDetails);
      cheBackend.setup();

    }]);
};

exports.updatedProjectVisibility = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', function($httpBackend, cheAPIBuilder, cheHttpBackendProvider) {

      // setup tests objects
      var newVisibility = 'public';

      var projectDetails = {
        name: 'project-tst',
        description: 'test description',
        workspaceName: 'oleksii',
        workspaceId: 'idOleksii',
        visibility: 'private',
        typeName: 'Maven Project'
      };

      // create backend
      var cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      // setup it
      cheBackend.addProjectDetails(projectDetails);
      cheBackend.addFetchProjectDetails(projectDetails.workspaceId, projectDetails.name);
      cheBackend.addSwitchVisibility(projectDetails.workspaceId, projectDetails.name, projectDetails.visibility);
      cheBackend.addSwitchVisibility(projectDetails.workspaceId, projectDetails.name, newVisibility);
      cheBackend.setup();

    }]);
};

