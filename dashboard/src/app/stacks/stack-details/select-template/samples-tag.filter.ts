/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

export class SamplesTagFilter {

  static filter() {
    return (templates: Array<che.IStack>, tagFilter: Array<string>) => {
      if (!templates) {
        return [];
      }
      if (!tagFilter || !tagFilter.length) {
        return templates;
      }

      const filtered: Array<che.IStack> = [];
      templates.forEach((template: che.IStack) => {
        for (let i: number = 0; i < template.tags.length; i++) {
          for (let j: number = 0; j < tagFilter.length; j++) {
            if (template.tags[i].toLowerCase() === tagFilter[j].toLowerCase()) {
              filtered.push(template);
              return;
            }
          }
        }
      });
      return filtered.length ? filtered : templates;
    };
  }

}
