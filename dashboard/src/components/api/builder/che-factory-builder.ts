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
 * This class is providing a builder for factory
 * @author Oleksii Orel
 */
export class CheFactoryBuilder {

  private factory: che.IFactory;

  /**
   * Default constructor.
   */
  constructor() {
    this.factory = {};
    this.factory.creator = {};
  }

  /**
   * Sets the creator email
   * @param email
   * @returns {CheFactoryBuilder}
   */
  withCreatorEmail(email) {
    this.factory.creator.email = email;
    return this;
  }

  /**
   * Sets the creator name
   * @param name
   * @returns {CheFactoryBuilder}
   */
  withCreatorName(name) {
    this.factory.creator.name = name;
    return this;
  }

  /**
   * Sets the id of the factory
   * @param id
   * @returns {CheFactoryBuilder}
   */
  withId(id) {
    this.factory.id = id;
    return this;
  }

  /**
   * Sets the name of the factory
   * @param name
   * @returns {CheFactoryBuilder}
   */
  withName(name) {
    this.factory.name = name;
    return this;
  }

  /**
   * Sets the workspace of the factory
   * @param workspace
   * @returns {CheFactoryBuilder}
   */
  withWorkspace(workspace) {
    this.factory.workspace = workspace;
    return this;
  }

  /**
   * Build the factory
   * @returns {CheFactoryBuilder|*}
   */
  build() {
    return this.factory;
  }
}
