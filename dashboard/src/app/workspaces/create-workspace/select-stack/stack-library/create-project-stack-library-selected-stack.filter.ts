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
 * Defines a filter that match if given value is containing stack ID
 * @author Oleksii Kurinnyi
 */

export class CreateProjectStackLibrarySelectedStackFilter {

  constructor(register) {
    register.app.filter('stackSelectedStackFilter', () => {
      return function (templates, idFilter) {
        if (!templates) {
          return [];
        }

        if (!idFilter || !idFilter.length) {
          return templates;
        }

        var filtered = [];
        templates.forEach((template) => {
          if (idFilter.indexOf(template.id) > -1) {
            filtered.push(template);
          }
        });
        return filtered;
      };
    });
  }
}
