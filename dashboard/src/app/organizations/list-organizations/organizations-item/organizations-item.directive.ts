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
 * @ngdoc directive
 * @name  organizations.list.Item.controller:WorkspaceItem
 * @restrict E
 * @element
 *
 * @description
 * `<organizations-item organization="ctrl.organization"></organizations-item>` for displaying list of organizations
 *
 * @usage
 *   <organizations-item organization="ctrl.organization"></organizations-item>
 *
 * @author Oleksii Orel
 */
export class OrganizationsItem implements ng.IDirective {

  restrict = 'E';
  require = ['ngModel'];
  templateUrl = 'app/organizations/list-organizations/organizations-item/organizations-item.html';
  controller = 'OrganizationsItemController';
  controllerAs = 'organizationsItemController';
  bindToController = true;

  // scope values
  scope = {
    organization: '=',
    members: '=',
    totalRam: '=',
    availableRam: '=',
    isChecked: '=cdvyChecked',
    isSelect: '=?ngModel',
    isSelectable: '=?cdvyIsSelectable',
    onCheckboxClick: '&?cdvyOnCheckboxClick',
    onUpdate: '&?onUpdate'
  };
}
