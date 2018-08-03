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

import {GitUrlValidator} from './git-url-validator.directive';
import {UniqueProjectNameValidator} from './unique-project-name-validator.directive';
import {UniqueWorkspaceNameValidator} from './unique-workspace-name-validator.directive';
import {CustomValidator} from './custom-validator.directive';
import {UniqueStackNameValidator} from './unique-stack-name-validator.directive';
import {CityNameValidator} from './city-name-validator.directive';
import {CustomAsyncValidator} from './custom-async-validator.directive';
import {UniqueTeamNameValidator} from './unique-team-name-validator.directive';
import {UniqueFactoryNameValidator} from './unique-factory-name-validator.directive';

export class ValidatorConfig {

  constructor(register: che.IRegisterService) {

    register.directive('gitUrl', GitUrlValidator);
    register.directive('cityNameValidator', CityNameValidator);
    register.directive('uniqueProjectName', UniqueProjectNameValidator);
    register.directive('uniqueWorkspaceName', UniqueWorkspaceNameValidator);
    register.directive('customValidator', CustomValidator);
    register.directive('customAsyncValidator', CustomAsyncValidator);
    register.directive('uniqueStackName', UniqueStackNameValidator);
    register.directive('uniqueTeamName', UniqueTeamNameValidator);
    register.directive('uniqueFactoryName', UniqueFactoryNameValidator);
  }
}
