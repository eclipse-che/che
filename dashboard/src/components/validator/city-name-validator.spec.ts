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
import {CheHttpBackend} from '../api/test/che-http-backend';

/**
 * Test for city-name-validator directive
 *
 * @author Oleksii Kurinnyi
 */

describe('city-name-validator', () => {
  let $scope, form, $compile;

  let validNames = [
    'Toronto',
    'St. Catharines',
    'San Fransisco',
    'Val-d\'Or',
    'Presqu\'ile',
    'Niagara on the Lake',
    'Niagara-on-the-Lake',
    'München',
    'toronto',
    'toRonTo',
    'villes du Québec',
    'Provence-Alpes-Côte d\'Azur',
    'Île-de-France',
    'Kópavogur',
    'Garðabær',
    'Sauðárkrókur',
    'Þorlákshöfn'
  ];
  let invalidNames = [
    'San ',
    'St.',
    'Val-',
    'A----B',
    '------',
    '*******',
    '&&',
    '()',
    '//',
    '\\'
  ];

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService, $rootScope: ng.IRootScopeService, cheHttpBackend: CheHttpBackend) => {
    $scope = $rootScope;
    $compile = _$compile_;

    httpBackend = cheHttpBackend.getHttpBackend();
    // avoid tracking requests from branding controller
    httpBackend.whenGET(/.*/).respond(200, '');
    httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  validNames.forEach((validName: string) => {

    (function shouldPass(validCityName: string) {
      it(`"${validCityName}" should be OK`, () => {
        $scope.model = {value: ''};

        let element = angular.element(
          '<form name="form">' +
          '<input ng-model="model.value" name="value" city-name-validator />' +
          '</form>'
        );
        $compile(element)($scope);

        form = $scope.form;
        form.value.$setViewValue(validCityName);

        // check form (expect invalid)
        expect(form.value.$invalid).toBe(false);
        expect(form.value.$valid).toBe(true);
      });
    })(validName);

  });

  invalidNames.forEach((invalidName: string) => {

    (function shouldFail(invalidName: string) {
      it(`"${invalidName}" should fail`, () => {
        $scope.model = {value: ''};

        let element = angular.element(
          '<form name="form">' +
          '<input ng-model="model.value" name="value" city-name-validator />' +
          '</form>'
        );
        $compile(element)($scope);

        form = $scope.form;
        form.value.$setViewValue(invalidName);

        // check form (expect valid)
        expect(form.value.$invalid).toBe(true);
        expect(form.value.$valid).toBe(false);
      });
    })(invalidName);

  });

});
