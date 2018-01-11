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
