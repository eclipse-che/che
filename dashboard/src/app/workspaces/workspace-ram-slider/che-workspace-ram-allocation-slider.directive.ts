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

/**
 * Defines a directive for creating RAM allocation slider that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheWorkspaceRamAllocationSlider implements ng.IDirective {

  restrict = 'E';

  replace = true;
  templateUrl = 'app/workspaces/workspace-ram-slider/che-workspace-ram-allocation-slider.html';

  // we require ngModel as we want to use it inside our directive
  require = 'ngModel';

  bindToController = true;

  controller = 'CheWorkspaceRamAllocationSliderController';
  controllerAs = 'cheWorkspaceRamAllocationSliderController';

  // scope values
  scope = {
    ngModel: '=',
    cheOnChange: '&'
  };

}
