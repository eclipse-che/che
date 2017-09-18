/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
import {CheHttpBackend} from '../../../../components/api/test/che-http-backend';
interface ITestScope extends ng.IRootScopeService {
  model: {
    workspaceStatus?: string;
    onStopWorkspace: Function;
    onRunWorkspace: Function;
  };
}

const STARTING = 'STARTING';
const RUNNING = 'RUNNING';
const SNAPSHOTTING = 'SNAPSHOTTING';
const STOPPING = 'STOPPING';
const STOPPED = 'STOPPED';
const STOP_WITH_SNAPSHOT = 'Stop with snapshot';
const STOP_WITHOUT_SNAPSHOT = 'Stop without snapshot';

/**
 * Test of the WorkspaceStatusButton directive.
 * @author Oleksii Orel
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

  function getCompiledElement(workspaceStatus?: string): ng.IRootElementService {
    $rootScope.model.workspaceStatus = workspaceStatus ? workspaceStatus : STOPPED;

    const element = $compile(angular.element(
      `<workspace-status-button workspace-status="model.workspaceStatus"
                                on-run-workspace="model.onRunWorkspace()"
                                on-stop-workspace="model.onStopWorkspace(isCreateSnapshot)"></workspace-status-button>`
    ))($rootScope);
    $rootScope.$digest();

    return element;
  }

  describe('initially STOPPED > ', () => {
    let jqstatusButton: JQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement();
      jqstatusButton = jqElement.find('#split-button button');
    });

    it('should have "Run" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqstatusButton).toBeTruthy();
      expect(jqstatusButton.attr('disabled')).toBeFalsy();
      expect($rootScope.model.workspaceStatus).toEqual(STOPPED);
      expect(jqstatusButton.html()).toContain('Run');
    });

    it('should call the runWorkspace callback', () => {
      spyOn($rootScope.model, 'onRunWorkspace');

      // click Run button
      jqstatusButton.click();
      $rootScope.$digest();

      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.workspaceStatus).toEqual(STOPPED);
      expect($rootScope.model.onRunWorkspace).toHaveBeenCalled();
    });
  });

  describe('initially STOPPING > ', () => {
    let jqstatusButton: JQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(STOPPING);
      jqstatusButton = jqElement.find('#split-button button');
    });

    it('should have "Stop" button disabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqstatusButton).toBeTruthy();
      expect(jqstatusButton.attr('disabled')).toBeTruthy();
      expect($rootScope.model.workspaceStatus).toEqual(STOPPING);
      expect(jqstatusButton.html()).toContain('Stop');
    });
  });

  describe('initially SNAPSHOTTING > ', () => {
    let jqstatusButton: JQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(SNAPSHOTTING);
      jqstatusButton = jqElement.find('#split-button button');
    });

    it('should have "Stop" button disabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqstatusButton).toBeTruthy();
      expect(jqstatusButton.attr('disabled')).toBeTruthy();
      expect($rootScope.model.workspaceStatus).toEqual(SNAPSHOTTING);
      expect(jqstatusButton.html()).toContain('Stop');
    });
  });

  describe('initially STARTING > ', () => {
    let jqstatusButton: JQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(STARTING);
      jqstatusButton = jqElement.find('#split-button button');
    });

    it('should have "Stop" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqstatusButton).toBeTruthy();
      expect(jqstatusButton.attr('disabled')).toBeFalsy();
      expect($rootScope.model.workspaceStatus).toEqual(STARTING);
      expect(jqstatusButton.html()).toContain('Stop');
    });

    it('should call the stopWorkspace callback without snapshot', () => {
      spyOn($rootScope.model, 'onStopWorkspace');

      // click Stop button
      jqstatusButton.click();
      $rootScope.$digest();

      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.workspaceStatus).toEqual(STARTING);
      expect($rootScope.model.onStopWorkspace).toHaveBeenCalledWith(false);
    });
  });

  describe('initially RUNNING > ', () => {
    let jqstatusButton: JQuery,
      jqDropDownButton: JQuery,
      jqMenuItems: JQuery;

    beforeEach(() => {
      const jqElement = getCompiledElement(RUNNING);
      jqstatusButton = jqElement.find('#split-button button');
      jqDropDownButton = jqElement.find('che-button-default.drop-down');
      jqMenuItems = jqElement.find('ul.area-drop-down li');

      // need to investigate this extra click
      jqstatusButton.click();
      $rootScope.$digest();
    });

    it('should have "Stop" button enabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(jqstatusButton).toBeTruthy();
      expect(jqstatusButton.attr('disabled')).toBeFalsy();
      expect($rootScope.model.workspaceStatus).toEqual(RUNNING);
      expect(jqstatusButton.html()).toContain('Stop');
    });

    it('should open dropDown after click on button', () => {
      // open popup menu
      jqDropDownButton.click();
      $rootScope.$digest();

      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.workspaceStatus).toEqual(RUNNING);
      expect(jqDropDownButton.attr('aria-expanded')).toEqual('true');
    });

    it('should call the stopWorkspace callback with snapshot', () => {
      // open popup menu
      jqDropDownButton.click();
      $rootScope.$digest();
      spyOn($rootScope.model, 'onStopWorkspace');

      // click Stop With Snapshot button
      jqMenuItems.find(`span:contains(${STOP_WITH_SNAPSHOT})`).mousedown();

      $rootScope.$digest();
      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.workspaceStatus).toEqual(RUNNING);
      expect($rootScope.model.onStopWorkspace).toHaveBeenCalledWith(true);
    });

    it('should call the stopWorkspace callback without snapshot', () => {
      // open popup menu
      jqDropDownButton.click();
      $rootScope.$digest();
      spyOn($rootScope.model, 'onStopWorkspace');

      // click Stop Without Snapshot button
      jqMenuItems.find(`span:contains(${STOP_WITHOUT_SNAPSHOT})`).mousedown();

      $rootScope.$digest();
      // timeout should be flashed to get callback called and content visible
      $timeout.flush();

      expect($rootScope.model.workspaceStatus).toEqual(RUNNING);
      expect($rootScope.model.onStopWorkspace).toHaveBeenCalledWith(false);
    });
  });
});
