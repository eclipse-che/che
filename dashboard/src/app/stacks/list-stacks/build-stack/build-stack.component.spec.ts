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

import {CheHttpBackend} from '../../../../components/api/test/che-http-backend';
import {CheAPIBuilder} from '../../../../components/api/builder/che-api-builder.factory';
import {BuildStackController} from './build-stack.controller';
import {CheStack} from '../../../../components/api/che-stack.factory';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {CheRecipeTypes} from '../../../../components/api/recipe/che-recipe-types';
import {DockerImageEnvironmentManager} from '../../../../components/api/environment/docker-image-environment-manager';
import {DockerFileEnvironmentManager} from '../../../../components/api/environment/docker-file-environment-manager';
import {ComposeEnvironmentManager} from '../../../../components/api/environment/compose-environment-manager';
import {OpenshiftEnvironmentManager} from '../../../../components/api/environment/openshift-environment-manager';
import {RecipeEditor} from './recipe-editor/recipe-editor';

interface ITestScope extends ng.IScope {
  model: any;
}

/**
 * Test of the BuildStack dialog
 * @author Oleksii Kurinnyi
 */
describe(`BuildStack dialog >`, () => {

  let $scope: ITestScope;

  let $compile: ng.ICompileService;

  let $mdDialog: ng.material.IDialogService;

  let $timeout: ng.ITimeoutService;

  let cheStack: CheStack;

  let cheAPIBuilder: CheAPIBuilder;

  let $controller: ng.IControllerService;
  let controller: BuildStackController;

  let cheHttpBackend: CheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;

  let modalDialogElement: ng.IAugmentedJQuery;

  const supportedEnvTypes = [CheRecipeTypes.DOCKERFILE, CheRecipeTypes.DOCKERIMAGE, CheRecipeTypes.COMPOSE];

  /**
   *  setup module
   */
  beforeEach(() => {
    angular.mock.module('userDashboard');

    // new module with dependencies which I want to mock
    angular.module('buildStackMock', [])
      .service('cheEnvironmentRegistry', function() {
        const managers = new Map();
        managers.set(CheRecipeTypes.DOCKERIMAGE, new DockerImageEnvironmentManager(null));
        managers.set(CheRecipeTypes.DOCKERFILE, new DockerFileEnvironmentManager(null));
        managers.set(CheRecipeTypes.COMPOSE, new ComposeEnvironmentManager(null));
        managers.set(CheRecipeTypes.OPENSHIFT, new OpenshiftEnvironmentManager(null));

        this.getEnvironmentManager = (recipeType: string): EnvironmentManager => {
          return managers.get(recipeType);
        };
        this.getEnvironmentManagers = (): Map<string, EnvironmentManager> => {
          return managers;
        };
      })
      .service('cheWorkspace', ['$q', function($q: ng.IQService) {
        this.fetchWorkspaceSettings = (): ng.IPromise<any> => {
          return $q.when();
        };
        this.getSupportedRecipeTypes = (): string[] => {
          return supportedEnvTypes;
        };
      }])
      .service('cheStack', function() {
        this.getStackTemplate = (): che.IStack => {
          return <che.IStack>{
            'name': 'New Stack',
            'description': 'New Java Stack',
            'scope': 'general',
            'tags': [
              'Java 1.8'
            ],
            'components': [],
            'workspaceConfig': {
              'projects': [],
              'environments': {
                'default': {
                  'machines': {
                    'dev-machine': {
                      'installers': [
                        'org.eclipse.che.exec', 'org.eclipse.che.terminal', 'org.eclipse.che.ws-agent', 'org.eclipse.che.ssh'
                      ],
                      'servers': {},
                      'attributes': {
                        'memoryLimitBytes': '2147483648'
                      }
                    }
                  },
                  'recipe': {
                    'content': 'services:\n dev-machine:\n  image: eclipse/ubuntu_jdk8\n',
                    'contentType': 'application/x-yaml',
                    'type': 'compose'
                  }
                }
              },
              'name': 'default',
              'defaultEnv': 'default',
              'commands': []
            }
          };
        };
      })
      .service('importStackService', function() {
        this.setStack = jasmine.createSpy('setStack');
      });

    // this will override providers from the original module
    angular.mock.module('buildStackMock');
  });

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$controller_: ng.IControllerService,
                     _$mdDialog_: ng.material.IDialogService,
                     _$rootScope_: ng.IRootScopeService,
                     _$timeout_: ng.ITimeoutService,
                     _cheStack_: CheStack,
                     _cheAPIBuilder_: CheAPIBuilder,
                     _cheHttpBackend_: CheHttpBackend) => {
    $compile = _$compile_;
    $controller = _$controller_;
    $mdDialog = _$mdDialog_;
    $scope = _$rootScope_.$new() as ITestScope;
    $timeout = _$timeout_;

    cheStack = _cheStack_;

    cheAPIBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;

    $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  beforeEach(() => {
    modalDialogElement = angular.element('<div></div>');
    $mdDialog.show({
      parent: modalDialogElement, // set parent element for dialog

      controller: 'BuildStackController',
      controllerAs: 'buildStackController',
      scope: $scope,
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        callbackController: this
      },
      templateUrl: 'app/stacks/list-stacks/build-stack/build-stack.html'
    });

    $scope.$digest();
    $timeout.flush();
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  it(`should show modal window >`, () => {
    expect(modalDialogElement.find('.build-stack').length).toEqual(1);
  });

  describe(`dialog window >`, () => {

    it(`should have correct number of editor tabs >`, () => {
      expect(modalDialogElement.find('md-tab-item').length).toEqual(supportedEnvTypes.length);
    });

    it(`should have correct tab names >`, () => {
      for (let name of supportedEnvTypes) {
        expect(modalDialogElement.find(`md-tab-item .build-stack-${name}`).length).toEqual(1);
      }
    });

  });

  describe(`controller >`, () => {

    beforeEach(() => {
      controller = $controller('BuildStackController');
      $timeout.flush();
    });

    it(`should create expected stack from DOCKERIMAGE recipe >`, () =>  {
      const expectedStack = {
        'name': 'New Stack',
        'description': 'New Java Stack',
        'scope': 'general',
        'tags': ['Java 1.8'],
        'components': [],
        'workspaceConfig': {
          'projects': [],
          'environments': {
            'default': {
              'machines': {
                'new-machine': {
                  'installers': [],
                  'attributes': {'memoryLimitBytes': 2147483648},
                  'env': undefined,
                  'servers': undefined,
                  'volumes': undefined
                }
              },
              'recipe': {
                'contentType': '',
                'type': 'dockerimage',
                'content': 'repository:tag'
              }
            }
          },
          'name': 'default',
          'defaultEnv': 'default',
          'commands': []
        }
      };

      const environmentManagers = controller.cheEnvironmentRegistry.getEnvironmentManagers(),
        dockerimageEnvaromentManager = environmentManagers.get(CheRecipeTypes.DOCKERIMAGE),
        editor = new RecipeEditor(dockerimageEnvaromentManager);
      editor.changeRecipe('repository:tag');
      controller.buildStack(editor);

      expect(controller.importStackService.setStack).toHaveBeenCalledWith(expectedStack);
    });

    it(`should create expected stack from DOCKERFILE recipe >`, () => {
      const expectedStack = {
        'name': 'New Stack',
        'description': 'New Java Stack',
        'scope': 'general',
        'tags': ['Java 1.8'],
        'components': [],
        'workspaceConfig': {
          'projects': [],
          'environments': {
            'default': {
              'machines': {
                'new-machine': {
                  'installers': [],
                  'attributes': {'memoryLimitBytes': 2147483648},
                  'env': undefined,
                  'servers': undefined,
                  'volumes': undefined
                }
              },
              'recipe': {'contentType': 'text/x-dockerfile', 'type': 'dockerfile', 'content': 'FROM repository:tag\n'}
            }
          },
          'name': 'default',
          'defaultEnv': 'default',
          'commands': []
        }
      };

      const environmentManagers = controller.cheEnvironmentRegistry.getEnvironmentManagers(),
        dockerimageEnvaromentManager = environmentManagers.get(CheRecipeTypes.DOCKERFILE),
        editor = new RecipeEditor(dockerimageEnvaromentManager);
      editor.changeRecipe('FROM repository:tag');
      controller.buildStack(editor);

      expect(controller.importStackService.setStack).toHaveBeenCalledWith(expectedStack);
    });

    it(`should create expected stack from COMPOSE recipe >`, () => {
      const expectedStack = {
        'name': 'New Stack',
        'description': 'New Java Stack',
        'scope': 'general',
        'tags': ['Java 1.8'],
        'components': [],
        'workspaceConfig': {
          'projects': [],
          'environments': {
            'default': {
              'machines': {
                'machine-1': {
                  'attributes': {'memoryLimitBytes': '2147483648'},
                  'env': undefined,
                  'installers': undefined,
                  'servers': undefined,
                  'volumes': undefined
                },
                'machine-2': {
                  'attributes': {'memoryLimitBytes': '2147483648'},
                  'env': undefined,
                  'installers': undefined,
                  'servers': undefined,
                  'volumes': undefined
                }
              },
              'recipe': {
                'contentType': 'text/x-yaml',
                'type': 'compose',
                'content': 'services:\n machine-1:\n  image: repository-1\n machine-2:\n  image: repository-2\n'
              }
            }
          },
          'name': 'default',
          'defaultEnv': 'default',
          'commands': []
        }
      };

      const environmentManagers = controller.cheEnvironmentRegistry.getEnvironmentManagers(),
        dockerimageEnvaromentManager = environmentManagers.get(CheRecipeTypes.COMPOSE),
        editor = new RecipeEditor(dockerimageEnvaromentManager);
      editor.changeRecipe('services:\n machine-1:\n  image: repository-1\n machine-2:\n  image: repository-2\n');
      controller.buildStack(editor);

      expect(controller.importStackService.setStack).toHaveBeenCalledWith(expectedStack);
    });

  });

});
