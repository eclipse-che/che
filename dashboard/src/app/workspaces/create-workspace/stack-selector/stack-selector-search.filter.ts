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

/**
 * Returns stacks which contain given substring in title, description, tags or components.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorSearchFilter {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.filter('stackSearchFilter', () => {
      return (stacksList: che.IStack[], searchString: string) => {
        if (!searchString) {
          return angular.copy(stacksList);
        }

        searchString = searchString.toLowerCase();

        return stacksList.filter((stack: che.IStack) => {
          return stack.name.toLowerCase().indexOf(searchString) > -1
            || stack.description.toLowerCase().indexOf(searchString) > -1
            || stack.tags.some((tag: string) => {
              return tag.toLowerCase().indexOf(searchString) > -1;
            })
            || stack.components.some((component: {name: string, version: string}) => {
              return component.name.toLowerCase().indexOf(searchString) > -1;
            });
        });
      };
    });
  }
}
