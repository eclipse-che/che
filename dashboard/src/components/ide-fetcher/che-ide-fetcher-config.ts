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

import {CheIdeFetcher} from './che-ide-fetcher.service';

export class CheIdeFetcherConfig {

  constructor(register) {

  register.factory('cheIdeFetcher', CheIdeFetcher);


  }
}
