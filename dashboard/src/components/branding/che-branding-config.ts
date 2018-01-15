/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheBranding} from './che-branding.factory';

export class CheBrandingConfig {

  constructor(register: che.IRegisterService) {
    // register this factory
    register.factory('cheBranding', CheBranding);

  }
}
