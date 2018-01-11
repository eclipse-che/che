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
