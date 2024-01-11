/** *******************************************************************
 * copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const PLUGIN_TEST_CONSTANTS: { TS_SAMPLE_LIST: string } = {
	/**
	 * dashboard samples to check in RecommendedExtensions.spec.ts
	 */
	TS_SAMPLE_LIST:
		process.env.TS_SAMPLE_LIST ||
		'Node.js MongoDB,Node.js Express,Java Lombok,Quarkus REST API,Python,.NET,C/C++,Go,PHP,Ansible'
};
