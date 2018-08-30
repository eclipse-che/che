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
import {CheWorkspace} from '../workspace/che-workspace.factory';
import {CheAPIBuilder} from '../builder/che-api-builder.factory';
import {CheHttpBackend} from '../test/che-http-backend';

/**
 * Test of the CheTypeResolver
 */
describe('CheTypeResolver', () => {
  const workspaceId = 'workspaceTest';
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
  /**
   * User Factory for the test
   */
  let factory;
  /**
   * API builder.
   */
  let apiBuilder: CheAPIBuilder;

  let workspace: CheWorkspace;


  /**
   * Backend for handling http operations
   */
  let httpBackend: ng.IHttpBackendService;

  /**
   * Che backend
   */
  let cheBackend: CheHttpBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((cheWorkspace: CheWorkspace, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {
    workspace = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    // setup tests workspace
    const test_workspace = apiBuilder.getWorkspaceBuilder().withId(workspaceId).withRuntime(runtime).build();
    cheBackend.addWorkspaces([test_workspace]);
    // setup backend
    cheBackend.setup();
    workspace.fetchWorkspaceDetails(workspaceId);
    // flush command
    httpBackend.flush();
    factory = workspace.getWorkspaceAgent(workspaceId).getProjectTypeResolver();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  /**
   * Check that we're able to resolve project type.
   */
  it('Resolve import project type', () => {
    let attributeLanguageJava = apiBuilder.getProjectTypeAttributeDescriptorBuilder().withValues(['java']).withRequired(true).withDescription('language');
    let mavenType = apiBuilder.getProjectTypeBuilder().withId('maven').withDisplayname('Maven project').withAttributeDescriptors([attributeLanguageJava]).build();
    let project: any = {
      attributes: {language: ['java']},
      category: 'Samples',
      commands: [],
      description: 'A hello world Java application.',
      displayName: 'console-java-simple',
      links: [],
      mixins: [],
      name: 'console-java-simple',
      options: {},
      path: '/console-java-simple',
      problems: [],
      projectType: 'maven',
      projects: [],
      tags: ['java', 'maven'],
      source: {
        location: 'https://github.com/che-samples/console-java-simple.git',
        type: 'git',
        parameters: {}
      }
    };

    let resolve_project = angular.copy(project);
    resolve_project.type = project.projectType;

    // providing request
    // add a project and project types on Http backend
    cheBackend.addProjectTypes(workspaceId, [mavenType]);
    cheBackend.addProjects(workspace.getWorkspaceById(workspaceId), [resolve_project]);

    // setup backend
    cheBackend.setup();

    // resolve import project type
    factory.resolveProjectType(project);

    // expecting a GET
    httpBackend.expectGET(agentUrl + '/project-type');

    // expecting a PUT
    httpBackend.expectPUT(agentUrl + '/project' + resolve_project.path, resolve_project);

    httpBackend.flush();

    delete project.projectType;
    resolve_project = angular.copy(project);
    resolve_project.type = 'blank';

    // providing request
    // add a new project on Http backend
    cheBackend.addProjects(workspace.getWorkspaceById(workspaceId), [resolve_project]);

    // resolve import project type
    factory.resolveProjectType(project);

    // expecting a PUT
    httpBackend.expectPUT(agentUrl + '/project' + resolve_project.path, resolve_project);

    httpBackend.flush();
  });


  /**
   * Check that we're able to resolve import project type.
   */
  it('Resolve import project type', () => {
    let attributeLanguageJava = apiBuilder.getProjectTypeAttributeDescriptorBuilder().withValues(['java']).withRequired(true).withDescription('language');
    let mavenType = apiBuilder.getProjectTypeBuilder().withId('maven').withDisplayname('Maven project').withAttributeDescriptors([attributeLanguageJava]).build();
    let importProject = {
      project: {
        attributes: {language: ['java']},
        commands: [],
        description: 'A hello world Java application.',
        displayName: 'console java test',
        links: [],
        mixins: [],
        name: 'console-java-simple',
        options: {},
        path: '/console-java-simple',
        problems: [],
        type: 'maven',
        projects: [],
        tags: ['java', 'maven']
      },
      source: {
        location: 'https://github.com/che-samples/console-java-simple.git',
        type: 'git',
        parameters: {}
      }
    };

    let resolve_project = factory.getProjectDetails(importProject);

    // providing request
    // add a project and project types on Http backend
    cheBackend.addProjectTypes(workspaceId, [mavenType]);
    cheBackend.addProjects(workspace.getWorkspaceById(workspaceId), [resolve_project]);

    // setup backend
    cheBackend.setup();

    // resolve import project type
    factory.resolveImportProjectType(importProject);

    // expecting a GET
    httpBackend.expectGET(agentUrl + '/project-type');

    // expecting a PUT
    httpBackend.expectPUT(agentUrl + '/project' + resolve_project.path, resolve_project);

    httpBackend.flush();

    delete importProject.project.type;
    resolve_project = factory.getProjectDetails(importProject);
    resolve_project.type = 'blank';

    // providing request
    // add a new project on Http backend
    cheBackend.addProjects(workspace.getWorkspaceById(workspaceId), [resolve_project]);

    // resolve import project type
    factory.resolveImportProjectType(importProject);

    // expecting a PUT
    httpBackend.expectPUT(agentUrl + '/project' + resolve_project.path, resolve_project);

    httpBackend.flush();
  });

  /**
   * Check that we're able to fetch project types.
   */
  it('Fetch project types', () => {
    let attributeLanguageJava = apiBuilder.getProjectTypeAttributeDescriptorBuilder().withValues(['java']).withRequired(true).withDescription('language');
    let mavenType = apiBuilder.getProjectTypeBuilder().withId('maven').withDisplayname('Maven project').withAttributeDescriptors([attributeLanguageJava]).build();
    let antType = apiBuilder.getProjectTypeBuilder().withId('ant').withDisplayname('Ant project').withAttributeDescriptors([attributeLanguageJava]).build();

    // add workspaces and project types on Http backend
    cheBackend.addProjectTypes(workspaceId, [mavenType, antType]);

    // setup backend
    cheBackend.setup();

    // no types now on factory
    expect(factory.typesIds.size).toEqual(0);

    // fetch types
    factory.fetchTypes();

    // expecting a GET
    httpBackend.expectGET(agentUrl + '/project-type');

    // flush command
    httpBackend.flush();

    // now, check types
    expect(factory.typesIds.size).toEqual(2);

    let firstType = factory.typesIds.get('maven');
    expect(firstType.id).toEqual(mavenType.id);
    expect(firstType.displayName).toEqual(mavenType.displayName);

    let secondType = factory.typesIds.get('ant');
    expect(secondType.id).toEqual(antType.id);
    expect(secondType.displayName).toEqual(antType.displayName);
  });

  /**
   * Check that we're able to get project details from import project object.
   */
  it('Get project details from import project object', () => {
      let importProject: che.IImportProject = {
        source: {
          location: 'https://github.com/test-blank-sample.git',
          parameters: {},
          type: 'git'
        },
        project: {
          name: 'test-application',
          path: '/test-application',
          type: 'blank',
          description: 'A hello world test application.',
          source: {} as che.IProjectSource,
          attributes: {}
        }
      };
      let projectDetails: any = importProject.project;
      projectDetails.source = importProject.source;

      let project = factory.getProjectDetails(importProject);

      // check projectDetails
      expect(project).toEqual(projectDetails);
    }
  );

});
