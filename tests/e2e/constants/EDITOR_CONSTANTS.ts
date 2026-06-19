/** *******************************************************************
 * copyright (c) 2026 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export type EditorType = 'vscode' | 'intellij';

export interface EditorConfig {
	readonly environmentId: string;
	readonly xpath: string;
	readonly name: string;
	readonly type: EditorType;
}

export const ALL_EDITORS: Map<string, EditorConfig> = new Map([
	[
		'VSCODE_DESKTOP_SSH_EDITOR',
		{
			environmentId: 'VSCODE_DESKTOP_SSH_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-code-sshd/latest"]',
			name: 'VSCode Desktop SSH',
			type: 'vscode'
		}
	],
	[
		'CLION_EDITOR',
		{
			environmentId: 'CLION_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-clion-server/latest"]',
			name: 'CLion',
			type: 'intellij'
		}
	],
	[
		'GOLAND_EDITOR',
		{
			environmentId: 'GOLAND_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-goland-server/latest"]',
			name: 'GoLand',
			type: 'intellij'
		}
	],
	[
		'INTELLIJ_IDEA_EDITOR',
		{
			environmentId: 'INTELLIJ_IDEA_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-idea-server/latest"]',
			name: 'IntelliJ IDEA',
			type: 'intellij'
		}
	],
	[
		'PHPSTORM_EDITOR',
		{
			environmentId: 'PHPSTORM_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-phpstorm-server/latest"]',
			name: 'PhpStorm',
			type: 'intellij'
		}
	],
	[
		'PYCHARM_EDITOR',
		{
			environmentId: 'PYCHARM_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-pycharm-server/latest"]',
			name: 'PyCharm',
			type: 'intellij'
		}
	],
	[
		'RIDER_EDITOR',
		{
			environmentId: 'RIDER_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-rider-server/latest"]',
			name: 'Rider',
			type: 'intellij'
		}
	],
	[
		'RUBYMINE_EDITOR',
		{
			environmentId: 'RUBYMINE_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-rubymine-server/latest"]',
			name: 'RubyMine',
			type: 'intellij'
		}
	],
	[
		'WEBSTORM_EDITOR',
		{
			environmentId: 'WEBSTORM_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-webstorm-server/latest"]',
			name: 'WebStorm',
			type: 'intellij'
		}
	],
	[
		'KIRO_EDITOR',
		{
			environmentId: 'KIRO_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/che-code-sshd-kiro/latest"]',
			name: 'Kiro',
			type: 'vscode'
		}
	],
	[
		'JETBRAINS_SSH_EDITOR',
		{
			environmentId: 'JETBRAINS_SSH_EDITOR',
			xpath: '//*[@id="editor-selector-card-che-incubator/jetbrains-sshd/latest"]',
			name: 'JetBrains Toolbox App',
			type: 'intellij'
		}
	]
]);
