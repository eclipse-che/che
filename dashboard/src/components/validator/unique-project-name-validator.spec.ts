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

/**
 * Test the git URL
 * @author Florent Benoit
 */

describe('unique-project-name-validator', function() {
  var $scope, form, $compiler;

  /**
   * Project API
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


  beforeEach(angular.mock.module('userDashboard'));


  beforeEach(inject(function($compile, $rootScope, cheWorkspace, cheAPIBuilder, cheHttpBackend) {
    $scope = $rootScope;
    $compiler = $compile;
    workspace = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();

  }));

  describe('Validate Project Name', function() {

    it('projectAlready exists', function() {

      // setup tests objects
      var idWorkspace1 = 'idOfMyWorkspace1';
      var workspace1 = apiBuilder.getWorkspaceBuilder().withName('testWorkspace1').withId(idWorkspace1).build();
      var wksp1Project1 = apiBuilder.getProjectReferenceBuilder().withName('project-wk1-1').build();


      // add into backend
      cheBackend.addProjects(workspace1, [wksp1Project1]);
      cheBackend.setup();


      // update projects workspaces
      workspace.fetchWorkspaceDetails(idWorkspace1);

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = {
        name: null,
        getWorkspaceProjects() {
          return workspace.getWorkspaceProjects()[idWorkspace1];
        }
      };

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" unique-project-name="model.getWorkspaceProjects()" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue('project-wk1-1');

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(true);
      expect(form.name.$valid).toBe(false);

    });


    it('project not yet defined', function() {

      // setup tests objects
      var idWorkspace1 = 'idOfMyWorkspace1';
      var workspace1 = apiBuilder.getWorkspaceBuilder().withName('testWorkspace1').withId(idWorkspace1).build();
      var wksp1Project1 = apiBuilder.getProjectReferenceBuilder().withName('project-wk1-1').build();


      // add into backend
      cheBackend.addProjects(workspace1, [wksp1Project1]);

      // update projects workspaces
      workspace.fetchWorkspaceDetails(idWorkspace1);

      // setup backend
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = {
        name: null,
        getWorkspaceProjects() {
          return workspace.getWorkspaceProjects()[idWorkspace1];
        }
      };

      var element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.name" name="name" unique-project-name="model.getWorkspaceProjects()" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue('dummyProject');

      // check form valid
      expect(form.name.$invalid).toBe(false);
      expect(form.name.$valid).toBe(true);

    });
  });
});
