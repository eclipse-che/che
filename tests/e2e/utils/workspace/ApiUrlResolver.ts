/** *******************************************************************
 * copyright (c) 2019-2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { inject, injectable } from 'inversify';
import { CLASSES } from '../../configs/inversify.types';
import { Logger } from '../Logger';
import { CheApiRequestHandler } from '../request-handlers/CheApiRequestHandler';
import { AxiosResponse } from 'axios';

@injectable()
export class ApiUrlResolver {
	private static readonly DASHBOARD_API_URL: string = 'dashboard/api/namespace';
	private static readonly KUBERNETES_API_URL: string = 'api/kubernetes/namespace';

	private userNamespace: string = '';

	constructor(
		@inject(CLASSES.CheApiRequestHandler)
		private readonly processRequestHandler: CheApiRequestHandler
	) {}

	async getWorkspaceApiUrl(workspaceName: string): Promise<string> {
		const actualWorkspaceName: string = await this.resolveWorkspaceName(workspaceName);
		return `${await this.getWorkspacesApiUrl()}/${actualWorkspaceName}`;
	}

	async getWorkspacesApiUrl(): Promise<string> {
		const namespace: string = await this.obtainUserNamespace();
		return `${ApiUrlResolver.DASHBOARD_API_URL}/${namespace}/devworkspaces`;
	}

	/**
	 * resolves the actual DevWorkspace name from the API.
	 * If the exact workspace name exists, returns it as is.
	 * If not found, searches for a workspace whose name matches the pattern: workspaceName + '-' + random suffix
	 * (to handle cases where DevWorkspace has a random suffix like '-4fnq' after backup/restore).
	 * @param workspaceName - The workspace name to search for
	 * @returns The actual DevWorkspace name from the API
	 * @throws Error if no matching workspace is found
	 */
	private async resolveWorkspaceName(workspaceName: string): Promise<string> {
		Logger.debug(`Resolving workspace name: ${workspaceName}`);

		try {
			// first, try to get the workspace directly by the provided name
			const directUrl: string = `${await this.getWorkspacesApiUrl()}/${workspaceName}`;
			const directResponse: AxiosResponse = await this.processRequestHandler.get(directUrl);
			if (directResponse.status === 200) {
				Logger.debug(`Found exact match: ${workspaceName}`);
				return workspaceName;
			}
		} catch (error) {
			// workspace not found by exact name, will search by prefix with suffix pattern
			Logger.debug(`Exact match not found for ${workspaceName}, searching by prefix with suffix`);
		}

		// if exact match not found, get all workspaces and search by prefix + dash + suffix pattern
		const allWorkspacesResponse: AxiosResponse = await this.processRequestHandler.get(await this.getWorkspacesApiUrl());
		if (allWorkspacesResponse.status !== 200) {
			throw new Error(`Cannot get workspaces list. Code: ${allWorkspacesResponse.status} Data: ${allWorkspacesResponse.data}`);
		}

		const workspaces: Array<{ metadata: { name: string } }> = allWorkspacesResponse.data.items || [];
		// look for workspace with pattern: workspaceName + '-' + suffix (e.g., 'test-workspace-2-4fnq')
		// this ensures we don't match 'test-workspace-20' when looking for 'test-workspace-2'
		const matchingWorkspaces: Array<{ metadata: { name: string } }> = workspaces.filter((ws): boolean => {
			const dwName: string = ws.metadata.name;
			// check if name starts with workspaceName followed by a dash
			if (dwName.startsWith(workspaceName + '-')) {
				// verify that what follows the dash looks like a random suffix (lowercase letters/numbers)
				const suffix: string = dwName.substring(workspaceName.length + 1);
				return suffix.length > 0 && /^[a-z0-9]+$/.test(suffix);
			}
			return false;
		});

		if (matchingWorkspaces.length === 1) {
			Logger.debug(`Found workspace by prefix: ${matchingWorkspaces[0].metadata.name} (requested: ${workspaceName})`);
			return matchingWorkspaces[0].metadata.name;
		}

		if (matchingWorkspaces.length > 1) {
			const names: string = matchingWorkspaces.map((ws): string => ws.metadata.name).join(', ');
			throw new Error(
				`Multiple workspaces found matching '${workspaceName}': ${names}. Please use exact DevWorkspace name or delete duplicates.`
			);
		}

		throw new Error(`Workspace not found: ${workspaceName} (tried exact match and prefix search)`);
	}

	private async obtainUserNamespace(): Promise<string> {
		Logger.debug(`${this.userNamespace}`);
		if (this.userNamespace.length === 0) {
			Logger.trace('USER_NAMESPACE.length = 0, calling kubernetes API');
			const kubernetesResponse: AxiosResponse = await this.processRequestHandler.get(ApiUrlResolver.KUBERNETES_API_URL);
			if (kubernetesResponse.status !== 200) {
				throw new Error(
					`Cannot get user namespace from kubernetes API. Code: ${kubernetesResponse.status} Data: ${kubernetesResponse.data}`
				);
			}
			this.userNamespace = kubernetesResponse.data[0].name;
			Logger.debug(`kubeapi success: ${this.userNamespace}`);
		}
		return this.userNamespace;
	}
}
