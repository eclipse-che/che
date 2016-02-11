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

exports.listPermissions = function(){

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', function($httpBackend, cheAPIBuilder, cheHttpBackendProvider) {

      // setup tests objects
      var idWorkspace1 = 'idFlorent';
      var nameWorkspace1 = 'florent';
      var projectName = 'project-wk1-1';

      var workspace1 = cheAPIBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();

      var projectReference = cheAPIBuilder.getProjectReferenceBuilder().withName(projectName).build();

      var projectDetails = cheAPIBuilder.getProjectDetailsBuilder().withName(projectName).withWorkspaceId(idWorkspace1).withWorkspaceName(nameWorkspace1).withVisibility('public').build();


      // add users
      var idOther1 = 'idUser1';
      var idOther2 = 'idUser2';
      var idGroup1 = 'idGroup1';

      var permissionUser1 = cheAPIBuilder.getProjectPermissionsBuilder().withUser(idOther1, ['read', 'build']).build();
      var permissionUser2 = cheAPIBuilder.getProjectPermissionsBuilder().withUser(idOther2, ['read', 'write']).build();

      var permissionGroup1 = cheAPIBuilder.getProjectPermissionsBuilder().withGroup(idGroup1, ['build']).build();

      // create backend
      var cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      // setup it
      cheBackend.addWorkspaces([workspace1]);
      cheBackend.addProjects(workspace1, [projectReference]);
      cheBackend.addProjectDetails(projectDetails);
      cheBackend.addPermissions(idWorkspace1, projectName, [permissionUser1, permissionUser2, permissionGroup1]);
      cheBackend.setup();

    }]);
};

