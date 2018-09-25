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
 * Defines a directive for user item in permissions list.
 *
 * @author Ann Shumilova
 */
export class MemberItem implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-members/member-item/member-item.html';
  replace: boolean = false;

  controller: string = 'MemberItemController';
  controllerAs: string = 'memberItemController';

  bindToController: boolean = true;

  scope: any = {
    member: '=member',
    callback: '=callback',
    hideDetails: '=hideDetails',
    editable: '=editable',
    isOwner: '=isOwner'
  };

}
