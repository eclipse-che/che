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
import {CheWorkspace} from '../api/workspace/che-workspace.factory';
import {CheAPIBuilder} from '../api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../api/test/che-http-backend';

/**
 * Test the git URL
 * @author Florent Benoit
 */

describe('unique-project-name-validator', function() {
  let $scope, form, $compiler;

  /**
   * Project API
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


  beforeEach(angular.mock.module('userDashboard'));


  beforeEach(inject(function($compile: ng.ICompileService,
                             $rootScope: ng.IRootScopeService,
                             cheWorkspace: CheWorkspace,
                             cheAPIBuilder: CheAPIBuilder,
                             cheHttpBackend: CheHttpBackend) {
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
      let idWorkspace1 = 'idOfMyWorkspace1';
      let workspace1 = apiBuilder.getWorkspaceBuilder().withName('testWorkspace1').withId(idWorkspace1).build();
      let wksp1Project1 = apiBuilder.getProjectReferenceBuilder().withName('project-wk1-1').build();


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

      let element = angular.element(
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
      let idWorkspace1 = 'idOfMyWorkspace1';
      let workspace1 = apiBuilder.getWorkspaceBuilder().withName('testWorkspace1').withId(idWorkspace1).build();
      let wksp1Project1 = apiBuilder.getProjectReferenceBuilder().withName('project-wk1-1').build();


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

      let element = angular.element(
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
