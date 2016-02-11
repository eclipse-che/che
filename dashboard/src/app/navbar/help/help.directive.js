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
 * @ngdoc directive
 * @name help.directive:HelpWidget
 * @description This class is handling the directive of the help
 * @author Florent Benoit
 */
export class HelpWidget {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/navbar/help/help.html';
    this.replace = false;

    this.controller = 'HelpCtrl';
    this.controllerAs = 'helpCtrl';
    this.bindToController = true;
  }

}
