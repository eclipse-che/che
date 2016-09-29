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
 * Registry for maintaining environment managers.
 *
 * @author Ann Shumilova
 */
export class CheEnvironmentRegistry {

  constructor() {
    this.managers = new Map();
  }

  /**
   * Adds new environment manager to handle environments with specified type.
   *
   * @param type environment's type
   * @param manager environment's manager
   */
  addEnvironmentManager(type, manager) {
    this.managers.set(type, manager);
  }

  /**
   * Returns the registered environment manager by type.
   *
   * @param type environment's type
   * @returns {*}
   */
  getEnvironmentManager(type) {
    return this.managers.get(type);
  }

  /**
   * Returns the list of registered environments managers.
   *
   * @returns {lookup.managers|*|managers}
   */
  getEnvironmentManagers() {
    return this.managers;
  }
}
