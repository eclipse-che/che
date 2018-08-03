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
import {CheProjectTemplate} from './che-project-template.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheHttpBackend} from './test/che-http-backend';

/**
 * Test of the CheProjectTemplate
 */
describe('CheProjectTemplate', function(){

  /**
   * Project Template Factory for the test
   */
  let factory;

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
  beforeEach(inject(function(cheProjectTemplate: CheProjectTemplate,
                             cheAPIBuilder: CheAPIBuilder,
                             cheHttpBackend: CheHttpBackend) {
    factory = cheProjectTemplate;
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
      var templateHelloWorldCli = apiBuilder.getProjectTemplateBuilder()
        .withProjectType('maven')
        .withCategory('Samples - Hello World')
        .withSourceLocation('https://github.com/eclipse/dummy-che-templates/desktop-console-java.git')
        .withSourceType('git')
        .withSourceParameters({branch: '3.1.0', keepVcs: 'false'})
        .withDescription('A simple JAR project based on Maven.')
        .withDisplayname('Maven Java Console')
        .build();

      var templateSwing = apiBuilder.getProjectTemplateBuilder()
        .withProjectType('maven')
        .withCategory('Samples - Hello World')
        .withSourceLocation('https://github.com/eclipse/dummy-che-templates/desktop-swing-java-basic.git')
        .withSourceType('git')
        .withSourceParameters({branch: '3.1.0', keepVcs: 'false'})
        .withDescription('A simple Java Swing application.')
        .withDisplayname('Swing')
        .build();

      var templateUD = apiBuilder.getProjectTemplateBuilder()
        .withProjectType('AngularJS')
        .withCategory('Samples - Che')
        .withSourceLocation('https://github.com/eclipse/che-dashboard.git')
        .withSourceType('git')
        .withSourceParameters({branch: '3.1.0', keepVcs: 'false'})
        .withDescription('Che Dashboard example..')
        .withDisplayname('Che Dashboard')
        .build();

      // providing request
      // add workspaces on Http backend
      cheBackend.addProjectTemplates([templateHelloWorldCli, templateSwing, templateUD]);

      // setup backend
      cheBackend.setup();

      // no templates now on factory
      expect(factory.getAllProjectTemplates().length).toEqual(0);

      // fetch types
      factory.fetchTemplates();

      // expecting a GET
      httpBackend.expectGET('/api/project-template/all');

      // flush command
      httpBackend.flush();


      // now, check types
      var projectTemplates = factory.getAllProjectTemplates();
      var templatesByCategory = factory.getTemplatesByCategory();


      // check we have 3 types
      expect(projectTemplates.length).toEqual(3);

      // check category hello world
      var helloWorldCategory = templatesByCategory['Samples - Hello World'];
      expect(helloWorldCategory).not.toBeNull();

      expect(helloWorldCategory.length).toEqual(2);

      var firstType = helloWorldCategory[0];
      expect(firstType.projectType).toEqual(templateHelloWorldCli.projectType);
      expect(firstType.displayName).toEqual(templateHelloWorldCli.displayName);

      var secondType = helloWorldCategory[1];
      expect(secondType.projectType).toEqual(templateSwing.projectType);
      expect(secondType.displayName).toEqual(templateSwing.displayName);


      // check category samples
      var sampleCategory = templatesByCategory['Samples - Che'];
      expect(sampleCategory).not.toBeNull();
      expect(sampleCategory.length).toEqual(1);
      var anotherType = sampleCategory[0];
      expect(anotherType.projectType).toEqual(templateUD.projectType);
      expect(anotherType.displayName).toEqual(templateUD.displayName);


    }
  );



});
