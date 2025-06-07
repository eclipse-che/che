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

export enum KubernetesCommandLineTool {
	OC = 'oc',
	KUBECTL = 'kubectl'
}

export const SUPPORTED_DEVFILE_REGISTRIES: {
	INBUILT_APPLICATION_DEVFILE_REGISTRY_URL: () => string;
	GIT_HUB_CHE_DEVFILE_REGISTRY_URL: string;
	TS_GIT_API_AUTH_TOKEN: string;
} = {
	INBUILT_APPLICATION_DEVFILE_REGISTRY_URL: (): string => `${BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL}/devfile-registry/devfiles/`,
	GIT_HUB_CHE_DEVFILE_REGISTRY_URL: 'https://api.github.com/repos/eclipse-che/che-devfile-registry/contents/devfiles/',
	/**
	 * gitHub has a rate limit for unauthorized requests to GitHub API. We can prevent this problems using authorization token
	 */
	TS_GIT_API_AUTH_TOKEN: process.env.TS_GIT_API_AUTH_TOKEN || ''
};
export const API_TEST_CONSTANTS: {
	TS_API_TEST_DEV_WORKSPACE_LIST: string | undefined;
	TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL: string;
	TS_API_TEST_PLUGIN_REGISTRY_URL: string | undefined;
	TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI: string | undefined;
	TS_API_TEST_UDI_IMAGE: string | undefined;
	TS_API_TEST_NAMESPACE: string | undefined;
	TS_API_ACCEPTANCE_TEST_REGISTRY_URL(): string;
	TS_API_TEST_STORAGE_TYPE: string;
} = {
	/**
	 * possible values "oc" or "kubectl"
	 */
	TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL: process.env.TS_API_TEST_KUBERNETES_COMMAND_LINE_TOOL || KubernetesCommandLineTool.OC,

	/**
	 * 'quay.io/devfile/universal-developer-image:latest'
	 * is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
	 */
	TS_API_TEST_UDI_IMAGE: process.env.TS_API_TEST_UDI_IMAGE || undefined,

	/**
	 * https://eclipse-che.github.io/che-plugin-registry/main/v3/plugins/che-incubator/che-code/latest/devfile.yaml
	 * is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
	 */
	TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI: process.env.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI || undefined,

	/**
	 * https://eclipse-che.github.io/che-plugin-registry/main/v3
	 * is default assigned by DevWorkspaceConfigurationHelper.generateDevfileContext() using @eclipse-che/che-devworkspace-generator
	 */
	TS_API_TEST_PLUGIN_REGISTRY_URL: process.env.TS_API_TEST_PLUGIN_REGISTRY_URL || undefined,

	/**
	 * namespace on openshift platform
	 */
	TS_API_TEST_NAMESPACE: process.env.TS_API_TEST_NAMESPACE || undefined,

	/**
	 * to run all devfile from registry. used in DevfileAcceptanceTestAPI.suite.ts
	 */
	TS_API_ACCEPTANCE_TEST_REGISTRY_URL(): string {
		return process.env.TS_API_ACCEPTANCE_TEST_REGISTRY_URL || '';
	},

	TS_API_TEST_DEV_WORKSPACE_LIST: process.env.TS_API_TEST_DEV_WORKSPACE_LIST || undefined,

	TS_API_TEST_STORAGE_TYPE: process.env.TS_API_TEST_STORAGE_TYPE || 'per-user'
};
