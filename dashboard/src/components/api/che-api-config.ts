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
import {CheWorkspace} from './che-workspace.factory';
import {CheProjectTemplate} from './che-project-template.factory';
import {CheRecipe} from './che-recipe.factory';
import {CheRecipeTemplate} from './che-recipe-template.factory';
import {CheStack} from './che-stack.factory';
import {CheWebsocket} from './che-websocket.factory';
import {CheProfile} from './che-profile.factory';
import {ChePreferences} from './che-preferences.factory';
import {CheService} from './che-service.factory';
import {CheHttpBackend} from './test/che-http-backend';
import {CheHttpBackendProviderFactory} from './test/che-http-backend-provider.factory'
import {CheHttpBackendFactory} from './test/che-http-backend.factory';
import {CheAPIBuilder} from './builder/che-api-builder.factory';
import {CheAdminPlugins} from './che-admin-plugins.factory';
import {CheAdminService} from './che-admin-service.factory';
import {CheRemote} from './remote/che-remote.factory';
import {CheOAuthProvider} from './che-o-auth-provider.factory';
import {CheEnvironmentRegistry} from './environment/che-environment-registry.factory';
import {CheAgent} from './che-agent.factory';
import {CheSsh} from './che-ssh.factory';
import {CheNamespaceRegistry} from './namespace/che-namespace-registry.factory';

export class ApiConfig {

  constructor(register) {
    register.factory('cheWorkspace', CheWorkspace);
    register.factory('cheProjectTemplate', CheProjectTemplate);
    register.factory('cheProfile', CheProfile);
    register.factory('chePreferences', ChePreferences);
    register.factory('cheWebsocket', CheWebsocket);
    register.factory('cheRecipe', CheRecipe);
    register.factory('cheRecipeTemplate', CheRecipeTemplate);
    register.factory('cheStack', CheStack);
    register.factory('cheHttpBackendProvider', CheHttpBackendProviderFactory);
    register.factory('cheHttpBackend', CheHttpBackendFactory);
    register.factory('cheAPIBuilder', CheAPIBuilder);
    register.factory('cheAdminPlugins', CheAdminPlugins);
    register.factory('cheAdminService', CheAdminService);
    register.factory('cheService', CheService);
    register.factory('cheAPI', CheAPI);
    register.factory('cheRemote', CheRemote);
    register.factory('cheOAuthProvider', CheOAuthProvider);
    register.factory('cheEnvironmentRegistry', CheEnvironmentRegistry);
    register.factory('cheAgent', CheAgent);
    register.factory('cheSsh', CheSsh);
    register.factory('cheNamespaceRegistry', CheNamespaceRegistry);
  }
}
