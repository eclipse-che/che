/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
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
 * This class is providing a builder for Project details
 * @author Florent Benoit
 */
export class CheProjectDetailsBuilder {

  private projectDetails: che.IProjectTemplate;

  /**
   * Default constructor.
   */
  constructor() {
    this.projectDetails = {} as che.IProjectTemplate;
    this.projectDetails.mixins = [];
    this.projectDetails.problems = [];
    this.projectDetails.description = '';
  }

  /**
   * Sets workspaceId
   * @param {string} workspaceId the workspace ID
   * @returns {CheProjectDetailsBuilder}
   */
  withWorkspaceId(workspaceId: string): CheProjectDetailsBuilder {
    this.projectDetails.workspaceId = workspaceId;
    return this;
  }

  /**
   * Sets workspaceName
   * @param {string} workspaceName the workspace name
   * @returns {CheProjectDetailsBuilder}
   */
  withWorkspaceName(workspaceName: string): CheProjectDetailsBuilder {
    this.projectDetails.workspaceName = workspaceName;
    return this;
  }

  /**
   * Sets Name
   * @param {string} name the project's name
   * @returns {CheProjectDetailsBuilder}
   */
  withName(name: string): CheProjectDetailsBuilder {
    this.projectDetails.name = name;
    return this;
  }

  /**
   * Sets type
   * @param {string} type the project's type
   * @returns {CheProjectDetailsBuilder}
   */
  withType(type: string): CheProjectDetailsBuilder {
    this.projectDetails.type = type;
    return this;
  }

  /**
   * Build the project details
   * @returns {CheProjectDetailsBuilder.projectDetails|*}
   */
  build(): che.IProjectTemplate {
    return this.projectDetails;
  }

}
