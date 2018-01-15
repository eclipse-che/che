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
 * This class is providing a builder for Organization
 *
 * @autor Oleksii Kurinnyi
 */
export class CheOrganizationsBuilder {

  private organization: che.IOrganization;

  /**
   * Default constructor
   */
  constructor() {
    this.organization = {
      id: '',
      links: [],
      name: '',
      qualifiedName: ''
    };
  }

  /**
   * Sets the ID of the organization
   *
   * @param {string} id organization ID
   * @return {CheOrganizationsBuilder}
   */
  withId(id: string): CheOrganizationsBuilder {
    this.organization.id = id;
    return this;
  }

  /**
   * Sets the id of the parent organization
   *
   * @param {string} id parent organization ID
   * @return {CheOrganizationsBuilder}
   */
  withParentId(id: string): CheOrganizationsBuilder {
    this.organization.parent = id;
    return this;
  }

  /**
   * Sets the name of the organization
   *
   * @param {string} name organization name
   * @return {CheOrganizationsBuilder}
   */
  withName(name: string): CheOrganizationsBuilder {
    this.organization.name = name;
    return this;
  }

  /**
   * Sets the qualified name of the organization
   *
   * @param {string} name qualified name of organization
   * @return {CheOrganizationsBuilder}
   */
  withQualifiedName(name: string): CheOrganizationsBuilder {
    this.organization.qualifiedName = name;
    return this;
  }

  /**
   * Build the organization
   *
   * @return {che.IOrganization}
   */
  build(): che.IOrganization {
    return this.organization;
  }

}
