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
 * Defines a directive for creating List Items that can be checked.
 * @author Ann Shumilova
 */
export class CheListItemChecked implements ng.IDirective {

  restrict = 'E';
  replace = true;
  transclude = true;
  templateUrl = 'components/widget/list/che-list-item-checked.html';

  // we require ngModel as we want to use it inside our directive
  require = ['ngModel'];

  // scope values
  scope = {
    valueModel: '=?ngModel',
    icon: '@?cheIcon',
    ariaLabel: '@cheAriaLabelCheckbox'
  };

}
