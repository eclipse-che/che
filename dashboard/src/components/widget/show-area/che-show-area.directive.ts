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
 * @name components.directive:cheShowArea
 * @restrict E
 * @function
 * @element
 *
 * @description
 * `<che-show-area>` defines area to show/hide content with button
 *
 * @author Oleksii Orel
 */
export class CheShowArea {
  restrict: string;
  templateUrl: string;
  transclude: boolean;
  scope: Object;

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.restrict = 'E';
    this.transclude = true;
    this.templateUrl = 'components/widget/show-area/che-show-area.html';

    // scope values
    this.scope = {};
  }
}
