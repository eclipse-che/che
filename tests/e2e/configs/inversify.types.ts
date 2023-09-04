/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

const TYPES: any = {
	Driver: Symbol.for('Driver'),
	CheLogin: Symbol.for('CheLogin'),
	OcpLogin: Symbol.for('OcpLogin'),
	WorkspaceUtil: Symbol.for('WorkspaceUtil'),
	IAuthorizationHeaderHandler: Symbol.for('IAuthorizationHeaderHandler'),
	ITokenHandler: Symbol.for('ITokenHandler'),
	IKubernetesCommandLineToolsExecutor: Symbol.for('IKubernetesCommandLineToolsExecutor'),
	IContextParams: Symbol.for('IContextParams')
};

const CLASSES: any = {
	DriverHelper: 'DriverHelper',
	Dashboard: 'Dashboard',
	Workspaces: 'Workspaces',
	WorkspaceDetails: 'WorkspaceDetails',
	ScreenCatcher: 'ScreenCatcher',
	OcpLoginPage: 'OcpLoginPage',
	CheApiRequestHandler: 'CheApiRequestHandler',
	CreateWorkspace: 'CreateWorkspace',
	BrowserTabsUtil: 'BrowserTabsUtil',
	ProjectAndFileTests: 'ProjectAndFileTests',
	StringUtil: 'StringUtil',
	ApiUrlResolver: 'ApiUrlResolver',
	LoginTests: 'LoginTests',
	WorkspaceHandlingTests: 'WorkspaceHandlingTests',
	RedHatLoginPage: 'RedHatLoginPage',
	KubernetesLoginPage: 'KubernetesLoginPage',
	DexLoginPage: 'DexLoginPage',
	OcpRedHatLoginPage: 'OcpRedHatLoginPage',
	OcpApplicationPage: 'OcpApplicationPage',
	OcpMainPage: 'OcpMainPage',
	OcpImportFromGitPage: 'OcpImportFromGitPage',
	CheCodeLocatorLoader: 'CheCodeLocatorLoader',
	LocatorLoader: 'LocatorLoader',
	OauthPage: 'OauthPage',
	DevfilesRegistryHelper: 'DevfilesRegistryHelper',
	KubernetesCommandLineToolsExecutor: 'KubernetesCommandLineToolsExecutor',
	ShellExecutor: 'ShellExecutor',
	ContainerTerminal: 'ContainerTerminal'
};

const EXTERNAL_CLASSES: any = {
	Generator: 'Generator',
	LocatorLoader: 'LocatorLoader'
};

export { TYPES, CLASSES, EXTERNAL_CLASSES };
