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

exports.initialList = function(){



  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', function($httpBackend, cheAPIBuilder, cheHttpBackendProvider) {


      // setup tests objects
      var idWorkspace1 = 'idFlorent';
      var idWorkspace2 = 'idStevan';

      var nameWorkspace1 = 'florent';
      var nameWorkspace2 = 'stevan';

      var workspace1 = cheAPIBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();
      var workspace2 = cheAPIBuilder.getWorkspaceBuilder().withName(nameWorkspace2).withId(idWorkspace2).build();

      var wksp1Project1 = cheAPIBuilder.getProjectReferenceBuilder().withName('project-wk1-1').build();
      var wksp1Project2 = cheAPIBuilder.getProjectReferenceBuilder().withName('project-wk1-2').build();
      var wksp2Project1 = cheAPIBuilder.getProjectReferenceBuilder().withName('project-wk2-1').build();


      // create backend
      var cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      // setup it
      cheBackend.addWorkspaces([workspace1, workspace2]);
      cheBackend.addProjects(workspace1, [wksp1Project1, wksp1Project2]);
      cheBackend.addProjects(workspace2, [wksp2Project1]);
      cheBackend.setup();

    }]);
};

