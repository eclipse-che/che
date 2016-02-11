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
 * This class is providing a builder for Project Permissions
 * @author Florent Benoit
 */
export class CheProjectPermissionsBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.projectPermission = {};
  }


  /**
   * Adds permission for the given user
   * @param userId the userID
   * @param roles the roles to add
   * @returns {CheProjectPermissionsBuilder}
   */
  withUser(userId, roles) {
    if (!roles) {
      roles = [];
    }
    this.projectPermission.principal = {};
    this.projectPermission.principal.name = userId;
    this.projectPermission.principal.type = 'USER';
    this.projectPermission.permissions = roles;
    return this;
  }


  /**
   * Adds permission for the given group
   * @param group the group
   * @param roles the roles to add
   * @returns {CheProjectPermissionsBuilder}
   */
  withGroup(group, roles) {
    if (!roles) {
      roles = [];
    }
    this.projectPermission.principal = {};
    this.projectPermission.principal.name = group;
    this.projectPermission.principal.type = 'GROUP';
    this.projectPermission.permissions = roles;
    return this;
  }

  /**
   * Build the project permissions
   * @returns {CheProjectPermissionsBuilder.projectPermissions|*}
   */
  build() {
    return this.projectPermission;
  }


}
