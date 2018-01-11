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

