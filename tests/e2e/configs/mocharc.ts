/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

'use strict';

import { TimeoutConstants } from '../constants/TimeoutConstants';

module.exports = {
  timeout: 1200000,
  reporter: 'dist/utils/CheReporter.js',
  ui: 'tdd',
  require: [
    'dist/specs/MochaHooks.js',
    'ts-node/register',
  ],
  bail: true,
  'full-trace': true,
  spec:
  // variable MOCHA_DIRECTORY uses in command "test-all-devfiles" and sets up automatically.
  // you can set it up to run files from specific directory with export environmental variable.
    process.env.MOCHA_DIRECTORY ?
      // to run one file (name without extension). uses in "test", "test-all-devfiles".
      process.env.USERSTORY ?
        `dist/specs/${process.env.MOCHA_DIRECTORY}/${process.env.USERSTORY}.spec.js`
        : `dist/specs/${process.env.MOCHA_DIRECTORY}/**.spec.js`
      : process.env.USERSTORY ?
        [`dist/specs/**/${process.env.USERSTORY}.spec.js`, `dist/specs/${process.env.USERSTORY}.spec.js`]
        : [`dist/specs/**/**.spec.js`, `dist/specs/**.spec.js`],
  retries: TimeoutConstants.TS_SELENIUM_DEFAULT_ATTEMPTS
};
