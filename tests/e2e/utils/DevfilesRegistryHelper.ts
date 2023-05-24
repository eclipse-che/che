/*********************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { SupportedDevfilesRegistries, TestConstants } from '../constants/TestConstants';
import axios, { AxiosResponse } from 'axios';
import { Logger } from './Logger';
import YAML from 'yaml';

export class DevfilesRegistryHelper {

    async getInbuiltDevfilesRegistryContent(): Promise<AxiosResponse> {
        Logger.debug(`${this.constructor.name}.${this.getInbuiltDevfilesRegistryContent.name}`);
        return await this.getContent(SupportedDevfilesRegistries.INBUILT_APPLICATION_DEVFILE_REGISTRY_URL());
    }

    async getGitHubCheDevfileRegistryContent(): Promise<AxiosResponse> {
        Logger.debug(`${this.constructor.name}.${this.getGitHubCheDevfileRegistryContent.name}`);
        return await this.getContent(SupportedDevfilesRegistries.GIT_HUB_CHE_DEVFILE_REGISTRY_URL);
    }

    async collectPathsToDevfilesFromRegistry(): Promise<object[]> {
        Logger.debug(`${this.constructor.name}.${this.collectPathsToDevfilesFromRegistry.name}`);
        const devfileSamples: object[] = [];
        const sampleNames: string[] = [];
        switch (TestConstants.TS_API_ACCEPTANCE_TEST_REGISTRY_URL()) {
            case (SupportedDevfilesRegistries.GIT_HUB_CHE_DEVFILE_REGISTRY_URL): {
                const content: any[any] = await this.getGitHubCheDevfileRegistryContent();
                content.forEach((e: any) => {
                    if (e.name[0] !== '.') { sampleNames.push(e.name); }
                });

                for (const sample of sampleNames) {
                    const sampleEndpoint: string = `${SupportedDevfilesRegistries.GIT_HUB_CHE_DEVFILE_REGISTRY_URL}${sample}/meta.yaml`;
                    const sampleEndpointContent: AxiosResponse = await this.getContent(sampleEndpoint);
                    const decodedFileContent: string = Buffer.from((sampleEndpointContent as any).content, 'base64').toString();
                    const metaYamlContent: any = YAML.parse(decodedFileContent);
                    devfileSamples.push({name: sample, link: metaYamlContent.links.v2});
                }
                Logger.debug(`${this.constructor.name}.${this.collectPathsToDevfilesFromRegistry.name}: samples list: ${JSON.stringify(devfileSamples)}`);
            }
                break;
            case (SupportedDevfilesRegistries.INBUILT_APPLICATION_DEVFILE_REGISTRY_URL()): {
                const content: any[any] = await this.getInbuiltDevfilesRegistryContent();

                for (const sample of content) {
                    devfileSamples.push({name: sample.displayName, link: sample.links.v2});
                }
                Logger.debug(`${this.constructor.name}.${this.collectPathsToDevfilesFromRegistry.name}: samples list: ${JSON.stringify(devfileSamples)}`);
            }
                break;
            default: {
                Logger.error(`${this.constructor.name}.${this.collectPathsToDevfilesFromRegistry.name}: unsupported registry url - ${TestConstants.TS_API_ACCEPTANCE_TEST_REGISTRY_URL()}\n
                supported registries: ${JSON.stringify(SupportedDevfilesRegistries)}`);
            }
        }
        return devfileSamples;
    }

    private async getContent(url: string, headers?: object): Promise<AxiosResponse> {
        Logger.debug(`${this.constructor.name}.${this.getContent.name}: ${url}`);

        let response: AxiosResponse | undefined;
        try {
            response = await axios.get(url, headers);
        } catch (error) {
            Logger.error(`${error} + ${url}`);
            throw error;
        }
        return response?.data;
    }
}
