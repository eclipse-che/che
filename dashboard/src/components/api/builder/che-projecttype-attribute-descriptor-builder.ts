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
 * This class is providing a builder for AttributeDescriptor
 * @author Florent Benoit
 */
export class CheProjectTypeAttributeDescriptorBuilder {

  private attribute: any;

  constructor() {
    this.attribute = {};
    this.attribute.values = [];
  }

  withName(name: string): CheProjectTypeAttributeDescriptorBuilder {
  this.attribute.name = name;
    return this;
  }

  withRequired(required: boolean): CheProjectTypeAttributeDescriptorBuilder {
    this.attribute.required = required;
    return this;
  }

  withVariable(variable: any): CheProjectTypeAttributeDescriptorBuilder {
    this.attribute.variable = variable;
    return this;
  }

  withDescription(description: string): CheProjectTypeAttributeDescriptorBuilder {
    this.attribute.description = description;
    return this;
  }

  withValues(values: any): CheProjectTypeAttributeDescriptorBuilder {
    this.attribute.values = values;
    return this;
  }

  build(): any {
    return this.attribute;
  }

}
