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

import {CheHttpBackend} from '../../../components/api/test/che-http-backend';
import {WorkspaceDetailsController} from './workspace-details.controller';
import {CheWorkspace, WorkspaceStatus} from '../../../components/api/workspace/che-workspace.factory';

/**
 * Test of the WorkspaceDetailsController
 *
 * @author Oleksii Kurinnyi
 */
describe(`WorkspaceDetailsController >`, () => {

  /**
   * Controller to test.
   */
  let controller: WorkspaceDetailsController;

  let cheHttpBackend: CheHttpBackend;

  let $httpBackend: ng.IHttpBackendService;

  let $compile: ng.ICompileService;

  let $timeout: ng.ITimeoutService;

  let $q: ng.IQService;

  let $scope: ng.IScope;

  let compiledDirective;

  let cheWorkspace: CheWorkspace;
  let newWorkspace: che.IWorkspace; // the updated workspace

  function getWorkspace(): che.IWorkspace {
    return {
      'namespace': 'che',
      'status': 'RUNNING',
      'devfile': {
        'metadata': {
          'name': 'wksp-tkbi'
        },
        'projects': [
          {
            'name': 'project-1', 'source': { 'location': 'https://example.com/project-1.git', 'type': 'git', 'branch': 'master' }
          }
        ],
        'components': [],
        'apiVersion': '1.0.0',
        'commands': []
      },
      'temporary': false,
      'links': {
        'self': 'http://localhost:8080/api/workspace/workspacezbkov1e8qcm00dli',
        'ide': 'http://localhost:8080/che/wksp-98cs'
      },
      'id': 'workspacezbkov1e8qcm00dli',
      'attributes': {'created': 1516282666658, 'stackId': 'blank-default', 'infrastructureNamespace': 'che'}
    };
  }

  /**
   * Setup module
   */
  beforeEach(() => {
    angular.mock.module('userDashboard');
  });

  beforeEach(() => {
    const workspace = getWorkspace();

    angular.module('workspaceDetailsMock', [])
      // create a directive to test
      .directive('workspaceDetails', function() {
        return {
          restrict: 'E',
          scope: {},
          controller: 'WorkspaceDetailsController',
          controllerAs: 'workspaceDetailsController',
          templateUrl: 'app/workspaces/workspace-details/workspace-details.html'
        };
      })
      .service('initData', function() {
        return {
          namespaceId: workspace.namespace,
          workspaceName: workspace.devfile.metadata.name,
          workspaceDetails: workspace
        };
      })
      .service('workspacesService', function() {
        this.isSupported = () => { return true; };
      })
      .service('$route', function() {
        this.current = {
          params: {
            tab: 'Overview'
          }
        };
      })
      .service('$location', function () {
        this.path = (path: string) => {
          console.log('$location.path: ', path);
          return this;
        };
        this.search = () => {
          return {
            tab: 'Overview'
          };
        };
      })
      .service('$log', function () {
        this.log = this.warn = (message: string) => {
          console.log('$log.log: ', message);
        };
      })
      .service('cheNotification', function () {
        this.showInfo = (message: string): void => {
          console.log('cheNotification.info: ', message);
        };
      })
      .service('ideSvc', function () {
        return;
      })
      .service('namespaceSelectorSvc', function () {
        this.getNamespaceId = () => {
          return workspace.namespace;
        };
      })
      .service('cheWorkspace', function () {
        const subscriptions: any = {};
        this.fetchWorkspaceDetails = (workspaceId: string): ng.IPromise<any> => {
          if (subscriptions[workspaceId]) {
            subscriptions[workspaceId].forEach((action: Function) => action(newWorkspace));
          }
          return $q.when();
        };
        this.subscribeOnWorkspaceChange = (workspaceId: string, action: Function): void => {
          if (!subscriptions[workspaceId]) {
            subscriptions[workspaceId] = [];
          }
          subscriptions[workspaceId].push(action);
        };
        this.unsubscribeOnWorkspaceChange = (workspaceId: string, action: Function): void => {
          const actions = subscriptions[workspaceId];
          if (actions === undefined) {
            return;
          }
          const index = actions.indexOf(action);
          if (index === -1) {
            return;
          }
          actions.splice(index, 1);
        };
        this.getWorkspaceById = (workspaceId: string): che.IWorkspace => {
          return workspace;
        };
        this.startWorkspace = (workspaceId: string): ng.IPromise<any> => {
          const startingPromise = $q.when().then(() => {
            workspace.status = WorkspaceStatus[WorkspaceStatus.STARTING];
          });
          startingPromise.then(() => {
            return $timeout(() => {
              workspace.status = WorkspaceStatus[WorkspaceStatus.RUNNING];
            });
          });
          return startingPromise;
        };
        this.stopWorkspace = (workspaceId: string): ng.IPromise<any> => {
          const stoppingPromise = $q.when().then(() => {
            workspace.status = WorkspaceStatus[WorkspaceStatus.STOPPING];
          });
          stoppingPromise.then(() => {
            return $timeout(() => {
              workspace.status = WorkspaceStatus[WorkspaceStatus.STOPPED];
            });
          });
          return stoppingPromise;
        };
        this.fetchStatusChange = (workspaceId: string, status: string): ng.IPromise<any> => {
          workspace.status = status;
          return $q.when();
        };
        this.updateWorkspace = (workspaceId: string, data: che.IWorkspace): ng.IPromise<any> => {
          if (subscriptions[workspaceId]) {
            subscriptions[workspaceId].forEach((action: Function) => action(newWorkspace));
          }
          return $q.when(workspace);
        };
        this.getSupportedRecipeTypes = () => {
          return ['dockerimage', 'dockerfile', 'compose'];
        };
        this.fetchWorkspaceSettings = (): any => {
          // todo: rework to use Angular promise instead of native one
          return Promise.resolve({
            cheWorkspacePluginRegistryUrl: 'cheWorkspacePluginRegistryUrl'
          });
        };
        this.getWorkspaceSettings = () => {
          return {};
        };
        this.getWorkspaceDataManager = () => {
          return {
            getName(data: che.IWorkspace): string {
              return 'name';
            },
            getEditor() {
              return '';
            },
            getPlugins() {
              return [];
            }
          };
        };
      })
      .factory('pluginRegistry', function () {
        return {
          fetchPlugins: (url: string) => {
            return $q.when([]);
          }
        }
      })
      .factory('cheBranding', function () {
        return {
          getDocs: () => {
            const converting = 'converting-a-che-6-workspace-to-a-che-7-devfile';
            return { converting };
          },
          registerCallback: (callbackId: string, callback: Function): void => {
            callback();
          },
          unregisterCallback: (callbackId: string): void => {},
          getConfiguration: () => {
            return {
              menu: {
                disabled: []
              },
              prefetch: {},
              features: {
                disabled: []
              }
            };
          }
        };
      })
      // terminal directives which prevent to execute an original ones
      .directive('mdTab', function () {
        // this directive produces timeout task which cannot be flushed
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('cheWorkspaceConfigImport', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('cheMachineVolumes', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('cheEnvVariables', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('cheMachineServers', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('cheMachineSelector', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('workspaceMachines', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('workspaceDetailsProjects', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      })
      .directive('workspaceDetailsSsh', function () {
        return { priority: 100000, terminal: true, restrict: 'E' };
      });

    angular.mock.module('workspaceDetailsMock');
  });

  beforeEach(inject((
    _$rootScope_: ng.IRootScopeService,
    _$compile_: ng.ICompileService,
    _cheHttpBackend_: CheHttpBackend,
    _$timeout_: ng.ITimeoutService,
    _$q_: ng.IQService,
    _cheWorkspace_: CheWorkspace
  ) => {
    $scope = _$rootScope_.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;
    $q = _$q_;
    cheWorkspace = _cheWorkspace_;
    cheHttpBackend = _cheHttpBackend_;
    $httpBackend = cheHttpBackend.getHttpBackend();

    cheHttpBackend.setup();
    $httpBackend.flush();
  }));

  function compileDirective(): void {
    compiledDirective = $compile(angular.element(
      `<workspace-details></workspace-details>`
    ))($scope);
    $scope.$digest();
    $timeout.flush();
    controller = compiledDirective.controller('workspaceDetails');
  }

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

  afterEach(() => {
    compiledDirective = undefined;
    cheWorkspace = undefined;
    newWorkspace = undefined;
  });

  describe(`overflow panel >`, () => {

    function getOverlayPanelEl(): ng.IAugmentedJQuery {
      return compiledDirective.find('che-edit-mode-overlay');
    }
    function getSaveButton(): ng.IAugmentedJQuery {
      return compiledDirective.find('.save-button button');
    }
    function getApplyButton(): ng.IAugmentedJQuery {
      return compiledDirective.find('.apply-button button');
    }
    function getCancelButton(): ng.IAugmentedJQuery {
      return compiledDirective.find('.cancel-button button');
    }

    describe('initially >', () => {

      beforeEach(() => {
        compileDirective();
      });

      it(`should be hidden >`, () => {
        expect(getOverlayPanelEl().children().length).toEqual(0);
      });

      it('should not prevent to leave page', () => {
        expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
      });

    });

    describe(`when config is changed >`, () => {

      describe(`and workspace is RUNNING >`, () => {

        describe(`and restart is not necessary > `, () => {

          beforeEach(() => {
            compileDirective();

            (controller as any).workspaceDetails.devfile.metadata.name = 'wksp-new-name';
            controller.checkEditMode();
            $scope.$digest();
            $timeout.flush();
          });

          it('should prevent to leave page', () => {
            expect((controller as any).editOverlayConfig.preventPageLeave).toBeTruthy();
          });

          it(`the overflow panel should be shown >`, () => {
            expect(getOverlayPanelEl().length).toEqual(1);
          });

          it(`the saveButton should be enabled >`, () => {
            expect(getSaveButton().attr('disabled')).toBeFalsy();
          });

          it(`the applyButton should be disabled >`, () => {
            expect(getApplyButton().attr('disabled')).toBeTruthy();
          });

          it(`the cancelButton should be enabled >`, () => {
            expect(getCancelButton().attr('disabled')).toBeFalsy();
          });

          describe(`and cancelButton is clicked >`, () => {

            beforeEach(() => {
              getCancelButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

          describe(`and saveButton is clicked >`, () => {

            beforeEach(() => {
              // set new workspace to publish
              newWorkspace = angular.copy((controller as any).workspaceDetails);

              getSaveButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

        });

        describe(`and restart is necessary >`, () => {

          beforeEach(() => {
            compileDirective();

            ((controller as any).workspaceDetails as che.IWorkspace).devfile.projects.push({
              'name': 'project-2', 'source': { 'location': 'https://example.com/project-2.git', 'type': 'git', 'branch': 'master' }
            });
            controller.checkEditMode();
            $scope.$digest();
            $timeout.flush();
          });

          it('should prevent to leave page', () => {
            expect((controller as any).editOverlayConfig.preventPageLeave).toBeTruthy();
          });

          it(`the overflow panel should be shown >`, () => {
            expect(getOverlayPanelEl().length).toEqual(1);
          });

          it(`the saveButton should be enabled >`, () => {
            expect(getSaveButton().attr('disabled')).toBeFalsy();
          });

          it(`the applyButton should be enabled >`, () => {
            expect(getApplyButton().attr('disabled')).toBeFalsy();
          });

          it(`the cancelButton should be enabled >`, () => {
            expect(getCancelButton().attr('disabled')).toBeFalsy();
          });

          describe(`and cancelButton is clicked >`, () => {

            beforeEach(() => {
              getCancelButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

          describe(`and saveButton is clicked >`, () => {

            beforeEach(() => {
              newWorkspace = angular.copy((controller as any).workspaceDetails);
              getSaveButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should remain visible >`, () => {
              expect(getOverlayPanelEl().length).toEqual(1);
            });

            it(`the saveButton should be disabled >`, () => {
              expect(getSaveButton().attr('disabled')).toBeTruthy();
            });

            it(`the applyButton should be enabled >`, () => {
              expect(getApplyButton().attr('disabled')).toBeFalsy();
            });

            it(`the cancelButton should be disabled >`, () => {
              expect(getCancelButton().attr('disabled')).toBeTruthy();
            });

          });

          describe(`and applyButton is clicked >`, () => {

            beforeEach(() => {
              // set new workspace to publish
              newWorkspace = angular.copy((controller as any).workspaceDetails);

              getApplyButton().click();
              $scope.$digest();
              $timeout.flush();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

        });

      });

      describe(`and workspace is STOPPED >`, () => {

        beforeEach(() => {
          compileDirective();

          controller.stopWorkspace();
          (controller as any).workspaceDetails.devfile.metadata.name = 'wksp-new-name';
        });

        describe(`and restart is not necessary >`, () => {

          beforeEach(() => {
            controller.checkEditMode();
            $scope.$digest();
            $timeout.flush();
          });

          it('should prevent to leave page', () => {
            expect((controller as any).editOverlayConfig.preventPageLeave).toBeTruthy();
          });

          it(`the overflow panel should be shown >`, () => {
            expect(getOverlayPanelEl().length).toEqual(1);
          });

          it(`the saveButton should be enabled >`, () => {
            expect(getSaveButton().attr('disabled')).toBeFalsy();
          });

          it(`the applyButton should be disabled >`, () => {
            expect(getApplyButton().attr('disabled')).toBeTruthy();
          });

          it(`the cancelButton should be enabled >`, () => {
            expect(getCancelButton().attr('disabled')).toBeFalsy();
          });

          describe(`and cancelButton is clicked >`, () => {

            beforeEach(() => {
              getCancelButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

          describe(`and saveButton is clicked >`, () => {

            beforeEach(() => {
              // set new workspace to publish
              newWorkspace = angular.copy((controller as any).workspaceDetails);

              getSaveButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

        });

        describe(`and restart is necessary >`, () => {

          beforeEach(() => {
            controller.checkEditMode();
            $scope.$digest();
            $timeout.flush();
          });

          it('should prevent to leave page', () => {
            expect((controller as any).editOverlayConfig.preventPageLeave).toBeTruthy();
          });

          it(`the overflow panel should be shown >`, () => {
            expect(getOverlayPanelEl().length).toEqual(1);
          });

          it(`the saveButton should be enabled >`, () => {
            expect(getSaveButton().attr('disabled')).toBeFalsy();
          });

          it(`the applyButton should be disabled >`, () => {
            expect(getApplyButton().attr('disabled')).toBeTruthy();
          });

          it(`the cancelButton should be enabled >`, () => {
            expect(getCancelButton().attr('disabled')).toBeFalsy();
          });

          describe(`and cancelButton is clicked >`, () => {

            beforeEach(() => {
              getCancelButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

          describe(`and saveButton is clicked >`, () => {

            beforeEach(() => {
              // set new workspace to publish
              newWorkspace = angular.copy((controller as any).workspaceDetails);

              getSaveButton().click();
              $scope.$digest();
            });

            it('should not prevent to leave page', () => {
              expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
            });

            it(`the overlay panel should be hidden >`, () => {
              expect(getOverlayPanelEl().children().length).toEqual(0);
            });

          });

        });

      });

      describe(`and workspace recipe type is not supported >`, () => {

        beforeEach(() => {
          compileDirective();

          (controller as any).workspacesService.isSupported = jasmine.createSpy('workspaceDetailsController.isSupported')
            .and
            .callFake(() => {
              return false;
            });

          ((controller as any).workspaceDetails as che.IWorkspace).devfile.metadata.name = 'wksp-new-name';
          controller.checkEditMode();
          $scope.$digest();
          $timeout.flush();
        });

        it('should not prevent to leave page', () => {
          expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
        });

        it(`the overflow panel should be shown >`, () => {
          expect(getOverlayPanelEl().length).toEqual(1);
        });

        it(`the saveButton should be disabled >`, () => {
          expect(getSaveButton().attr('disabled')).toBeTruthy();
        });

        it(`the applyButton should be disabled >`, () => {
          expect(getApplyButton().attr('disabled')).toBeTruthy();
        });

        it(`the cancelButton should be enabled >`, () => {
          expect(getCancelButton().attr('disabled')).toBeFalsy();
        });

        describe(`and cancelButton is clicked >`, () => {

          beforeEach(() => {
            getCancelButton().click();
            $scope.$digest();
          });

          it('should not prevent to leave page', () => {
            expect((controller as any).editOverlayConfig.preventPageLeave).toBeFalsy();
          });

          it(`the overlay panel should be hidden >`, () => {
            expect(getOverlayPanelEl().children().length).toEqual(0);
          });

        });

      });

    });

  });

});
