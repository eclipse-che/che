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
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          expectedResult = input;
=======
        expectedResult = input;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(input, ['B', 'GB']);

      expect(result).toEqual(expectedResult);
    });

    it('negative numbers', () => {
      let input = -12345678,
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          expectedResult = input;
=======
        expectedResult = input;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(input, ['B', 'GB']);

      expect(result).toEqual(expectedResult);
    });

    it('unexpected unit type', () => {
      let input = 12345678,
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          unknownUnit = 'BM',
          expectedResult = input;
=======
        unknownUnit = 'BM',
        expectedResult = input;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(input, ['B', unknownUnit]);

      expect(result).toEqual(expectedResult);
    });

  });

  describe('should convert B to', () => {
    unitFrom = 'B';

    it('KB', () => {
      let number = 48 * 1024,
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          unitTo = 'KB',
          expectedResult = 48 + ' ' + unitTo;
=======
        unitTo = 'KB',
        expectedResult = 48 + ' ' + unitTo;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('MB', () => {
      let number = 48 * Math.pow(1024, 2),
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          unitTo = 'MB',
          expectedResult = 48 + ' ' + unitTo;
=======
        unitTo = 'MB',
        expectedResult = 48 + ' ' + unitTo;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('GB', () => {
      let number = 48 * Math.pow(1024, 3),
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          unitTo = 'GB',
          expectedResult = 48 + ' ' + unitTo;
=======
        unitTo = 'GB',
        expectedResult = 48 + ' ' + unitTo;
>>>>>>> CHE-5259: add machine page to workspace details

      let result = $filter('changeMemoryUnit')(number, [unitFrom, unitTo]);

      expect(result).toEqual(expectedResult);
    });

    it('GB', () => {
      let number = 48 * Math.pow(1024, 4),
<<<<<<< 2a4cf1ed39159fe78ecce06ca74c03be66393a17
          unitTo = 'TB',
          expectedResult = 48 + ' ' + unitTo;
=======
        unitTo = 'TB',
        expectedResult = 48 + ' ' + unitTo;
>>>>>>> CHE-5259: add machine page to workspace details

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
