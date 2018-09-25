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
 * @name diagnostics.directive:diagnostics
 * @restrict E
 * @element
 *
 * @description
 * <diagnostics></diagnostics>` for displaying diagnostics.
 *
 * @usage
 *   <diagnostics></diagnostics>
 *
 * @author Florent Benoit
 */
export class Diagnostics implements ng.IDirective {

  replace: boolean = false;
  restrict: string = 'E';
  templateUrl: string = 'app/diagnostics/diagnostics-widget.html';
  controller: string = 'DiagnosticsController';
  controllerAs: string = 'diagnosticsController';
  bindToController: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    // scope values
    this.scope = {};
  }

}
