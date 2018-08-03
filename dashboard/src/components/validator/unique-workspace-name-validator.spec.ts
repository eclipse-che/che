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
 * Test the workspace name uniqueness
 * @author Oleksii Kurinnyi
 */

describe('unique-workspace-name-validator', function() {
  let $scope, form, $compiler;

  /**
   * Workspace API
   */
  let factoryWorkspace;

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
                             cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend,
                             $document: ng.IDocumentService) {
    $scope = $rootScope;
    $compiler = $compile;
    factoryWorkspace = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    this.$document = $document;

  }));

  describe('Validate Workspace Name', function() {

    it('workspaceAlready exists', function() {

      // setup tests objects
      const idWorkspace1 = 'idOfMyWorkspace1';
      const nameWorkspace1 = 'testWorkspace1';
      const workspace1 = apiBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();

      const idWorkspace2 = 'idOfMyWorkspace2';
      const workspace2 = apiBuilder.getWorkspaceBuilder().withId(idWorkspace2).build();

      factoryWorkspace.fetchWorkspaces();

      // add into backend
      cheBackend.addWorkspaces([workspace1, workspace2]);
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { workspaceName: null};

      const element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.workspaceName" name="name" unique-workspace-name="workspace2.config.name" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue(nameWorkspace1);

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(true);
      expect(form.name.$valid).toBe(false);
    });

    it('workspace not yet defined', function() {

      // setup tests objects
      const idWorkspace1 = 'idOfMyWorkspace1';
      const nameWorkspace1 = 'testWorkspace1';
      const workspace1 = apiBuilder.getWorkspaceBuilder().withName(nameWorkspace1).withId(idWorkspace1).build();

      const idWorkspace2 = 'idOfMyWorkspace2';
      const nameWorkspace2 = 'testWorkspace2';
      const workspace2 = apiBuilder.getWorkspaceBuilder().withName().withId(idWorkspace2).build();

      factoryWorkspace.fetchWorkspaces();

      // add into backend
      cheBackend.addWorkspaces([workspace1, workspace2]);

      // setup backend
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { workspaceName: null };

      const element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.workspaceName" name="name" unique-workspace-name="workspace2.config.name" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue(nameWorkspace2);

      // check form valid
      expect(form.name.$invalid).toBe(false);
      expect(form.name.$valid).toBe(true);

    });
  });
});
