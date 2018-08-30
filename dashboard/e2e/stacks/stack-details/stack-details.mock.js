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

exports.dockerimageStack = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', ($httpBackend, cheAPIBuilder, cheHttpBackendProvider) => {

      let stackId = 'testStackId',
          stackName = 'testStack',
          stackWorkspaceConfig = {
            'environments': {
              'default': {
                'machines': {
                  'dev-machine': {
                    'servers': {},
                    'installers': ['org.eclipse.che.exec', 'org.eclipse.che.terminal', 'org.eclipse.che.ws-agent', 'org.eclipse.che.ssh'],
                    'attributes': {'memoryLimitBytes': '2147483648'}
                  }
                }, 'recipe': {'content': 'eclipse/node', 'type': 'dockerimage'}
              }
            }, 'commands': [], 'projects': [], 'defaultEnv': 'default', 'name': 'default', 'links': []
          };

      let stack = cheAPIBuilder.getStackBuilder().withId(stackId).withName(stackName).withWorkspaceConfig(stackWorkspaceConfig).build();

      let cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      cheBackend.addStacks([stack]);
      cheBackend.setup();

    }]);

};

exports.dockerfileStack = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', ($httpBackend, cheAPIBuilder, cheHttpBackendProvider) => {

      let stackId = 'testStackId',
          stackName = 'testStack',
          stackWorkspaceConfig = {
            'environments': {
              'default': {
                'recipe': {
                  'contentType': 'text/x-dockerfile',
                  'type': 'dockerfile',
                  'content': 'FROM eclipse/node\n'
                },
                'machines': {
                  'dev-machine': {
                    'servers': {},
                    'installers': ['org.eclipse.che.ws-agent', 'org.eclipse.che.ssh', 'org.eclipse.che.exec', 'org.eclipse.che.terminal'],
                    'attributes': {'memoryLimitBytes': '2147483648'}
                  }
                }
              }
            }, 'commands': [], 'projects': [], 'defaultEnv': 'default', 'name': 'default', 'links': []
          };

      let stack = cheAPIBuilder.getStackBuilder().withId(stackId).withName(stackName).withWorkspaceConfig(stackWorkspaceConfig).build();

      let cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      cheBackend.addStacks([stack]);
      cheBackend.setup();

    }]);

};

exports.composefileStack = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', ($httpBackend, cheAPIBuilder, cheHttpBackendProvider) => {

      let stackId = 'testStackId',
          stackName = 'testStack',
          stackWorkspaceConfig = {
            'environments': {
              'default': {
                'recipe': {
                  'contentType': 'application/x-yaml',
                  'type': 'compose',
                  'content': 'services:\n  db:\n    image: mysql\n    environment:\n      MYSQL_ROOT_PASSWORD: password\n      MYSQL_DATABASE: petclinic\n      MYSQL_USER: petclinic\n      MYSQL_PASSWORD: password\n    mem_limit: 1073741824\n  dev-machine:\n    image: eclipse/ubuntu_jdk8\n    mem_limit: 2147483648\n    depends_on:\n      - db\n'
                },
                'machines': {
                  'db': {
                    'servers': {},
                    'installers': [
                      'org.eclipse.che.exec',
                      'org.eclipse.che.terminal'
                    ],
                    'attributes': {
                      'memoryLimitBytes': 1073741824
                    }
                  },
                  'dev-machine': {
                    'servers': {},
                    'installers': [
                      'org.eclipse.che.exec',
                      'org.eclipse.che.terminal',
                      'org.eclipse.che.ws-agent',
                      'org.eclipse.che.ssh'
                    ],
                    'attributes': {
                      'memoryLimitBytes': 2147483648
                    }
                  }
                }
              }
            }, 'commands': [], 'projects': [], 'defaultEnv': 'default', 'name': 'default', 'links': []
          };

      let stack = cheAPIBuilder.getStackBuilder().withId(stackId).withName(stackName).withWorkspaceConfig(stackWorkspaceConfig).build();

      let cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      cheBackend.addStacks([stack]);
      cheBackend.setup();

    }]);

};
