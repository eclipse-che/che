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

import {ListWorkspacesCtrl} from './list-workspaces/list-workspaces.controller';
import {CheWorkspaceItem} from './list-workspaces/workspace-item/workspace-item.directive';
import {CheWorkspaceStatus} from './list-workspaces/workspace-status-action/workspace-status.directive';
import {WorkspaceStatusController} from './list-workspaces/workspace-status-action/workspace-status.controller';
import {WorkspaceDetailsController} from './workspace-details/workspace-details.controller';
import {WorkspaceStacksController} from './workspace-details/workspace-stacks/workspace-stacks.controller';
import {WorkspaceStacks} from './workspace-details/workspace-stacks/workspace-stacks.directive';
import {UsageChart} from './list-workspaces/workspace-item/usage-chart.directive';
import {WorkspaceItemCtrl} from './list-workspaces/workspace-item/workspace-item.controller';
import {WorkspaceEditModeOverlay} from './workspace-edit-mode/workspace-edit-mode-overlay.directive';
import {WorkspaceEditModeToolbarButton} from './workspace-edit-mode/workspace-edit-mode-toolbar-button.directive';
import {WorkspaceDetailsSsh} from './workspace-details/workspace-ssh/workspace-details-ssh.directive';
import {WorkspaceDetailsSshCtrl} from './workspace-details/workspace-ssh/workspace-details-ssh.controller';
import {WorkspaceDetailsProjectsCtrl} from './workspace-details/workspace-projects/workspace-details-projects.controller';
import {WorkspaceDetailsService} from './workspace-details/workspace-details.service';
import {ExportWorkspaceController} from './workspace-details/export-workspace/export-workspace.controller';
import {ExportWorkspace} from './workspace-details/export-workspace/export-workspace.directive';
import {ExportWorkspaceDialogController} from  './workspace-details/export-workspace/dialog/export-workspace-dialog.controller';
import {WorkspaceDetailsProjects} from './workspace-details/workspace-projects/workspace-details-projects.directive';
import {ReadyToGoStacksController} from './workspace-details/select-stack/ready-to-go-stacks/ready-to-go-stacks.controller';
import {ReadyToGoStacks} from './workspace-details/select-stack/ready-to-go-stacks/ready-to-go-stacks.directive';
import {WorkspaceRecipeImportController} from './workspace-details/select-stack/recipe-import/workspace-recipe-import.controller';
import {WorkspaceRecipeImport} from './workspace-details/select-stack/recipe-import/workspace-recipe-import.directive';
import {WorkspaceRecipeAuthoringController} from './workspace-details/select-stack/recipe-authoring/workspace-recipe-authoring.controller';
import {WorkspaceRecipeAuthoring} from './workspace-details/select-stack/recipe-authoring/workspace-recipe-authoring.directive';
import {WorkspaceConfigImportController} from './workspace-details/config-import/workspace-config-import.controller';
import {WorkspaceConfigImport} from './workspace-details/config-import/workspace-config-import.directive';
import {CheStackLibrarySelecter} from './workspace-details/select-stack/stack-library/stack-library-selecter/che-stack-library-selecter.directive';
import {CreateProjectStackLibraryController} from './workspace-details/select-stack/stack-library/create-project-stack-library.controller';
import {CreateProjectStackLibrary} from './workspace-details/select-stack/stack-library/create-project-stack-library.directive';
import {WorkspaceSelectStackController} from './workspace-details/select-stack/workspace-select-stack.controller';
import {WorkspaceSelectStack} from './workspace-details/select-stack/workspace-select-stack.directive';

import {CheWorkspaceRamAllocationSliderController} from './workspace-ram-slider/che-workspace-ram-allocation-slider.controller';
import {CheWorkspaceRamAllocationSlider} from './workspace-ram-slider/che-workspace-ram-allocation-slider.directive';
import {WorkspaceStatus} from './workspace-status/workspace-status.directive';
import {WorkspaceStatusIndicator} from './workspace-status/workspace-status-indicator.directive';

import {CheStackLibraryFilterController} from './workspace-details/select-stack/stack-library/stack-library-filter/che-stack-library-filter.controller';
import {CheStackLibraryFilter}     from './workspace-details/select-stack/stack-library/stack-library-filter/che-stack-library-filter.directive';
import {CreateProjectStackLibrarySelectedStackFilter} from './workspace-details/select-stack/stack-library/create-project-stack-library-selected-stack.filter';

import {WorkspaceEnvironmentsController} from './workspace-details/environments/environments.controller';
import {WorkspaceEnvironments} from './workspace-details/environments/environments.directive';
import {WorkspaceMachineConfigController} from './workspace-details/environments/machine-config/machine-config.controller';
import {WorkspaceMachineConfig} from './workspace-details/environments/machine-config/machine-config.directive';
import {EditMachineNameDialogController} from  './workspace-details/environments/machine-config/edit-machine-name-dialog/edit-machine-name-dialog.controller';

import {ListEnvVariablesController} from './workspace-details/environments/list-env-variables/list-env-variables.controller';
import {ListEnvVariables} from './workspace-details/environments/list-env-variables/list-env-variables.directive';
import {AddVariableDialogController} from  './workspace-details/environments/list-env-variables/add-variable-dialog/add-variable-dialog.controller';
import {EditVariableDialogController} from  './workspace-details/environments/list-env-variables/edit-variable-dialog/edit-variable-dialog.controller';

import {ListPortsController} from './workspace-details/environments/list-ports/list-ports.controller';
import {ListPorts} from './workspace-details/environments/list-ports/list-ports.directive';
import {AddPortDialogController} from  './workspace-details/environments/list-ports/add-port-dialog/add-port-dialog.controller';
import {EditPortDialogController} from  './workspace-details/environments/list-ports/edit-port-dialog/edit-port-dialog.controller';

import {ListCommandsController} from './workspace-details/list-commands/list-commands.controller';
import {ListCommands} from './workspace-details/list-commands/list-commands.directive';
import {EditCommandDialogController} from  './workspace-details/list-commands/edit-command-dialog/edit-command-dialog.controller';

import {ListAgentsController} from  './workspace-details/environments/list-agents/list-agents.controller';
import {AddMachineDialogController} from  './workspace-details/environments/add-machine-dialog/add-machine-dialog.controller';
import {ListAgents} from  './workspace-details/environments/list-agents/list-agents.directive';


/**
 * @ngdoc controller
 * @name workspaces:WorkspacesConfig
 * @description This class is used for configuring all workspaces stuff.
 * @author Ann Shumilova
 */
export class WorkspacesConfig {

  constructor(register) {

    new CreateProjectStackLibrarySelectedStackFilter(register);

    register.controller('WorkspaceDetailsSshCtrl', WorkspaceDetailsSshCtrl);
    register.directive('workspaceDetailsSsh', WorkspaceDetailsSsh);

    register.controller('ListWorkspacesCtrl', ListWorkspacesCtrl);
    register.controller('WorkspaceDetailsController', WorkspaceDetailsController);
    register.controller('WorkspaceStacksController', WorkspaceStacksController);
    register.directive('workspaceStacks', WorkspaceStacks);

    register.directive('cheWorkspaceItem', CheWorkspaceItem);
    register.controller('WorkspaceItemCtrl', WorkspaceItemCtrl);
    register.directive('usageChart', UsageChart);

    register.directive('cheWorkspaceStatus', CheWorkspaceStatus);
    register.controller('WorkspaceStatusController', WorkspaceStatusController);

    register.directive('workspaceEditModeOverlay', WorkspaceEditModeOverlay);
    register.directive('workspaceEditModeToolbarButton', WorkspaceEditModeToolbarButton);

    register.controller('WorkspaceDetailsProjectsCtrl', WorkspaceDetailsProjectsCtrl);
    register.directive('workspaceDetailsProjects', WorkspaceDetailsProjects);
    register.service('workspaceDetailsService', WorkspaceDetailsService);

    register.controller('ExportWorkspaceDialogController', ExportWorkspaceDialogController);
    register.controller('ExportWorkspaceController', ExportWorkspaceController);
    register.directive('exportWorkspace', ExportWorkspace);

    register.controller('WorkspaceRecipeImportController', WorkspaceRecipeImportController);
    register.directive('cheWorkspaceRecipeImport', WorkspaceRecipeImport);

    register.controller('WorkspaceRecipeAuthoringController', WorkspaceRecipeAuthoringController);
    register.directive('cheWorkspaceRecipeAuthoring', WorkspaceRecipeAuthoring);

    register.controller('WorkspaceConfigImportController', WorkspaceConfigImportController);
    register.directive('cheWorkspaceConfigImport', WorkspaceConfigImport);

    register.controller('CheWorkspaceRamAllocationSliderController', CheWorkspaceRamAllocationSliderController);
    register.directive('cheWorkspaceRamAllocationSlider', CheWorkspaceRamAllocationSlider);

    register.directive('workspaceStatus', WorkspaceStatus);
    register.directive('workspaceStatusIndicator', WorkspaceStatusIndicator);

    register.controller('ReadyToGoStacksController', ReadyToGoStacksController);
    register.directive('readyToGoStacks', ReadyToGoStacks);

    register.controller('CreateProjectStackLibraryController', CreateProjectStackLibraryController);

    register.directive('createProjectStackLibrary', CreateProjectStackLibrary);
    register.directive('cheStackLibrarySelecter', CheStackLibrarySelecter);

    register.controller('WorkspaceSelectStackController', WorkspaceSelectStackController);
    register.directive('cheWorkspaceSelectStack', WorkspaceSelectStack);

    register.controller('CheStackLibraryFilterController', CheStackLibraryFilterController);
    register.directive('cheStackLibraryFilter', CheStackLibraryFilter);

    register.controller('WorkspaceEnvironmentsController', WorkspaceEnvironmentsController);
    register.directive('workspaceEnvironments', WorkspaceEnvironments);
    register.controller('WorkspaceMachineConfigController', WorkspaceMachineConfigController);
    register.directive('workspaceMachineConfig', WorkspaceMachineConfig);
    register.controller('EditMachineNameDialogController', EditMachineNameDialogController);

    register.controller('ListEnvVariablesController', ListEnvVariablesController);
    register.directive('listEnvVariables', ListEnvVariables);
    register.controller('AddVariableDialogController', AddVariableDialogController);
    register.controller('EditVariableDialogController', EditVariableDialogController);

    register.controller('ListPortsController', ListPortsController);
    register.directive('listPorts', ListPorts);
    register.controller('AddPortDialogController', AddPortDialogController);
    register.controller('EditPortDialogController', EditPortDialogController);

    register.controller('ListCommandsController', ListCommandsController);
    register.directive('listCommands', ListCommands);
    register.controller('EditCommandDialogController', EditCommandDialogController);

    register.controller('AddMachineDialogController', AddMachineDialogController);
    register.controller('ListAgentsController', ListAgentsController);
    register.directive('listAgents', ListAgents);

    let locationProvider = {
      title: (params: any) => { return params.workspaceName; },
      templateUrl: 'app/workspaces/workspace-details/workspace-details.html',
      controller: 'WorkspaceDetailsController',
      controllerAs: 'workspaceDetailsController'
    };

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/workspaces', {
        title: 'Workspaces',
        templateUrl: 'app/workspaces/list-workspaces/list-workspaces.html',
        controller: 'ListWorkspacesCtrl',
        controllerAs: 'listWorkspacesCtrl'
      })
      .accessWhen('/workspace/:namespace/:workspaceName', locationProvider)
      .accessWhen('/workspace/:namespace/:workspaceName/:page', locationProvider)
      .accessWhen('/create-workspace', {
        title: 'New Workspace',
        templateUrl: 'app/workspaces/workspace-details/workspace-details.html',
        controller: 'WorkspaceDetailsController',
        controllerAs: 'workspaceDetailsController'
      });
    });
  }
}
