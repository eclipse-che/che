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
import {CheStack} from '../api/che-stack.factory';
import {CheAPIBuilder} from '../api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../api/test/che-http-backend';

/**
 * Test the stack name uniqueness directive
 * @author Ann Shumilova
 */

describe('unique-stack-name-validator', function() {
  let $scope, form, $compiler;

  /**
   * Stack API
   */
  let factoryStack;

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
                             cheStack: CheStack,
                             cheAPIBuilder: CheAPIBuilder,
                             cheHttpBackend: CheHttpBackend,
                             $document: ng.IDocumentService) {
    $scope = $rootScope;
    $compiler = $compile;
    factoryStack = cheStack;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    this.$document = $document;

  }));

  describe('Validate Stack Name', function() {

    it('stack already exists', function() {
      const stacks = [];
      // setup tests objects
      const idStack1 = 'idStack1';
      const nameStack1 = 'stack1';
      const stack1 = apiBuilder.getStackBuilder().withName(nameStack1).withId(idStack1).build();
      stacks.push(stack1);

      const idStack2 = 'idStack2';
      const stack2 = apiBuilder.getStackBuilder().withId(idStack2).build();
      stacks.push(stack2);

      // add into backend
      cheBackend.addStacks(stacks);
      cheBackend.setup();

      factoryStack.fetchStacks();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = {stackName: null};

      let element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.stackName" name="name" unique-stack-name="stack2.name" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue(nameStack1);

      // check form (expect invalid)
      expect(form.name.$invalid).toBe(true);
      expect(form.name.$valid).toBe(false);
    });

    it('stack not yet defined', function() {

      // setup tests objects
      const idStack1 = 'idStack1';
      const nameStack1 = 'stack1';
      const stack1 = apiBuilder.getStackBuilder().withName(nameStack1).withId(idStack1).build();

      const idStack2 = 'idStack2';
      const nameStack2 = 'stack2';
      const stack2 = apiBuilder.getStackBuilder().withName('').withId(idStack2).build();

      factoryStack.fetchStacks();

      // add into backend
      cheBackend.addStacks([stack1, stack2]);

      // setup backend
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { stackName: null };

      const element = angular.element(
        '<form name="form">' +
        '<input ng-model="model.stackName" name="name" unique-stack-name="stack2.name" />' +
        '</form>'
      );
      $compiler(element)($scope);
      form = $scope.form;

      form.name.$setViewValue(nameStack2);

      // check form valid
      expect(form.name.$invalid).toBe(false);
      expect(form.name.$valid).toBe(true);

    });
  });
});
