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
 * Test of the CheGit
 */
describe('CheGit', function () {

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
    cheBackend = cheHttpBackend;
    apiBuilder = cheAPIBuilder;
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
   * Check that we're able to fetch local git url
   */
  it('Fetch local git url', function () {
      // setup tests objects
      var agentUrl = 'localhost:3232/wsagent/ext';
      var workspaceId = 'workspace123test';
      var projectPath = '/testProject';
      var localUrl = 'http://eclipse.org/che/git/f1/' + workspaceId + projectPath;
      var runtime =  {'links': [{'href': agentUrl, 'rel': 'wsagent'}]};
      var workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();

      cheBackend.addWorkspaces([workspace1]);

      // providing request
      // add test localUrl on Http backend
      cheBackend.addLocalGitUrl(workspaceId, encodeURIComponent(projectPath), localUrl);

      // setup backend
      cheBackend.setup();

      workspace.fetchWorkspaceDetails(workspaceId);
      httpBackend.expectGET('/api/workspace/' + workspaceId);


      // flush command
      httpBackend.flush();

      var factory = workspace.getWorkspaceAgent(workspaceId).getGit();

      cheBackend.getLocalGitUrl(workspaceId, encodeURIComponent(projectPath));

      // fetch localUrl
      factory.fetchLocalUrl(projectPath);

      // expecting GETs
      httpBackend.expectGET(agentUrl + '/git/read-only-url?projectPath=' + encodeURIComponent(projectPath));

      // flush command
      httpBackend.flush();

      // now, check url
      var url = factory.getLocalUrlByKey(projectPath);

      // check local url
      expect(localUrl).toEqual(url);
    }
  );

  /**
   * Check that we're able to fetch remote git urls
   */
  it('Fetch remote git urls', function () {
      // setup tests objects
      var agentUrl = 'localhost:3232/wsagent/ext';
      var workspaceId = 'workspace123test';
      var projectPath = '/testProject';
      var remoteArray = [{
        'url': 'https://github.com/test1',
        'name': 'test3'
      }, {
        'url': 'https://github.com/test2',
        'name': 'test1'
      }, {
        'url': 'https://github.com/test3',
        'name': 'test2'
      }];
      var runtime =  {'links': [{'href': agentUrl, 'rel': 'wsagent'}]};
      var workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();
      cheBackend.addWorkspaces([workspace1]);

      // add test remote array with urls on Http backend
      cheBackend.addRemoteGitUrlArray(workspaceId, encodeURIComponent(projectPath), remoteArray);

      // setup backend
      cheBackend.setup();

      workspace.fetchWorkspaceDetails(workspaceId);
      httpBackend.expectGET('/api/workspace/' + workspaceId);

      // flush command
      httpBackend.flush();

      var factory = workspace.getWorkspaceAgent(workspaceId).getGit();

      cheBackend.getRemoteGitUrlArray(workspaceId, encodeURIComponent(projectPath));

      // fetch localUrl
      factory.fetchRemoteUrlArray(projectPath);

      // expecting POSTs
      httpBackend.expectPOST(agentUrl + '/git/remote-list?projectPath=' + encodeURIComponent(projectPath));

      // flush command
      httpBackend.flush();

      // now, check url
      var urlArray = factory.getRemoteUrlArrayByKey(projectPath);
      remoteArray.sort(function (a, b) {
        if (a.name > b.name) {
          return 1;
        }
        if (a.name < b.name) {
          return -1;
        }
        return 0;
      });

      // check
      expect(remoteArray.join()).toEqual(urlArray.join());
    }
  );


});
