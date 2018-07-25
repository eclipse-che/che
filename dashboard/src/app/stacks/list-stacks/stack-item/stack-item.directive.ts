/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for items in stack list.
 * Expects in parent scope:
 * @param{object} stack
 */
export class StackItem implements ng.IDirective {

  restrict = 'E';

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  // scope values
  scope = {
    stack: '=stack',
    userId: '=userId',
    isSelectable: '=cheSelectable',
    isSelect: '=?ngModel',
    onCheckboxClick: '&?cheOnCheckboxClick',
    onDelete: '&cheOnDelete',
    onDuplicate: '&cheOnDuplicate'
  };

  templateUrl = 'app/stacks/list-stacks/stack-item/stack-item.html';

  controller = 'StackItemController';
  controllerAs = 'stackItemController';
  bindToController = true;

}
