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
 * This class is providing a builder for Stack
 * @author Ann Shumilova
 */
export class CheStackBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.stack = {};
    this.stack.source = {};
    this.stack.source.origin = '';
    this.stack.source.type = ''
    this.stack.components = [];
    this.stack.tags = [];
    this.stack.workspaceConfig = {};
  }

  /**
   * Sets the id of the stack
   * @param id the id to use
   * @returns {CheStackBuilder}
   */
  withId(id) {
    this.stack.id = id;
    return this;
  }

  /**
   * Sets the name of the stack
   * @param name the name to use
   * @returns {CheStackBuilder}
   */
  withName(name) {
    this.stack.name = name;
    return this;
  }

  /**
   * Sets the description of the stack
   * @param description the description to use
   * @returns {CheStackBuilder}
   */
  withDescription(description) {
    this.stack.description = description;
    return this;
  }

  /**
   * Sets the tags of the stack
   * @param tags the tags to use
   * @returns {CheStackBuilder}
   */
  withTags(tags) {
    this.stack.tags = tags;
    return this;
  }

  /**
   * Sets the components of the stack
   * @param components the components to use
   * @returns {CheStackBuilder}
   */
  withComponents(components) {
    this.stack.components = components;
    return this;
  }

  /**
   * Sets the workspace config of the stack
   * @param workspaceConfig the workspace config to use
   * @returns {CheStackBuilder}
   */
  withWorkspaceConfig(workspaceConfig) {
    this.stack.workspaceConfig = workspaceConfig;
    return this;
  }

  /**
   * Build the stack
   * @returns {CheStackBuilder.stack|*}
   */
  build() {
    return this.stack;
  }

}
