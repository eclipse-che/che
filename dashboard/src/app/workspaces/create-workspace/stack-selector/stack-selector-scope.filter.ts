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
import {StackSelectorScope} from './stack-selector-scope.enum';

/**
 * Returns stacks list which belongs to one of specified scope.
 *
 * @author Oleksii Kurinnyi
 */
export class StackSelectorScopeFilter {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.filter('stackScopeFilter', () => {
      return (stacksList: che.IStack[], scope: number, stackMachines: {[stackId: string]: any[]}) => {
        switch (scope) {
          case StackSelectorScope.QUICK_START:
            return stacksList.filter((stack: che.IStack) => {
              return stack.scope === 'general';
            });
          case StackSelectorScope.SINGLE_MACHINE:
            return stacksList.filter((stack: che.IStack) => {
              return stackMachines[stack.id].length === 1;
            });
          case StackSelectorScope.MULTI_MACHINE:
            return stacksList.filter((stack: che.IStack) => {
              return stackMachines[stack.id].length > 1;
            });
          default:
            return angular.copy(stacksList);
        }
      };
    });
  }
}
