/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
