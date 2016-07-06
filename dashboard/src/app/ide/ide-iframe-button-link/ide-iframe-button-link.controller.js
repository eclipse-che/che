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
 * This class is handling the controller for redirecting IDE to UD in Iframe
 * @author Florent Benoit
 */
class IdeIFrameButtonLinkCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav, $rootScope) {
    this.$mdSidenav = $mdSidenav;
    this.$rootScope = $rootScope;
  }

  toggleLeftMenu() {
    let isLockedOpen = this.$mdSidenav('left').isLockedOpen(),
      isOpen  = this.$mdSidenav('left').isOpen();

    if (isOpen || isLockedOpen) {
      this.$rootScope.hideNavbar = true;
      this.$mdSidenav('left').close();
    } else {
      this.$rootScope.hideNavbar = isLockedOpen;
      this.$mdSidenav('left').toggle();
    }
  }
}


export default IdeIFrameButtonLinkCtrl;
