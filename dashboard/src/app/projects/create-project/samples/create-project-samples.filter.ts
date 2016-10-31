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


export class CreateProjectSamplesFilter {

  constructor(register) {
    // Register this factory
    register.app.filter('sampleFilterProjectType', function () {
      return function (templates, categoryFilter) {
        if (!templates) {
          return [];
        }
        if (!categoryFilter) {
          return templates;
        }

        var filtered = [];
        templates.forEach((template) => {
          if (categoryFilter === template.projectType) {
            filtered.push(template);
          }
        });
        return filtered;
      };
    });
  }
}
