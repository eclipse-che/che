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
import {CheTypeNumber} from './input-number.directive';

/**
 * Test for CheTypeNumber class.
 *
 * @author Oleksii Kurinnyi
 */

describe('CheTypeNumber', () => {
  let cheTypeNumber: CheTypeNumber;

  let validSymbols = '0123456789';
  let invalidSymbols = '`~!@#$%^&*()[]{}<>_+=:;.?,/|\'"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';

  beforeEach(() => {
    cheTypeNumber = new CheTypeNumber();
  });

  validSymbols.split('').forEach((validSymbol: string) => {

    (function(symbol: string) {
      it(`should pass "${symbol}" symbol`, () => {
        expect(cheTypeNumber.symbolIsValid(symbol)).toBeTruthy();
      });
    })(validSymbol);

  });

  invalidSymbols.split('').forEach((invalidSymbol: string) => {

    (function(symbol: string) {
      it(`should not pass "${symbol}" symbol`, () => {
        expect(cheTypeNumber.symbolIsValid(symbol)).toBeFalsy();
      });
    })(invalidSymbol);

  });

});
