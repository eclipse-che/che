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


import {CreateFactoryCtrl} from '../create-factory/create-factory.controller';

import {FactoryFromWorkspaceCtrl} from '../create-factory/workspaces-tab/factory-from-workpsace.controller';
import {FactoryFromWorkspace} from '../create-factory/workspaces-tab/factory-from-workspace.directive';
import {FactoryFromFileCtrl} from '../create-factory/config-file-tab/factory-from-file.controller';
import {FactoryFromFile} from '../create-factory/config-file-tab/factory-from-file.directive';
import {FactoryFromTemplateController} from '../create-factory/template-tab/factory-from-template.controller';
import {FactoryFromTemplate} from '../create-factory/template-tab/factory-from-template.directive';
import {FactoryActionBoxController} from './action/factory-action-box.controller';
import {FactoryActionBox} from './action/factory-action-box.directive';
import {FactoryActionDialogEditController} from './action/factory-action-edit.controller';
import {FactoryCommandController} from './command/factory-command.controller';
import {FactoryCommand} from './command/factory-command.directive';
import {FactoryCommandDialogEditController} from './command/factory-command-edit.controller';
import {CreateFactoryGitController} from './git/create-factory-git.controller';
import {CreateFactoryGit} from './git/create-factory-git.directive';

export class CreateFactoryConfig {

  constructor(register: che.IRegisterService) {

    register.controller('CreateFactoryCtrl', CreateFactoryCtrl);

    register.controller('FactoryFromWorkspaceCtrl', FactoryFromWorkspaceCtrl);
    register.directive('cdvyFactoryFromWorkspace', FactoryFromWorkspace);

    register.controller('FactoryFromFileCtrl', FactoryFromFileCtrl);
    register.directive('cdvyFactoryFromFile', FactoryFromFile);

    register.controller('FactoryFromTemplateController', FactoryFromTemplateController);
    register.directive('cheFactoryFromTemplate', FactoryFromTemplate);

    register.controller('FactoryActionBoxController', FactoryActionBoxController);
    register.directive('cdvyFactoryActionBox', FactoryActionBox);

    register.controller('FactoryCommandController', FactoryCommandController);
    register.directive('cdvyFactoryCommand', FactoryCommand);

    register.controller('CreateFactoryGitController', CreateFactoryGitController);
    register.directive('cdvyCreateFactoryGit', CreateFactoryGit);

    register.controller('FactoryActionDialogEditController', FactoryActionDialogEditController);
    register.controller('FactoryCommandDialogEditController', FactoryCommandDialogEditController);



    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/factories/create-factory', {
        title: 'New Factory',
        templateUrl: 'app/factories/create-factory/create-factory.html',
        controller: 'CreateFactoryCtrl',
        controllerAs: 'createFactoryCtrl'
      });

    }]);

  }
}
