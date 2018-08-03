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
import {ICheButtonDropdownMainAction, ICheButtonDropdownOtherAction} from './che-button-dropdown.directive';

interface ITestScope extends ng.IScope {
  model: {
    mainActionConfig: ICheButtonDropdownMainAction;
    otherActionsConfig: ICheButtonDropdownOtherAction[];
    disabled: boolean;
  };
}

describe(`cheButtonDropdown >`, () => {

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
    $scope.model = {
      mainActionConfig: {
        title: 'Main Action',
        type: 'button'
      },
      otherActionsConfig: [{
        title: 'Second Action',
        type: 'button',
        action: jasmine.createSpy('secondAction.callback'),
        orderNumber: 2
      }, {
        title: 'First Action',
        href: 'http://example.com',
        type: 'link',
        orderNumber: 1
      }],
      disabled: false
    };
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compileDirective(): void {
    let buttonDropdownTemplate = `<div>
      <che-button-dropdown button-style="che-button-default"
                           main-action-config="model.mainActionConfig"
                           other-actions-config="model.otherActionsConfig"
                           button-disabled="model.disabled"></che-button-dropdown>
    </div>`;

    compiledDirective = $compile(angular.element( buttonDropdownTemplate ))($scope);
    $timeout.flush();
    $scope.$digest();
  }

  it(`directive compiled successfully >`, () => {
    compileDirective();

    expect(compiledDirective.find('.che-button-dropdown').length).toEqual(1);
  });

  describe(`the split button >`, () => {

    describe(`which type is BUTTON >`, () => {

      let splitButton;

      beforeEach(() => {
        $scope.model.mainActionConfig.action = jasmine.createSpy('mainAction.action');
        $scope.model.disabled = false;
        compileDirective();

        splitButton = compiledDirective.find('.split-button button');
      });

      it(`should have correct title> `, () => {
        expect(splitButton.text()).toEqual($scope.model.mainActionConfig.title);
      });

      it(`should call a callback when clicked >`, () => {
        splitButton.click();

        expect($scope.model.mainActionConfig.action).toHaveBeenCalled();
      });

      describe(`and it's disabled >`, () => {

        it(`shouldn't call a callback when clicked`, () => {
          $scope.model.disabled = true;
          $scope.$digest();

          expect($scope.model.mainActionConfig.action).not.toHaveBeenCalled();
        });

      });

    });

    describe(`which type is LINK >`, () => {

      let splitLinkButton;

      beforeEach(() => {
        $scope.model.mainActionConfig.type = 'link';
        $scope.model.mainActionConfig.href = 'http://example.com';
        compileDirective();

        splitLinkButton = compiledDirective.find('.split-button a');
      });

      it(`should have correct title >`, () => {
        expect(splitLinkButton.text()).toEqual($scope.model.mainActionConfig.title);
      });

      it(`should have correct url >`, () => {
        expect(splitLinkButton.attr('href')).toEqual($scope.model.mainActionConfig.href);
      });

    });

  });

  describe(`the dropdown menu >`, () => {

    beforeEach(() => {
      compileDirective();
    });

    it(`should have correct number of items >`, () => {
      expect(compiledDirective.find('li').length).toEqual($scope.model.otherActionsConfig.length);
    });

    describe(`the first other action >`, () => {

      let firstOtherActionEl, firstOtherActionConfig;

      describe(`which type is LINK >`, () => {

        beforeEach(() => {
          firstOtherActionConfig = $scope.model.otherActionsConfig.find((action: ICheButtonDropdownOtherAction) => {
            return action.orderNumber === 1;
          });

          firstOtherActionEl = compiledDirective.find('li a');
        });

        it(`should be named correctly`, () => {
          expect(firstOtherActionEl.text().trim()).toEqual(firstOtherActionConfig.title);
        });

        it(`should have correct url >`, () => {
          expect(firstOtherActionEl.attr('href')).toEqual(firstOtherActionConfig.href);
        });

      });

    });

    describe(`the second other action >`, () => {

      let secondOtherActionEl, secondOtherActionConfig;

      describe(`which type is BUTTON >`, () => {

        beforeEach(() => {
          secondOtherActionConfig = $scope.model.otherActionsConfig.find((action: ICheButtonDropdownOtherAction) => {
            return action.orderNumber === 2;
          });

          secondOtherActionEl = compiledDirective.find('li span');
        });

        it(`should have second action named correctly`, () => {
          expect(secondOtherActionEl.text().trim()).toEqual(secondOtherActionConfig.title);
        });

        it(`should call an action callback when clicked >`, () => {
          secondOtherActionEl.click();
          $scope.$digest();

          expect(secondOtherActionConfig.action).toHaveBeenCalled();
        });

        describe(`and it's disabled >`, () => {

          it(`shouldn't call a callback when clicked`, () => {
            $scope.model.disabled = true;
            $scope.$digest();

            expect(secondOtherActionConfig.action).not.toHaveBeenCalled();
          });

        });

      });

    });

  });

});
