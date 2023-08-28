/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export interface IContextParams {
	devfilePath?: string | undefined;
	devfileUrl?: string | undefined;
	devfileContent?: string | undefined;
	outputFile?: string | undefined;
	editorPath?: string | undefined;
	editorContent?: string | undefined;
	editorEntry?: string | undefined;
	pluginRegistryUrl?: string | undefined;
	projects?: {
		name: string;
		location: string;
	}[];
	injectDefaultComponent?: string | undefined;
	defaultComponentImage?: string | undefined;
}
