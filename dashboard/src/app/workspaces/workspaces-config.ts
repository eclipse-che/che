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

import {ListWorkspacesCtrl} from './list-workspaces/list-workspaces.controller';
import {CheWorkspaceItem} from './list-workspaces/workspace-item/workspace-item.directive';
import {CheWorkspaceStatus} from './list-workspaces/workspace-status-action/workspace-status.directive';
import {WorkspaceStatusController} from './list-workspaces/workspace-status-action/workspace-status.controller';
import {UsageChart} from './list-workspaces/workspace-item/usage-chart.directive';
import {WorkspaceItemCtrl} from './list-workspaces/workspace-item/workspace-item.controller';
import {WorkspaceEditModeOverlay} from './workspace-edit-mode/workspace-edit-mode-overlay.directive';
import {WorkspaceEditModeToolbarButton} from './workspace-edit-mode/workspace-edit-mode-toolbar-button.directive';
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
import {StackSelectorScopeFilter} from './create-workspace/stack-selector/stack-selector-scope.filter';
import {StackSelectorSearchFilter} from './create-workspace/stack-selector/stack-selector-search.filter';
import {StackSelectorTagsFilter} from './create-workspace/stack-selector/stack-selector-tags.filter';
import {CreateWorkspaceController} from './create-workspace/create-workspace.controller';
import {CreateWorkspaceSvc} from './create-workspace/create-workspace.service';
import {AfterCreationDialogController} from './create-workspace/after-creation-dialog/after-creation-dialog.controller';
import {ShareWorkspaceController} from './share-workspace/share-workspace.controller';
import {ShareWorkspace} from './share-workspace/share-workspace.directive';
import {AddDeveloperController} from './share-workspace/add-developers/add-developers.controller';
import {AddMemberController} from './share-workspace/add-members/add-members.controller';
import {UserItemController} from './share-workspace/user-item/user-item.controller';
import {UserItem} from './share-workspace/user-item/user-item.directive';
import {WorkspaceConfigService} from './workspace-config.service';
import {WorkspaceDetailsConfig} from './workspace-details/workspace-details-config';
import {WorkspaceWarnings} from './workspace-details/warnings/workspace-warnings.directive';
import {WorkspaceWarningsController} from './workspace-details/warnings/workspace-warnings.controller';
import {WorkspacesService} from './workspaces.service';
import {WorkspacePluginsConfig} from './workspace-details/workspace-plugins/workspace-plugins-config';
import {WorkspaceEditorsConfig} from './workspace-details/workspace-editors/workspace-editors-config';
import {DevfileSelector} from './create-workspace/devfile-selector/devfile-selector.directive';
import {DevfileSelectorController} from './create-workspace/devfile-selector/devfile-selector.controller';

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
    new WorkspaceDetailsConfig(register);
    new WorkspacePluginsConfig(register);
    new WorkspaceEditorsConfig(register);
    /* tslint:enable */

    register.controller('ListWorkspacesCtrl', ListWorkspacesCtrl);
    register.directive('cheWorkspaceItem', CheWorkspaceItem);
    register.controller('WorkspaceItemCtrl', WorkspaceItemCtrl);
    register.directive('usageChart', UsageChart);
    register.directive('cheWorkspaceStatus', CheWorkspaceStatus);
    register.controller('WorkspaceStatusController', WorkspaceStatusController);
    register.directive('workspaceEditModeOverlay', WorkspaceEditModeOverlay);
    register.directive('workspaceEditModeToolbarButton', WorkspaceEditModeToolbarButton);
    register.controller('CheWorkspaceRamAllocationSliderController', CheWorkspaceRamAllocationSliderController);
    register.directive('cheWorkspaceRamAllocationSlider', CheWorkspaceRamAllocationSlider);
    register.directive('workspaceStatus', WorkspaceStatus);
    register.directive('workspaceStatusIndicator', WorkspaceStatusIndicator);
    register.directive('workspaceWarnings', WorkspaceWarnings);
    register.controller('WorkspaceWarningsController', WorkspaceWarningsController);
    register.controller('StackSelectorController', StackSelectorController);
    register.service('stackSelectorSvc', StackSelectorSvc);
    register.directive('stackSelector', StackSelector);
    register.directive('stackSelectorItem', StackSelectorItem);
    register.directive('devfileSelector', DevfileSelector);
    register.controller('DevfileSelectorController', DevfileSelectorController);
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
    register.controller('CreateWorkspaceController', CreateWorkspaceController);
    register.service('createWorkspaceSvc', CreateWorkspaceSvc);
    register.controller('AfterCreationDialogController', AfterCreationDialogController);
    register.controller('ShareWorkspaceController', ShareWorkspaceController);
    register.directive('shareWorkspace', ShareWorkspace);
    register.controller('AddDeveloperController', AddDeveloperController);
    register.controller('AddMemberController', AddMemberController);
    register.controller('UserItemController', UserItemController);
    register.directive('userItem', UserItem);
    register.service('workspaceConfigService', WorkspaceConfigService);
    register.service('workspacesService', WorkspacesService);

    // config routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/workspaces', {
        title: 'Workspaces',
        templateUrl: 'app/workspaces/list-workspaces/list-workspaces.html',
        controller: 'ListWorkspacesCtrl',
        controllerAs: 'listWorkspacesCtrl'
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
    }]);
  }
}
