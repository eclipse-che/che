/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for displaying iframe for displaying the IDE.
 * @author Florent Benoit
 */
class IdeIframe  implements ng.IDirective {
  restrict: string;
  templateUrl: string;

  /**
   * Default constructor that is using resource
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/ide/ide-iframe/ide-iframe.html';
  }

}

export default IdeIframe;

