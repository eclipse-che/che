/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * Defines a directive for items in stack list.
 * Expects in parent scope:
 * @param{object} stack
 */
export class StackItem {

  /**
   * Default constructor.
   */
  constructor() {
    this.restrict = 'E';

    // we require ngModel as we want to use it inside our directive
    this.require = ['ngModel'];

    // scope values
    this.scope = {
      stack: '=stack',
      userId: '=userId',
      isSelectable: '=cheSelectable',
      isSelect: '=?ngModel',
      onCheckboxClick: '&?cheOnCheckboxClick',
      onDelete: '&cheOnDelete',
      onDuplicate: '&cheOnDuplicate'
    };

    this.templateUrl = 'app/stacks/list-stacks/stack-item/stack-item.html';

    this.controller = 'StackItemController';
    this.controllerAs = 'stackItemController';
    this.bindToController = true;
  }

}
