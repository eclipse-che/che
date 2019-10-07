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

import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';
import {WorkspacesService} from './workspaces.service';

/**
 * WorkspacesService tests.
 * 
 * @author Oleksii Orel
 */
describe(`WorkspacesService >`, () => {

  /**
   * Service to test.
   */
  let workspacesService: WorkspacesService;
  let cheWorkspace: CheWorkspace;

  function getCHE6Workspace(recipeType?: string): che.IWorkspace {
    return {
      'namespace': 'che',
      'status': 'RUNNING',
      'config': {
        'attributes': {},
        'environments': {
          'default': {
            'recipe': {
              'type': recipeType ? recipeType : 'dockerimage', 
              'content': 'eclipse/ubuntu_jdk8'
            },
            'machines': {
              'dev-machine': {
                'env': {},
                'volumes': {},
                'installers': [
                  'org.eclipse.che.exec',
                  'org.eclipse.che.terminal'
                ],
                'servers': {
                  'tomcat8-debug': {'protocol': 'http', 'port': '8000'},
                  'codeserver': {'protocol': 'http', 'port': '9876'},
                  'tomcat8': {'protocol': 'http', 'port': '8080'}
                },
                'attributes': {'memoryLimitBytes': '2147483648'}
              }
            }
          }
        }, 'projects': [], 'commands': [], 'defaultEnv': 'default', 'name': 'wksp-98cs'
      },
      'temporary': false,
      'links': {
        'self': '...Fake self link...',
        'ide': '...Fake ide link...'
      },
      'id': 'workspacezbkov1e8qcm00dli',
      'attributes': {
        'created': 1516282666658,
        'stackId': '...Fake stack id...'
      }
    };
  }

  function getCHE7Workspace(recipeType?: string): che.IWorkspace {
    return {
      'namespace': 'che',
      'status': 'RUNNING',
       'config': {
        'defaultEnv': 'default',
        'environments': {
        'default': {
          'machines': {},
          'recipe': {
            'type': recipeType ? recipeType : 'kubernetes',
            'content': '...Fake recipe content...',
            'contentType': '...Fake recipe contentType...'
          }
        }
      },
      'attributes': {
        'editor': 'eclipse/che-theia/0.0.1',
        'plugins': 'eclipse/che-machine-exec-plugin/0.0.1'
      },
      'projects': [],
      'name': 'wksp-7znm',
      'commands': []
      },
      'temporary': false,
      'links': {
        'self': '...Fake self link...',
        'ide': '...Fake ide link...'
      },
      'id': 'workspacezbkov1e8qcm00dli',
      'attributes': {
        'created': 1516282666658,
        'stackId': '...Fake stack id...'
      }
    };
  }

  /**
   * Setup module
   */
  beforeEach(() => {
    angular.mock.module('userDashboard');
  });

  beforeEach(() => {
    angular.module('workspacesServiceMock', [])
      .service('cheWorkspace', function () {
        this.getSupportedRecipeTypes = (): string[] => {
          return ['kubernetes','dockerimage','no-environment'];
        }
      });
    angular.mock.module('workspacesServiceMock');
  });

  beforeEach(inject((_workspacesService_: WorkspacesService,
                     _cheWorkspace_: CheWorkspace) => {
    cheWorkspace = _cheWorkspace_;
    workspacesService = _workspacesService_;
  }));

  describe(`supported workspace version >`, () => {

    it(`should support a CHE7 workspace >`, () => {
      const newCHE7Workspace = getCHE7Workspace();
      expect(workspacesService.isSupported(newCHE7Workspace)).toBeTruthy();
      expect(workspacesService.isSupportedVersion(newCHE7Workspace)).toBeTruthy();
    });

    it(`shouldn't support a CHE6 workspace >`, () => {
      const oldCHE6Workspace = getCHE6Workspace();
      expect(workspacesService.isSupported(oldCHE6Workspace)).toBeFalsy();
      expect(workspacesService.isSupportedVersion(oldCHE6Workspace)).toBeFalsy();
    });

  });

  describe(`supported recipe type >`, () => {

    it(`should support a default recipe type >`, () => {
      const newCHE7Workspace = getCHE7Workspace();
      expect(workspacesService.isSupported(newCHE7Workspace)).toBeTruthy();
      expect(workspacesService.isSupportedRecipeType(newCHE7Workspace)).toBeTruthy();
    });

    it(`shouldn't support a fake recipe type >`, () => {
      const recipeType = '...Fake recipeType...';
      const newCHE7Workspace = getCHE7Workspace(recipeType);
      expect(workspacesService.isSupported(newCHE7Workspace)).toBeFalsy();
      expect(workspacesService.isSupportedRecipeType(newCHE7Workspace)).toBeFalsy();
    });

  });

});
