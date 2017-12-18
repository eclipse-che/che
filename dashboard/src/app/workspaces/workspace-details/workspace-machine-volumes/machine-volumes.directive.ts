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

/**
 * @ngdoc directive
 * @name environment.machine.volumes.directive:MachineVolumes
 * @restrict E
 * @element
 *
 * @description
 * `<che-machine-volumes></che-machine-volumes>` for displaying list of machine volumes.
 *
 * @usage
 *   <che-machine-volumes selected-machine="machine" on-change="ctrl.onChangeCallback()"></che-machine-volumes>
 *
 * @author Oleksii Orel
 */
export class MachineVolumes implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/workspace-details/workspace-machine-volumes/machine-volumes.html';
  controller: string = 'MachineVolumesController';
  controllerAs: string = 'machineVolumesController';
  bindToController: boolean = true;
  scope = {
    environmentManager: '=',
    selectedMachine: '=',
    onChange: '&'
  };
}
