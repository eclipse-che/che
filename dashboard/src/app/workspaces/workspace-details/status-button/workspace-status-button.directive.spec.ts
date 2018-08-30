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
import {WorkspaceStatus} from '../../../../components/api/workspace/che-workspace.factory';

interface ITestScope extends ng.IRootScopeService {
  model: {
    workspaceStatus?: string;
    buttonDisabled?: boolean;
    onStopWorkspace: Function;
    onRunWorkspace: Function;
  };
}

/**
 * Test of the WorkspaceStatusButton directive.
 * @author Oleksii Orel
 * @author Oleksii Kurinnyi
 */
describe('WorkspaceStatusButton >', () => {

  let $rootScope: ITestScope,
    $timeout: ng.ITimeoutService,
    $compile: ng.ICompileService;

  /**
   * Backend for handling http operations
   */
  let httpBackend: ng.IHttpBackendService;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService, _$timeout_: ng.ITimeoutService, _$rootScope_: ng.IRootScopeService, cheHttpBackend: CheHttpBackend) => {
    $rootScope = <ITestScope>_$rootScope_.$new();
    $compile = _$compile_;
    $timeout = _$timeout_;

    httpBackend = cheHttpBackend.getHttpBackend();
    httpBackend.whenGET(/.*/).respond(200, '');
    httpBackend.when('OPTIONS', '/api/').respond({});

    $rootScope.model = {
      onStopWorkspace: angular.noop,
      onRunWorkspace: angular.noop
    };

  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function getCompiledElement(status: WorkspaceStatus): ng.IAugmentedJQuery {
    $rootScope.model.workspaceStatus = WorkspaceStatus[status];

    const element = $compile(angular.element(
      `<workspace-status-button workspace-status="model.workspaceStatus"
                                button-disabled="model.buttonDisabled"
                                on-run-workspace="model.onRunWorkspace()"
                                on-stop-workspace="model.onStopWorkspace()"></workspace-status-button>`
    ))($rootScope);
    $rootScope.$digest();

    return element;
  }

  describe(`button is disabled >`, () => {
    let jqRunButton: ng.IAugmentedJQuery;
    let jqStopButton: ng.IAugmentedJQuery;

    beforeEach(() => {
      $rootScope.model.buttonDisabled = true;

      const jqElement = getCompiledElement(WorkspaceStatus.STOPPED);
      jqRunButton = jqElement.find('#run-workspace-button button');
      jqStopButton = jqElement.find('#stop-workspace-button button');
    });

    it('should have "Run" button disabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqRunButton.get(0)).toBeTruthy();
      expect(jqRunButton.attr('disabled')).toBeTruthy();

      expect(jqStopButton.get(0)).toBeFalsy();
    });

  });

  describe('initially STOPPED >', () => {
    let jqRunButton: ng.IAugmentedJQuery;
    let jqStopButton: ng.IAugmentedJQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(WorkspaceStatus.STOPPED);
      jqRunButton = jqElement.find('#run-workspace-button button');
      jqStopButton = jqElement.find('#stop-workspace-button button');
    });

    it('should have "Run" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqRunButton.get(0)).toBeTruthy();
      expect(jqRunButton.attr('disabled')).toBeFalsy();

      expect(jqStopButton.get(0)).toBeFalsy();
    });

    it('should call the runWorkspace callback on "Run" button is clicked', () => {
      spyOn($rootScope.model, 'onRunWorkspace');

      // click Run button
      jqRunButton.click();
      $rootScope.$digest();

      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.onRunWorkspace).toHaveBeenCalled();
    });
  });

  describe('initially STOPPING >', () => {
    let jqRunButton: ng.IAugmentedJQuery;
    let jqStopButton: ng.IAugmentedJQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(WorkspaceStatus.STOPPING);
      jqRunButton = jqElement.find('#run-workspace-button button');
      jqStopButton = jqElement.find('#stop-workspace-button button');
    });

    it('should have "Stop" button disabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqRunButton.get(0)).toBeFalsy();

      expect(jqStopButton.get(0)).toBeTruthy();
      expect(jqStopButton.attr('disabled')).toBeTruthy();
    });
  });

  describe('initially STARTING >', () => {
    let jqRunButton: ng.IAugmentedJQuery;
    let jqStopButton: ng.IAugmentedJQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(WorkspaceStatus.STARTING);
      jqRunButton = jqElement.find('#run-workspace-button button');
      jqStopButton = jqElement.find('#stop-workspace-button button');
    });

    it('should have "Stop" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqRunButton.get(0)).toBeFalsy();

      expect(jqStopButton.get(0)).toBeTruthy();
      expect(jqStopButton.attr('disabled')).toBeFalsy();
    });

    it('should call the stopWorkspace callback on "Stop" button is clicked', () => {
      spyOn($rootScope.model, 'onStopWorkspace');

      // click Stop button
      jqStopButton.click();
      $rootScope.$digest();

      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.onStopWorkspace).toHaveBeenCalled();
    });
  });

  describe('initially RUNNING >', () => {
    let jqRunButton: ng.IAugmentedJQuery;
    let jqStopButton: ng.IAugmentedJQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(WorkspaceStatus.RUNNING);
      jqRunButton = jqElement.find('#run-workspace-button button');
      jqStopButton = jqElement.find('#stop-workspace-button button');
    });

    it('should have "Stop" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqRunButton.get(0)).toBeFalsy();

      expect(jqStopButton.get(0)).toBeTruthy();
      expect(jqStopButton.attr('disabled')).toBeFalsy();
    });

    it('should call the stopWorkspace callback on "Stop" button is clicked', () => {
      spyOn($rootScope.model, 'onStopWorkspace');

      jqStopButton.click();

      $rootScope.$digest();
      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.onStopWorkspace).toHaveBeenCalled();
    });

  });

});
