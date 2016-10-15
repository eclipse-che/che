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
 * This class is handling the call to remote Project API
 * @author Florent Benoit
 */
export class CheRemoteProject {

  /**
   * Default constructor that is using resource
   */
  constructor($resource, authData) {
    this.$resource = $resource;
    this.authData = authData;

    // remote call
    this.remoteProjectsAPI = this.$resource('', {}, {
      import: {method: 'POST', url: authData.url + '/project/import/:path?token=' + authData.token},
      update: {method: 'PUT', url: authData.url + '/project/:path?token=' + authData.token}
    });

  }


  /**
   * Import a project based located on the given path
   * @param path the path of the project
   * @param data the project body description
   * @returns {$promise|*|T.$promise}
   */
  importProject(path, data) {
    // remove unused description because we cannot set project description without project type
    if ((!data.type || data.type.length === 0) && data.description) {
      delete(data.description);
    }
    return this.remoteProjectsAPI.import({path: path}, data).$promise;
  }


  updateProject(path, projectDetails) {
    return this.remoteProjectsAPI.update({
      path: path
    }, projectDetails).$promise;
  }


}
