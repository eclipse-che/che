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
