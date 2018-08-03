/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheWorkspace} from './workspace/che-workspace.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the CheGit
 */
describe('CheGit', function () {

  /**
   * API builder.
   */
  let apiBuilder;

  let workspace;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  /**
   * Che backend
   */
  let cheBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (cheWorkspace: CheWorkspace,
                              cheAPIBuilder: CheAPIBuilder,
                              cheHttpBackend: CheHttpBackend) {
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
  it('Fetch local git url', () => {
    // setup tests objects
    const agentUrl = 'http://172.17.0.1:33441/api';
    const workspaceId = 'workspace123test';
    const projectPath = '/testProject';
    const localUrl = 'https://eclipse.org/che/git/f1/' + workspaceId + projectPath;
    const agentWsUrl = 'ws://172.17.0.1:33441/wsagent';
    const runtime = {
      'links': [{'rel': 'wsagent', 'href': agentUrl}],
      'machines': {
        'dev-machine': {
          'servers': {
            'wsagent/ws': {'url': agentWsUrl},
            'wsagent/http': {'url': agentUrl}
          }
        }
      }
    };
    cheBackend.addWorkspaces([apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build()]);

    // providing request
    // add test localUrl on Http backend
    cheBackend.addLocalGitUrl(workspaceId, encodeURIComponent(projectPath), localUrl);

    // setup backend
    cheBackend.setup();

    workspace.fetchWorkspaceDetails(workspaceId);
    httpBackend.expectGET('/api/workspace/' + workspaceId);


    // flush command
    httpBackend.flush();

    const factory = workspace.getWorkspaceAgent(workspaceId).getGit();

    cheBackend.getLocalGitUrl(workspaceId, encodeURIComponent(projectPath));

    // fetch localUrl
    factory.fetchLocalUrl(projectPath);

    // expecting GETs
    httpBackend.expectGET(agentUrl + '/git/read-only-url?projectPath=' + encodeURIComponent(projectPath));

    // flush command
    httpBackend.flush();

    // now, check url
    const url = factory.getLocalUrlByKey(projectPath);

    // check local url
    expect(localUrl).toEqual(url);
  });

  /**
   * Check that we're able to fetch remote git urls
   */
  it('Fetch remote git urls', function () {
    // setup tests objects
    const agentUrl = 'http://172.17.0.1:33441/api';
    const workspaceId = 'workspace123test';
    const projectPath = '/testProject';
    const remoteArray = [{
      'url': 'https://github.com/test1',
      'name': 'test3'
    }, {
      'url': 'https://github.com/test2',
      'name': 'test1'
    }, {
      'url': 'https://github.com/test3',
      'name': 'test2'
    }];
    const agentWsUrl = 'ws://172.17.0.1:33441/wsagent';
    const runtime = {
      'links': [{'rel': 'wsagent', 'href': agentUrl}],
      'machines': {
        'dev-machine': {
          'servers': {
            'wsagent/ws': {'url': agentWsUrl},
            'wsagent/http': {'url': agentUrl}
          }
        }
      }
    };

    const workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();
    cheBackend.addWorkspaces([workspace1]);

    // add test remote array with urls on Http backend
    cheBackend.addRemoteGitUrlArray(workspaceId, encodeURIComponent(projectPath), remoteArray);

    // setup backend
    cheBackend.setup();

    workspace.fetchWorkspaceDetails(workspaceId);
    httpBackend.expectGET('/api/workspace/' + workspaceId);

    // flush command
    httpBackend.flush();

    const factory = workspace.getWorkspaceAgent(workspaceId).getGit();

    cheBackend.getRemoteGitUrlArray(workspaceId, encodeURIComponent(projectPath));

    // fetch localUrl
    factory.fetchRemoteUrlArray(projectPath);

    // expecting POSTs
    httpBackend.expectPOST(agentUrl + '/git/remote-list?projectPath=' + encodeURIComponent(projectPath));

    // flush command
    httpBackend.flush();

    // now, check url
    const urlArray = factory.getRemoteUrlArrayByKey(projectPath);

    // check
    expect(remoteArray.join()).toEqual(urlArray.join());
  });


});
