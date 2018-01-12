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

/**
 * This class is handling the controller for the machine item row in ram settings.
 *
 * @author Oleksii Kurinnyi
 */
export class RamSettingsMachineItemController {
  /**
   * The name of the machine.
   */
  machineName: string;
  /**
   * Callback which is called on machine's memory limit is changed.
   */
  onRamChange: (data: {name: string, memoryLimitGBytes: number}) => void;

  /**
   * Callback which is called when machine's RAM setting is changed.
   *
   * @param {number} value a machine's memory limit in GB
   */
  onRamChanged(value: number) {
    this.onRamChange({name: this.machineName, memoryLimitGBytes: value});
  }

}

