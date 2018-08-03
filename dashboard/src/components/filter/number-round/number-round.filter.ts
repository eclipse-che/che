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

export class CheNumberRoundFilter {

  static filter(): Function {
    return (numberStr: string, precisionStr: string) => {
      const number = parseFloat(numberStr);
      let precision = parseInt(precisionStr, 10);

      if (isNaN(number)) {
        return 'NaN';
      }
      if (isNaN(precision)) {
        precision = 0;
      }

      const factor = Math.pow(10, precision);
      const tempNumber = number * factor;
      const roundedTempNumber = Math.round(tempNumber);
      return (roundedTempNumber / factor).toString();
    };
  }
}

