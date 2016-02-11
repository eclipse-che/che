/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

import {CheAPI} from './che-api.factory';
import {CheProject} from './che-project.factory';
import {CheWorkspace} from './che-workspace.factory';
import {CheUser} from './che-user.factory';
import {CheProjectType} from './che-project-type.factory';
import {CheProjectTemplate} from './che-project-template.factory';
import {CheRecipe} from './che-recipe.factory';
import {CheRecipeTemplate} from './che-recipe-template.factory';
import {CheStack} from './che-stack.factory';
import {CheWebsocket} from './che-websocket.factory';
import {CheProfile} from './che-profile.factory';
import {CheService} from './che-service.factory';
import {CheGit} from './che-git.factory';
import {CheSvn} from './che-svn.factory';
import {CheHttpBackend} from './test/che-http-backend';
import {CheHttpBackendProviderFactory} from './test/che-http-backend-provider.factory'
import {CheHttpBackendFactory} from './test/che-http-backend.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheAdminPlugins} from './che-admin-plugins.factory';
import {CheAdminService} from './che-admin-service.factory';

export class ApiConfig {

  constructor(register) {
    register.factory('cheProject', CheProject);
    register.factory('cheWorkspace', CheWorkspace);
    register.factory('cheUser', CheUser);
    register.factory('cheProjectType', CheProjectType);
    register.factory('cheProjectTemplate', CheProjectTemplate);
    register.factory('cheProfile', CheProfile);
    register.factory('cheWebsocket', CheWebsocket);
    register.factory('cheRecipe', CheRecipe);
    register.factory('cheRecipeTemplate', CheRecipeTemplate);
    register.factory('cheStack', CheStack);
    register.factory('cheGit', CheGit);
    register.factory('cheSvn', CheSvn);
    register.factory('cheHttpBackendProvider', CheHttpBackendProviderFactory);
    register.factory('cheHttpBackend', CheHttpBackendFactory);
    register.factory('cheAPIBuilder', CheAPIBuilder);
    register.factory('cheAdminPlugins', CheAdminPlugins);
    register.factory('cheAdminService', CheAdminService);
    register.factory('cheService', CheService);
    register.factory('cheAPI', CheAPI);
  }
}
