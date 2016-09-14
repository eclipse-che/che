/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Test of the CheSvn
 */
describe('CheSvn', function () {

  /**
   * User Factory for the test
   */
  var factory;

  /**
   * API builder.
   */
  var apiBuilder;

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
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (cheWorkspace, cheAPIBuilder, cheHttpBackend) {
    workspace = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
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
   * Check that we're able to fetch remote svn url
   */
  it('Fetch remote svn url', function () {
      // setup tests objects
      var agentUrl = 'localhost:3232/wsagent/ext';
      var workspaceId = 'workspace456test';
      var projectPath = '/testSvnProject';
      var remoteSvnUrl = 'https://svn.apache.org' + projectPath;
      var runtime =  {'links': [{'href': agentUrl, 'rel': 'wsagent'}]};
      var workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();

      cheBackend.addWorkspaces([workspace1]);

      // providing request
      // add test remote svn url on http backend
      cheBackend.addRemoteSvnUrl(workspaceId, encodeURIComponent(projectPath), remoteSvnUrl);

      // setup backend
      cheBackend.setup();

      workspace.fetchWorkspaceDetails(workspaceId);
      httpBackend.expectGET('/api/workspace/' + workspaceId);

      // flush command
      httpBackend.flush();

      var factory = workspace.getWorkspaceAgent(workspaceId).getSvn();

      cheBackend.getRemoteSvnUrl(workspaceId, encodeURIComponent(projectPath));

      // fetch remote url
      factory.fetchRemoteUrl(workspaceId, projectPath);

      // expecting POST
      httpBackend.expectPOST(agentUrl + '/svn/info?workspaceId='+workspaceId);

      // flush command
      httpBackend.flush();

      // now, check
      var repo = factory.getRemoteUrlByKey(workspaceId, projectPath);

      // check local url
      expect(remoteSvnUrl).toEqual(repo.url);
    }
  );


});
