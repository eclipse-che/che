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
 * This class is providing a builder for Permissions
 *
 * @autor Oleksii Kurinnyi
 */
export class ChePermissionsBuilder {

  private permissions: che.IPermissions;

  /**
   * Default constructor
   */
  constructor() {
    this.permissions = {
      actions: [],
      domainId: '',
      instanceId: '',
      userId: ''
    };
  }

  /**
   * Sets actions of the permissions
   *
   * @param {string[]} actions list
   * @return {ChePermissionsBuilder}
   */
  withActions(actions: string[]): ChePermissionsBuilder {
    this.permissions.actions = actions;
    return this;
  }
  /**
   * Sets the user ID of the permissions
   *
   * @param {string} id user ID
   * @return {ChePermissionsBuilder}
   */
  withUserId(id: string): ChePermissionsBuilder {
    this.permissions.userId = id;
    return this;
  }

  /**
   * Sets the instance ID of the permissions
   *
   * @param {string} id instance ID
   * @return {ChePermissionsBuilder}
   */
  withInstanceId(id: string): ChePermissionsBuilder {
    this.permissions.instanceId = id;
    return this;
  }

  /**
   * Sets the domain ID of the permissions
   *
   * @param {string} id domain ID
   * @return {ChePermissionsBuilder}
   */
  withDomainId(id: string): ChePermissionsBuilder {
    this.permissions.domainId = id;
    return this;
  }

  /**
   * Build the permissions
   *
   * @return {che.IPermissions}
   */
  build(): che.IPermissions {
    return this.permissions;
  }

}
