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
 * Defines a directive for creating a "Delete" button in list's header.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHeaderDeleteButton implements ng.IDirective {
  restrict: string = 'E';
  scope: {
    [propName: string]: string;
  } = {
    title: '@',
    onDelete: '&',
    disabled: '=?',
    disabledMessage: '@?'
  };

  template(): string {
    return `<che-button-primary class="che-list-header-delete-button"
                                che-button-title="{{title}}"
                                ng-click="onDelete()"
                                ng-disabled="disabled === true"></che-button-primary>
      <span ng-if="disabled === true && disabledMessage"
            class="che-list-header-delete-button-disable-message">
        {{disabledMessage}}
      </span>`;
  }

}
