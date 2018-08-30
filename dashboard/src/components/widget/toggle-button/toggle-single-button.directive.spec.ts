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

interface ITestScope extends ng.IRootScopeService {
  model?: any;
}

/**
 * Test of the ToggleSingleButton directive.
 * @author Oleksii Kurinnyi
 */
describe('ToggleSingleButton >', () => {

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
      title: 'Toggle Single Button Title',
      state: false,
      onChange: (state: boolean) => {
        /* tslint:disable */
        const newState = state;
        /* tslint:enable */
      }
    };

  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function getCompiledElement() {
    const element = $compile(angular.element(
      `<div><toggle-single-button che-title="{{model.title}}"
                                  che-state="model.state"
                                  che-on-change="model.onChange(state)">
      </toggle-single-button></div>`
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

    it('should have button disabled', () => {
      // timeout should be flashed
      $timeout.flush();

      expect(toggleSingleButton.get(0)).toBeTruthy();
      expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
    });

    describe('click on button >', () => {

      it('should enable button', () => {
        toggleSingleButton.click();
        $rootScope.$digest();

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        toggleSingleButton.click();
        $rootScope.$digest();

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(true);
      });

    });

    describe(`change state of toggle button from outside of directive >`, () => {

      it('should enable button', () => {
        $rootScope.model.state = true;
        $rootScope.$digest();

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        $rootScope.model.state = true;
        $rootScope.$digest();

        // timeout should be flashed to get callback called and content visible
        $timeout.flush();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(true);
      });

    });

  });

  describe('initially switched on >', () => {
    let toggleSingleButton;

    beforeEach(() => {
      $rootScope.model.state = true;

      compiledDirective = getCompiledElement();
      toggleSingleButton = compiledDirective.find('button');

      $timeout.flush();
    });

    it('should have button enabled', () => {
      expect(toggleSingleButton.get(0)).toBeTruthy();
      expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeFalsy();
    });

    describe('click on button >', () => {

      it('should disable button', () => {
        toggleSingleButton.click();
        $rootScope.$digest();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        toggleSingleButton.click();
        $rootScope.$digest();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(false);
      });

    });

    describe(`change state of toggle button from outside of directive >`, () => {

      it('should disable button', () => {
        $rootScope.model.state = false;
        $rootScope.$digest();

        expect(toggleSingleButton.hasClass('toggle-single-button-disabled')).toBeTruthy();
      });

      it('should call the callback', () => {
        spyOn($rootScope.model, 'onChange');

        $rootScope.model.state = false;
        $rootScope.$digest();

        expect($rootScope.model.onChange).toHaveBeenCalledWith(false);
      });

    });

  });

});
