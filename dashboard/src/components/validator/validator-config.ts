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

import {GitUrlValidator} from './git-url-validator.directive';
import {UniqueProjectNameValidator} from './unique-project-name-validator.directive';
import {UniqueWorkspaceNameValidator} from './unique-workspace-name-validator.directive';
import {CustomValidator} from './custom-validator.directive';
import {UniqueStackNameValidator} from './unique-stack-name-validator.directive';


export class ValidatorConfig {

  constructor(register) {

    register.directive('gitUrl', GitUrlValidator)
      .directive('uniqueProjectName', UniqueProjectNameValidator)
      .directive('uniqueWorkspaceName', UniqueWorkspaceNameValidator)
      .directive('customValidator', CustomValidator)
      .directive('uniqueStackName', UniqueStackNameValidator);
  }
}
