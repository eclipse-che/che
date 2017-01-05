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
 * Defines a directive for creating an indicator of workspace's status, which contains
 * both icon and text representation of current status
 * @author Oleksii Kurinnyi
 */
export class WorkspaceStatus {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';

    this.replace = true;
    this.templateUrl = 'app/workspaces/workspace-status/workspace-status.html';

    // scope values
    this.scope = {
      status : '=cheStatus'
    };
  }
}
