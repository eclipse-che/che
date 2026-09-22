/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const AI_PROVIDER_TEST_CONSTANTS: {
	TS_AI_PROVIDER_API_KEY: string;
	TS_AI_PROVIDER_NAME: string;
	TS_AI_PROVIDER_ID: string;
	TS_AI_PROVIDER_ENV_VAR_NAME: string;
} = {
	TS_AI_PROVIDER_API_KEY: process.env.TS_AI_PROVIDER_API_KEY || '',

	TS_AI_PROVIDER_NAME: process.env.TS_AI_PROVIDER_NAME || 'OpenCode',

	TS_AI_PROVIDER_ID: process.env.TS_AI_PROVIDER_ID || 'opencodeai/opencode',

	TS_AI_PROVIDER_ENV_VAR_NAME: process.env.TS_AI_PROVIDER_ENV_VAR_NAME || 'OPENAI_API_KEY'
};
