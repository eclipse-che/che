import { Main as Generator } from '@eclipse-che/che-devworkspace-generator/lib/main';
import { DevfileContext } from '@eclipse-che/che-devworkspace-generator/lib/api/devfile-context';
import { V1alpha2DevWorkspaceTemplate } from '@devfile/api';
import YAML from 'yaml';
import * as axios from 'axios';
import { Logger } from './Logger';

interface IContextParams {
    devfilePath?: string | undefined;
    devfileUrl?: string | undefined;
    devfileContent?: string | undefined;
    outputFile?: string | undefined;
    editorPath?: string | undefined;
    editorContent?: string | undefined;
    editorEntry?: string | undefined;
    pluginRegistryUrl?: string | undefined;
    projects: {
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
        if (!(params.editorPath && params.editorEntry && params.editorContent)) {
            params.editorEntry = 'che-incubator/che-code/latest';
        }
        this.params = params;
    }

    async generateDevfileContext(): Promise<DevfileContext> {
        Logger.debug(`${this.constructor.name}.${this.generateDevfileContext.name}`);
        return await this.generator.generateDevfileContext(
            {
                ...this.params
            },
            axios.default as any
        );
    }

    // write templates and then DevWorkspace in a single file
    async getDevWorkspaceConfigurationYamlAsString(context: DevfileContext): Promise<string> {
        Logger.debug(`${this.constructor.name}.${this.getDevWorkspaceConfigurationYamlAsString.name}`);
        const allContentArray: any[] = context.devWorkspaceTemplates.map(
            (template: V1alpha2DevWorkspaceTemplate) => YAML.stringify(template)
        );
        allContentArray.push(YAML.stringify(context.devWorkspace));

        return allContentArray.join('---\n');
    }
}
