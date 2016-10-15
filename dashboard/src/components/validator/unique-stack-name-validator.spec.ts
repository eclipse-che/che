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
 * Test the stack name uniqueness directive
 * @author Ann Shumilova
 */

describe('unique-stack-name-validator', function() {
  var $scope, form, $compiler;

  /**
   * Stack API
   */
  var factoryStack;

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


  beforeEach(inject(function($compile, $rootScope, cheStack, cheAPIBuilder, cheHttpBackend, $document) {
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
      let stacks = [];
      // setup tests objects
      var idStack1 = 'idStack1';
      var nameStack1 = 'stack1';
      var stack1 = apiBuilder.getStackBuilder().withName(nameStack1).withId(idStack1).build();
      stacks.push(stack1);

      var idStack2 = 'idStack2';
      var stack2 = apiBuilder.getStackBuilder().withId(idStack2).build();
      stacks.push(stack2);

      // add into backend
      cheBackend.addStacks(stacks);
      cheBackend.setup();

      factoryStack.fetchStacks();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = {stackName: null};

      var element = angular.element(
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
      var idStack1 = 'idStack1';
      var nameStack1 = 'stack1';
      var stack1 = apiBuilder.getStackBuilder().withName(nameStack1).withId(idStack1).build();

      var idStack2 = 'idStack2';
      var nameStack2 = 'stack2';
      var stack2 = apiBuilder.getStackBuilder().withName('').withId(idStack2).build();

      factoryStack.fetchStacks();

      // add into backend
      cheBackend.addStacks([stack1, stack2]);

      // setup backend
      cheBackend.setup();

      // flush HTTP backend
      httpBackend.flush();

      $scope.model = { stackName: null };

      var element = angular.element(
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
