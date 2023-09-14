/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

'use strict';

import { MOCHA_CONSTANTS } from '../constants/MOCHA_CONSTANTS';

/**
 * full available options list https://mochajs.org/api/mocha
 */

module.exports = {
	timeout: MOCHA_CONSTANTS.MOCHA_DEFAULT_TIMEOUT,
	reporter: 'mocha-multi-reporters',
	reporterOptions: 'configFile=configs/reporters-config.json',
	ui: 'tdd',
	require: ['dist/specs/MochaHooks.js', 'ts-node/register'],
	bail: MOCHA_CONSTANTS.MOCHA_BAIL,
	'full-trace': true,
	spec:
		// variable MOCHA_DIRECTORY uses in command "test-all-devfiles" and sets up automatically.
		// you can set it up to run files from specific directory with export environmental variable.
		MOCHA_CONSTANTS.MOCHA_DIRECTORY
			? // to run one file (name without extension). uses in "test", "test-all-devfiles".
			  MOCHA_CONSTANTS.MOCHA_USERSTORY
				? `dist/specs/${MOCHA_CONSTANTS.MOCHA_DIRECTORY}/${MOCHA_CONSTANTS.MOCHA_USERSTORY}.spec.js`
				: `dist/specs/${MOCHA_CONSTANTS.MOCHA_DIRECTORY}/**.spec.js`
			: MOCHA_CONSTANTS.MOCHA_USERSTORY
			? [`dist/specs/**/${MOCHA_CONSTANTS.MOCHA_USERSTORY}.spec.js`, `dist/specs/${MOCHA_CONSTANTS.MOCHA_USERSTORY}.spec.js`]
			: ['dist/specs/**/**.spec.js', 'dist/specs/**.spec.js'],
	retries: MOCHA_CONSTANTS.MOCHA_RETRIES
};
