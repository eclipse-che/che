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
 * This class is providing a builder for ProjectTemplate
 * @author Florent Benoit
 */
export class CheProjectTemplateBuilder {

  constructor() {
    this.template = {};
    this.template.source = {};
    this.template.config = {};

  }
  withDescription(desc) {
    this.template.description = desc;
    return this;
  }

  withSourceParameters(parameters) {
    this.template.source.parameters = parameters;
    return this;
  }

  withSourceType(type) {
    this.template.source.type = type;
    return this;
  }

  withSourceLocation(location) {
    this.template.source.location = location;
    return this;
  }

  withDisplayname(name) {
  this.template.displayName = name;
    return this;
  }

  withCategory(category) {
    this.template.category = category;
    return this;
  }

  withProjectType(projectType) {
    this.template.projectType = projectType;
    return this;
  }

  build() {
    return this.template;
  }


}
