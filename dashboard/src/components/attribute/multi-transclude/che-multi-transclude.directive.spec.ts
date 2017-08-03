/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
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

  const part1 = '<button type="button" name="my-button">Click</button>';
  const part2 = '<div><span>My text.</span></div>';

  function getCompiledElement() {
    const element = $compile(
      angular.element(
        `<div>
          <div che-multi-transclude>
            <div target="one"></div>
            <div target="two"></div>
          </div>
        </div>`
      ), function($scope: ng.IScope, cloneAttachFn: ng.ICloneAttachFunction): ng.IAugmentedJQuery {
        const transcludingContent: ng.IAugmentedJQuery = angular.element(
          `<div part="one">${part1}</div>
           <div part="two">${part2}</div>`
        );
        cloneAttachFn(transcludingContent);
        return transcludingContent;
      }
    )($rootScope);
    $rootScope.$digest();
    return element;
  }

  beforeEach(() => {
    compiledDirective = getCompiledElement();
  });

  it('should transclude multiple parts >', () => {
    $timeout.flush();

    expect(compiledDirective.html()).toContain(part1);
    expect(compiledDirective.html()).toContain(part2);
  });

});
