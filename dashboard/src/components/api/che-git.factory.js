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
 * This class is handling the git API.
 * @author Oleksii Orel
 */
export class CheGit {


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, lodash) {

    // keep resource
    this.$resource = $resource;
    this.lodash = lodash;

    this.remoteGitUrlArraysMap = new Map();
    this.localGitUrlsMap = new Map();

    // remote call
    this.remoteGitAPI = this.$resource('/api/git', {}, {
      getLocalUrl: {method: 'GET', url: '/api/git/:workspaceId/read-only-url?projectPath=:path', isArray: false},
      getRemoteUrlArray: {method: 'POST', url: '/api/git/:workspaceId/remote-list?projectPath=:path', isArray: true}
    });
  }

  /**
   * Ask for loading local repository url for the given project
   * @param workspaceId
   * @param projectPath
   */
  fetchLocalUrl(workspaceId, projectPath) {
    let promise = this.remoteGitAPI.getLocalUrl({
      workspaceId: workspaceId,
      path: projectPath
    }, null).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((data) => {
      var localUrl = '';
      //TODO why the type may not be a string
      if (typeof data === 'string') {
        localUrl = data;
      } else {
        angular.forEach(data, function (value) {
          if (typeof value === 'string') {
            localUrl += value;
          }
        });
      }
      this.localGitUrlsMap.set(workspaceId + projectPath, localUrl);
    });

    return parsedResultPromise;
  }

  /**
   * Ask for loading remote repository urls for the given project
   * @param workspaceId
   * @param projectPath
   */
  fetchRemoteUrlArray(workspaceId, projectPath) {
    var data = {remote: null, verbose: true, attributes: {}};

    let promise = this.remoteGitAPI.getRemoteUrlArray({
      workspaceId: workspaceId,
      path: projectPath
    }, data).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((remoteArray) => {
      remoteArray = this.lodash.sortBy(remoteArray, 'name');
      this.remoteGitUrlArraysMap.set(workspaceId + projectPath, remoteArray);
    });

    return parsedResultPromise;
  }

  getRemoteUrlArrayByKey(workspaceId, projectPath) {
    return this.remoteGitUrlArraysMap.get(workspaceId + projectPath);
  }

  getLocalUrlByKey(workspaceId, projectPath) {
    return this.localGitUrlsMap.get(workspaceId + projectPath);
  }

}
