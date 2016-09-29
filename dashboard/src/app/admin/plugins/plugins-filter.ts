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


export class PluginsFilter {

  constructor(register) {
    // Register this factory
    register.app.filter('filterStagedPlugins', function () {
      return function (toFilterPlugins) {
        // no plugins, nothing to get
        if (!toFilterPlugins) {
          return [];
        }

        let filtered = [];
        for (var i = 0; i < toFilterPlugins.length; i++) {
          var plugin = toFilterPlugins[i];
          if ('STAGED_INSTALL' === plugin.status || 'STAGED_UNINSTALL' === plugin.status) {
            filtered.push(plugin);
          }
        }

        return filtered;
      };
    });
  }
}



