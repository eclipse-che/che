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
 * This class is handling the projects type retrieval
 * It sets to the array project types
 * @author Florent Benoit
 */
export class CheProjectType {
  private $q: ng.IQService;
  private $resource: ng.resource.IResourceService;
  private typesIds: Map<string, any>;
  private workspaceTypes: any[];
  private remoteProjectTypeAPI: any;

  /**
   * Default constructor that is using resource
   */
  constructor ($resource: ng.resource.IResourceService,
               $q: ng.IQService,
               wsagentPath: string) {
    this.$q = $q;
    this.$resource = $resource;

    // types per category per workspace ID : workspace ID ==> map<projectTypeId, projectType>
    this.typesIds = new Map();

    // project types per workspace ID
    this.workspaceTypes = [];
    // remote call
    this.remoteProjectTypeAPI = this.$resource(wsagentPath + '/project-type');
  }


  /**
   * Fetch the project types
   *
   * @returns {angular.IPromise<any>}
   */
  fetchTypes(): ng.IPromise<any> {
    const defer = this.$q.defer();
    const promise = this.remoteProjectTypeAPI.query().$promise;
    promise.then((projectTypes: any[]) => {

      // reset global list
      this.workspaceTypes = projectTypes;

      projectTypes.forEach((projectType: any) => {
        this.typesIds.set(projectType.id, projectType);
      });
      defer.resolve();
    }, (error: any) => {
      if (error.status !== 304) {
        defer.reject(error);
      } else {
        defer.resolve();
      }
    });

    return defer.promise;
  }

  /**
   * Gets all project types
   * @returns {Array}
   */
  getAllProjectTypes(): any[] {
    return this.workspaceTypes;
  }

  /**
   * The types per category
   * @returns {Map<string, any>}
   */
  getProjectTypesIDs(): Map<string, any> {
    return this.typesIds;
  }

}
