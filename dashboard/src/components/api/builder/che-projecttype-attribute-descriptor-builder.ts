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
