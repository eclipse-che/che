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
import {StackSelectorScope, IStackSelectorItem} from './stack-selector.controller';

export class StackSelectorScopeFilter {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.filter('stackScopeFilter', () => {
      return (stacksList: IStackSelectorItem[], scope: number) => {

        if (scope === StackSelectorScope.ALL) {
          return angular.copy(stacksList);
        }

        if (scope === StackSelectorScope.QUICK_START) {
          return stacksList.filter((stack: IStackSelectorItem) => {
            return stack.scope === 'general';
          });
        }

        if (scope === StackSelectorScope.SINGLE_MACHINE) {
          return stacksList.filter((stack: IStackSelectorItem) => {
            return stack.isMultiMachine === false;
          });
        }

        if (scope === StackSelectorScope.MULTI_MACHINE) {
          return stacksList.filter((stack: IStackSelectorItem) => {
            return stack.isMultiMachine === true;
          });
        }

        return stacksList;
      };
    });
  }
}
