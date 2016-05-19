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
 * Test of the CheProjectType
 */
describe('CheProjectType', function(){

  /**
   * Project Type Factory for the test
   */
  var factory;

  /**
   * Workspace for the test
   */
  var workspace;

  /**
   * API builder.
   */
  var apiBuilder;

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
  beforeEach(inject(function(cheWorkspace, cheAPIBuilder, cheHttpBackend) {
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
  it('Fetch project types', function() {

      // setup tests objects
      var attributeLanguageJava = apiBuilder.getProjectTypeAttributeDescriptorBuilder().withValues(['java']).withRequired(true).withDescription('language').withName('language').build();
      var mavenType = apiBuilder.getProjectTypeBuilder().withId('maven').withDisplayname('Maven project').withAttributeDescriptors([attributeLanguageJava]).build();
      var antType = apiBuilder.getProjectTypeBuilder().withId('ant').withDisplayname('Ant project').withAttributeDescriptors([attributeLanguageJava]).build();
      let workspaceId = 'florentWorkspace';
      let agentUrl = 'localhost:3232/wsagent/ext';

      var runtime =  {'links': [{'href': agentUrl, 'rel': 'wsagent'}]};
      var workspace1 = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();

      cheBackend.addWorkspaces([workspace1]);

      // providing request
      // add workspaces on Http backend
      cheBackend.addProjectTypes(workspaceId, [mavenType, antType]);

      // setup backend
      cheBackend.setup();

      //fetch runtime
      workspace.fetchWorkspaceDetails(workspaceId);
      httpBackend.expectGET('/api/workspace/' + workspaceId);

      // flush command
      httpBackend.flush();

      var factory = workspace.getWorkspaceAgent(workspaceId).getProjectType();

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
      var projectTypes = factory.getAllProjectTypes();
      // check we have 2 PT
      expect(projectTypes.length).toEqual(2);

      var typesIds = factory.getProjectTypesIDs();
      expect(typesIds.size).toEqual(2);

      var firstType = typesIds.get('maven');

      expect(firstType.id).toEqual(mavenType.id);
      expect(firstType.displayName).toEqual(mavenType.displayName);

      var secondType = typesIds.get('ant');
      expect(secondType.id).toEqual(antType.id);
      expect(secondType.displayName).toEqual(antType.displayName);

    }
  );
});
