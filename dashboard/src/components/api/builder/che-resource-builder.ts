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
 * This class is providing a builder for Resources
 *
 * @autor Oleksii Kurinnyi
 */
export class CheResourceBuilder {

  private resource: che.IResource;

  /**
   * Default constructor
   */
  constructor() {
    this.resource = {
      type: '',
      amount: 0,
      unit: ''
    };
  }

  /**
   * Sets type of resource
   *
   * @param {string} type resource's type
   * @return {CheResourceBuilder}
   */
  withType(type: string): CheResourceBuilder {
    this.resource.type = type;
    return this;
  }

  /**
   * Sets amount of resource
   *
   * @param {number} amount resource's amount
   * @return {CheResourceBuilder}
   */
  withAmount(amount: number): CheResourceBuilder {
    this.resource.amount = amount;
    return this;
  }

  /**
   * Sets unit of resource
   *
   * @param {string} unit resource's unit
   * @return {CheResourceBuilder}
   */
  withUnit(unit: string): CheResourceBuilder {
    this.resource.unit = unit;
    return this;
  }

  /**
   * Build the resource
   *
   * @return {che.IResource}
   */
  build(): che.IResource {
    return this.resource;
  }

}

