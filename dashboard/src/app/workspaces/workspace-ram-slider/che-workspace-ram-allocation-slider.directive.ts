/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
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
 * Defines a directive for creating RAM allocation slider that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Oleksii Kurinnyi
 */
export class CheWorkspaceRamAllocationSlider {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';

    this.replace = true;
    this.templateUrl = 'app/workspaces/workspace-ram-slider/che-workspace-ram-allocation-slider.html';

    // we require ngModel as we want to use it inside our directive
    this.require = 'ngModel';

    this.bindToController = true;

    this.controller = 'CheWorkspaceRamAllocationSliderController';
    this.controllerAs = 'cheWorkspaceRamAllocationSliderController';

    // scope values
    this.scope = {
      ngModel: '=',
      cheOnChange: '&'
    };
  }
}
