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
import {WorkspaceDevfileEditorController} from './devfile/workspace-devfile-editor.controller';
import {WorkspaceDevfileEditor} from './devfile/workspace-devfile-editor.directive';
import {CheWorkspaceStatusButton} from './status-button/workspace-status-button.directive';
import {WorkspaceDetailsOverviewController} from './workspace-overview/workspace-details-overview.controller';
import {WorkspaceDetailsOverview} from './workspace-overview/workspace-details-overview.directive';
import {CheWorkspace} from '../../../components/api/workspace/che-workspace.factory';
import {WorkspaceConfigService} from '../workspace-config.service';
import {CheProjectItem} from './workspace-projects/project-item/project-item.directive';
import {ProjectItemCtrl} from './workspace-projects/project-item/project-item.controller';
import {NoGithubOauthDialogController} from '../create-workspace/ready-to-go-stacks/project-source-selector/add-import-project/import-github-project/oauth-dialog/no-github-oauth-dialog.controller';

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

    register.service('workspaceDetailsService', WorkspaceDetailsService);

    register.directive('cheProjectItem', CheProjectItem);
    register.controller('ProjectItemCtrl', ProjectItemCtrl);
    register.controller('NoGithubOauthDialogController', NoGithubOauthDialogController);

    register.controller('AddProjectPopoverController', AddProjectPopoverController);
    register.directive('addProjectPopover', AddProjectPopover);
    register.controller('ExportWorkspaceDialogController', ExportWorkspaceDialogController);
    register.controller('ExportWorkspaceController', ExportWorkspaceController);
    register.directive('exportWorkspace', ExportWorkspace);
    register.controller('WorkspaceDevfileEditorController', WorkspaceDevfileEditorController);
    register.directive('workspaceDevfileEditor', WorkspaceDevfileEditor);
    register.directive('workspaceStatusButton', CheWorkspaceStatusButton);
    register.controller('WorkspaceDetailsOverviewController', WorkspaceDetailsOverviewController);
    register.directive('workspaceDetailsOverview', WorkspaceDetailsOverview);

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
