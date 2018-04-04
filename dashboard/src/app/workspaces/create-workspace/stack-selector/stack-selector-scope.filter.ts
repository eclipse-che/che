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
