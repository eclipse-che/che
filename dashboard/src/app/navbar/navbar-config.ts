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

import {CheNavBarController} from './navbar.controller';
import {CheNavBar} from './navbar.directive';
import {NavBarSelectedCtrl} from './navbar-selected.controller';
import {NavBarSelected} from './navbar-selected.directive';
import {NavbarRecentWorkspacesController} from './recent-workspaces/recent-workspaces.controller';
import {NavbarRecentWorkspaces} from './recent-workspaces/recent-workspaces.directive';

import {NavbarDropdownMenuController} from './navbar-dropdown-menu/navbar-dropdown-menu.controller';
import {NavbarDropdownMenu} from './navbar-dropdown-menu/navbar-dropdown-menu.directive';
import {NavbarNotificationController} from './notification/navbar-notification.controller';
import {NavbarNotification} from './notification/navbar-notification.directive';

export class NavbarConfig {

  constructor(register: che.IRegisterService) {
    register.controller('CheNavBarController', CheNavBarController);
    register.controller('NavBarSelectedCtrl', NavBarSelectedCtrl);
    register.directive('cheNavBar', CheNavBar);
    register.directive('navBarSelected', NavBarSelected);

    register.controller('NavbarDropdownMenuController', NavbarDropdownMenuController);
    register.directive('navbarDropdownMenu', NavbarDropdownMenu);

    register.controller('NavbarRecentWorkspacesController', NavbarRecentWorkspacesController);
    register.directive('navbarRecentWorkspaces', NavbarRecentWorkspaces);

    register.controller('NavbarNotificationController', NavbarNotificationController);
    register.directive('navbarNotification', NavbarNotification);
  }
}
