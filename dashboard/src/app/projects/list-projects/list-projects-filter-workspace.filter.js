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


export class ListProjectsWorkspaceFilter {

  constructor(register) {
    // Register this factory
    register.app.filter('listProjectWorkspaceFilter', function() {
      return function (workspaces, workspaceFilter) {
        // no workspaces, nothing to get
        if (!workspaces) {
          return {};
        }

        // no filter, return original content
        if (!workspaceFilter) {
          return workspaces;
        }

        // workspaces is on the following form : Map<key = workspaceId, value = array of projects>
        var filtered = {};

        // get the keys
        var workspacesID = Object.keys(workspaces);

        // for each workspace ID, check if filter is enabled for the given workspace ID
        workspacesID.forEach((workspaceID) => {
          if (workspaceFilter[workspaceID]) {
            filtered[workspaceID] = workspaces[workspaceID];
          }
        });

        return filtered;
      };
    });

  }
}
