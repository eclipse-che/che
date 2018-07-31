/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating an indicator of workspace's status, which contains
 * both icon and text representation of current status
 * @author Oleksii Kurinnyi
 */
export class WorkspaceStatus implements ng.IDirective {

  restrict = 'E';

  replace = true;
  templateUrl = 'app/workspaces/workspace-status/workspace-status.html';

  // scope values
  scope = {
    status: '=cheStatus',
    isSupported: '=cheIsSupported'
  };
}
