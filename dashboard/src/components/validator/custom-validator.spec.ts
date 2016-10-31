/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Test the custom validation directive
 * @author Oleksii Kurinnyi
 */

describe('custom-validator', () => {
  let $scope, form, $compile;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_, $rootScope, cheHttpBackend) => {
    $scope = $rootScope;
    $compile = _$compile_;

    httpBackend = cheHttpBackend.getHttpBackend();
    httpBackend.whenGET(/.*/).respond(200, '');

    $scope.validateFn = (value) => {
      return value % 2 === 0;
    };
  }));

  it('should make form invalid if value isn\'t valid', () => {
    let nonValidValue = 5;
    $scope.model = {value: ''};

    let element = angular.element(
      '<form name="form">' +
      '<input ng-model="model.value" name="value" custom-validator="validateFn($value)" />' +
      '</form>'
    );
    $compile(element)($scope);

    form = $scope.form;
    form.value.$setViewValue(nonValidValue);

    // check form (expect invalid)
    expect(form.value.$invalid).toBe(true);
    expect(form.value.$valid).toBe(false);
  });

  it('should leave form valid for valid value', () => {
    let newValidValue = 10;
    $scope.model = {value: ''};

    let element = angular.element(
      '<form name="form">' +
      '<input ng-model="model.value" name="value" custom-validator="validateFn($value)" />' +
      '</form>'
    );
    $compile(element)($scope);

    form = $scope.form;
    form.value.$setViewValue(newValidValue);

    // check form (expect valid)
    expect(form.value.$invalid).toBe(false);
    expect(form.value.$valid).toBe(true);
  });
});
