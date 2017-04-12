/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * This class is handling the factory template retrieval
 * It sets to the Map factory templates
 * @author Oleksii Orel
 */
export class CheFactoryTemplate {

  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private factoryTemplatesByName: Map<string, any>;
  private remoteFactoryTemplateAPI: ng.resource.IResourceClass<any>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService) {
    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    // factory templates map
    this.factoryTemplatesByName = new Map();

    // remote call
    this.remoteFactoryTemplateAPI = this.$resource('https://dockerfiles.codenvycorp.com/templates-4.8/factory/:fileName');
  }

  /**
   * Ask for loading the factory template in asynchronous way
   * If there are no changes, it's not updated
   * @param templateName the template name
   * @returns {*} the promise
   */
  fetchFactoryTemplate(templateName: string): ng.IPromise<any> {
    var deferred = this.$q.defer();

    let templateFileName = templateName + '.json';

    let promise = this.remoteFactoryTemplateAPI.get({fileName: templateFileName}).$promise;

    promise.then((factoryTemplateContent) => {
      //update factory template map
      this.factoryTemplatesByName.set(templateName, factoryTemplateContent);
      deferred.resolve(factoryTemplateContent);
    }, (error) => {
      if (error.status === 304) {
        let findFactoryTemplateContent = this.factoryTemplatesByName.get(templateName);
        deferred.resolve(findFactoryTemplateContent);
      } else {
        deferred.reject(error);
      }
    });
    return deferred.promise;
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
