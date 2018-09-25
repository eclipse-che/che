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

interface IGitResource<T> extends ng.resource.IResourceClass<T> {
  getLocalUrl: any;
  getRemoteUrlArray: any;
}

/**
 * This class is handling the git API.
 * @author Oleksii Orel
 */
export class CheGit {

  private remoteGitUrlArraysMap: Map<string, Array<string>>;
  private localGitUrlsMap: Map<string, string>;
  private remoteGitAPI: IGitResource<any>;

  /**
   * Default constructor that is using resource
   */
  constructor($resource: ng.resource.IResourceService, wsagentPath: string) {
    this.remoteGitUrlArraysMap = new Map();
    this.localGitUrlsMap = new Map();

    // remote call
    this.remoteGitAPI = <IGitResource<any>>$resource(wsagentPath + '/git', {}, {
      getLocalUrl: {method: 'GET', url: wsagentPath + '/git/read-only-url?projectPath=:path',
        responseType: 'text', transformResponse: (data: string) => {
        return {url: data};
      }},
      getRemoteUrlArray: {method: 'POST', url: wsagentPath + '/git/remote-list?projectPath=:path', isArray: true}
    });
  }

  /**
   * Ask for loading local repository url for the given project
   * @param projectPath{string}
   * @returns {ng.IPromise<any>}
   */
  fetchLocalUrl(projectPath: string): ng.IPromise<any> {
    let promise = this.remoteGitAPI.getLocalUrl({
      path: projectPath
    }, null).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((data: any) => {
      this.localGitUrlsMap.set(projectPath, data.url);
    });

    return parsedResultPromise;
  }

  /**
   * Ask for loading remote repository urls for the given project
   * @param projectPath{string}
   * @returns {ng.IPromise<any>}
   */
  fetchRemoteUrlArray(projectPath: string): ng.IPromise<any> {
    let data = {remote: null, verbose: true, attributes: {}};

    let promise = this.remoteGitAPI.getRemoteUrlArray({
      path: projectPath
    }, data).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((remoteArray: Array<string>) => {
      this.remoteGitUrlArraysMap.set(projectPath, remoteArray);
    });

    return parsedResultPromise;
  }

  getRemoteUrlArrayByKey(projectPath: string): Array<string> {
    return this.remoteGitUrlArraysMap.get(projectPath);
  }

  getLocalUrlByKey(projectPath: string): string {
    return this.localGitUrlsMap.get(projectPath);
  }

}
