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
import {CheHttpBackend} from '../../../../components/api/test/che-http-backend';
import {CheWorkspace, WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';
import {CheAPIBuilder} from '../../../../components/api/builder/che-api-builder.factory';

interface ITestScope extends ng.IRootScopeService {
  model: {
    workspaceId: string;
  };
}

/**
 * Test of the workspace status widget directive.
 * @author Oleksii Orel
 */
describe('WorkspaceStatus >', () => {

  let $rootScope: ITestScope,
    $timeout: ng.ITimeoutService,
    $compile: ng.ICompileService,
    httpBackend: ng.IHttpBackendService;

  /**
   * Workspace Factory for the test
   */
  let workspaceFactory;
  /**
   * API builder
   */
  let apiBuilder: CheAPIBuilder;
  /**
   * Che backend
   */
  let cheBackend: CheHttpBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));


  beforeEach(inject((_$compile_: ng.ICompileService, _$timeout_: ng.ITimeoutService, _$rootScope_: ng.IRootScopeService, cheWorkspace: CheWorkspace, cheAPIBuilder: CheAPIBuilder, cheHttpBackend: CheHttpBackend) => {
    $rootScope = <ITestScope>_$rootScope_.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;

    workspaceFactory = cheWorkspace;
    apiBuilder = cheAPIBuilder;
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
    httpBackend.when('OPTIONS', '/api/').respond({});

    $rootScope.model = {
      workspaceId: 'testWorkspaceId'
    };
  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function getCompiledElement(status: WorkspaceStatus): ng.IAugmentedJQuery {
    const defaultEnvironment = 'default';
    const environments = {
      [defaultEnvironment]: {
        'machines': {
          'dev-machine': {
            'installers': ['org.eclipse.che.ws-agent']
          }
        },
        'recipe': {
          'type': 'compose',
          'content': 'services:\n dev-machine:\n image: eclipse/ubuntu_jdk8\n',
          'contentType': 'application/x-yaml'
        }
      }
    };
    const workspace = apiBuilder.getWorkspaceBuilder().withId($rootScope.model.workspaceId).withEnvironments(<any>environments).withDefaultEnvironment('default').withStatus(WorkspaceStatus[status]).build();
    // add workspaces on Http backend
    cheBackend.addWorkspaces([workspace]);
    // setup backend
    cheBackend.setup();
    // fetch workspaces
    workspaceFactory.fetchWorkspaces();
    // flush command
    httpBackend.flush();

    const element = $compile(angular.element(
      `<che-workspace-status workspace-id="model.workspaceId"></che-workspace-status>`
    ))($rootScope);
    $rootScope.$digest();

    return element;
  }

  describe('initially RUN button >', () => {
    let jqElement: ng.IAugmentedJQuery;

    beforeEach(() => {
      jqElement = getCompiledElement(WorkspaceStatus.STOPPED);
    });

    it('should be enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqElement.get(0)).toBeTruthy();
      expect(jqElement.attr('disabled')).toBeFalsy();
      expect(jqElement.find('.fa-play')).toBeTruthy();
    });

    it('should call the run workspace callback on click', () => {
      // timeout should be flashed
      $timeout.flush();

      // expecting POSTs
      httpBackend.expectPOST('/api/workspace/' + $rootScope.model.workspaceId + '/runtime?environment=default').respond(200, {});

      jqElement.find('.workspace-status').click();

      expect(jqElement.get(0)).toBeTruthy();
      expect(jqElement.attr('disabled')).toBeFalsy();
      expect(jqElement.find('.fa-play')).toBeTruthy();
    });
  });

  describe('initially STOP button >', () => {
    let jqElement: ng.IAugmentedJQuery;

    beforeEach(() => {
      jqElement = getCompiledElement(WorkspaceStatus.RUNNING);
    });

    it('should be enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqElement.get(0)).toBeTruthy();
      expect(jqElement.attr('disabled')).toBeFalsy();
      expect(jqElement.find('.fa-stop')).toBeTruthy();
    });

    it('should call the stop workspace callback on click', () => {
      // timeout should be flashed
      $timeout.flush();

      // expecting DELETEs
      httpBackend.expectDELETE('/api/workspace/' + $rootScope.model.workspaceId + '/runtime').respond(200);

      jqElement.find('.workspace-status').click();

      expect(jqElement.get(0)).toBeTruthy();
      expect(jqElement.attr('disabled')).toBeFalsy();
      expect(jqElement.find('.fa-stop')).toBeTruthy();
    });
  });

});
