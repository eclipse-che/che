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
 * This class is handling the controller for the dropdown menu on navbar
 * @author Oleksii Kurinnyi
 */
export class NavbarDropdownMenuCtrl {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($window) {
    this.$window = $window;

    this.offset = angular.isUndefined(this.offset) ? '0 0' : this.offset;
  }

  process(item) {
    if (item.url) {
      this.redirect(item.url);
      return;
    }

    if (item.onclick) {
      item.onclick();
    }
  }

  redirect(newPath) {
    if (!newPath || this.isDisabled) {
      return;
    }
    this.$window.location.href = newPath;
  }
}


