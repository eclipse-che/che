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

/**
 * This class is providing a builder for Stack
 * @author Ann Shumilova
 */
export class CheStackBuilder {
  stack: che.IStack;

  /**
   * Default constructor.
   */
  constructor() {
    this.stack = {} as che.IStack;
    this.stack.components = [];
    this.stack.tags = [];
    this.stack.workspaceConfig = {} as che.IWorkspaceConfig;
  }

  /**
   * Sets the id of the stack
   * @param {string} id the id to use
   * @returns {CheStackBuilder}
   */
  withId(id: string): CheStackBuilder {
    this.stack.id = id;
    return this;
  }

  /**
   * Sets the name of the stack
   * @param {string} name the name to use
   * @returns {CheStackBuilder}
   */
  withName(name: string): CheStackBuilder {
    this.stack.name = name;
    return this;
  }

  /**
   * Sets the description of the stack
   * @param {string} description the description to use
   * @returns {CheStackBuilder}
   */
  withDescription(description: string): CheStackBuilder {
    this.stack.description = description;
    return this;
  }

  /**
   * Sets the tags of the stack
   * @param {string[]} tags the tags to use
   * @returns {CheStackBuilder}
   */
  withTags(tags: string[]): CheStackBuilder {
    this.stack.tags = tags;
    return this;
  }

  /**
   * Sets the components of the stack
   * @param {any[]} components the components to use
   * @returns {CheStackBuilder}
   */
  withComponents(components: any[]): CheStackBuilder {
    this.stack.components = components;
    return this;
  }

  /**
   * Sets the workspace config of the stack
   * @param {che.IWorkspaceConfig} workspaceConfig the workspace config to use
   * @returns {CheStackBuilder}
   */
  withWorkspaceConfig(workspaceConfig: che.IWorkspaceConfig): CheStackBuilder {
    this.stack.workspaceConfig = workspaceConfig;
    return this;
  }

  /**
   * Build the stack
   * @return {che.IStack}
   */
  build(): che.IStack {
    return this.stack;
  }

}
