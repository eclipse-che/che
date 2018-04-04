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
 * This class is handling the call to remote Recipe API
 * @author Florent Benoit
 */
export class CheRemoteRecipe {
  $resource: ng.resource.IResourceService;
  authData: any;
  remoteRecipesAPI: any;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              authData: any) {
    this.$resource = $resource;
    this.authData = authData;

    // remote call
    this.remoteRecipesAPI = this.$resource('', {}, {
      create: {method: 'POST', url: authData.url + '/api/recipe?token=' + authData.token}
    });
  }

  /**
   * Create a recipe
   * @param {any} recipe the recipe to create
   * @returns {ng.IPromise<any>}
   */
  create(recipe: any): ng.IPromise<any> {
    return this.remoteRecipesAPI.create(recipe).$promise;
  }

}
