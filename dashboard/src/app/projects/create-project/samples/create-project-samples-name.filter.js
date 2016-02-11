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
 * Defines a filter that match if category or description or project type is containing the given value
 * @author Florent Benoit
 */

export class CreateProjectSamplesNameFilter {

  constructor(register) {
    // Register this factory
    register.app.filter('sampleNameFilter', function () {
      return function (templates, categoryFilter) {

        if (!templates) {
          return [];
        }
        if (!categoryFilter) {
          return templates;
        }

        var filtered = [];
        templates.forEach((template) => {

          if (template.projectType !== null && template.projectType.toLowerCase().indexOf(categoryFilter.toLowerCase()) >= 0) {
            filtered.push(template);
          } else if (template.category !== null && template.category.toLowerCase().indexOf(categoryFilter.toLowerCase()) >= 0) {
            filtered.push(template);
          } else if (template.description !== null && template.description.toLowerCase().indexOf(categoryFilter.toLowerCase()) >= 0) {
            filtered.push(template);
          }
        });
        return filtered;
      };
    });
  }
}
