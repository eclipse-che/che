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
 * @name projects.create.directive:ReadyToGoStacks
 * @restrict E
 * @element
 *
 * @description
 * `<ready-to-go-stacks></ready-to-go-stacks>` for creating new projects from ready to go stacks.
 *
 * @usage
 *   <ready-to-go-stacks></ready-to-go-stacks>
 *
 * @author Florent Benoit
 */
export class ReadyToGoStacks implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/select-stack/ready-to-go-stacks/ready-to-go-stacks.html';

  controller = 'ReadyToGoStacksController';
  controllerAs = 'readyToGoStacksCtrl';
  bindToController = true;

  // scope values
  scope = {
    tabName: '@cheTabName'
  };

}
