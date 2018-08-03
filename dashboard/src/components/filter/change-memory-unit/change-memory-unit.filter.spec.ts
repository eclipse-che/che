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
 * Test the filter which converts memory volume units in defined by user way.
 *
 * @author Oleksii Kurinnyi
 */
describe('ChangeMemoryUnitFilter', () => {
  let $filter, unitFrom: string;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$filter_: ng.IFilterService, cheHttpBackend: CheHttpBackend) => {
    $filter = _$filter_;

    httpBackend = cheHttpBackend.getHttpBackend();
    httpBackend.whenGET(/.*/).respond(200, '');
  }));

  describe('should properly process', () => {

    it('string instead of a number', () => {
      let input = 'some string',
          expectedResult = input;

      let result = $filter('changeMemoryUnit')(input, ['B', 'GB']);

      expect(result).toEqual(expectedResult);
    });

    it('negative numbers', () => {
      let input = -12345678,
        expectedResult = input;

      let result = $filter('changeMemoryUnit')(input, ['B', 'GB']);

      expect(result).toEqual(expectedResult);
    });

    it('unexpected unit type', () => {
      let input = 12345678,
        unknownUnit = 'BM',
        expectedResult = input;

      let result = $filter('changeMemoryUnit')(input, ['B', unknownUnit]);

      expect(result).toEqual(expectedResult);
    });

  });

  describe('should convert B to', () => {
    unitFrom = 'B';

    it('KB', () => {
      let number = 48 * 1024,
        unitTo = 'KB',
        expectedResult = 48 + ' ' + unitTo;

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('MB', () => {
      let number = 48 * Math.pow(1024, 2),
          unitTo = 'MB',
          expectedResult = 48 + ' ' + unitTo;

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('GB', () => {
      let number = 48 * Math.pow(1024, 3),
        unitTo = 'GB',
        expectedResult = 48 + ' ' + unitTo;

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('GB', () => {
      let number = 48 * Math.pow(1024, 4),
        unitTo = 'TB',
        expectedResult = 48 + ' ' + unitTo;

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

  });

  describe(`bugfix https://github.com/eclipse/che/issues/5601 >`, () => {

    it(`should round value in bytes >`, () => {
      const number = 2.3,
            unitFrom = 'GB',
            unitTo = 'B',
            notExpectedResult = 2469606195.2 + ' ' + unitTo,
            expectedResult = 2469606195 + ' ' + unitTo;

      const result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).not.toEqual(notExpectedResult);
      expect(result).toEqual(expectedResult);
    });

  });

});
