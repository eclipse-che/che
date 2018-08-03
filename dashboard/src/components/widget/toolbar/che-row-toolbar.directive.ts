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
 * @name components.directive:cheRowToolbar
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-row-toolbar>` defines a top for row toolbar.
 *
 * @param {string=} cheTitle the title of the toolbar
 * @param {string=} link-href the optional link of the toolbar
 * @param {string=} link-title the link title
 * @usage
 *   <che-row-toolbar title="Projects"></che-row-toolbar>
 *
 * @author Oleksii Orel
 */
export class CheRowToolbar {
  restrict = 'E';
  transclude = true;
  templateUrl = 'components/widget/toolbar/che-row-toolbar.html';

  // scope values
  scope = {
    linkTitle: '@',
    linkHref: '@',
    cheTitle: '@'
  };
}

