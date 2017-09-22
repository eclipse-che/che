/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
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
 * This class is providing a builder for factory
 * @author Oleksii Orel
 */
export class CheFactoryBuilder {

  private factory: che.IFactory;

  /**
   * Default constructor.
   */
  constructor() {
    this.factory = {} as che.IFactory;
    this.factory.creator = {};
  }

  /**
   * Sets the creator email
   * @param {string} email
   * @return {CheFactoryBuilder}
   */
  withCreatorEmail(email: string): CheFactoryBuilder {
    this.factory.creator.email = email;
    return this;
  }

  /**
   * Sets the creator name
   * @param {string} name
   * @returns {CheFactoryBuilder}
   */
  withCreatorName(name: string): CheFactoryBuilder {
    this.factory.creator.name = name;
    return this;
  }

  /**
   * Sets the id of the factory
   * @param {string} id
   * @returns {CheFactoryBuilder}
   */
  withId(id: string): CheFactoryBuilder {
    this.factory.id = id;
    return this;
  }

  /**
   * Sets the name of the factory
   * @param {string} name
   * @returns {CheFactoryBuilder}
   */
  withName(name: string): CheFactoryBuilder {
    this.factory.name = name;
    return this;
  }

  /**
   * Sets the workspace of the factory
   * @param {che.IWorkspaceConfig} workspace
   * @returns {CheFactoryBuilder}
   */
  withWorkspace(workspace: che.IWorkspaceConfig): CheFactoryBuilder {
    this.factory.workspace = workspace;
    return this;
  }

  /**
   * Build the factory
   * @return {che.IFactory}
   */
  build(): che.IFactory {
    return this.factory;
  }
}
