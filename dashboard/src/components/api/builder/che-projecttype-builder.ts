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
 * This class is providing a builder for ProjectTemplate
 * @author Florent Benoit
 */
export class CheProjectTypeBuilder {

  private type: any;

  constructor() {
    this.type = {};
    this.type.attributeDescriptors = [];

  }

  withAttributeDescriptors(attributeDescriptors: any): CheProjectTypeBuilder {
    this.type.attributeDescriptors = attributeDescriptors;
    return this;
  }

  withDisplayname(name: string): CheProjectTypeBuilder {
  this.type.displayName = name;
    return this;
  }

  withId(id: string): CheProjectTypeBuilder {
    this.type.id = id;
    return this;
  }

  build(): any {
    return this.type;
  }


}
