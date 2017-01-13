/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating simple indicator of workspace's status.
 * @author Oleksii Kurinnyi
 */
export class WorkspaceStatusIndicator {
  restrict: string;
  replace: boolean;
  scope;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';

    this.replace = true;

    this.scope = {
      status: '=cheStatus'
    };
  }

  /**
   * Template for the simple indicator of workspace's status
   * @param element
   * @param attr
   * @returns {string} the template
   */
  template (element, attr) {
    let emptyCircleOnStopped = attr.cheEmptyCircle;

    return '<span ng-switch="status" class="workspace-status-indicator">' +
      '<span ng-switch-when="STOPPED" class="fa ' + (emptyCircleOnStopped ? 'fa-circle-o' : 'fa-circle') + ' workspace-status-stopped"></span>' +
      '<span ng-switch-when="PAUSED" class="fa fa-pause workspace-status-paused"></span>' +
      '<span ng-switch-when="RUNNING" class="fa fa-circle workspace-status-running"></span>' +
      '<span ng-switch-when="STARTING" class="workspace-status-spinner">' +
      '<div class="spinner"><div class="rect1"></div><div class="rect2"></div><div class="rect3"></div></div>' +
      '</span>' +
      '<span ng-switch-when="STOPPING" class="workspace-status-spinner">' +
      '<div class="spinner"><div class="rect1"></div><div class="rect2"></div><div class="rect3"></div></div>' +
      '</span>' +
      '<span ng-switch-when="ERROR" class="fa fa-circle workspace-status-error"></span>' +
      '<span ng-switch-when="SNAPSHOTTING" class="fa fa-circle workspace-status-snapshot"></span>' +
      '<span ng-switch-default class="fa ' + (emptyCircleOnStopped ? 'fa-circle-o' : 'fa-circle') + ' workspace-status-default"></span>' +
      '</span>';
  }
}
