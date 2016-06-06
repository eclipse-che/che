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

import {CheNavBarCtrl} from './navbar.controller';
import {CheNavBar} from './navbar.directive';
import {NavBarSelectedCtrl} from './navbar-selected.controller';
import {NavBarSelected} from './navbar-selected.directive';
import {NavbarRecentWorkspacesCtrl} from './recent-workspaces/recent-workspaces.controller';
import {NavbarRecentWorkspaces} from './recent-workspaces/recent-workspaces.directive';

import {NavbarDropdownMenuCtrl} from './navbar-dropdown-menu/navbar-dropdown-menu.controller';
import {NavbarDropdownMenu} from './navbar-dropdown-menu/navbar-dropdown-menu.directive';

export class NavbarConfig {

  constructor(register) {
    register.controller('CheNavBarCtrl', CheNavBarCtrl);
    register.controller('NavBarSelectedCtrl', NavBarSelectedCtrl);
    register.directive('cheNavBar', CheNavBar);
    register.directive('navBarSelected', NavBarSelected);

    register.controller('NavbarRecentWorkspacesCtrl', NavbarRecentWorkspacesCtrl);
    register.directive('navbarRecentWorkspaces', NavbarRecentWorkspaces);

    register.controller('NavbarDropdownMenuCtrl', NavbarDropdownMenuCtrl);
    register.directive('navbarDropdownMenu', NavbarDropdownMenu);
  }
}
