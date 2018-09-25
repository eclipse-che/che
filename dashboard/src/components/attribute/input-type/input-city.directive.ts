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
import {CheInputType} from './input-type.directive';

/**
 * @ngdoc directive
 * @name components.directive:cheTypeCity
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-type-city` defines an attribute for input tag which allows to enter symbols only used in city names. This directive should be used along with city-name-validator.
 *
 * @usage
 *   <input che-type-city city-name-validator />
 *
 * @author Oleksii Kurinnyi
 */

export class CheTypeCity extends CheInputType {
  private validSymbolsRE = /[.\-' a-zA-Z\u00A0-\u024F]/;

  symbolIsValid(symbol: string): boolean {
    return this.validSymbolsRE.test(symbol);
  }

}
