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
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {CheAPIBuilder} from '../../../components/api/builder/che-api-builder.factory';
import {CheHttpBackend} from '../../../components/api/test/che-http-backend';
import IdeSvc from '../../ide/ide.service';
import {CheBranding} from '../../../components/branding/che-branding.factory';

/**
 * Test of the NavbarRecentWorkspacesController
 */
describe('NavbarRecentWorkspacesController', () => {
  /**
   * NavbarRecentWorkspacesController
   */
  let navbarRecentWorkspacesController;

  /**
   * API builder
   */
  let apiBuilder: CheAPIBuilder;

  /**
   * Backend for handling http operations
   */
  let httpBackend: ng.IHttpBackendService;

  /**
   * Che backend
   */
  let cheBackend: CheHttpBackend;


  let workspaces: Array<che.IWorkspace>;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject(($rootScope: ng.IRootScopeService, cheWorkspace: CheWorkspace, cheBranding: CheBranding, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend, $controller: any, ideSvc: IdeSvc, $window: ng.IWindowService, $log: ng.ILogService) => {
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();

    let scope = $rootScope.$new();
    navbarRecentWorkspacesController = $controller('NavbarRecentWorkspacesController', {
      ideSvc: IdeSvc, cheWorkspace: cheWorkspace, cheBranding: cheBranding, $window: $window, $log: $log, $scope: scope, $rootScope: $rootScope
    });

    workspaces = [];
    for (let i = 0; i < 20; ++i) {
      let wrkspId = 'workspaceId' + i;
      let wrkspName = 'testName' + i;
      let wrkspCreateDate = new Date(2001, 1, 1, i, 1).toString();
      let wrkspUpdateDate = new Date(2001, 1, 1, i, 2).toString();
      let wrkspAttr = <che.IWorkspaceAttributes>{'created': Date.parse(wrkspCreateDate), 'updated': Date.parse(wrkspUpdateDate)};
      let workspace = apiBuilder.getWorkspaceBuilder().withId(wrkspId).withAttributes(wrkspAttr).withName(wrkspName).build();
      workspaces.push(workspace);
    }
    // shuffle the workspaces
    workspaces.sort(() => {
      return 0.5 - Math.random();
    });
    // providing request
    // add workspaces on Http backend
    cheBackend.addWorkspaces(workspaces);

    // setup backend
    cheBackend.setup();

    // fetch workspaces
    cheWorkspace.fetchWorkspaces();

    // flush command
    httpBackend.flush();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  /**
   * Check sorting rule for recent workspaces
   */
  it('Check very recent workspaces', inject(() => {
      // get recentWorkspaces
      let recentWorkspaces = navbarRecentWorkspacesController.getRecentWorkspaces();

      // check max length
      expect(recentWorkspaces.length).toEqual(navbarRecentWorkspacesController.maxItemsNumber);

      // prepare test objects
      let testWorkspaces: Array<che.IWorkspace> = angular.copy(workspaces);
      testWorkspaces.sort((workspace1: che.IWorkspace, workspace2: che.IWorkspace) => {
        return workspace2.attributes.updated - workspace1.attributes.updated;
      });
      let veryRecentWorkspaceId = testWorkspaces[testWorkspaces.length - 1].id;

      // check default sorting
      let lastPosition = recentWorkspaces.length - 1;
      for (let i = 0; i < lastPosition; i++) {
        expect(recentWorkspaces[i].id).toEqual(testWorkspaces[i].id);
      }
      // check the last one workspace is equal to the last test workspace and not equal to the very recent workspace,
      // because we are going to update very recent workspace in controller and sorting rule should be changed
      expect(recentWorkspaces[lastPosition].id).toEqual(testWorkspaces[lastPosition].id);
      expect(recentWorkspaces[lastPosition].id).not.toEqual(veryRecentWorkspaceId);

      // update very recent workspace
      navbarRecentWorkspacesController.updateRecentWorkspace(veryRecentWorkspaceId);
      recentWorkspaces = navbarRecentWorkspacesController.getRecentWorkspaces();

      // check sorting with veryRecentWorkspace
      for (let i = 0; i < lastPosition; i++) {
        expect(recentWorkspaces[i].id).toEqual(testWorkspaces[i].id);
      }
      // check the last one workspace is equal to the very recent workspace and not equal to the last test workspace
      expect(recentWorkspaces[lastPosition].id).not.toEqual(testWorkspaces[lastPosition].id);
      expect(recentWorkspaces[lastPosition].id).toEqual(veryRecentWorkspaceId);
    })
  );
});
