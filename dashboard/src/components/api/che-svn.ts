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
 * This class is handling the svn API.
 * @author Oleksii Orel
 */
export class CheSvn {

  static $inject = ['$resource', 'wsagentPath'];

  private $resource: ng.resource.IResourceService;
  private remoteUrlMap: Map<string, any>;
  private remoteSvnAPI: any;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService,
              wsagentPath: string) {

    // keep resource
    this.$resource = $resource;

    this.remoteUrlMap = new Map();

    // remote call
    this.remoteSvnAPI = this.$resource(wsagentPath + '/svn', {}, {
      getRemoteUrl: {method: 'POST', url: wsagentPath + '/svn/info'}
    });
  }

  /**
   * Ask for loading repository svn url for the given project
   *
   * @param {string} workspaceId
   * @param {string} projectPath
   * @returns {angular.IPromise<any>}
   */
  fetchRemoteUrl(workspaceId: string, projectPath: string): ng.IPromise<any> {
    const data = {children: false, revision: 'HEAD', projectPath: projectPath, target: '.'};

    let promise = this.remoteSvnAPI.getRemoteUrl({
      workspaceId: workspaceId
    }, data).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((svnInfo: any) => {
      if (svnInfo.items) {
        svnInfo.items.forEach((item: any) => {
          this.remoteUrlMap.set(workspaceId + projectPath, {
            name: item.path,
            url: item.uRL
          });
        });
      }
    });

    return parsedResultPromise;
  }

  getRemoteUrlByKey(workspaceId: string, projectPath: string): any {
    return this.remoteUrlMap.get(workspaceId + projectPath);
  }

}
