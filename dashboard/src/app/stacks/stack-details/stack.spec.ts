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
import { CheHttpBackend } from '../../../components/api/test/che-http-backend';
import { StackController } from './stack.controller';

'use strict';

/**
 * Test for Stack Details directive.
 *
 * @author Oleksii Kurinny
 */
describe(`StackDetailsDirective >`, () => {

  let controller: StackController;

  let $scope: ng.IScope;
  let $compile: ng.ICompileService;
  let $timeout: ng.ITimeoutService;
  let $httpBackend: ng.IHttpBackendService;
  let cheHttpBackend: CheHttpBackend;
  let compiledDirective;

  /**
   * Setup module
   */
  beforeEach(() => {
    angular.mock.module('userDashboard');

    angular.module('stackDetailsMock', [])
      .directive('stackDetails', function () {
        return {
          restrict: 'E',
          scope: {},
          controller: 'StackController',
          controllerAs: 'stackController',
          templateUrl: 'app/stacks/stack-details/stack.html'
        };
      })
      .service('initData', function() {
        return getInitData();
      })
      .service('cheNotification', function() {
        this.showInfo = (text: string) => console.log(text);
      });

    angular.mock.module('stackDetailsMock');
  });

  beforeEach(inject((
    _$rootScope_: ng.IRootScopeService,
    _$compile_: ng.ICompileService,
    _$timeout_: ng.ITimeoutService,
    _cheHttpBackend_: CheHttpBackend
  ) => {
    $scope = _$rootScope_.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;
    cheHttpBackend = _cheHttpBackend_;
    $httpBackend = cheHttpBackend.getHttpBackend();

    cheHttpBackend.setup();
    $httpBackend.flush();
  }));

  /**
   * Compile the directive.
   */
  beforeEach(() => {
    compiledDirective = $compile(angular.element(`<stack-details></stack-details>`))($scope);
    $scope.$digest();
    $timeout.flush();
    controller = compiledDirective.controller('stackDetails');
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  describe(`"Runtimes" section >`, () => {

    let runtimeElements;

    beforeEach(() => {
      runtimeElements = compiledDirective.find('.workspace-environments');
    });

    it(`"should have one environment >`, () => {
      expect(runtimeElements).toBeTruthy();
      expect(runtimeElements.length).toEqual(1);
    });

    describe(`"Machines" section >`, () => {

      let machineElements;

      beforeEach(() => {
        machineElements = angular.element(runtimeElements[0]).find('.workspace-machine-config');
      });

      it(`"should have one machine >`, () => {
        expect(machineElements).toBeTruthy();
        expect(machineElements.length).toEqual(1);
      });
    });

  });

});

function getInitData() {
  const stackId = 'blank-default';
  return { stackId: stackId, stack: getTestStack() };
}

function getTestStack(): che.IStack {
  return {
    'description': 'Default Blank Stack.',
    'scope': 'general',
    'tags': ['Blank', 'Ubuntu', 'Git'],
    'creator': 'ide',
    'workspaceConfig': {
      'environments': {
        'default': {
          'recipe': {
            'type': 'dockerimage',
            'content': 'eclipse/ubuntu_jdk8'
          },
          'machines': {
            'dev-machine': {
              'env': { 'CHE_MACHINE_NAME': 'dev-machine' },
              'volumes': {},
              'installers': ['org.eclipse.che.ws-agent'],
              'servers': {
                'tomcat8-debug': {
                  'protocol': 'http',
                  'port': '8000'
                },
                'tomcat8': {
                  'protocol': 'http',
                  'port': '8080'
                }
              },
              'attributes': { 'memoryLimitBytes': '2147483648' }
            }
          }
        }
      },
      'projects': [],
      'commands': [],
      'defaultEnv': 'default',
      'name': 'default'
    },
    'components': [
      { 'version': '16.04', 'name': 'Ubuntu' },
      { 'version': '1.8.0_162', 'name': 'JDK' },
      { 'version': '3.3.9', 'name': 'Maven' },
      { 'version': '8.0.24', 'name': 'Tomcat' }
    ],
    'name': 'Blank',
    'id': 'blank-default',
    'links': [
      {
        'rel': 'remove stack',
        'href': 'http://localhost:8080/api/stack/blank-default',
        'method': 'DELETE',
        'parameters': []
      },
      {
        'rel': 'get stack by id',
        'produces': 'application/json',
        'href': 'http://localhost:8080/api/stack/blank-default',
        'method': 'GET',
        'parameters': []
      }
    ]
  };
}
