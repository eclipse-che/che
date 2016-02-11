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
 * @name dashboard.directive:WelcomeBack
 * @description This class is handling the directive of the 'welcome back' panel in the dashboard
 * @author Florent Benoit
 */
export class WelcomeBack {


  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/dashboard/welcome-back/welcome-back.html';

    this.controller = 'WelcomeBackCtrl';
    this.controllerAs = 'welcomeBackCtrl';
    this.bindToController = true;
  }

}
