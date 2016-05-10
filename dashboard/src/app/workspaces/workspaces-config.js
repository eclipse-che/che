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
import {CreateWorkspaceCtrl} from './create-workspace/create-workspace.controller';
import {UsageChart} from './list-workspaces/workspace-item/usage-chart.directive';
import {WorkspaceItemCtrl} from './list-workspaces/workspace-item/workspace-item.controller';
import {WorkspaceDetailsCtrl} from './workspace-details/workspace-details.controller';
import {WorkspaceDetailsProjectsCtrl} from './workspace-details/workspace-projects/workspace-details-projects.controller';
import {WorkspaceDetailsService} from './workspace-details/workspace-details.service.js';
import {ExportWorkspaceController} from './workspace-details/export-workspace/export-workspace.controller';
import {ExportWorkspace} from './workspace-details/export-workspace/export-workspace.directive';
import {ExportWorkspaceDialogController} from  './workspace-details/export-workspace/dialog/export-workspace-dialog.controller';
import {WorkspaceDetailsProjects} from './workspace-details/workspace-projects/workspace-details-projects.directive';
import {ReadyToGoStacksCtrl} from './create-workspace/select-stack/ready-to-go-stacks/ready-to-go-stacks.controller';
import {ReadyToGoStacks} from './create-workspace/select-stack/ready-to-go-stacks/ready-to-go-stacks.directive';
import {WorkspaceRecipeCtrl} from './create-workspace/select-stack/recipe/workspace-recipe.controller';
import {WorkspaceRecipe} from './create-workspace/select-stack/recipe/workspace-recipe.directive';
import {CheStackLibrarySelecter} from './create-workspace/select-stack/stack-library/stack-library-selecter/che-stack-library-selecter.directive';
import {CreateProjectStackLibraryCtrl} from './create-workspace/select-stack/stack-library/create-project-stack-library.controller';
import {CreateProjectStackLibrary} from './create-workspace/select-stack/stack-library/create-project-stack-library.directive';
import {WorkspaceSelectStackCtrl} from './create-workspace/select-stack/workspace-select-stack.controller';
import {WorkspaceSelectStack} from './create-workspace/select-stack/workspace-select-stack.directive';

import {CheWorkspaceRamAllocationSliderCtrl} from './workspace-ram-slider/che-workspace-ram-allocation-slider.controller';
import {CheWorkspaceRamAllocationSlider} from './workspace-ram-slider/che-workspace-ram-allocation-slider.directive';
import {WorkspaceStatusIndicator} from './workspace-status-indicator/workspace-status-indicator.directive';
import {WorkspaceStatusIndicatorCircle} from './workspace-status-indicator/workspace-status-indicator-circle.directive';

import {CheStackLibraryFilterCtrl} from './create-workspace/select-stack/stack-library/stack-library-filter/che-stack-library-filter.controller';
import {CheStackLibraryFilter}     from './create-workspace/select-stack/stack-library/stack-library-filter/che-stack-library-filter.directive';
import {CreateProjectStackLibrarySelectedStackFilter} from './create-workspace/select-stack/stack-library/create-project-stack-library-selected-stack.filter.js';

/**
 * @ngdoc controller
 * @name workspaces:WorkspacesConfig
 * @description This class is used for configuring all workspaces stuff.
 * @author Ann Shumilova
 */
export class WorkspacesConfig {

  constructor(register) {

    new CreateProjectStackLibrarySelectedStackFilter(register);

    register.controller('ListWorkspacesCtrl', ListWorkspacesCtrl);
    register.controller('CreateWorkspaceCtrl', CreateWorkspaceCtrl);

    register.directive('cheWorkspaceItem', CheWorkspaceItem);
    register.controller('WorkspaceItemCtrl', WorkspaceItemCtrl);
    register.directive('usageChart', UsageChart);

    register.controller('WorkspaceDetailsCtrl', WorkspaceDetailsCtrl);

    register.controller('WorkspaceDetailsProjectsCtrl', WorkspaceDetailsProjectsCtrl);
    register.directive('workspaceDetailsProjects', WorkspaceDetailsProjects);
    register.service('workspaceDetailsService', WorkspaceDetailsService);

    register.controller('ExportWorkspaceDialogController', ExportWorkspaceDialogController);
    register.controller('ExportWorkspaceController', ExportWorkspaceController);
    register.directive('exportWorkspace', ExportWorkspace);

    register.controller('WorkspaceRecipeCtrl', WorkspaceRecipeCtrl);
    register.directive('cheWorkspaceRecipe', WorkspaceRecipe);

    register.controller('CheWorkspaceRamAllocationSliderCtrl', CheWorkspaceRamAllocationSliderCtrl);
    register.directive('cheWorkspaceRamAllocationSlider', CheWorkspaceRamAllocationSlider);

    register.directive('workspaceStatusIndicator', WorkspaceStatusIndicator);
    register.directive('workspaceStatusIndicatorCircle', WorkspaceStatusIndicatorCircle);

    register.controller('ReadyToGoStacksCtrl', ReadyToGoStacksCtrl);
    register.directive('readyToGoStacks', ReadyToGoStacks);

    register.controller('CreateProjectStackLibraryCtrl', CreateProjectStackLibraryCtrl);

    register.directive('createProjectStackLibrary', CreateProjectStackLibrary);
    register.directive('cheStackLibrarySelecter', CheStackLibrarySelecter);

    register.controller('WorkspaceSelectStackCtrl', WorkspaceSelectStackCtrl);
    register.directive('cheWorkspaceSelectStack', WorkspaceSelectStack);

    register.controller('CheStackLibraryFilterCtrl', CheStackLibraryFilterCtrl);
    register.directive('cheStackLibraryFilter', CheStackLibraryFilter);

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/workspaces', {
        templateUrl: 'app/workspaces/list-workspaces/list-workspaces.html',
        controller: 'ListWorkspacesCtrl',
        controllerAs: 'listWorkspacesCtrl'
      })
      .accessWhen('/workspace/:workspaceId', {
          templateUrl: 'app/workspaces/workspace-details/workspace-details.html',
          controller: 'WorkspaceDetailsCtrl',
          controllerAs: 'workspaceDetailsCtrl'
        })
      .accessWhen('/create-workspace', {
          templateUrl: 'app/workspaces/create-workspace/create-workspace.html',
          controller: 'CreateWorkspaceCtrl',
          controllerAs: 'createWorkspaceCtrl'
        });
    });
  }
}
