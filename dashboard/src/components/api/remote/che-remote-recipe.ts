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
 * This class is handling the call to remote Recipe API
 * @author Florent Benoit
 */
export class CheRemoteRecipe {

  /**
   * Default constructor that is using resource
   */
  constructor($resource, authData) {
    this.$resource = $resource;
    this.authData = authData;


    // remote call
    this.remoteRecipesAPI = this.$resource('',{}, {
      create: {method: 'POST', url: authData.url + '/api/recipe?token=' + authData.token},
    });

  }

  /**
   * Create a recipe
   * @param recipe the recipe to create
   * @returns {*}
   */
  create(recipe) {
    return this.remoteRecipesAPI.create(recipe).$promise;
  }

}
