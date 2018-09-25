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
 * Test of the CheProjectType
 */
describe('CheProjectType', function(){

  /**
   * Workspace for the test
   */
  let workspace;

  /**
   * API builder.
   */
  let apiBuilder;

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
  beforeEach(inject(function(cheWorkspace: CheWorkspace,
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
   * Check that we're able to fetch project types
   */
  it('Fetch project types', () => {
    // setup tests objects
    const attributeLanguageJava = apiBuilder.getProjectTypeAttributeDescriptorBuilder().withValues(['java']).withRequired(true).withDescription('language').withName('language').build();
    const mavenType = apiBuilder.getProjectTypeBuilder().withId('maven').withDisplayname('Maven project').withAttributeDescriptors([attributeLanguageJava]).build();
    const antType = apiBuilder.getProjectTypeBuilder().withId('ant').withDisplayname('Ant project').withAttributeDescriptors([attributeLanguageJava]).build();
    const workspaceId = 'florentWorkspace';
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
    const workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();

    cheBackend.addWorkspaces([workspace1]);

    // providing request
    // add workspaces on Http backend
    cheBackend.addProjectTypes(workspaceId, [mavenType, antType]);

    // setup backend
    cheBackend.setup();

    // fetch runtime
    workspace.fetchWorkspaceDetails(workspaceId);
    httpBackend.expectGET('/api/workspace/' + workspaceId);

    // flush command
    httpBackend.flush();

    const factory = workspace.getWorkspaceAgent(workspaceId).getProjectType();

    // no types now on factory
    expect(factory.getAllProjectTypes().length).toEqual(0);

    // fetch types
    factory.fetchTypes();

    // expecting a GET
    httpBackend.expectGET(agentUrl + '/project-type');

    // flush command
    httpBackend.flush();

    expect(factory.getAllProjectTypes().length).toEqual(2);

    // now, check types
    const projectTypes = factory.getAllProjectTypes();
    // check we have 2 PT
    expect(projectTypes.length).toEqual(2);

    const typesIds = factory.getProjectTypesIDs();
    expect(typesIds.size).toEqual(2);

    const firstType = typesIds.get('maven');

    expect(firstType.id).toEqual(mavenType.id);
    expect(firstType.displayName).toEqual(mavenType.displayName);

    const secondType = typesIds.get('ant');
    expect(secondType.id).toEqual(antType.id);
    expect(secondType.displayName).toEqual(antType.displayName);
  });
});
