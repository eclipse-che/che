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

exports.listStacksTheeEntries = function () {

  angular.module('userDashboardMock', ['userDashboard', 'ngMockE2E'])
    .run(['$httpBackend', 'cheAPIBuilder', 'cheHttpBackendProvider', ($httpBackend, cheAPIBuilder, cheHttpBackendProvider) => {

      let cheBackend = cheHttpBackendProvider.buildBackend($httpBackend, cheAPIBuilder);

      let stacks = [];

      // let it be the same for all stacks
      let workspaceConfig = {
            'environments': {
              'default': {
                'machines': {
                  'dev-machine': {
                    'servers': {},
                    'installers': ['org.eclipse.che.exec', 'org.eclipse.che.terminal', 'org.eclipse.che.ws-agent', 'org.eclipse.che.ssh'],
                    'attributes': {'memoryLimitBytes': '2147483648'}
                  }
                }, 'recipe': {'content': 'codenvy/node', 'type': 'dockerimage'}
              }
            }, 'commands': [], 'projects': [], 'defaultEnv': 'default', 'name': 'default', 'links': []
          };

      let name1 = 'testStack1',
          id1 = 'testStackId1',
          stack1 = cheAPIBuilder.getStackBuilder().withId(id1).withName(name1).withWorkspaceConfig(workspaceConfig).build();
      stacks.push(stack1);

      let name2 = 'testStack2',
          id2 = 'testStackId2',
          stack2 = cheAPIBuilder.getStackBuilder().withId(id2).withName(name2).withWorkspaceConfig(workspaceConfig).build();
      stacks.push(stack2);

      let name3 = 'testStack3',
          id3 = 'testStackId3',
          stack3 = cheAPIBuilder.getStackBuilder().withId(id3).withName(name3).withWorkspaceConfig(workspaceConfig).build();
      stacks.push(stack3);

      cheBackend.addStacks(stacks);
      cheBackend.setup();

    }]);

};
