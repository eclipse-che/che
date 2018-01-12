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
 * @ngdoc directive
 * @name workspaces.details.directive:devMachineLabel
 * @restrict E
 * @element
 *
 * @description
 * <dev-machine-label></dev-machine-label>` for displaying label for machine with ws-agent activated.
 *
 * @usage
 *   <dev-machine-label></dev-machine-label>
 *
 * @author Oleksii Kurinnyi
 */
export class DevMachineLabel {
  restrict = 'E';

  /**
   * Template for the label
   * @returns {string} the template
   */
  template() {
    return '<div class="dev-machine-label">dev</div>';
  }
}
