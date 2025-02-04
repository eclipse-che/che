/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { BASE_TEST_CONSTANTS } from './BASE_TEST_CONSTANTS';

export const MOCHA_CONSTANTS: {
	MOCHA_SUITE: string | undefined;
	MOCHA_DELAYED_SUITE: boolean;
	MOCHA_DEFAULT_TIMEOUT: string | number;
	MOCHA_DIRECTORY: string | undefined;
	MOCHA_USERSTORY: undefined | string;
	MOCHA_RETRIES: string | number;
	MOCHA_BAIL: boolean;
	MOCHA_GREP: string | undefined;
} = {
	MOCHA_DIRECTORY: process.env.MOCHA_DIRECTORY || undefined,

	MOCHA_USERSTORY: process.env.USERSTORY || undefined,

	MOCHA_BAIL: process.env.MOCHA_BAIL !== 'false',

	MOCHA_DELAYED_SUITE: process.env.MOCHA_DELAYED_SUITE === 'true',

	MOCHA_DEFAULT_TIMEOUT: Number(process.env.MOCHA_DEFAULT_TIMEOUT) || 840000,

	MOCHA_RETRIES: process.env.MOCHA_RETRIES || BASE_TEST_CONSTANTS.TEST_ENVIRONMENT === '' ? 0 : 2,

	MOCHA_SUITE: process.env.MOCHA_SUITE || undefined,

	MOCHA_GREP: process.env.MOCHA_GREP || undefined
};
