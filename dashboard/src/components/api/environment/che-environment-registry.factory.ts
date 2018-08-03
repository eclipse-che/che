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
import {EnvironmentManager} from './environment-manager';

/**
 * Registry for maintaining environment managers.
 *
 * @author Ann Shumilova
 */
export class CheEnvironmentRegistry {
  managers: Map<string, EnvironmentManager>;

  constructor() {
    this.managers = new Map();
  }

  /**
   * Adds new environment manager to handle environments with specified type.
   *
   * @param {string} type environment's type
   * @param {EnvironmentManager} manager environment's manager
   */
  addEnvironmentManager(type: string, manager: EnvironmentManager): void {
    this.managers.set(type, manager);
  }

  /**
   * Returns the registered environment manager by type.
   *
   * @param {string} type environment's type
   * @returns {EnvironmentManager}
   */
  getEnvironmentManager(type: string): EnvironmentManager {
    return this.managers.get(type);
  }

  /**
   * Returns the list of registered environments managers.
   *
   * @returns {Map<string, EnvironmentManager>}
   */
  getEnvironmentManagers(): Map<string, EnvironmentManager> {
    return this.managers;
  }
}
