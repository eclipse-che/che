/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { AxiosResponse } from 'axios';
import { expect } from 'chai';
import { CheApiRequestHandler } from '../../utils/request-handlers/CheApiRequestHandler';
import { ApiUrlResolver } from '../../utils/workspace/ApiUrlResolver';

suite('Workspace API URL resolver', function (): void {
	type DevWorkspaceItem = { metadata: { name: string; labels?: Record<string, string> } };
	const workspacesUrl: string = 'dashboard/api/namespace/user/devworkspaces';

	function createResolver(workspaces: DevWorkspaceItem[], directMatch: boolean = false): ApiUrlResolver {
		const requestHandler: CheApiRequestHandler = {
			get: (relativeUrl: string): Promise<AxiosResponse> => {
				if (relativeUrl === 'api/kubernetes/namespace') {
					return Promise.resolve({ status: 200, data: [{ name: 'user' }] } as AxiosResponse);
				}
				if (relativeUrl === `${workspacesUrl}/workspace`) {
					if (directMatch) {
						return Promise.resolve({ status: 200, data: {} } as AxiosResponse);
					}
					throw new Error('Workspace not found');
				}
				if (relativeUrl === workspacesUrl) {
					return Promise.resolve({ status: 200, data: { items: workspaces } } as AxiosResponse);
				}
				throw new Error(`Unexpected request: ${relativeUrl}`);
			}
		} as unknown as CheApiRequestHandler;
		return new ApiUrlResolver(requestHandler);
	}

	async function expectResolutionError(workspaces: DevWorkspaceItem[], expectedMessage: string): Promise<void> {
		let actualError: Error | undefined;
		try {
			await createResolver(workspaces).getWorkspaceApiUrl('workspace');
		} catch (error) {
			actualError = error as Error;
		}
		expect(actualError?.message).to.contain(expectedMessage);
	}

	test('resolves an exact DevWorkspace name directly', async function (): Promise<void> {
		const url: string = await createResolver([], true).getWorkspaceApiUrl('workspace');
		expect(url).to.equal(`${workspacesUrl}/workspace`);
	});

	test('resolves an exact DevWorkspace name from the list after a failed direct request', async function (): Promise<void> {
		const url: string = await createResolver([{ metadata: { name: 'workspace' } }]).getWorkspaceApiUrl('workspace');
		expect(url).to.equal(`${workspacesUrl}/workspace`);
	});

	test('resolves a restored DevWorkspace with a matching display-name label', async function (): Promise<void> {
		const workspaces: DevWorkspaceItem[] = [
			{ metadata: { name: 'workspace-4fnq', labels: { 'kubernetes.io/metadata.name': 'workspace' } } }
		];
		const url: string = await createResolver(workspaces).getWorkspaceApiUrl('workspace');
		expect(url).to.equal(`${workspacesUrl}/workspace-4fnq`);
	});

	test('does not resolve a different workspace with a similar name', async function (): Promise<void> {
		await expectResolutionError(
			[{ metadata: { name: 'workspace-2', labels: { 'kubernetes.io/metadata.name': 'workspace-2' } } }],
			'Workspace not found: workspace (tried exact match and display-name search)'
		);
	});

	test('does not resolve a suffixed workspace without a display-name label', async function (): Promise<void> {
		await expectResolutionError(
			[{ metadata: { name: 'workspace-4fnq' } }],
			'Workspace not found: workspace (tried exact match and display-name search)'
		);
	});

	test('rejects multiple restored DevWorkspaces with the same display name', async function (): Promise<void> {
		await expectResolutionError(
			[
				{ metadata: { name: 'workspace-4fnq', labels: { 'kubernetes.io/metadata.name': 'workspace' } } },
				{ metadata: { name: 'workspace-x7q2', labels: { 'kubernetes.io/metadata.name': 'workspace' } } }
			],
			'Multiple workspaces found matching'
		);
	});
});
