import { Main as Generator } from '@eclipse-che/che-devworkspace-generator/lib/main';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { V1alpha2DevWorkspaceTemplate } from '@devfile/api';
import YAML from 'yaml';
import * as axios from 'axios';
import { Logger } from './Logger';
import { ShellExecutor } from './ShellExecutor';
import { APITestConstants } from '../constants/APITestConstants';

/**
 * to see more about IContextParams and generateDevfileContext(params) check README.md in "@eclipse-che/che-devworkspace-generator;
 * tests/e2e/node_modules/@eclipse-che/che-devworkspace-generator/README.md
 */

interface IContextParams {
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

export class DevWorkspaceConfigurationHelper {
    private generator: Generator = new Generator();
    private readonly params: IContextParams;

    constructor(params: IContextParams) {
        // check if all undefined
        if (!(params.editorPath || params.editorEntry || params.editorContent)) {
            params.editorEntry = 'che-incubator/che-code/latest';
        }
        // check if one or both has value
        if (APITestConstants.TS_API_TEST_UDI_IMAGE || params.defaultComponentImage) {
            params.injectDefaultComponent = 'true';
            // check if not explicitly passed than assign value from the constants
            if (!params.defaultComponentImage) {
                params.defaultComponentImage = APITestConstants.TS_API_TEST_UDI_IMAGE;
            }
        }
        // assign value from the constants if not explicitly passed
        if (APITestConstants.TS_API_TEST_PLUGIN_REGISTRY_URL && !params.pluginRegistryUrl) {
            params.pluginRegistryUrl = APITestConstants.TS_API_TEST_PLUGIN_REGISTRY_URL;
        }
        if (APITestConstants.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI && !params.editorContent) {
            params.editorContent = ShellExecutor.curl(APITestConstants.TS_API_TEST_CHE_CODE_EDITOR_DEVFILE_URI).stdout;
        }
        this.params = params;
    }

    async generateDevfileContext(): Promise<DevfileContext> {
        Logger.debug();
        if (!this.params.projects) {
            this.params.projects = [];
        }
        return await this.generator.generateDevfileContext(
            {
                ...this.params,
                projects: this.params.projects
            },
            axios.default as any
        );
    }

    // write templates and then DevWorkspace in a single file
    async getDevWorkspaceConfigurationYamlAsString(context: DevfileContext): Promise<string> {
        Logger.debug();
        const allContentArray: any[] = context.devWorkspaceTemplates.map(
            (template: V1alpha2DevWorkspaceTemplate) => YAML.stringify(template)
        );
        allContentArray.push(YAML.stringify(context.devWorkspace));

        return allContentArray.join('---\n');
    }
}
