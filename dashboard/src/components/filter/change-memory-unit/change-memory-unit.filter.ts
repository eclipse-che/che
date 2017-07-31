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


export enum MemoryUnit {'B', 'KB', 'MB', 'GB', 'TB'}
export namespace MemoryUnit {
  export function keys(): string[] {
    return [
      MemoryUnit[MemoryUnit.B].toString(),
      MemoryUnit[MemoryUnit.KB].toString(),
      MemoryUnit[MemoryUnit.MB].toString(),
      MemoryUnit[MemoryUnit.GB].toString(),
      MemoryUnit[MemoryUnit.TB].toString()
    ];
  }
}

/**
 * Converts units in defined by user way.
 *
 * @usage
 *    <div>{{ctrl.memoryLimitBytes | changeMemoryUnit:['B','GB']}}</div>
 *
 * @author Oleksii Kurinnyi
 */
export class ChangeMemoryUnitFilter {

  constructor(register: che.IRegisterService) {
    /**
     * Default constructor that is using resource injection
     * @ngInject for Dependency injection
     */
    register.filter('changeMemoryUnit', ($log: ng.ILogService) => {
      return (num: number|string, units: [string, string]) => {
        const unitFrom = units[0].toUpperCase(),
              unitTo = units[1].toUpperCase();

        // check if number is valid
        const number = parseFloat(num as string);
        if (isNaN(number)) {
          // do nothing if this isn't a number
          $log.error(`ChangeMemoryUnitFilter: got "${num}", but can process only number greater than zero.` );
          return num;
        }
        if (number < 0) {
          // do nothing if number is less than 0
          $log.error(`ChangeMemoryUnitFilter: got "${num}", but can process only number greater than zero.`);
          return num;
        }

        const availableUnits = MemoryUnit.keys();
        // check if unit types are valid
        if (availableUnits.indexOf(unitFrom) === -1) {
          // do nothing if unit type is unknown
          $log.error(`ChangeMemoryUnitFilter: unitFrom="${unitFrom}" doesn't match any of [${availableUnits.toString()}]`);
          return num;
        }
        if (availableUnits.indexOf(unitTo) === -1) {
          // do nothing if unit type is unknown
          $log.error(`ChangeMemoryUnitFilter: unitTo="${unitFrom}" doesn't match any of [${availableUnits.toString()}]`);
          return num;
        }

        // process numbers
        const numberBytes = this.castToBytes(number, unitFrom),
              resultValue = this.castBytesTo(numberBytes, unitTo);
        return resultValue + ' ' + unitTo;
      };
    });
  }

  castUp(number: number, power: number): number {
    const temp = number / Math.pow(1024, power);
    return Math.round(temp * 10) / 10;
  }

  castDown(number: number, power: number): number {
    return number * Math.pow(1024, power);
  }

  getPower(unit: string) {
    return MemoryUnit[unit];
  }

  castToBytes(number: number, unitFrom: string): number {
    const power = this.getPower(unitFrom);
    number = Math.round(this.castDown(number, power));
    return number;
  }

  castBytesTo(number: number, unitTo: string): number {
    const power = this.getPower(unitTo);
    number = this.castUp(number, power);
    return number;
  }

}

