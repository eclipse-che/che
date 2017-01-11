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

/**
 * Registry for maintaining system namespaces.
 *
 * @author Ann Shumilova
 */
export class CheNamespaceRegistry {

  namespaces : Array<string>;

  constructor() {
    this.namespaces = [];
  }

  /**
   * Adds the list of namespaces.
   *
   * @param namespaces namespace to be added
   */
  addNamespaces(namespaces : Array<string>) : void {
    this.namespaces = this.namespaces.concat(namespaces);
  }

  /**
   * Returns the list of available namespaces.
   *
   * @returns {Array<string>} namespaces
   */
  getNamespaces() : Array<string> {
    return this.namespaces;
  }
}
