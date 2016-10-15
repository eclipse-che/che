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

import {CheUIElementsInjectorService} from './che-ui-elements-injector.service';

export class CheUIElementsInjectorConfig {

  constructor(register) {
    register.service('cheUIElementsInjectorService', CheUIElementsInjectorService);
  }
}
