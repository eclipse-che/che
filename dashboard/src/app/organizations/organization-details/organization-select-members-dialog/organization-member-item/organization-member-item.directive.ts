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
 * Defines a directive for member in list.
 *
 * @author Oleksii Kurinnyi
 */
export class OrganizationMemberItem {
  restrict: string = 'E';

  templateUrl: string = 'app/organizations/organization-details/organization-select-members-dialog/organization-member-item/organization-member-item.html';
  replace: boolean = false;

  scope: {[prop: string]: string} = {
    member: '=',
    isSelected: '=',
    onChange: '&'
  };

}
