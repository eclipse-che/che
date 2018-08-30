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
 * @name organization.details:OrganizationNotFound
 * @restrict E
 * @element
 *
 * @description
 * `<organization-not-found organization-name="myOrganization"></organization-not-found>` for displaying "Organization not found" page.
 *
 * @usage
 *   <organization-not-found organization-name="myOrganization"></organization-not-found>
 *
 * @author Oleksii Kurinnyi
 */
export class OrganizationNotFound implements ng.IDirective {
  restrict: string = 'E';
  replace: boolean = true;
  templateUrl: string = 'app/organizations/organization-details/organization-not-found/organization-not-found.html';

  scope: any = {
    organizationName: '='
  };

}
