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

import {IPlugin} from '../../../components/api/plugin-registry.factory';
import {WorkspaceDetailsService} from './workspace-details.service';
import {CheHttpBackend} from '../../../components/api/test/che-http-backend';

/**
 * WorkspaceDetailsService tests.
 *
 * @author Oleksii Orel
 */
describe(`WorkspaceDetailsService >`, () => {

  /**
   * Service to test.
   */
  let workspaceDetailsService: WorkspaceDetailsService;

  let cheHttpBackend: CheHttpBackend;

  let $httpBackend: ng.IHttpBackendService;

  function getPlugins(): Array<IPlugin> {
    return [{
      id: 'camel-tooling/vscode-apache-camel/0.0.14',
      deprecate: {
        automigrate: true,
        migrateTo: 'camel-tooling/vscode-apache-camel/latest'
      },
      name: 'vscode-apache-camel',
      publisher: 'camel-tooling',
      displayName: 'Language Support for Apache Camel',
      type: 'VS Code extension'
    }, {
      id: 'camel-tooling/vscode-apache-camel/latest',
      name: 'vscode-apache-camel',
      publisher: 'camel-tooling',
      displayName: 'Language Support for Apache Camel',
      type: 'VS Code extension'
    }, {
      id: 'eclipse/che-theia/7.2.0',
      deprecate: {
        automigrate: true,
        migrateTo: 'eclipse/che-theia/latest'
      },
      name: 'che-theia',
      publisher: 'eclipse',
      displayName: 'theia-ide',
      type: 'Che Editor'
    }, {
      id: 'eclipse/che-theia/latest',
      name: 'che-theia',
      publisher: 'eclipse',
      displayName: 'theia-ide',
      type: 'Che Editor'
    }];
  }

  function getNewWorkspace(): che.IWorkspace {
    return {
      devfile: {
        metadata: {
          name: 'wksp-i8cb55'
        },
        apiVersion: '1.0.0',
        components: [
          {
            id: 'eclipse/che-theia/latest',
            type: 'cheEditor'
          }, {
            id: 'camel-tooling/vscode-apache-camel/latest',
            type: 'chePlugin'
          }
        ]
      },
      id: 'workspacefbymkrh72ptwudud',
      namespace: 'che'
    }
  }

  function getWorkspaceWithDeprecatedPlugins(): che.IWorkspace {
    return {
      devfile: {
        metadata: {
          name: 'wksp-i8cb55'
        },
        apiVersion: '1.0.0',
        components: [
          {
            id: 'eclipse/che-theia/latest',
            type: 'cheEditor'
          }, {
            id: 'camel-tooling/vscode-apache-camel/0.0.14',
            type: 'chePlugin'
          }
        ]
      },
      id: 'workspacefbymkrh72ptwudud',
      namespace: 'che'
    }
  }

  function getWorkspaceWithDeprecatedEditor(): che.IWorkspace {
    return {
      devfile: {
        metadata: {
          name: 'wksp-i8cb55'
        },
        apiVersion: '1.0.0',
        components: [
          {
            id: 'eclipse/che-theia/7.2.0',
            type: 'cheEditor'
          }, {
            id: 'camel-tooling/vscode-apache-camel/latest',
            type: 'chePlugin'
          }
        ]
      },
      id: 'workspacefbymkrh72ptwudud',
      namespace: 'che'
    }
  }

  /**
   * Setup module
   */
  beforeEach(() => {
    angular.mock.module('userDashboard');
  });

  beforeEach(inject((_workspaceDetailsService_: WorkspaceDetailsService,
                     _cheHttpBackend_: CheHttpBackend) => {

    workspaceDetailsService = _workspaceDetailsService_;
    cheHttpBackend = _cheHttpBackend_;
    $httpBackend = cheHttpBackend.getHttpBackend();

    cheHttpBackend.setPlugins(getPlugins());
    cheHttpBackend.setup();

    $httpBackend.flush();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

  describe(`deprecated workspace plugins >`, () => {

    it(`returns an empty array if workspace doesn't have any selected deprecated plugin`, () => {
      const newWorkspace = getNewWorkspace();

      expect(workspaceDetailsService.getSelectedDeprecatedPlugins(newWorkspace)).toEqual([]);
    });

    it(`returns an array with deprecated plugins which is selected in the workspace`, () => {
      const workspaceWithDeprecatedPlugins = getWorkspaceWithDeprecatedPlugins();

      expect(workspaceDetailsService.getSelectedDeprecatedPlugins(workspaceWithDeprecatedPlugins)).toEqual(['camel-tooling/vscode-apache-camel/0.0.14']);
    });

  });

  describe(`deprecated workspace editor >`, () => {

    it(`returns an empty string if workspace doesn't have a selected deprecated editor`, () => {
      const newWorkspace = getNewWorkspace();

      expect(workspaceDetailsService.getSelectedDeprecatedEditor(newWorkspace)).toEqual('');
    });

    it(`returns a string with editorId if workspace has a selected deprecated editor`, () => {
      const workspaceWithDeprecatedEditor = getWorkspaceWithDeprecatedEditor();

      expect(workspaceDetailsService.getSelectedDeprecatedEditor(workspaceWithDeprecatedEditor)).toEqual('eclipse/che-theia/7.2.0');
    });

  });
});
