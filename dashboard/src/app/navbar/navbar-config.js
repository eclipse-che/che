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
import {HelpCtrl} from './help/help.controller';
import {HelpWidget} from './help/help.directive';

export class NavbarConfig {

  constructor(register) {
    register.controller('CheNavBarCtrl', CheNavBarCtrl);
    register.controller('HelpCtrl', HelpCtrl);
    register.controller('NavBarSelectedCtrl', NavBarSelectedCtrl);
    register.directive('cheNavBar', CheNavBar);
    register.directive('navBarSelected', NavBarSelected);
    register.directive('helpWidget', HelpWidget);
  }
}
