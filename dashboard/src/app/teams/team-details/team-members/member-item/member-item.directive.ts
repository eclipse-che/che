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
