/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
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
