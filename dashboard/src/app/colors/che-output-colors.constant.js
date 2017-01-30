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

export class CheOutputColorsConfig {

  constructor(register) {
    // Register this factory
    register.app.constant('jsonOutputColors', JSON.stringify([
  {
    'type': 'DOCKER',
    'color': '#4EABFF'
  },
  {
    'type': 'INFO',
    'color': '#FFFFFF'
  },
  {
    'type': 'ERROR',
    'color': '#FF2727'
  },
  {
    'type': 'WARNING',
    'color': '#F5A623'
  },
  {
    'type': 'STDOUT',
    'color': '#8ED72B'
  },
  {
    'type': 'STDERR',
    'color': '#FF4343'
  }
]
));
  }
}
