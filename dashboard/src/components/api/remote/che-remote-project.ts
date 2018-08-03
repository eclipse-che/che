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
 * This class is handling the call to remote Project API
 * @author Florent Benoit
 */
export class CheRemoteProject {
  $resource: ng.resource.IResourceService;
  authData: any;
  remoteProjectsAPI: any;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              authData: any) {
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
   * @param {string} path the path of the project
   * @param {any} data the project body description
   * @returns {ng.IPromise<any>}
   */
  importProject(path: string, data: any): ng.IPromise<any> {
    // remove unused description because we cannot set project description without project type
    if ((!data.type || data.type.length === 0) && data.description) {
      delete(data.description);
    }
    return this.remoteProjectsAPI.import({path: path}, data).$promise;
  }

  updateProject(path: string, projectDetails: any): ng.IPromise<any> {
    return this.remoteProjectsAPI.update({
      path: path
    }, projectDetails).$promise;
  }

}
