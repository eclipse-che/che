/*
 * Copyright (c) 2015-2016 Codenvy, S.A., Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mario Loriedo - initial implementation
 */
'use strict';

import {UrlAdapter} from './url-adapter.service';

export class UrlAdapterConfig {

  constructor(register) {

    register.service('urlAdapter', UrlAdapter);

  }
}
