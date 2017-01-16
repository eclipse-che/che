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
 * This class is handling the recipe template retrieval
 * @author Oleksii Orel
 */
export class CheRecipeTemplate {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $q) {
    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    // Switch to local copy for now
    this.defaultRecipe = {
      type: 'docker',
      name: 'defaultName',
      script: '# This is a template for your machine recipe.\n' +
      '# Uncomment instructions that you want to use and replace them with yours.\n' +
      '# Inherit from a base image. This can be a Eclipse Che verified image or any base image you can find at Docker Hub.\n' +
      '# FROM dockerHubUser/yourImage\n' +
      '# Expose ports. All processes running in a Docker container should be access from outside.\n' +
      '# EXPOSE 8080\n' +
      '# Run instructions are identical to commands in your local Unix terminal.\n' +
      '# RUN echo "hello world"\n' +
      '# Map application port to the IDE client.\n'
    };
  }


  /**
   * Gets default recipe template
   * @returns default recipe
   */
  getDefaultRecipe() {
    return this.defaultRecipe;
  }

  /**
   * Ask for loading the recipe default template in asynchronous way
   * @returns {*} the promise
   */
  fetchDefaultRecipe() {
    var deferred = this.$q.defer();
    var promise = deferred.promise;
    deferred.resolve(true);
    return promise;
  }

}
