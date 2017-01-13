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
