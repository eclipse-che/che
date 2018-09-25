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
   * Sets the command.
   *
   * @param {string} commandName
   * @param {string} commandLine
   * @returns {CheFactoryBuilder}
   */
  withCommand(commandName: string, commandLine: string): CheFactoryBuilder {
    const command = {
      commandLine: commandLine,
      name: commandName,
      attributes: {
        previewUrl: ''
      },
      type: 'custom'
    };
    if (!this.factory.workspace) {
      this.factory.workspace = {} as che.IWorkspaceConfig;
    }
    if (!this.factory.workspace.commands) {
      this.factory.workspace.commands = [];
    }
    this.factory.workspace.commands.push(command);
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
