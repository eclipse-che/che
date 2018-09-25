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
 * Defines a directive for team item in list.
 *
 * @author Ann Shumilova
 */
export class TeamItem {
  restrict: string = 'E';
  templateUrl: string = 'app/teams/list/team-item/team-item.html';
  replace: boolean = false;

  controller: string = 'TeamItemController';
  controllerAs: string = 'teamItemController';
  bindToController: boolean = true;
  require: Array<string> = ['ngModel'];
  scope: {
    [propName: string]: string
  };

  constructor() {
    this.scope = {
      team: '=team',
      members: '=',
      ramCap: '=',
      isChecked: '=cdvyChecked',
      isSelect: '=?ngModel',
      onCheckboxClick: '&?cdvyOnCheckboxClick',
      selectable: '=cdvyIsSelectable',
      onUpdate: '&?onUpdate'
    };
  }
}
