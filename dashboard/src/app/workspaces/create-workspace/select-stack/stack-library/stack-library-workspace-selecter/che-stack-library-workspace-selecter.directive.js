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


/**
 * Defines a directive for the stack library of existing workspace selecter.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CheStackLibraryWorkspaceSelecter {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (lodash) {
    this.restrict='E';
    this.templateUrl = 'app/workspaces/create-workspace/select-stack/stack-library/stack-library-workspace-selecter/che-stack-library-workspace-selecter.html';

    this.lodash = lodash;

    // scope values
    this.scope = {
      isActive: '=cheIsActive',
      isSelect: '=cheIsSelect',
      workspace: '=cheWorkspace'
    };
  }


  link($scope) {
    //get workspace details
    $scope.getWorkspaceStackDetails = (keepFullValue) => {
      // return recipe for now
      let environments = $scope.workspace.config.environments;
      let defaultEnvName = $scope.workspace.config.defaultEnv;
      let defaultEnvironment = this.lodash.find(environments, (environment) => {
          return environment.name === defaultEnvName;
      });

      let machineConfigs = defaultEnvironment.machineConfigs;
      let source = machineConfigs[0].source;
      let location = source.location;

      if (keepFullValue) {
        return location;
      }

      if (location && location.length > 20) {
        return '...' + location.substr(location.length - 20);
      }
      return location;
    };

    //get workspace tooltip
    $scope.getWorkspaceTooltip = () => {
      return $scope.getWorkspaceStackDetails(true);
    };

    //select item
    $scope.select = () => {
      $scope.$emit('event:selectWorkspaceId', $scope.workspace.id);
    };
  }


}

