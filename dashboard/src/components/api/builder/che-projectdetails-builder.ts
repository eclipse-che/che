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
 * This class is providing a builder for Project details
 * @author Florent Benoit
 */
export class CheProjectDetailsBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.projectDetails = {};
    this.projectDetails.creationDate = new Date().getTime();
    this.projectDetails.modificationDate = new Date().getTime();
    this.projectDetails.mixins = [];
    this.projectDetails.problems = [];
    this.projectDetails.description = '';
  }

  /**
   * Sets workspaceId
   * @param workspaceId the workspace ID
   * @returns {CheProjectDetailsBuilder}
   */
  withWorkspaceId(workspaceId) {
    this.projectDetails.workspaceId = workspaceId;
    return this;
  }

  /**
   * Sets workspaceName
   * @param workspaceName the workspace name
   * @returns {CheProjectDetailsBuilder}
   */
  withWorkspaceName(workspaceName) {
    this.projectDetails.workspaceName = workspaceName;
    return this;
  }

  /**
   * Sets Name
   * @param Name the project's name
   * @returns {CheProjectDetailsBuilder}
   */
  withName(name) {
    this.projectDetails.name = name;
    return this;
  }

  /**
   * Sets permissions
   * @param permissions the project's permissions
   * @returns {CheProjectDetailsBuilder}
   */
  withPermissions(permissions) {
    this.projectDetails.permissions = permissions;
    return this;
  }

  /**
   * Sets type
   * @param type the project's type
   * @returns {CheProjectDetailsBuilder}
   */
  withType(type) {
    this.projectDetails.type = type;
    return this;
  }


  /**
   * Build the project details
   * @returns {CheProjectDetailsBuilder.projectDetails|*}
   */
  build() {
    return this.projectDetails;
  }


}
