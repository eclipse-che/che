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
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {MemoryUnit} from '../../../../components/filter/change-memory-unit/change-memory-unit.filter';

type machine = {
  name: string;
  image: string;
  memoryLimitGBytes: number;
};

/**
 * @ngdoc controller
 * @name workspaces.ram-settings.controller:RamSettingsController
 * @description This class is handling the controller of RAM settings.
 * @author Oleksii Kurinnyi
 */
export class RamSettingsController {

  static $inject = ['$filter', '$scope'];

  /**
   * Filter service.
   */
  private $filter: ng.IFilterService;
  /**
   * List of machines provided by parent controller.
   */
  private machines: Array<IEnvironmentManagerMachine>;
  /**
   * The environment manager.
   */
  private environmentManager: EnvironmentManager;
  /**
   * List of machines.
   */
  private machinesList: Array<machine>;
  /**
   * Callback should be called when memory limit changes.
   */
  private onRamChange: (data: {name: string, memoryLimitBytes: number}) => void;

  /**
   * Default constructor that is using resource injection
   */
  constructor($filter: ng.IFilterService, $scope: ng.IScope) {
    this.$filter = $filter;

    $scope.$watch(() => { return this.machines; }, () => {
      this.updateMachinesList();
    }, true);
  }

  /**
   * Builds list of machines properties.
   */
  updateMachinesList(): void {
    if (!angular.isArray(this.machines)) {
      this.machinesList = [];
    } else {
      this.machinesList = this.machines.map((machine: IEnvironmentManagerMachine) => {
        const source: any = this.environmentManager.getSource(machine),
              memoryLimitBytes = this.environmentManager.getMemoryLimit(machine),
              memoryLimitGBytesWithUnit = this.$filter('changeMemoryUnit')(memoryLimitBytes, [MemoryUnit[MemoryUnit.B], MemoryUnit[MemoryUnit.GB]]);
        return <machine>{
          image: source && source.image ? source.image : '',
          name: machine.name,
          memoryLimitGBytes: this.getNumber(memoryLimitGBytesWithUnit)
        };
      });
    }
  }

  /**
   * Callback which is called when RAM is changes.
   *
   * @param {string} name a machine name
   * @param {number} memoryLimitGBytes amount of ram in GB
   */
  onRamChanged(name: string, memoryLimitGBytes: number): void {
    const memoryLimitBytesWithUnit = this.$filter('changeMemoryUnit')(memoryLimitGBytes, [MemoryUnit[MemoryUnit.GB], MemoryUnit[MemoryUnit.B]]);
    this.onRamChange({name: name, memoryLimitBytes: this.getNumber(memoryLimitBytesWithUnit)});
  }

  /**
   * Returns number.
   *
   * @param {string} memoryLimit a string which contains machine's memory limit.
   * @return {number}
   */
  private getNumber(memoryLimit: string): number {
    const [, memoryLimitNumber] = /^([^\s]+)\s+[^\s]+$/.exec(memoryLimit);
    return parseFloat(memoryLimitNumber);
  }

}

