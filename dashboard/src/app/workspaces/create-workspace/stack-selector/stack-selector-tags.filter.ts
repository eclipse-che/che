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

/**
 * Returns stacks which contain given substring in title, description, tags or components.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorTagsFilter {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.filter('stackTagsFilter', () => {
      return (stacksList: che.IStack[], tagsList: string[]) => {
        if (!tagsList || tagsList.length === 0) {
          return angular.copy(stacksList);
        }

        return stacksList.filter((stack: che.IStack) => {
          const stackTags = stack.tags.map((tag: string) => tag.toLowerCase());
          return tagsList.every((tag: string) => {
            return stackTags.indexOf(tag.toLowerCase()) !== -1;
          });
        });
      };
    });
  }
}
