/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {ListWorkspacesCtrl} from './list-workspaces/list-workspaces.controller';
import {CheWorkspaceItem} from './list-workspaces/workspace-item/workspace-item.directive';
import {CheWorkspaceStatus} from './list-workspaces/workspace-status-action/workspace-status.directive';
import {WorkspaceStatusController} from './list-workspaces/workspace-status-action/workspace-status.controller';
import {WorkspaceDetailsController} from './workspace-details/workspace-details.controller';
import {UsageChart} from './list-workspaces/workspace-item/usage-chart.directive';
import {WorkspaceItemCtrl} from './list-workspaces/workspace-item/workspace-item.controller';
import {WorkspaceEditModeOverlay} from './workspace-edit-mode/workspace-edit-mode-overlay.directive';
import {WorkspaceEditModeToolbarButton} from './workspace-edit-mode/workspace-edit-mode-toolbar-button.directive';
import {WorkspaceDetailsSsh} from './workspace-details/workspace-ssh/workspace-details-ssh.directive';
import {WorkspaceDetailsSshCtrl} from './workspace-details/workspace-ssh/workspace-details-ssh.controller';
import {WorkspaceDetailsProjectsCtrl} from './workspace-details/workspace-projects/workspace-details-projects.controller';
import {WorkspaceDetailsProjects} from './workspace-details/workspace-projects/workspace-details-projects.directive';
import {WorkspaceDetailsProjectsService} from './workspace-details/workspace-projects/workspace-details-projects.service';
import {ProjectDetailsController} from './workspace-details/workspace-projects/project-details/project-details.controller';
import {ProjectRepositoryController} from './workspace-details/workspace-projects/project-details/repository/project-repository.controller';
import {ProjectRepository} from './workspace-details/workspace-projects/project-details/repository/project-repository.directive';
import {CheProjectItem} from './workspace-details/workspace-projects/project-item/project-item.directive';
import {ProjectItemCtrl} from './workspace-details/workspace-projects/project-item/project-item.controller';
import {AddProjectPopoverController} from './workspace-details/workspace-projects/add-project-popover/add-project-popover.controller';
import {AddProjectPopover} from './workspace-details/workspace-projects/add-project-popover/add-project-popover.directive';
import {WorkspaceDetailsService} from './workspace-details/workspace-details.service';
import {ExportWorkspaceController} from './workspace-details/export-workspace/export-workspace.controller';
import {ExportWorkspace} from './workspace-details/export-workspace/export-workspace.directive';
import {ExportWorkspaceDialogController} from  './workspace-details/export-workspace/dialog/export-workspace-dialog.controller';
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
import {StackSelectorController} from './create-workspace/stack-selector/stack-selector.controller';
import {StackSelectorSvc} from './create-workspace/stack-selector/stack-selector.service';
import {StackSelector} from './create-workspace/stack-selector/stack-selector.directive';
import {StackSelectorItem} from './create-workspace/stack-selector/stack-selector-item/stack-selector-item.directive';
import {RamSettingsController} from './create-workspace/ram-settings/ram-settings.controller';
import {RamSettings} from './create-workspace/ram-settings/ram-settings.directive';
import {RamSettingsMachineItemController} from './create-workspace/ram-settings/ram-settings-machine-item/ram-settings-machine-item.controller';
import {RamSettingsMachineItem} from './create-workspace/ram-settings/ram-settings-machine-item/ram-settings-machine-item.directive';
import {NamespaceSelectorController} from './create-workspace/namespace-selector/namespace-selector.controller';
import {NamespaceSelectorSvc} from './create-workspace/namespace-selector/namespace-selector.service';
import {NamespaceSelector} from './create-workspace/namespace-selector/namespace-selector.directive';
import {ProjectSourceSelectorController} from './create-workspace/project-source-selector/project-source-selector.controller';
import {ProjectSourceSelectorService} from './create-workspace/project-source-selector/project-source-selector.service';
import {ProjectSourceSelector} from './create-workspace/project-source-selector/project-source-selector.directive';
import {AddImportProjectController} from './create-workspace/project-source-selector/add-import-project/add-import-project.controller';
import {AddImportProjectService} from './create-workspace/project-source-selector/add-import-project/add-import-project.service';
import {AddImportProject} from './create-workspace/project-source-selector/add-import-project/add-import-project.directive';
import {ImportBlankProjectController} from './create-workspace/project-source-selector/add-import-project/import-blank-project/import-blank-project.controller';
import {ImportBlankProjectService} from './create-workspace/project-source-selector/add-import-project/import-blank-project/import-blank-project.service';
import {ImportBlankProject} from './create-workspace/project-source-selector/add-import-project/import-blank-project/import-blank-project.directive';
import {ImportGitProjectController} from './create-workspace/project-source-selector/add-import-project/import-git-project/import-git-project.controller';
import {ImportGitProjectService} from './create-workspace/project-source-selector/add-import-project/import-git-project/import-git-project.service';
import {ImportGitProject} from './create-workspace/project-source-selector/add-import-project/import-git-project/import-git-project.directive';
import {ImportZipProjectController} from './create-workspace/project-source-selector/add-import-project/import-zip-project/import-zip-project.controller';
import {ImportZipProjectService} from './create-workspace/project-source-selector/add-import-project/import-zip-project/import-zip-project.service';
import {ImportZipProject} from './create-workspace/project-source-selector/add-import-project/import-zip-project/import-zip-project.directive';
import {ImportGithubProjectController} from './create-workspace/project-source-selector/add-import-project/import-github-project/import-github-project.controller';
import {NoGithubOauthDialogController} from './create-workspace/project-source-selector/add-import-project/import-github-project/oauth-dialog/no-github-oauth-dialog.controller';
import {ImportGithubProjectService} from './create-workspace/project-source-selector/add-import-project/import-github-project/import-github-project.service';
import {ImportGithubProject} from './create-workspace/project-source-selector/add-import-project/import-github-project/import-github-project.directive';
import {GithubRepositoryItem} from './create-workspace/project-source-selector/add-import-project/import-github-project/github-repository-item/github-repository-item.directive';
import {TemplateSelectorController} from './create-workspace/project-source-selector/add-import-project/template-selector/template-selector.controller';
import {TemplateSelectorSvc} from './create-workspace/project-source-selector/add-import-project/template-selector/template-selector.service';
import {TemplateSelector} from './create-workspace/project-source-selector/add-import-project/template-selector/template-selector.directive';
import {TemplateSelectorItem} from './create-workspace/project-source-selector/add-import-project/template-selector/template-selector-item/template-selector-item.directive';
import {EditProjectController} from './create-workspace/project-source-selector/edit-project/edit-project.controller';
import {EditProject} from './create-workspace/project-source-selector/edit-project/edit-project.directive';
import {EditProjectService} from './create-workspace/project-source-selector/edit-project/edit-project.service';
import {ProjectMetadataController} from './create-workspace/project-source-selector/edit-project/project-metadata/project-metadata.controller';
import {ProjectMetadataService} from './create-workspace/project-source-selector/edit-project/project-metadata/project-metadata.service';
import {ProjectMetadata} from './create-workspace/project-source-selector/edit-project/project-metadata/project-metadata.directive';
import {CheWorkspaceRamAllocationSliderController} from './workspace-ram-slider/che-workspace-ram-allocation-slider.controller';
import {CheWorkspaceRamAllocationSlider} from './workspace-ram-slider/che-workspace-ram-allocation-slider.directive';
import {WorkspaceStatus} from './workspace-status/workspace-status.directive';
import {WorkspaceStatusIndicator} from './workspace-status/workspace-status-indicator.directive';
import {CheStackLibraryFilterController} from './create-workspace/stack-selector/stack-library-filter/che-stack-library-filter.controller';
import {CheStackLibraryFilter}     from './create-workspace/stack-selector/stack-library-filter/che-stack-library-filter.directive';
import {WorkspaceEnvironmentsController} from './workspace-details/environments/environments.controller';
import {WorkspaceEnvironments} from './workspace-details/environments/environments.directive';
import {WorkspaceMachineConfigController} from './workspace-details/environments/machine-config/machine-config.controller';
import {WorkspaceMachineConfig} from './workspace-details/environments/machine-config/machine-config.directive';
import {EditMachineNameDialogController} from  './workspace-details/environments/machine-config/edit-machine-name-dialog/edit-machine-name-dialog.controller';
import {DeleteDevMachineDialogController} from './workspace-details/environments/machine-config/delete-dev-machine-dialog/delete-dev-machine-dialog.controller';
import {DevMachineLabel} from './workspace-details/environments/machine-config/dev-machine-label/dev-machine-label.directive';
import {ListEnvVariablesController} from './workspace-details/environments/list-env-variables/list-env-variables.controller';
import {ListEnvVariables} from './workspace-details/environments/list-env-variables/list-env-variables.directive';
import {EditVariableDialogController} from  './workspace-details/environments/list-env-variables/edit-variable-dialog/edit-variable-dialog.controller';
import {ListServersController} from './workspace-details/environments/list-servers/list-servers.controller';
import {ListServers} from './workspace-details/environments/list-servers/list-servers.directive';
import {EditServerDialogController} from  './workspace-details/environments/list-servers/edit-server-dialog/edit-server-dialog.controller';
import {ListCommandsController} from './workspace-details/list-commands/list-commands.controller';
import {ListCommands} from './workspace-details/list-commands/list-commands.directive';
import {EditCommandDialogController} from  './workspace-details/list-commands/edit-command-dialog/edit-command-dialog.controller';
import {ListAgentsController} from  './workspace-details/environments/list-agents/list-agents.controller';
import {AddMachineDialogController} from  './workspace-details/environments/add-machine-dialog/add-machine-dialog.controller';
import {ListAgents} from  './workspace-details/environments/list-agents/list-agents.directive';
import {StackSelectorScopeFilter} from './create-workspace/stack-selector/stack-selector-scope.filter';
import {StackSelectorSearchFilter} from './create-workspace/stack-selector/stack-selector-search.filter';
import {StackSelectorTagsFilter} from './create-workspace/stack-selector/stack-selector-tags.filter';
import {CheWorkspaceStatusButton} from './workspace-details/status-button/workspace-status-button.directive';
import {CreateWorkspaceController} from './create-workspace/create-workspace.controller';
import {CreateWorkspaceSvc} from './create-workspace/create-workspace.service';

import {ShareWorkspaceController} from './share-workspace/share-workspace.controller';
import {ShareWorkspace} from './share-workspace/share-workspace.directive';
import {AddDeveloperController} from './share-workspace/add-developers/add-developers.controller';
import {AddMemberController} from './share-workspace/add-members/add-members.controller';
import {UserItemController} from './share-workspace/user-item/user-item.controller';
import {UserItem} from './share-workspace/user-item/user-item.directive';

import {WorkspaceConfigService} from './workspace-config.service';
import {WorkspaceMachines} from './workspace-details/workspace-machines/workspace-machines.directive';
import {WorkspaceMachinesController} from './workspace-details/workspace-machines/workspace-machines.controller';
import {WorkspaceMachineItem} from './workspace-details/workspace-machines/machine-item/workspace-machine-item.directive';
import {EditMachineDialogController} from './workspace-details/workspace-machines/edit-machine-dialog/edit-machine-dialog.controller';
import {ChangeDevMachineDialogController} from './workspace-details/workspace-machines/change-dev-machine-dialog/change-dev-machine-dialog.controller';
import {WorkspaceDetailsOverviewController} from './workspace-details/workspace-overview/workspace-details-overview.controller';
import {WorkspaceDetailsOverview} from './workspace-details/workspace-overview/workspace-details-overview.directive';
import {EnvVariablesController} from './workspace-details/workspace-machine-env-variables/env-variables.controller';
import {EnvVariables} from './workspace-details/workspace-machine-env-variables/env-variables.directive';
import {EditEnvVariableDialogController} from './workspace-details/workspace-machine-env-variables/edit-variable-dialog/edit-variable-dialog.controller';
import {MachineSelectorController} from './workspace-details/machine-selector/machine-selector.controller';
import {MachineSelector} from './workspace-details/machine-selector/machine-selector.directive';
import {MachineServersController} from './workspace-details/workspace-machine-servers/machine-servers.controller';
import {MachineServers} from './workspace-details/workspace-machine-servers/machine-servers.directive';
import {EditMachineServerDialogController} from './workspace-details/workspace-machine-servers/edit-machine-server-dialog/edit-server-dialog.controller';
import {MachineAgents} from './workspace-details/workspace-machine-agents/machine-agents.directive';
import {MachineAgentsController} from './workspace-details/workspace-machine-agents/machine-agents.controller';
import {CheWorkspace} from '../../components/api/workspace/che-workspace.factory';
import {WorkspaceWarnings} from './workspace-details/warnings/workspace-warnings.directive';
import {WorkspaceWarningsController} from './workspace-details/warnings/workspace-warnings.controller';

/**
 * @ngdoc controller
 * @name workspaces:WorkspacesConfig
 * @description This class is used for configuring all workspaces stuff.
 * @author Ann Shumilova
 */
export class WorkspacesConfig {

  constructor(register: che.IRegisterService) {

    /* tslint:disable */
    new StackSelectorScopeFilter(register);
    new StackSelectorSearchFilter(register);
    new StackSelectorTagsFilter(register);
    /* tslint:enable */

    register.controller('WorkspaceDetailsSshCtrl', WorkspaceDetailsSshCtrl);
    register.directive('workspaceDetailsSsh', WorkspaceDetailsSsh);

    register.controller('ListWorkspacesCtrl', ListWorkspacesCtrl);
    register.controller('WorkspaceDetailsController', WorkspaceDetailsController);

    register.directive('cheWorkspaceItem', CheWorkspaceItem);
    register.controller('WorkspaceItemCtrl', WorkspaceItemCtrl);
    register.directive('usageChart', UsageChart);

    register.directive('cheWorkspaceStatus', CheWorkspaceStatus);
    register.controller('WorkspaceStatusController', WorkspaceStatusController);

    register.directive('workspaceWarnings', WorkspaceWarnings);
    register.controller('WorkspaceWarningsController', WorkspaceWarningsController);

    register.directive('workspaceEditModeOverlay', WorkspaceEditModeOverlay);
    register.directive('workspaceEditModeToolbarButton', WorkspaceEditModeToolbarButton);

    register.controller('WorkspaceDetailsProjectsCtrl', WorkspaceDetailsProjectsCtrl);
    register.directive('workspaceDetailsProjects', WorkspaceDetailsProjects);
    register.controller('ProjectDetailsController', ProjectDetailsController);
    register.controller('ProjectRepositoryController', ProjectRepositoryController);
    register.directive('projectRepository', ProjectRepository);
    register.directive('cheProjectItem', CheProjectItem);
    register.controller('ProjectItemCtrl', ProjectItemCtrl);
    register.service('workspaceDetailsProjectsService', WorkspaceDetailsProjectsService);
    register.controller('AddProjectPopoverController', AddProjectPopoverController);
    register.directive('addProjectPopover', AddProjectPopover);
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
    register.directive('workspaceSelectStack', WorkspaceSelectStack);

    register.controller('StackSelectorController', StackSelectorController);
    register.service('stackSelectorSvc', StackSelectorSvc);
    register.directive('stackSelector', StackSelector);
    register.directive('stackSelectorItem', StackSelectorItem);

    register.controller('RamSettingsController', RamSettingsController);
    register.directive('ramSettings', RamSettings);
    register.controller('RamSettingsMachineItemController', RamSettingsMachineItemController);
    register.directive('ramSettingsMachineItem', RamSettingsMachineItem);

    register.controller('NamespaceSelectorController', NamespaceSelectorController);
    register.service('namespaceSelectorSvc', NamespaceSelectorSvc);
    register.directive('namespaceSelector', NamespaceSelector);

    register.controller('ProjectSourceSelectorController', ProjectSourceSelectorController);
    register.service('projectSourceSelectorService', ProjectSourceSelectorService);
    register.directive('projectSourceSelector', ProjectSourceSelector);
    register.controller('AddImportProjectController', AddImportProjectController);
    register.service('addImportProjectService', AddImportProjectService);
    register.directive('addImportProject', AddImportProject);
    register.controller('ImportBlankProjectController', ImportBlankProjectController);
    register.service('importBlankProjectService', ImportBlankProjectService);
    register.directive('importBlankProject', ImportBlankProject);
    register.controller('ImportGitProjectController', ImportGitProjectController);
    register.service('importGitProjectService', ImportGitProjectService);
    register.directive('importGitProject', ImportGitProject);
    register.controller('ImportGithubProjectController', ImportGithubProjectController);
    register.service('importGithubProjectService', ImportGithubProjectService);
    register.directive('importGithubProject', ImportGithubProject);
    register.directive('githubRepositoryItem', GithubRepositoryItem);
    register.controller('NoGithubOauthDialogController', NoGithubOauthDialogController);
    register.controller('ImportZipProjectController', ImportZipProjectController);
    register.service('importZipProjectService', ImportZipProjectService);
    register.directive('importZipProject', ImportZipProject);
    register.controller('TemplateSelectorController', TemplateSelectorController);
    register.service('templateSelectorSvc', TemplateSelectorSvc);
    register.directive('templateSelector', TemplateSelector);
    register.directive('templateSelectorItem', TemplateSelectorItem);
    register.controller('EditProjectController', EditProjectController);
    register.directive('editProject', EditProject);
    register.service('editProjectService', EditProjectService);
    register.controller('ProjectMetadataController', ProjectMetadataController);
    register.service('projectMetadataService', ProjectMetadataService);
    register.directive('projectMetadata', ProjectMetadata);

    register.controller('CheStackLibraryFilterController', CheStackLibraryFilterController);
    register.directive('cheStackLibraryFilter', CheStackLibraryFilter);

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

    register.controller('AddMachineDialogController', AddMachineDialogController);
    register.controller('ListAgentsController', ListAgentsController);
    register.directive('listAgents', ListAgents);

    register.controller('WorkspaceMachinesController', WorkspaceMachinesController);
    register.directive('workspaceMachines', WorkspaceMachines);

    register.directive('workspaceMachineItem', WorkspaceMachineItem);

    register.controller('ChangeDevMachineDialogController', ChangeDevMachineDialogController);

    register.controller('EditMachineDialogController', EditMachineDialogController);

    register.directive('workspaceStatusButton', CheWorkspaceStatusButton);

    register.controller('CreateWorkspaceController', CreateWorkspaceController);
    register.service('createWorkspaceSvc', CreateWorkspaceSvc);

    register.controller('ShareWorkspaceController', ShareWorkspaceController);
    register.directive('shareWorkspace', ShareWorkspace);
    register.controller('AddDeveloperController', AddDeveloperController);
    register.controller('AddMemberController', AddMemberController);
    register.controller('UserItemController', UserItemController);
    register.directive('userItem', UserItem);

    register.service('workspaceConfigService', WorkspaceConfigService);

    register.controller('WorkspaceDetailsOverviewController', WorkspaceDetailsOverviewController);
    register.directive('workspaceDetailsOverview', WorkspaceDetailsOverview);

    register.controller('EnvVariablesController', EnvVariablesController);
    register.directive('cheEnvVariables', EnvVariables);

    register.controller('EditEnvVariableDialogController', EditEnvVariableDialogController);

    register.controller('MachineSelectorController', MachineSelectorController);
    register.directive('cheMachineSelector', MachineSelector);

    register.controller('MachineServersController', MachineServersController);
    register.directive('cheMachineServers', MachineServers);

    register.controller('EditMachineServerDialogController', EditMachineServerDialogController);

    register.controller('MachineAgentsController', MachineAgentsController);
    register.directive('cheMachineAgents', MachineAgents);

    // config routes
    register.app.config(($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/workspaces', {
        title: 'Workspaces',
        templateUrl: 'app/workspaces/list-workspaces/list-workspaces.html',
        controller: 'ListWorkspacesCtrl',
        controllerAs: 'listWorkspacesCtrl'
      })
        .accessWhen('/workspace/:namespace*/:workspaceName/:projectName', {
          title: (params: any) => {
            return params.workspaceName + ' | ' + params.projectName;
          },
          templateUrl: 'app/workspaces/workspace-details/workspace-projects/project-details/project-details.html',
          controller: 'ProjectDetailsController',
          controllerAs: 'projectDetailsController'
        })
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
        })
        .accessWhen('/create-workspace', {
          title: 'New Workspace',
          templateUrl: 'app/workspaces/create-workspace/create-workspace.html',
          controller: 'CreateWorkspaceController',
          controllerAs: 'createWorkspaceController',
          resolve: {
            initData: ['workspaceConfigService', (workspaceConfigService: WorkspaceConfigService) => {
              return workspaceConfigService.resolveWorkspaceRoute();
            }]
          }
        });
    });
  }
}
