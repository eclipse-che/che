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
 * @name workspaces.details.directive:shareWorkspace
 * @restrict E
 * @element
 *
 * @description
 * <share-workspace></share-workspace> for managing sharing the workspace.
 *
 * @usage
 *   <share-workspace></share-workspace>
 *
 * @author Ann Shumilova
 */
export class ShareWorkspace implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/share-workspace/share-workspace.html';

  controller = 'ShareWorkspaceController';
  controllerAs = 'shareWorkspaceController';
  bindToController = true;
}
