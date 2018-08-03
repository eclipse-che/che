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
import {MachineSelectorController} from './machine-selector.controller';
import {IEnvironmentManagerMachine} from '../../../../components/api/environment/environment-manager-machine';
import {EnvironmentManager} from '../../../../components/api/environment/environment-manager';


interface IMachineSelectorScope extends ng.IScope {
  setMachine?: (machine: IEnvironmentManagerMachine) => void;
  setEnvironmentManager?: (environmentManager: EnvironmentManager) => void;
}

interface IMachineSelectorTranscludeScope extends ng.IScope {
  onChange?: Function;
  machine?: IEnvironmentManagerMachine;
  environmentManager?: EnvironmentManager;

}

/**
 * @ngdoc directive
 * @name workspaces.details.directive:cheMachineSelector
 * @restrict E
 * @element
 *
 * @description
 * `<che-machine-selector></che-machine-selector>` for displaying machine selector.
 *
 * @param {string=} content-title  the content title
 * @param {che.IWorkspace=} workspace-details  the workspace details
 * @param {Function=} on-change  the callback which is called when workspace is changed.
 *
 * @usage
 *   <che-machine-selector  content-title="Content title"
 *                          workspace-details="ctrl.workspaceDetails"
 *                          on-change="ctrl.onChange()"></che-machine-selector>
 *
 * @author Oleksii Orel
 */
export class MachineSelector implements ng.IDirective {
  restrict = 'E';
  templateUrl = 'app/workspaces/workspace-details/machine-selector/machine-selector.html';
  controller = 'MachineSelectorController';
  controllerAs = 'machineSelectorController';
  bindToController = true;
  transclude = true;
  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      onChange: '&',
      contentTitle: '@',
      workspaceDetails: '=',
      filter: '='
    };
  }

  link($scope: IMachineSelectorScope, $element: ng.IAugmentedJQuery, attr: ng.IAttributes, controller: MachineSelectorController) {
    const jqTranscludeEl = angular.element($element.find('div[ng-transclude]').children());
    if (jqTranscludeEl.length > 0) {
      const transcludeElScope: IMachineSelectorTranscludeScope = jqTranscludeEl.scope();
      transcludeElScope.machine = controller.selectedMachine;
      $scope.setMachine = (machine: IEnvironmentManagerMachine) => {
        transcludeElScope.machine = machine;
      };
      transcludeElScope.environmentManager = controller.environmentManager;
      $scope.setEnvironmentManager = (environmentManager: EnvironmentManager) => {
        transcludeElScope.environmentManager = environmentManager;
      };
      transcludeElScope.onChange = () => {
        controller.updateEnvironment();
      };
    }
  }
}
