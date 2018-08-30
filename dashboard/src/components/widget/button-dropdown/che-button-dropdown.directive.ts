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

/* tslint:disable */
type ButtonType = 'link' | 'button';
/* tslint:enable */

export interface ICheButtonDropdownMainAction {
  action?: (args?: any) => void;
  disabled?: boolean;
  href?: string;
  id?: string;
  name?: string;
  title: string;
  type: ButtonType;
}
export interface ICheButtonDropdownOtherAction extends ICheButtonDropdownMainAction {
  orderNumber: number;
}

interface ICheButtonDropdownAttributes extends ng.IAttributes {
  mainActionConfig: string;
  otherActionsConfig: string;
  buttonDisabled: string;
  buttonStyle: string;
}

/**
 * @ngdoc directive
 * @name components.directive:cheButtonDropdown
 * @restrict E
 *
 * @description
 * `<che-button-dropdown>` defines a dropdown button
 *
 * @param {ICheButtonDropdownMainAction} main-action-config configuration of the visible button (split-button)
 * @param {Array<ICheButtonDropdownOtherAction>} other-actions-config configuration of the dropdown menu items
 * @param {boolean} button-disabled disables component if it is set to `true`
 * @param {string} button-style desired style for the main button, i.e. 'che-button-default', 'che-button-primary' etc.
 *
 * @example
 * <example module="userDashboard">
 *   <file name="index.html">
 *     <che-button-dropdown button-style="che-button-default"
 *                          main-action-config="ctrl.mainAction"
 *                          other-actions-config="ctrl.otherActions"></che-button-dropdown>
 *   </file>
 * </example>
 *
 * @author Oleksii Kurinnyi
 */
export class CheButtonDropdownDirective implements ng.IDirective {
  restrict = 'E';

  // scope values
  scope = {
    mainActionConfig: '=mainActionConfig',
    otherActionsConfig: '=otherActionsConfig',
    isDisabled: '=?buttonDisabled',
    buttonStyle: '@buttonStyle'
  };

  template($element: ng.IAugmentedJQuery,
           $attrs: ICheButtonDropdownAttributes): string {
    return `
<div class="che-button-dropdown btn-group" uib-dropdown>

  <!--type='button'-->
  <${$attrs.buttonStyle} ng-if="mainActionConfig.type!=='link'"
                        ng-disabled="isDisabled"
                        ng-click="mainActionConfig.action()"
                        class="split-button"
                        che-button-title="{{mainActionConfig.title}}"
                        name="{{mainActionConfig.name ? mainActionConfig.name : 'split-button'}}"></${$attrs.buttonStyle}>
  <!--type='link'-->
  <${$attrs.buttonStyle} ng-if="mainActionConfig.type==='link'"
                        ng-disabled="isDisabled"
                        href="{{mainActionConfig.href}}"
                        class="split-button"
                        che-button-title="{{mainActionConfig.title}}"
                        name="{{mainActionConfig.name ? mainActionConfig.name : 'split-button'}}"></${$attrs.buttonStyle}>

  <${$attrs.buttonStyle} uib-dropdown-toggle
                        ng-disabled="isDisabled"
                        id="dropdown-toggle"
                        class="dropdown"
                        che-button-title=""
                        name="dropdown-toggle"
                        che-button-icon="fa fa-caret-down"></${$attrs.buttonStyle}>

  <ul class="area-dropdown"
      uib-dropdown-menu
      role="menu"
      aria-labelledby="split-button">
    <li role="menuitem"
        ng-repeat="otherAction in otherActionsConfig | orderBy:'orderNumber'">
      <!--type='button'-->
      <span ng-if="otherAction.type!=='link'"
            ng-click="otherAction.action()">{{otherAction.title}}</span>
      <!--type='link'-->
      <a ng-if="otherAction.type==='link'"
         href="{{otherAction.href}}">{{otherAction.title}}</a>
    </li>
  </ul>

</div>
    `;
  }

}
