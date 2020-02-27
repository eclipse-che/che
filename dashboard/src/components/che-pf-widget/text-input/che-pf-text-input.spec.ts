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

import { CheHttpBackend } from '../../api/test/che-http-backend';
import { IChePfTextInputProperties } from './che-pf-text-input.directive';

interface ITestScope extends ng.IScope {
  model?: IChePfTextInputProperties;
}

describe(`chePfInput >`, () => {

  let $scope: ITestScope;
  let $compile: ng.ICompileService;
  let $timeout: ng.ITimeoutService;
  let compiledDirective: ng.IAugmentedJQuery;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((
    _$compile_: ng.ICompileService,
    _$timeout_: ng.ITimeoutService,
    _$rootScope_: ng.IRootScopeService,
    _cheHttpBackend_: CheHttpBackend,
  ) => {
    $scope = _$rootScope_.$new() as ITestScope;
    $compile = _$compile_;
    $timeout = _$timeout_;

    const $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compile(): void {
    let buttonDropdownTemplate = `<div>
      <che-pf-text-input value="model.value"
        config="model.config"
        on-change="model.onChange($value)"></che-pf-text-input>
    </div>`;

    compiledDirective = $compile(angular.element(buttonDropdownTemplate))($scope);
    $timeout.flush();
    $scope.$digest();
  }

  it(`should be compiled >`, () => {

    const name = 'testInputName';
    let value;
    $scope.model = {
      config: {
        name
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input`).length).toEqual(1);
  });

  it(`should have provided "name" attribute value >`, () => {

    const name = 'testInputName';
    let value;
    $scope.model = {
      config: {
        name
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input[name="${name}"]`).length).toEqual(1);
  });

  it(`should have provided "id" attribute value >`, () => {

    const name = 'testInputName';
    const id = 'testInputId';
    let value;
    $scope.model = {
      config: {
        name,
        id
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input[id="${id}"]`).length).toEqual(1);

  });

  it(`should have generated "id" attribute value if not provided >`, () => {

    const name = 'testInputName';
    let value;
    $scope.model = {
      config: {
        name,
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input[id^="${name}"]`).length).toEqual(1);

  });

  it(`should have provided "pattern" attribute value >`, () => {

    const name = 'testInputName';
    const pattern = 'pattern';
    let value;
    $scope.model = {
      config: {
        name,
        pattern
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input[pattern="${pattern}"]`).length).toEqual(1);
  });

  it(`should have provided "placeholder" attribute value >`, () => {

    const name = 'testInputName';
    const placeHolder = 'placeholder';
    let value;
    $scope.model = {
      config: {
        name,
        placeHolder
      },
      value,
      onChange: () => { }
    };

    compile();

    expect(compiledDirective.find(`input[placeholder="${placeHolder}"]`).length).toEqual(1);
  });

  describe('ngModel >', () => {

    it('should correctly provide an empty value > ', () => {

      const name = 'testInputName';
      let value;
      $scope.model = {
        config: {
          name,
        },
        value,
        onChange: () => { }
      };

      compile();

      expect(compiledDirective.find('input').val()).toEqual('');
    });

    it('should correctly provide not empty value > ', () => {

      const name = 'testInputName';
      let value = 'initial value';
      $scope.model = {
        config: {
          name,
        },
        value,
        onChange: () => { }
      };

      compile();

      expect(compiledDirective.find('input').val()).toEqual(value);
    });

  });

  describe('ngChange > ', () => {

    // todo fix this test
    xit('should be called on ngModel changes > ', () => {

      const name = 'testInputName';
      let value;
      $scope.model = {
        config: {
          name,
        },
        value,
        onChange: ($value) => { console.log($value); }
      };

      spyOn($scope.model, 'onChange');
      compile();

      const input = compiledDirective.find('input');

      input.val('new user input');
      $scope.$digest();

      expect($scope.model.onChange).toHaveBeenCalled();
    });

  });

  describe('Input Label > ', () => {

    it('should be hidden initially > ', () => {

      const name = 'testInputName';
      let value;
      $scope.model = {
        config: {
          name,
        },
        value,
        onChange: () => { }
      };

      compile();

      expect(compiledDirective.find(`label`).length).toEqual(0);
    });

    describe('when provided', () => {

      const id = 'testInputId';
      const name = 'testInputName';
      const labelName = 'Input Test Label';
      let labelEl;

      beforeEach(() => {
        let value;
        $scope.model = {
          config: {
            id,
            name,
            labelName
          },
          value,
          onChange: () => { }
        };

        compile();

        labelEl = compiledDirective.find(`label`);
      });

      it(`should be shown if provided >`, () => {
        expect(labelEl.length).toEqual(1);
      });

      it(`should have provided label name content >`, () => {
        expect(labelEl.text().trim()).toEqual(labelName);
      });

      it(`should have 'for' attribute value to equal input's 'id' attribute value >`, () => {
        expect(labelEl.attr('for')).toEqual(id);
      });

    });

  });

});
