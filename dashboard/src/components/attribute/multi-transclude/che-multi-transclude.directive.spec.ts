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
import {CheHttpBackend} from '../../api/test/che-http-backend';

/**
 * Test of the CheMultiTransclude directive.
 *
 * @author Oleksii Kurinnyi
 */
describe('CheMultiTransclude >', () => {

  let $rootScope: ng.IRootScopeService,
      $timeout: ng.ITimeoutService,
      $compile: ng.ICompileService,
      compiledDirective: ng.IAugmentedJQuery;

  /**
   * Backend for handling http operations
   */
  let httpBackend: ng.IHttpBackendService;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$timeout_: ng.ITimeoutService,
                     _$compile_: ng.ICompileService,
                     _$rootScope_: ng.IRootScopeService,
                     cheHttpBackend: CheHttpBackend) => {
    $rootScope = _$rootScope_.$new();
    $timeout = _$timeout_;
    $compile = _$compile_;

    httpBackend = cheHttpBackend.getHttpBackend();
    // avoid tracking requests from branding controller
    httpBackend.whenGET(/.*/).respond(200, '');
    httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  const part1 = '<span id="my-text">My text</span>';
  const part2 = '<che-button-default che-button-title="Click" name="myButton"></che-button-default>';

  function getCompiledElement() {
    const transcludeFunc = function ($scope: ng.IScope, $cloneAttachFn: ng.ICloneAttachFunction): ng.IAugmentedJQuery {
        const transcludingContent = angular.element(
          `<div che-multi-transclude-part="one">${part1}</div>
           <div che-multi-transclude-part="two">${part2}</div>`
        );
        $cloneAttachFn(transcludingContent, $scope);
        return transcludingContent;
      },
      element = $compile(
        angular.element(
          `<div><div che-multi-transclude>
             <div che-multi-transclude-target="one"></div>
             <div che-multi-transclude-target="two"></div>
           </div></div>`
        ), transcludeFunc as ng.ITranscludeFunction)($rootScope);
    $rootScope.$digest();
    return element;
  }

  beforeEach(() => {
    compiledDirective = getCompiledElement();
  });

  describe('first part, span >', () => {

    it('span with text should be transcluded >', () => {
      $timeout.flush();

      expect(compiledDirective.find('span#my-text').length).toEqual(1);
      expect(compiledDirective.html()).toContain('My text');
    });

  });

  describe('second part, directive >', () => {

    it('directive should be transcluded and compiled >', () => {
      $timeout.flush();

      expect(compiledDirective.find('button.md-button').length).toBeTruthy();
      expect(compiledDirective.html()).toContain('Click');
    });

  });

});
