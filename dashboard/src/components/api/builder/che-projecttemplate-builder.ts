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
 * This class is providing a builder for ProjectTemplate
 * @author Florent Benoit
 */
export class CheProjectTemplateBuilder {

  template: che.IProjectTemplate;

  constructor() {
    this.template = {} as che.IProjectTemplate;
    this.template.source = {} as che.IProjectSource;
  }

  withDescription(desc: string): CheProjectTemplateBuilder {
    this.template.description = desc;
    return this;
  }

  withSourceParameters(parameters: {[paramName: string]: string}): CheProjectTemplateBuilder {
    this.template.source.parameters = parameters;
    return this;
  }

  withSourceType(type: string): CheProjectTemplateBuilder {
    this.template.source.type = type;
    return this;
  }

  withSourceLocation(location: string): CheProjectTemplateBuilder {
    this.template.source.location = location;
    return this;
  }

  withDisplayname(name: string): CheProjectTemplateBuilder {
  this.template.displayName = name;
    return this;
  }

  withCategory(category: string): CheProjectTemplateBuilder {
    this.template.category = category;
    return this;
  }

  withProjectType(projectType: string): CheProjectTemplateBuilder {
    this.template.projectType = projectType;
    return this;
  }

  build(): che.IProjectTemplate {
    return this.template;
  }


}
