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
import {CheWorkspace} from '../../../../../components/api/workspace/che-workspace.factory';

/**
 * @ngdoc controller
 * @name projects.list.controller:ProjectItemCtrl
 * @description This class is handling the controller for item of project list
 * @author Florent Benoit
 */
export class ProjectItemCtrl {

  static $inject = ['$location', 'cheWorkspace'];

  private $location: ng.ILocationService;
  private cheWorkspace: CheWorkspace;

  private workspace: che.IWorkspace;
  private project: che.IProject;

  /**
   * Default constructor that is using resource
   */
  constructor($location: ng.ILocationService,
              cheWorkspace: CheWorkspace) {
    this.$location = $location;
    this.cheWorkspace = cheWorkspace;
  }
}
