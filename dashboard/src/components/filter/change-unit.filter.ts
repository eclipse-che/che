/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Converts units in defined by user way.
 *
 * @usage
 *    <div>{{ctrl.memoryLimitBytes | changeUnit:['B','GB']}}</div>
 *
 * @author Oleksii Kurinnyi
 */
export class ChangeUnitFilter {

  constructor(register: che.IRegisterService) {
    /**
     * Default constructor that is using resource injection
     * @ngInject for Dependency injection
     */
    register.filter('changeUnit', ($log: ng.ILogService) => {
      return (num: number|string, units: [string, string]) => {
        let unitFrom = units[0].toUpperCase(),
            unitTo = units[1].toUpperCase();

        // check if number is valid
        let number = parseFloat(num as string);
        if (isNaN(number)) {
          // do nothing if this isn't a number
          $log.error('ChangeUnitFilter: got "' + num + '", but can process only number greater than zero.' );
          return num;
        }
        if (number < 0) {
          // do nothing if number is less than 0
          $log.error('ChangeUnitFilter: got "' + num + '", but can process only number greater than zero.');
          return num;
        }

        // check if unit types are valid
        let availableUnits = ['B', 'KB', 'MB', 'GB', 'TB'];
        if (availableUnits.indexOf(unitFrom) === -1) {
          // do nothing if unit type is unknown
          $log.error('ChangeUnitFilter: unitFrom="' + unitFrom + '" doesn\'t match any of [' + availableUnits.toString() + ']');
          return num;
        }
        if (availableUnits.indexOf(unitTo) === -1) {
          // do nothing if unit type is unknown
          $log.error('ChangeUnitFilter: unitTo="' + unitFrom + '" doesn\'t match any of [' + availableUnits.toString() + ']');
          return num;
        }

        // process numbers
        let numberBytes = this.castToBytes(number, unitFrom),
            resultValue = this.castBytesTo(numberBytes, unitTo);
        return resultValue + ' ' + unitTo;
      };
    });
  }

  castUp(number: number, power: number): number {
    let temp = number / Math.pow(1024, power);
    return Math.round(temp * 10) / 10;
  }

  castDown(number: number, power: number): number {
    return number * Math.pow(1024, power);
  }

  getPower(unit: string) {
    switch (unit) {
      case 'TB':
        return 4;
      case 'GB':
        return 3;
      case 'MB':
        return 2;
      case 'KB':
        return 1;
    }
    return 0;
  }

  castToBytes(number: number, unitFrom: string): number {
    let power = this.getPower(unitFrom);
    number = this.castDown(number, power);
    return number;
  }

  castBytesTo(number: number, unitTo: string): number {
    let power = this.getPower(unitTo);
    number = this.castUp(number, power);
    return number;
  }

}

