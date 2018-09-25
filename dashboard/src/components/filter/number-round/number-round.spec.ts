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

/**
 * Test for CheNumberRoundFilter
 *
 * @author Oleksii Kurinnyi
 */

describe('CheNumberRoundFilter', () => {
  let $scope, $compile;

  let testNumbers = [
    {
      number: -1234.5678,
      precision: -2,
      result: -1200
    },
    {
      number: -1234.5678,
      precision: 0,
      result: -1235
    },
    {
      number: -1234.5678,
      precision: 2,
      result: -1234.57
    },
    {
      number: 1234.5678,
      precision: -2,
      result: 1200
    },
    {
      number: 1234.5678,
      precision: 0,
      result: 1235
    },
    {
      number: 1234.5678,
      precision: 2,
      result: 1234.57
    }
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

  testNumbers.forEach((entry: {number: number, precision: number, result: number}) => {

    (function (entry: {number: number, precision: number, result: number}) {
      it(`Number ${entry.number} with precision ${entry.precision} should be equal to ${entry.result}`, () => {
        $scope.entry = entry;

        let element = angular.element(
          '<div>{{entry.number | numberRound:entry.precision}}</div>'
        );
        $compile(element)($scope);
        $scope.$digest();

        expect(element.text()).toEqual(entry.result.toString());
      });
    })(entry);

  });

});
