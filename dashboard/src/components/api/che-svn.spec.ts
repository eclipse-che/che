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
 * Test of the CheSvn
 */
describe('CheSvn', function () {

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
  it('Fetch remote svn url', () => {
    // setup tests objects
    const workspaceId = 'workspace456test';
    const projectPath = '/testSvnProject';
    const remoteSvnUrl = 'https://svn.apache.org' + projectPath;
    const agentUrl = 'http://172.17.0.1:33441/api';
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
    // add test remote svn url on http backend
    cheBackend.addRemoteSvnUrl(workspaceId, encodeURIComponent(projectPath), remoteSvnUrl);

    // setup backend
    cheBackend.setup();

    workspace.fetchWorkspaceDetails(workspaceId);
    httpBackend.expectGET('/api/workspace/' + workspaceId);

    // flush command
    httpBackend.flush();

    const factory = workspace.getWorkspaceAgent(workspaceId).getSvn();

    cheBackend.getRemoteSvnUrl(workspaceId, encodeURIComponent(projectPath));

    // fetch remote url
    factory.fetchRemoteUrl(workspaceId, projectPath);

    // expecting POST
    httpBackend.expectPOST(agentUrl + '/svn/info?workspaceId=' + workspaceId);

    // flush command
    httpBackend.flush();

    // now, check
    const repo = factory.getRemoteUrlByKey(workspaceId, projectPath);

    // check local url
    expect(remoteSvnUrl).toEqual(repo.url);
  });


});
