/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { Main as Generator } from '@eclipse-che/che-devworkspace-generator/lib/main';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { V1alpha2DevWorkspaceTemplate } from '@devfile/api';
import YAML from 'yaml';
import * as axios from 'axios';
import { Logger } from './Logger';
import { ShellExecutor } from './ShellExecutor';
import { API_TEST_CONSTANTS } from '../constants/API_TEST_CONSTANTS';
import { injectable } from 'inversify';
import { IContextParams } from './IContextParams';
import { e2eContainer } from '../configs/inversify.config';
import { CLASSES, EXTERNAL_CLASSES } from '../configs/inversify.types';
import getDecorators from 'inversify-inject-decorators';

const { lazyInject } = getDecorators(e2eContainer);

@injectable()
export class DevWorkspaceConfigurationHelper {
	@lazyInject(EXTERNAL_CLASSES.Generator)
	private readonly generator!: Generator;
	@lazyInject(CLASSES.ShellExecutor)
	private readonly shellExecutor!: ShellExecutor;
	private readonly params: IContextParams;

	constructor(params: IContextParams) {
		// check if all undefined
		if (!(params.editorPath || params.editorEntry || params.editorContent)) {
			params.editorEntry = 'che-incubator/che-code/latest';
		}
		// check if one or both has value
		if (API_TEST_CONSTANTS.TS_API_TEST_UDI_IMAGE || params.defaultComponentImage) {
			params.injectDefaultComponent = 'true';
			// check if not explicitly passed than assign value from the constants
			if (!params.defaultComponentImage) {
				params.defaultComponentImage = API_TEST_CONSTANTS.TS_API_TEST_UDI_IMAGE;
			}
		}
		// assign value from the constants if not explicitly passed
		if (API_TEST_CONSTANTS.TS_API_TEST_PLUGIN_REGISTRY_URL && !params.pluginRegistryUrl) {
			params.pluginRegistryUrl = API_TEST_CONSTANTS.TS_API_TEST_PLUGIN_REGISTRY_URL;
		}
		if (API_TEST_CONSTANTS.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI && !params.editorContent) {
			params.editorContent = this.shellExecutor.curl(API_TEST_CONSTANTS.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI).stdout;
		}
		this.params = params;
	}

	async generateDevfileContext(): Promise<DevfileContext> {
		Logger.debug();

		if (!this.params.projects) {
			this.params.projects = [];
		}

		const devfileContext: DevfileContext = await this.generator.generateDevfileContext(
			{
				...this.params,
				projects: this.params.projects
			},
			axios.default as any
		);

		this.patchDevWorkspaceConfigWithStorageTypeAttribute(devfileContext);

		return devfileContext;
	}

	// write templates and then DevWorkspace in a single file
	getDevWorkspaceConfigurationYamlAsString(context: DevfileContext): string {
		Logger.debug();

		const allContentArray: any[] = context.devWorkspaceTemplates.map((template: V1alpha2DevWorkspaceTemplate): string =>
			YAML.stringify(template)
		);
		allContentArray.push(YAML.stringify(context.devWorkspace));

		return allContentArray.join('---\n');
	}

	getDevWorkspaceConfigurationsAsYaml(allContentString: string): string {
		Logger.debug();
		const content: any = {};
		const contentArray: string[] = allContentString.split('---\n');
		contentArray.forEach((e: any): void => {
			e = YAML.parse(e);
			e.kind === 'DevWorkspace'
				? (content.DevWorkspace = e)
				: e.kind === 'DevWorkspaceTemplate'
					? (content.DevWorkspaceTemplate = e)
					: Logger.error(
							'Problems with configuration parsing, string should be in format "DevWorkspace\\n---\\nDevWorkspaceTemplate"'
						);
		});

		return content;
	}

	patchDevWorkspaceConfigWithBuildContainerAttribute(devfileContextDevWorkspace: any): void {
		Logger.debug();
		devfileContextDevWorkspace.spec.template.attributes = YAML.parse(`
                    controller.devfile.io/devworkspace-config:
                      name: devworkspace-config
                      namespace: openshift-devspaces
                    controller.devfile.io/scc: container-build
                    controller.devfile.io/storage-type: per-user`);
	}

	/**
	 * add storage type attribute to fix issue CRW-8922.
	 */
	patchDevWorkspaceConfigWithStorageTypeAttribute(
		devfileContextDevWorkspace: DevfileContext,
		storageType: string = API_TEST_CONSTANTS.TS_API_TEST_STORAGE_TYPE
	): void {
		Logger.debug();
		devfileContextDevWorkspace.devWorkspace?.spec?.template &&
			(devfileContextDevWorkspace.devWorkspace.spec.template.attributes = YAML.parse(`
					controller.devfile.io/storage-type: ${storageType}`));
	}
}
