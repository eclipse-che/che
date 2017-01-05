/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

describe('CheProject', function () {

  /**
   * Project Factory for the test
   */
  var factory;

  /**
   * Workspace for the test
   */
  var workspace;

  /**
   * Backend for handling http operations
   */
  var httpBackend;

  /**
   * Che backend
   */
  var cheBackend;

  /**
   * API builder.
   */
  var apiBuilder;


  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (cheAPIBuilder, cheWorkspace, cheHttpBackend) {
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    workspace = cheWorkspace;
    httpBackend = cheHttpBackend.getHttpBackend();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(function () {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  /**
   * Check that we're able to fetch project details
   */
  it('Fetch project details', function () {
      // setup tests objects
      var testProjectDetails = {
        name: 'project-tst',
        description: 'test description',
        workspaceName: 'qwerty',
        workspaceId: 'workspace12345'
      };
      let agentUrl = 'localhost:3232/wsagent/ext';
      var runtime =  {'links': [{'href': agentUrl, 'rel': 'wsagent'}]};
      var workspace1 = apiBuilder.getWorkspaceBuilder().withId(testProjectDetails.workspaceId).withRuntime(runtime).build();

      cheBackend.addWorkspaces([workspace1]);

      // providing request
      // add project details on http backend
      cheBackend.addProjectDetails(testProjectDetails);

      // setup backend
      cheBackend.setup();

      //fetch runtime
      workspace.fetchWorkspaceDetails(testProjectDetails.workspaceId);
      httpBackend.expectGET('/api/workspace/' + testProjectDetails.workspaceId);

      // flush command
      httpBackend.flush();

      var factory = workspace.getWorkspaceAgent(testProjectDetails.workspaceId).getProject();

      // fetch remote url
      factory.fetchProjectDetails(testProjectDetails.workspaceId, '/' + testProjectDetails.name);

      // expecting GET
      httpBackend.expectGET(agentUrl + '/project/' + testProjectDetails.name);

      // flush command
      httpBackend.flush();

      // now, check
      var projectDetails = factory.getProjectDetailsByKey('/' + testProjectDetails.name);

      // check project details
      expect(projectDetails.name).toEqual(testProjectDetails.name);
      expect(projectDetails.description).toEqual(testProjectDetails.description);
      expect(projectDetails.workspaceName).toEqual(testProjectDetails.workspaceName);
      expect(projectDetails.workspaceId).toEqual(testProjectDetails.workspaceId);
    }
  );

});
