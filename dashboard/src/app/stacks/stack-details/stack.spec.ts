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

  let installersList: Array<che.IAgent>;

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
    installersList = mockInstallers(cheHttpBackend);
    $httpBackend.flush();
  }));

  /**
   * Compile the directive.
   */
  beforeEach(() => {
    compiledDirective = $compile(angular.element(`<stack-details></stack-details>`))($scope);
    $scope.$digest();
    $httpBackend.flush();
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

      describe(`"Installers" subsection >`, () => {

        let availableInstallerIds = new Set();
        let initiallyEnabledInstallers;

        let editModeOverlay;
        let saveButton, cancelButton;

        function getAllInstallerElements() {
          return machineElements.find('che-machine-agents .machine-agents-item');
        }
        function getEnabledInstallerElements() {
          return getAllInstallerElements().find('md-switch[aria-checked="true"]');
        }
        function getInstallersFromStack(stack: che.IStack) {
            return (stack.workspaceConfig.environments as any).default.machines['dev-machine'].installers;
        }

        beforeEach(() => {
          installersList.forEach((installer: che.IAgent) => {
            availableInstallerIds.add(installer.id);
          });
          initiallyEnabledInstallers = getInstallersFromStack(getTestStack());

          editModeOverlay = compiledDirective.find('.workspace-edit-mode-overlay');
          saveButton = editModeOverlay.find('che-button-save-flat button');
          cancelButton = editModeOverlay.find('che-button-cancel-flat button');
        });

        it(`should have correct number of installers available >`, () => {
          const allInstallerElements = getAllInstallerElements();
          expect(allInstallerElements).toBeTruthy();
          expect(allInstallerElements.length).toEqual(availableInstallerIds.size);
        });

        it(`should have correct number of installers enabled >`, () => {
          const enabledInstallerElements = getEnabledInstallerElements();
          expect(enabledInstallerElements).toBeTruthy();
          expect(enabledInstallerElements.length).toEqual(initiallyEnabledInstallers.length);
        });

        it(`should have edit-mode overlay hidden >`, () => {
          expect(editModeOverlay.attr('aria-hidden')).toEqual('true');
        });

        describe(`switching an installer >`, () => {

          beforeEach(() => {
            // enable first three installers
            let i = 3;
            while (i > 0) {
              angular.element(getAllInstallerElements().get(i)).find('md-switch').click();
              i--;
            }
            $timeout.flush();
          });

          it(`should enable edit-mode overlay >`, () => {
            expect(editModeOverlay.attr('aria-hidden')).toEqual('false');
          });

          it(`should update installers list in stack configuration >`, () => {
            const enabledInstallers = getInstallersFromStack(controller.stack);
            expect(enabledInstallers.length).toEqual(initiallyEnabledInstallers.length + 3);
          });

          describe(`click on "Cancel" button >`, () => {

            beforeEach(() => {
              cancelButton.click();
            });

            it(`should hide edit-mode overlay >`, () => {
              expect(editModeOverlay.attr('aria-hidden')).toEqual('true');
            });

            it (`should reset installers list in stack configuration >`, () => {
              const enabledInstallers = getInstallersFromStack(controller.stack);
              expect(enabledInstallers.length).toEqual(initiallyEnabledInstallers.length);
            });

            it(`should reset installer switchers to initial state >`, () => {
              const enabledInstallerElements = getEnabledInstallerElements();
              expect(enabledInstallerElements.length).toEqual(initiallyEnabledInstallers.length);
            });

          });

          describe(`click on "Save" button >`, () => {

            it(`should save changes >`, () => {
              $httpBackend.expectPUT('/api/stack/blank-default', function (stackJson: string) {
                const stack = angular.fromJson(stackJson);
                return angular.equals(
                  getInstallersFromStack(controller.stack),
                  getInstallersFromStack(stack)
                );
              }).respond(200);

              saveButton.click();
              $httpBackend.flush();
              $timeout.flush();
            });

            it(`should hide edit-mode overlay >`, () => {
              $httpBackend.expectPUT('/api/stack/blank-default').respond(200);
              $httpBackend.expectGET('/api/stack?maxItems=50').respond(200, angular.toJson([controller.stack]));
              saveButton.click();
              $httpBackend.flush();
              $timeout.flush();

              expect(editModeOverlay.attr('aria-hidden')).toEqual('true');
            });

          });

        });

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

function mockInstallers(backend: CheHttpBackend): Array<che.IAgent> {
  const installers = <Array<che.IAgent>>[
    { 'version': '1.0.0', 'name': 'Terminal', 'id': 'org.eclipse.che.terminal' },
    { 'version': '1.0.1', 'name': 'Workspace API', 'id': 'org.eclipse.che.ws-agent' },
    { 'version': '1.0.1', 'name': 'Terminal', 'id': 'org.eclipse.che.terminal' },
    { 'version': '1.0.1', 'name': 'TypeScript language server', 'id': 'org.eclipse.che.ls.js-ts' },
    { 'version': '1.0.0', 'name': 'Workspace API', 'id': 'org.eclipse.che.ws-agent' },
    { 'version': '5.4.0', 'name': 'PHP language server', 'id': 'org.eclipse.che.ls.php' },
    { 'version': '5.3.7', 'name': 'PHP language server', 'id': 'org.eclipse.che.ls.php' },
    { 'version': '1.0.0', 'name': 'SSH', 'id': 'org.eclipse.che.ssh' },
    { 'version': '2.0.1', 'name': 'PHP language server', 'id': 'org.eclipse.che.ls.php' },
    { 'version': '1.0.0', 'name': 'Exec', 'id': 'org.eclipse.che.exec' },
    { 'version': '1.0.1', 'name': 'C# language server', 'id': 'org.eclipse.che.ls.csharp' },
    { 'version': '1.0.1', 'name': 'Exec', 'id': 'org.eclipse.che.exec' },
    { 'version': '1.0.0', 'name': 'Apache Camel language server', 'id': 'org.eclipse.che.ls.camel' },
    { 'version': '1.0.0', 'name': 'Git credentials', 'id': 'org.eclipse.che.git-credentials' },
    { 'version': '1.0.0', 'name': 'Clangd language server', 'id': 'org.eclipse.che.ls.clangd' },
    { 'version': '1.0.3', 'name': 'Python language server', 'id': 'org.eclipse.che.ls.python' },
    { 'version': '0.1.7', 'name': 'Golang language server', 'id': 'org.eclipse.che.ls.golang' },
    { 'version': '1.0.4', 'name': 'Python language server', 'id': 'org.eclipse.che.ls.python' },
    { 'version': '1.0.3', 'name': 'Workspace API', 'id': 'org.eclipse.che.ws-agent' },
    { 'version': '1.0.2', 'name': 'Workspace API', 'id': 'org.eclipse.che.ws-agent' },
    { 'version': '1.0.0', 'name': 'File sync', 'id': 'org.eclipse.che.unison' },
    { 'version': '1.0.1', 'name': 'JSON language server', 'id': 'org.eclipse.che.ls.json' },
    { 'version': '1.0.0', 'name': 'Yaml language server', 'id': 'org.eclipse.che.ls.yaml' },
    { 'version': '1.0.0', 'name': 'Simple Test language server', 'id': 'org.eclipse.che.test.ls' }
  ];
  for (const installer of installers) {
    backend.addInstaller(installer as che.IAgent);
  }
  backend.installersBackendSetup();

  return installers;
}
