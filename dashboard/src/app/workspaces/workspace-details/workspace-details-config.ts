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
import {WorkspaceDetailsSshCtrl} from './workspace-ssh/workspace-details-ssh.controller';
import {WorkspaceDetailsSsh} from './workspace-ssh/workspace-details-ssh.directive';
import {WorkspaceDetailsController} from './workspace-details.controller';
import {WorkspaceDetailsProjectsCtrl} from './workspace-projects/workspace-details-projects.controller';
import {WorkspaceDetailsProjects} from './workspace-projects/workspace-details-projects.directive';
import {WorkspaceDetailsProjectsService} from './workspace-projects/workspace-details-projects.service';
import {AddProjectPopoverController} from './workspace-projects/add-project-popover/add-project-popover.controller';
import {AddProjectPopover} from './workspace-projects/add-project-popover/add-project-popover.directive';
import {WorkspaceDetailsService} from './workspace-details.service';
import {ExportWorkspaceDialogController} from './export-workspace/dialog/export-workspace-dialog.controller';
import {ExportWorkspaceController} from './export-workspace/export-workspace.controller';
import {ExportWorkspace} from './export-workspace/export-workspace.directive';
import {WorkspaceRecipeImportController} from './select-stack/recipe-import/workspace-recipe-import.controller';
import {WorkspaceRecipeImport} from './select-stack/recipe-import/workspace-recipe-import.directive';
import {WorkspaceRecipeAuthoringController} from './select-stack/recipe-authoring/workspace-recipe-authoring.controller';
import {WorkspaceRecipeAuthoring} from './select-stack/recipe-authoring/workspace-recipe-authoring.directive';
import {WorkspaceConfigImportController} from './config-import/workspace-config-import.controller';
import {WorkspaceConfigImport} from './config-import/workspace-config-import.directive';
import {ReadyToGoStacksController} from './select-stack/ready-to-go-stacks/ready-to-go-stacks.controller';
import {ReadyToGoStacks} from './select-stack/ready-to-go-stacks/ready-to-go-stacks.directive';
import {CreateProjectStackLibraryController} from './select-stack/stack-library/create-project-stack-library.controller';
import {CreateProjectStackLibrary} from './select-stack/stack-library/create-project-stack-library.directive';
import {CheStackLibrarySelecter} from './select-stack/stack-library/stack-library-selecter/che-stack-library-selecter.directive';
import {WorkspaceSelectStackController} from './select-stack/workspace-select-stack.controller';
import {WorkspaceSelectStack} from './select-stack/workspace-select-stack.directive';
import {WorkspaceEnvironmentsController} from './environments/environments.controller';
import {WorkspaceEnvironments} from './environments/environments.directive';
import {WorkspaceMachineConfigController} from './environments/machine-config/machine-config.controller';
import {WorkspaceMachineConfig} from './environments/machine-config/machine-config.directive';
import {EditMachineNameDialogController} from './environments/machine-config/edit-machine-name-dialog/edit-machine-name-dialog.controller';
import {DeleteDevMachineDialogController} from './environments/machine-config/delete-dev-machine-dialog/delete-dev-machine-dialog.controller';
import {DevMachineLabel} from './environments/machine-config/dev-machine-label/dev-machine-label.directive';
import {ListEnvVariablesController} from './environments/list-env-variables/list-env-variables.controller';
import {ListEnvVariables} from './environments/list-env-variables/list-env-variables.directive';
import {EditVariableDialogController} from './environments/list-env-variables/edit-variable-dialog/edit-variable-dialog.controller';
import {ListServersController} from './environments/list-servers/list-servers.controller';
import {ListServers} from './environments/list-servers/list-servers.directive';
import {EditServerDialogController} from './environments/list-servers/edit-server-dialog/edit-server-dialog.controller';
import {ListCommandsController} from './list-commands/list-commands.controller';
import {ListCommands} from './list-commands/list-commands.directive';
import {EditCommandDialogController} from './list-commands/edit-command-dialog/edit-command-dialog.controller';
import {ListAgentsController} from './environments/list-agents/list-agents.controller';
import {ListAgents} from './environments/list-agents/list-agents.directive';
import {WorkspaceMachinesController} from './workspace-machines/workspace-machines.controller';
import {WorkspaceMachines} from './workspace-machines/workspace-machines.directive';
import {WorkspaceMachineItem} from './workspace-machines/machine-item/workspace-machine-item.directive';
import {EditMachineDialogController} from './workspace-machines/edit-machine-dialog/edit-machine-dialog.controller';
import {CheWorkspaceStatusButton} from './status-button/workspace-status-button.directive';
import {WorkspaceDetailsOverviewController} from './workspace-overview/workspace-details-overview.controller';
import {WorkspaceDetailsOverview} from './workspace-overview/workspace-details-overview.directive';
import {EnvVariablesController} from './workspace-machine-env-variables/env-variables.controller';
import {EnvVariables} from './workspace-machine-env-variables/env-variables.directive';
import {EditEnvVariableDialogController} from './workspace-machine-env-variables/edit-variable-dialog/edit-variable-dialog.controller';
import {MachineSelectorController} from './machine-selector/machine-selector.controller';
import {MachineSelector} from './machine-selector/machine-selector.directive';
import {MachineServersController} from './workspace-machine-servers/machine-servers.controller';
import {MachineServers} from './workspace-machine-servers/machine-servers.directive';
import {EditMachineServerDialogController} from './workspace-machine-servers/edit-machine-server-dialog/edit-server-dialog.controller';
import {MachineAgentsController} from './workspace-machine-agents/machine-agents.controller';
import {MachineAgents} from './workspace-machine-agents/machine-agents.directive';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {WorkspaceConfigService} from '../workspace-config.service';
import {CheRecipeService} from './che-recipe.service';
import {CheProjectItem} from './workspace-projects/project-item/project-item.directive';
import {ProjectItemCtrl} from './workspace-projects/project-item/project-item.controller';
import {NoGithubOauthDialogController} from '../create-workspace/project-source-selector/add-import-project/import-github-project/oauth-dialog/no-github-oauth-dialog.controller';
import {EditMachineVolumeDialogController} from './workspace-machine-volumes/edit-volume-dialog/edit-volume-dialog.controller';
import {MachineVolumes} from './workspace-machine-volumes/machine-volumes.directive';
import {MachineVolumesController} from './workspace-machine-volumes/machine-volumes.controller';
import {WorkspaceToolsConfig} from './workspace-tools/workspace-tools-config';



/**
 * @ngdoc controller
 * @name workspace-details:WorkspaceDetailsConfig
 * @description This class is used for configuring all workspace details stuff.
 * @author Oleksii Orel
 */
export class WorkspaceDetailsConfig {

  constructor(register: che.IRegisterService) {

    register.controller('WorkspaceDetailsSshCtrl', WorkspaceDetailsSshCtrl);
    register.directive('workspaceDetailsSsh', WorkspaceDetailsSsh);
    register.controller('WorkspaceDetailsController', WorkspaceDetailsController);

    register.controller('WorkspaceDetailsProjectsCtrl', WorkspaceDetailsProjectsCtrl);
    register.directive('workspaceDetailsProjects', WorkspaceDetailsProjects);
    register.service('workspaceDetailsProjectsService', WorkspaceDetailsProjectsService);

    register.service('cheRecipeService', CheRecipeService);
    register.service('workspaceDetailsService', WorkspaceDetailsService);

    register.directive('cheProjectItem', CheProjectItem);
    register.controller('ProjectItemCtrl', ProjectItemCtrl);
    register.controller('NoGithubOauthDialogController', NoGithubOauthDialogController);

    register.controller('AddProjectPopoverController', AddProjectPopoverController);
    register.directive('addProjectPopover', AddProjectPopover);
    register.controller('ExportWorkspaceDialogController', ExportWorkspaceDialogController);
    register.controller('ExportWorkspaceController', ExportWorkspaceController);
    register.directive('exportWorkspace', ExportWorkspace);
    register.controller('WorkspaceRecipeImportController', WorkspaceRecipeImportController);
    register.directive('cheWorkspaceRecipeImport', WorkspaceRecipeImport);
    register.controller('WorkspaceRecipeAuthoringController', WorkspaceRecipeAuthoringController);
    register.directive('cheWorkspaceRecipeAuthoring', WorkspaceRecipeAuthoring);
    register.controller('WorkspaceConfigImportController', WorkspaceConfigImportController);
    register.directive('cheWorkspaceConfigImport', WorkspaceConfigImport);
    register.controller('ReadyToGoStacksController', ReadyToGoStacksController);
    register.directive('readyToGoStacks', ReadyToGoStacks);
    register.controller('CreateProjectStackLibraryController', CreateProjectStackLibraryController);
    register.directive('createProjectStackLibrary', CreateProjectStackLibrary);
    register.directive('cheStackLibrarySelecter', CheStackLibrarySelecter);
    register.controller('WorkspaceSelectStackController', WorkspaceSelectStackController);
    register.directive('workspaceSelectStack', WorkspaceSelectStack);
    register.controller('WorkspaceEnvironmentsController', WorkspaceEnvironmentsController);
    register.directive('workspaceEnvironments', WorkspaceEnvironments);
    register.controller('WorkspaceMachineConfigController', WorkspaceMachineConfigController);
    register.directive('workspaceMachineConfig', WorkspaceMachineConfig);
    register.controller('EditMachineNameDialogController', EditMachineNameDialogController);
    register.controller('DeleteDevMachineDialogController', DeleteDevMachineDialogController);
    register.directive('devMachineLabel', DevMachineLabel);
    register.controller('ListEnvVariablesController', ListEnvVariablesController);
    register.directive('listEnvVariables', ListEnvVariables);
    register.controller('EditVariableDialogController', EditVariableDialogController);
    register.controller('ListServersController', ListServersController);
    register.directive('listServers', ListServers);
    register.controller('EditServerDialogController', EditServerDialogController);
    register.controller('ListCommandsController', ListCommandsController);
    register.directive('listCommands', ListCommands);
    register.controller('EditCommandDialogController', EditCommandDialogController);
    register.controller('ListAgentsController', ListAgentsController);
    register.directive('listAgents', ListAgents);
    register.controller('WorkspaceMachinesController', WorkspaceMachinesController);
    register.directive('workspaceMachines', WorkspaceMachines);
    register.directive('workspaceMachineItem', WorkspaceMachineItem);
    register.controller('EditMachineDialogController', EditMachineDialogController);
    register.directive('workspaceStatusButton', CheWorkspaceStatusButton);
    register.controller('WorkspaceDetailsOverviewController', WorkspaceDetailsOverviewController);
    register.directive('workspaceDetailsOverview', WorkspaceDetailsOverview);
    register.controller('EnvVariablesController', EnvVariablesController);
    register.directive('cheEnvVariables', EnvVariables);
    register.controller('EditEnvVariableDialogController', EditEnvVariableDialogController);
    register.controller('MachineVolumesController', MachineVolumesController);
    register.directive('cheMachineVolumes', MachineVolumes);
    register.controller('EditMachineVolumeDialogController', EditMachineVolumeDialogController);
    register.controller('MachineSelectorController', MachineSelectorController);
    register.directive('cheMachineSelector', MachineSelector);
    register.controller('MachineServersController', MachineServersController);
    register.directive('cheMachineServers', MachineServers);
    register.controller('EditMachineServerDialogController', EditMachineServerDialogController);
    register.controller('MachineAgentsController', MachineAgentsController);
    register.directive('cheMachineAgents', MachineAgents);

    /* tslint:disable */
    new WorkspaceToolsConfig(register);
    /* tslint:enable */

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider
        .accessWhen('/workspace/:namespace*/:workspaceName', {
          title: (params: any) => {
            return params.workspaceName;
          },
          reloadOnSearch: false,
          templateUrl: 'app/workspaces/workspace-details/workspace-details.html',
          controller: 'WorkspaceDetailsController',
          controllerAs: 'workspaceDetailsController',
          resolve: {
            initData: ['$q', '$route', 'cheWorkspace', 'workspaceConfigService', ($q: ng.IQService, $route: ng.route.IRouteService, cheWorkspace: CheWorkspace, workspaceConfigService: WorkspaceConfigService) => {
              return workspaceConfigService.resolveWorkspaceRoute().then(() => {
                const {namespace, workspaceName} = $route.current.params;
                const workspaceDetails = cheWorkspace.getWorkspaceByName(namespace, workspaceName);
                return {namespaceId: namespace, workspaceName: workspaceName, workspaceDetails: workspaceDetails};
              });
            }]
          }
        });
    }]);
  }
}
