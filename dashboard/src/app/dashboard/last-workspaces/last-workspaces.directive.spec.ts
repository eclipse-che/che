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
import {IAugmentedJQuery, ITemplateCacheService, ICompileService, IRootScopeService, ICompileProvider} from 'angular';
import {CheHttpBackend} from '../../../components/api/test/che-http-backend';

/**
 * @author Lucia Jelinkova
 */
describe(`Last workspaces directive >`, () => {

  let $compile: ICompileService;
  let $scope: any;
  let directiveElement: IAugmentedJQuery;

  beforeAll(() => {
    // this call replaces the inner directive <che-workspace-item> with mocked value.
    // the reason is that we want to test only <dashboard-last-workspaces> itself and not
    // underlying directives. Those should be tested separately.
    // NOTE: it is possible that if another test mocks the same directive, it will fail. In that
    // case the whole call needs to be extracted and executed before all .spec files.
    angular.module('userDashboard').config(function ($compileProvider: ICompileProvider) {
      $compileProvider.directive('cheWorkspaceItem', function () {
        var def = {
          // directive with the highest priority is executed first
          priority: 100,
          // if set to false also directives with lower priorities would be executed
          terminal: true,
          // the same as original directive
          restrict: 'E',
          // mocked output
          template: '<div class="workspace-mock">Mocked workspace</div>',
        };
        return def;
      });
    });
  });

  beforeEach(() => {
    angular.mock.module('userDashboard');

    inject((
      _$compile_: ng.ICompileService,
      _$rootScope_: IRootScopeService,
      _cheHttpBackend_: CheHttpBackend) => {

      $compile = _$compile_;
      $scope = _$rootScope_.$new();

      let $httpBackend = _cheHttpBackend_.getHttpBackend();
      $httpBackend.whenGET(/.*/).respond(200, '');
      $httpBackend.when('OPTIONS', '/api/').respond({});
    })
  });

  beforeEach(() => {
    directiveElement = $compile(" <dashboard-last-workspaces></dashboard-last-workspaces>")($scope);
    $scope.$digest();
  });

  it('no workspaces', async () => {
    noWorkspacesDirectiveTest();
  });

  it('one workspace', async () => {
    moreWorkspacesDirectiveTest(1);
  });

  it('5 workspaces', async () => {
    moreWorkspacesDirectiveTest(5);
  });

  it('6 workspaces', async () => {
    moreWorkspacesDirectiveTest(6, 5);
  });

  it('progress bar - loading', async () => {
    $scope.dashboardLastWorkspacesController.isLoading = true;
    $scope.$digest();

    let progressBar = directiveElement.find('md-progress-linear');
    let mainDiv = directiveElement.find('#last-workspaces');

    expect(progressBar.length).toBe(1);
    expect(progressBar.attr('class')).not.toContain('ng-hide');
    expect(mainDiv.length).toBe(1);
    expect(mainDiv.attr('class')).toContain('ng-hide');
  });

  it('progress bar - not loading', async () => {
    $scope.dashboardLastWorkspacesController.isLoading = false;
    $scope.$digest();

    let progressBar = directiveElement.find('md-progress-linear');
    let mainDiv = directiveElement.find('#last-workspaces');

    expect(progressBar.length).toBe(1);
    expect(progressBar.attr('class')).toContain('ng-hide');
    expect(mainDiv.length).toBe(1);
    expect(mainDiv.attr('class')).not.toContain('ng-hide');
  });

  function noWorkspacesDirectiveTest() {
    $scope.dashboardLastWorkspacesController.workspaces = [];
    $scope.$digest();

    let emptyLabel = directiveElement.find('.last-workspaces-empty-label');
    let workspaceList = directiveElement.find('#last-workspaces-list');
    let workspaceItems = directiveElement.find('.workspace-mock');

    expect(emptyLabel.length).toBe(1);
    expect(emptyLabel.attr('class')).not.toContain('ng-hide');
    expect(workspaceList.length).toBe(1);
    expect(workspaceList.attr('class')).toContain('ng-hide');
    expect(workspaceItems.length).toBe(0);
  }

  function moreWorkspacesDirectiveTest(workspacesCount: number, workspacesDisplayedCount: number = workspacesCount) {
    $scope.dashboardLastWorkspacesController.workspaces =
      Array.from(new Array(workspacesCount)).map((x, i) => {
        return {}
      });
    $scope.$digest();

    let emptyLabel = directiveElement.find('.last-workspaces-empty-label');
    let workspaceList = directiveElement.find('#last-workspaces-list');
    let workspaceItems = directiveElement.find('.workspace-mock');

    expect(emptyLabel.length).toBe(1);
    expect(emptyLabel.attr('class')).toContain('ng-hide');
    expect(workspaceList.length).toBe(1);
    expect(workspaceList.attr('class')).not.toContain('ng-hide');
    expect(workspaceItems.length).toBe(workspacesDisplayedCount);
  }
});

