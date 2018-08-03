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
 * Defines a directive for creating label container.
 * @author Florent Benoit
 */
export class CheLabelContainer implements ng.IDirective {

  restrict = 'E';
  replace = true;
  transclude = true;
  templateUrl = 'components/widget/label-container/che-label-container.html';

  // scope values
  scope = {
    labelName: '@cheLabelName',
    labelDescription: '@?cheLabelDescription',
    alignment: '@cheAlignment'
  };

}
