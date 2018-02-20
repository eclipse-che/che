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
import {CheFactoryTemplates} from './complete-factory-template';

/**
 * This class is handling the factory template retrieval
 * It sets to the Map factory templates
 * @author Oleksii Orel
 */
export class CheFactoryTemplate {
  private factoryTemplatesByName: Map<string, any>;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.factoryTemplatesByName = new Map<string, string>();
    // todo move factory templates to the server side
    this.factoryTemplatesByName.set('minimal', angular.fromJson(CheFactoryTemplates.MINIMAL));
    this.factoryTemplatesByName.set('complete', angular.fromJson(CheFactoryTemplates.COMPLETE));
  }

  /**
   * Gets factory template by template name
   * @param templateName the template name
   * @returns factory template content
   */
  getFactoryTemplate(templateName: string): any {
    return this.factoryTemplatesByName.get(templateName);
  }
}
