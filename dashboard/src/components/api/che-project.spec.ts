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
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheWorkspace} from './workspace/che-workspace.factory';
import {CheHttpBackend} from './test/che-http-backend';

describe('CheProject', function () {
  /**
   * Workspace for the test
   */
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
   * API builder.
   */
  let apiBuilder;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(function (cheAPIBuilder: CheAPIBuilder,
                              cheWorkspace: CheWorkspace,
                              cheHttpBackend: CheHttpBackend) {
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
  it('Fetch project details', () => {
      // setup tests objects
      const testProjectDetails = {
        name: 'project-tst',
        description: 'test description',
        workspaceName: 'qwerty',
        workspaceId: 'workspace12345'
      };
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
      cheBackend.addWorkspaces([apiBuilder.getWorkspaceBuilder().withId(testProjectDetails.workspaceId).withRuntime(runtime).build()]);

      // providing request
      // add project details on http backend
      cheBackend.addProjectDetails(testProjectDetails);

      // setup backend
      cheBackend.setup();

      // fetch runtime
      workspace.fetchWorkspaceDetails(testProjectDetails.workspaceId);
      httpBackend.expectGET('/api/workspace/' + testProjectDetails.workspaceId);

      // flush command
      httpBackend.flush();

      const factory = workspace.getWorkspaceAgent(testProjectDetails.workspaceId).getProject();

      // fetch remote url
      factory.fetchProjectDetails(testProjectDetails.workspaceId, '/' + testProjectDetails.name);

      // expecting GET
      httpBackend.expectGET(agentUrl + '/project/' + testProjectDetails.name);

      // flush command
      httpBackend.flush();

      // now, check
      const projectDetails = factory.getProjectDetailsByKey('/' + testProjectDetails.name);

      // check project details
      expect(projectDetails.name).toEqual(testProjectDetails.name);
      expect(projectDetails.description).toEqual(testProjectDetails.description);
      expect(projectDetails.workspaceName).toEqual(testProjectDetails.workspaceName);
      expect(projectDetails.workspaceId).toEqual(testProjectDetails.workspaceId);
    }
  );

});
