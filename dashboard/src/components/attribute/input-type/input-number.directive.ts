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
 * @name components.directive:cheTypeNumber
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-type-number` defines an attribute for input tag which allows to enter only digits.
 *
 * @usage
 *   <input che-type-number />
 *
 * @author Oleksii Kurinnyi
 */

export class CheTypeNumber extends CheInputType {

  symbolIsValid(symbol: string): boolean {
    return '0123456789'.indexOf(symbol) > -1;
  }

}
