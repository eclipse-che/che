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

interface ITestScope extends ng.IRootScopeService {
  model?: any;
}

/**
 * Test of the WorkspaceRecipeImport
 * @author Oleksii Kurinnyi
 */
describe('CheToggleButtonPopover >', () => {

  let $rootScope: ITestScope,
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

    $rootScope.model = {
      popoverButtonTitle: 'Popover Button Title',
      initialState: false,
      value: false,
      popoverContent: 'Simple popover content',
      onChange: (state: boolean) => {
        /* tslint:disable */
        const newState = state;
        /* tslint:enable */
      }
    };

  }));

  function getCompiledElement() {
    const element = $compile(angular.element(
      `<div><toggle-button-popover button-title="model.popoverButtonTitle"
                                   button-state="model.initialState"
                                   button-value="model.value"
                                   button-on-change="model.onChange(state)"
                                   che-popover-placement="right-top">
        <div>{{model.popoverContent}}</div>
      </toggle-button-popover></div>`
    ))($rootScope);
    $rootScope.$digest();
    return element;
  }

  describe('initially switched off > ', () => {

    let toggleSingleButton;

    beforeEach(() => {
      compiledDirective = getCompiledElement();
      toggleSingleButton = compiledDirective.find('button');
    });

    it('should have content hidden', () => {
      expect(compiledDirective.html()).not.toContain($rootScope.model.popoverContent);
    });

    it('should have button disabled', () => {
      expect(toggleSingleButton.get(0)).toBeTruthy();
      expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
    });

    describe('click on button >', () => {

      beforeEach(() => {
        toggleSingleButton.click();
        $rootScope.$digest();
      });

      it('should make content visible', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(compiledDirective.html()).toContain($rootScope.model.popoverContent);
      });

      it('should enable button', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(true);
      });

    });

    describe(`change value of button-value attribute >`, () => {

      beforeEach(() => {
        $rootScope.model.value = true;
        $rootScope.$digest();
      });

      it('should make content visible', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(compiledDirective.html()).toContain($rootScope.model.popoverContent);
      });

      it('should enable button', () => {
        const toggleSingleButton = compiledDirective.find('button');

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(true);
      });

    });

  });

  describe('initially switched on >', () => {
    let toggleSingleButton;

    beforeEach(() => {
      $rootScope.model.initialState = true;

      compiledDirective = getCompiledElement();
      toggleSingleButton = compiledDirective.find('button');

      $timeout.flush();
    });

    it('should have content visible', () => {
      expect(compiledDirective.html()).toContain($rootScope.model.popoverContent);
    });

    it('should have button enabled', () => {
      expect(toggleSingleButton.get(0)).toBeTruthy();
      expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
    });


    describe('click on button >', () => {

      beforeEach(() => {
        toggleSingleButton.click();
        $rootScope.$digest();
      });

      it('should make content hidden', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(compiledDirective.html()).not.toContain($rootScope.model.popoverContent);
      });

      it('should disable button', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(false);
      });

    });

    describe(`change value of button-value attribute >`, () => {

      beforeEach(() => {
        $rootScope.model.value = false;
        $rootScope.$digest();
      });

      it('should make content hidden', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();
        $timeout.flush();

        expect(compiledDirective.html()).not.toContain($rootScope.model.popoverContent);
      });

      it('should disable button', () => {
        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(false);
      });

    });

  });

});
