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
import {CheTypeCity} from './input-city.directive';

/**
 * Test for CheTypeCity class.
 *
 * @author Oleksii Kurinnyi
 */

describe('CheTypeCity', () => {
  let cheTypeCity: CheTypeCity;

  let validSymbols = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.\' ';
  let invalidSymbols = '`~!@#$%^&*()[]{}<>_+=:;?0123456789';

  beforeEach(() => {
    cheTypeCity = new CheTypeCity();
  });

  validSymbols.split('').forEach((validSymbol: string) => {

    (function shouldPass(symbol: string) {
      it(`should pass "${symbol}" symbol`, () => {
        expect(cheTypeCity.symbolIsValid(symbol)).toBeTruthy();
      });
    })(validSymbol);

  });

  invalidSymbols.split('').forEach((invalidSymbol: string) => {

    (function (symbol: string) {
      it(`should not pass "${symbol}" symbol`, () => {
        expect(cheTypeCity.symbolIsValid(symbol)).toBeFalsy();
      });
    })(invalidSymbol);

  });

});
