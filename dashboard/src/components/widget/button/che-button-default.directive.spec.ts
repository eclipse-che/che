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

import {CheHttpBackend} from '../../api/test/che-http-backend';

interface ITestScope extends ng.IScope {
  model: {
    href?: string;
    target?: string;
    tabindex?: number|string;
    ngHref?: any;
    ngClick?: any;
    ngDisabled?: any;
    cheButtonIcon?: string;
    cheButtonTitle?: string;
  };
}

describe(`cheButtonDefault >`, () => {

  let $scope: ITestScope ;

  let $compile: ng.ICompileService;

  let $timeout: ng.ITimeoutService;

  let compiledDirective;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$timeout_: ng.ITimeoutService,
                     _$rootScope_: ng.IRootScopeService,
                     _cheHttpBackend_: CheHttpBackend) => {
    $scope = _$rootScope_.$new() as ITestScope;
    $compile = _$compile_;
    $timeout = _$timeout_;

    const $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  beforeEach(() => {
    $scope.model = {};
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compileDirective(): void {

    let buttonTemplate = `<div><che-button-default`;
    if ($scope.model.cheButtonTitle) {
      buttonTemplate += ` che-button-title="{{model.cheButtonTitle}}" `;
    }
    if ($scope.model.cheButtonIcon) {
      // fa fa-check-square
      buttonTemplate += ` che-button-icon="{{model.cheButtonIcon}}" `;
    }
    if ($scope.model.href) {
      buttonTemplate += ` href="{{model.href}}" `;
    }
    if ($scope.model.ngHref !== undefined) {
      buttonTemplate += ` ng-href="model.ngHref" `;
    }
    if ($scope.model.ngClick !== undefined) {
      buttonTemplate += ` ng-click="model.ngClick()" `;
    }
    if ($scope.model.tabindex) {
      buttonTemplate += ` tabindex="{{model.tabindex}}" `;
    }
    if ($scope.model.target) {
      buttonTemplate += ` target="{{model.target}}" `;
    }
    if ($scope.model.ngDisabled !== undefined) {
      buttonTemplate += ` ng-disabled="model.ngDisabled" `;
    }
    buttonTemplate += `></che-button-default></div>`;

    compiledDirective = $compile(angular.element( buttonTemplate ))($scope);
    $timeout.flush();
    $scope.$digest();
  }

  it(`directive compiled successfully >`, () => {
    $scope.model.cheButtonTitle = 'Test button title';
    compileDirective();

    expect(compiledDirective.find('che-button-default').length).toEqual(1);
  });

  describe(`BUTTON >`, () => {

    it(`should have correct title >`, () => {
      $scope.model.cheButtonTitle = 'Test button title';
      compileDirective();

      expect(compiledDirective.find('button').html()).toContain('Test button title');
    });

    describe(`'tabindex' attribute >`, () => {

      beforeEach(() => {
        $scope.model.cheButtonTitle = 'Test button title';
        $scope.model.tabindex = '3';
        compileDirective();
      });

      it(`should be applied correctly >`, () => {
        expect(compiledDirective.find('button').attr('tabindex')).toEqual($scope.model.tabindex);
      });

      it(`should be removed from top-level element >`, () => {
        expect(compiledDirective.find('che-button-default').attr('tabindex')).toBeUndefined();
      });

    });

    describe(`ngDisable directive >`, () => {
      let buttonEl;

      beforeEach(() => {
        $scope.model.cheButtonTitle = 'Test button title';
        $scope.model.ngDisabled = false;

        compileDirective();
        buttonEl = compiledDirective.find('button');
      });

      it(`should be applied >`, () => {
        expect(buttonEl.attr('ng-disabled')).toBeDefined();
      });

      it(`should add 'disable' attribute >`, () => {
        expect(buttonEl.attr('disabled')).toBeFalsy();

        $scope.model.ngDisabled = true;
        $scope.$digest();

        expect(buttonEl.attr('disabled')).toBeTruthy();
      });

    });

    describe(`ngClick directive >`, () => {
      let buttonEl;
      let counter = 0;

      beforeEach(() => {
        $scope.model.ngClick = jasmine.createSpy('ngClick')
          .and.callFake(() => {
            // count number of calls
            counter++;
        });
        $scope.model.ngDisabled = false;
        $scope.model.cheButtonTitle = 'Test button title';
        compileDirective();

        buttonEl = compiledDirective.find('button');
      });

      it(`should call a callback when clicked >`, () => {
        buttonEl.click();
        $scope.$digest();

        expect($scope.model.ngClick).toHaveBeenCalled();
      });

      it(`should call a callback exactly once >`, () => {
        counter = 0;

        buttonEl.click();
        $scope.$digest();

        expect(counter).toEqual(1);
      });

      describe(`when button is disabled >`, () => {

        it(`shouldn't to call a callback when clicked >`, () => {
          $scope.model.ngDisabled = true;
          $scope.$digest();

          expect($scope.model.ngClick).not.toHaveBeenCalled();
        });

      });

    });

  });

  describe(`A >`, () => {

    it(`should have correct title >`, () => {
      $scope.model.cheButtonTitle = 'Test button title';
      $scope.model.href = 'http://';
      compileDirective();

      expect(compiledDirective.find('a').html()).toContain('Test button title');
    });

    it(`should correctly apply 'href' attribute >`, () => {
      $scope.model.cheButtonTitle = 'Test button title';
      $scope.model.href = 'http://';
      compileDirective();

      expect(compiledDirective.find('a').attr('href')).toContain($scope.model.href);
    });

    it(`should correctly apply 'ng-href' attribute >`, () => {
      $scope.model.cheButtonTitle = 'Test button title';
      $scope.model.ngHref = () => { return 'http://'; };
      compileDirective();

      expect(compiledDirective.find('a').attr('ng-href')).toBeDefined();
    });

    it(`should correctly apply 'target' attribute >`, () => {
      $scope.model.cheButtonTitle = 'Test button title';
      $scope.model.ngHref = 'http://';
      $scope.model.target = '_self';
      compileDirective();

      expect(compiledDirective.find('a').attr('target')).toEqual('_self');
    });

  });

});
