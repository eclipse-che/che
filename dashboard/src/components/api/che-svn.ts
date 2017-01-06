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
 * This class is handling the svn API.
 * @author Oleksii Orel
 */
export class CheSvn {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, wsagentPath) {

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
   * @param workspaceId
   * @param projectPath
   */
  fetchRemoteUrl(workspaceId, projectPath) {
    var data = {children: false, revision: 'HEAD', projectPath: projectPath, target: '.'};

    let promise = this.remoteSvnAPI.getRemoteUrl({
      workspaceId: workspaceId
    }, data).$promise;

    // check if it was OK or not
    let parsedResultPromise = promise.then((svnInfo) => {
      if(svnInfo.items){
        svnInfo.items.forEach((item) => {
          this.remoteUrlMap.set(workspaceId + projectPath, {
            name: item.path,
            url: item.uRL
          });
        });
      }
    });

    return parsedResultPromise;
  }

  getRemoteUrlByKey(workspaceId, projectPath) {
    return this.remoteUrlMap.get(workspaceId + projectPath);
  }

}
