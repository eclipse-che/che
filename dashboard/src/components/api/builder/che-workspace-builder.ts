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
import IWorkspaceEnvironment = che.IWorkspaceEnvironment;

/**
 * This class is providing a builder for Workspace
 * @author Florent Benoit
 */
export class CheWorkspaceBuilder {
  private workspace: che.IWorkspace;

  constructor() {
    this.workspace = {
      temporary: false,
      config: {
        environments: {},
        projects: []
      }
    };
  }

  withName(name: string): CheWorkspaceBuilder {
    this.workspace.config.name = name;
    return this;
  }

  withId(id: string): CheWorkspaceBuilder {
    this.workspace.id = id;
    return this;
  }

  withAttributes(attributes: che.IWorkspaceAttributes): CheWorkspaceBuilder {
    this.workspace.attributes = attributes;
    return this;
  }

  withTemporary(temporary: boolean): CheWorkspaceBuilder {
    this.workspace.temporary = temporary;
    return this;
  }

  withRuntime(runtime: any): CheWorkspaceBuilder {
    this.workspace.runtime = runtime;
    return this;
  }

  withNamespace(namespace: string): CheWorkspaceBuilder {
    this.workspace.namespace = namespace;
    return this;
  }

  withStatus(status: string): CheWorkspaceBuilder {
    this.workspace.status = status;
    return this;
  }

  withDefaultEnvironment(defaultEnv: string): CheWorkspaceBuilder {
    this.workspace.config.defaultEnv = defaultEnv;
    return this;
  }

  withEnvironments(environments: {[envName: string]: IWorkspaceEnvironment}): CheWorkspaceBuilder {
    this.workspace.config.environments = environments;
    return this;
  }

  build(): che.IWorkspace {
    return this.workspace;
  }

}

