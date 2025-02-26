/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import axios, { AxiosResponse } from 'axios';
import { Logger } from './Logger';
import YAML from 'yaml';
import { API_TEST_CONSTANTS, SUPPORTED_DEVFILE_REGISTRIES } from '../constants/API_TEST_CONSTANTS';
import { injectable } from 'inversify';
import { BASE_TEST_CONSTANTS, Platform } from '../constants/BASE_TEST_CONSTANTS';
import { ShellExecutor } from './ShellExecutor';
import { e2eContainer } from '../configs/inversify.config';
import { CLASSES } from '../configs/inversify.types';

@injectable()
export class DevfilesHelper {
	public getInternalClusterURLToDevFile(devFileName: string): string {
		const devfileSampleURIPrefix: string = `/dashboard/api/airgap-sample/devfile/download?id=${devFileName}`;

		return `http://devspaces-dashboard.openshift-devspaces.svc:8080${devfileSampleURIPrefix}`;
	}

	/**
	 * grab devfile content from the Dashboard pod (currently, in the image of dashboard builds with devfile content and we use it for getting devfile description)
	 * @param podName
	 * @param containerName
	 * @param devFileName
	 */
	public obtainDevFileContentUsingPod(podName: string, containerName: string, devFileName: string): string {
		const clusterURL: string = this.getInternalClusterURLToDevFile(devFileName);
		this.getShellExecutor().executeCommand(
			`oc exec -i ${podName} -n  ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()} -c ${containerName} -- sh -c 'curl -o /tmp/${devFileName}-devfile.yaml ${clusterURL}'`
		);
		return this.getShellExecutor()
			.executeArbitraryShellScript(
				`oc exec -i ${podName} -n  ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()} -c ${containerName} -- cat /tmp/${devFileName}-devfile.yaml`
			)
			.toString();
	}

	/**
	 * grab devfile content from the Che config map
	 * @param configMapName
	 */
	public obtainCheDevFileEditorFromCheConfigMap(configMapName: string): string {
		return this.getShellExecutor().executeCommand(
			`oc get configmap ${configMapName} -o jsonpath="{.data.che-code\\.yaml}" -n ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()}`
		);
	}

	/**
	 * find the Dashboard pod and container name and grab devfile content from it
	 * @param devSample
	 */
	public getDevfileContent(devSample: string): string {
		const command: string = `oc get pods -n ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()}`;
		console.log(`command: ${command}`);
		const podName: string = this.getShellExecutor()
			.executeArbitraryShellScript(
				`oc get pods -n ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()} | grep dashboard | awk \'{print $1}\'`
			)
			.trim();
		const containerName: string = this.getShellExecutor().executeArbitraryShellScript(
			`oc get pod -n ${BASE_TEST_CONSTANTS.TS_PLATFORM}-${BASE_TEST_CONSTANTS.TESTING_APPLICATION_NAME()} ${podName} -o jsonpath=\'{.spec.containers[*].name}\'`
		);
		const devfileContent: string = this.obtainDevFileContentUsingPod(podName, containerName, devSample);
		return devfileContent;
	}

	/**
	 * @deprecated applicable only for inbuilt devfiles
	 * @param sampleNamePatterns
	 */
	async getInbuiltDevfilesRegistryContent(sampleNamePatterns?: string[]): Promise<any[]> {
		Logger.trace();

		return this.filterSamples(
			sampleNamePatterns,
			await this.getContent(SUPPORTED_DEVFILE_REGISTRIES.INBUILT_APPLICATION_DEVFILE_REGISTRY_URL())
		);
	}

	async getGitHubCheDevfileRegistryContent(): Promise<AxiosResponse> {
		Logger.trace();

		const url: string =
			API_TEST_CONSTANTS.TS_API_ACCEPTANCE_TEST_REGISTRY_URL() === ''
				? SUPPORTED_DEVFILE_REGISTRIES.GIT_HUB_CHE_DEVFILE_REGISTRY_URL
				: API_TEST_CONSTANTS.TS_API_ACCEPTANCE_TEST_REGISTRY_URL();
		return await this.getContent(url);
	}

	/**
	 *
	 * @deprecated applicable only for inbuilt devfiles
	 * @param isInbuilt
	 * @param sampleNamePatterns
	 */
	async collectPathsToDevfilesFromRegistry(isInbuilt: boolean, sampleNamePatterns?: string[]): Promise<object[]> {
		Logger.debug();

		const devfileSamples: object[] = [];
		const sampleNames: string[] = [];
		if (!isInbuilt) {
			{
				const content: any[any] = await this.getGitHubCheDevfileRegistryContent();
				content.forEach((e: any): void => {
					if (e.name[0] !== '.') {
						sampleNames.push(e.name);
					}
				});

				for (const sample of sampleNames) {
					const sampleEndpoint: string = `${SUPPORTED_DEVFILE_REGISTRIES.GIT_HUB_CHE_DEVFILE_REGISTRY_URL}${sample}/meta.yaml`;
					const sampleEndpointContent: AxiosResponse = await this.getContent(sampleEndpoint);
					const decodedFileContent: string = Buffer.from((sampleEndpointContent as any).content, 'base64').toString();
					const metaYamlContent: any = YAML.parse(decodedFileContent);
					devfileSamples.push({
						name: sample,
						link: metaYamlContent.links.v2
					});
				}
				Logger.debug(`samples list: ${JSON.stringify(devfileSamples)}`);
			}
		} else if (isInbuilt) {
			{
				const content: any[any] = await this.getInbuiltDevfilesRegistryContent(sampleNamePatterns);
				const devfileRegistryPrefix: string =
					BASE_TEST_CONSTANTS.TS_PLATFORM === Platform.OPENSHIFT
						? BASE_TEST_CONSTANTS.TS_SELENIUM_BASE_URL + '/devfile-registry'
						: '';
				for (const sample of content) {
					const linkToDevWorkspaceYaml: any = `${devfileRegistryPrefix}${sample.links.devWorkspaces['che-incubator/che-code/latest']}`;
					devfileSamples.push({
						name: sample.displayName,
						devWorkspaceConfigurationString: await this.getContent(linkToDevWorkspaceYaml)
					});
				}
				Logger.debug(`samples list: ${JSON.stringify(devfileSamples)}`);
			}
		} else {
			{
				Logger.error(`unsupported registry url - ${API_TEST_CONSTANTS.TS_API_ACCEPTANCE_TEST_REGISTRY_URL()}\n
                supported registries: ${JSON.stringify(SUPPORTED_DEVFILE_REGISTRIES)}`);
			}
		}
		return devfileSamples;
	}

	private filterSamples(sampleNamePatterns: string[] | undefined, content: any): Promise<any[]> {
		if (sampleNamePatterns) {
			const commonSampleNamePattern: RegExp = new RegExp(sampleNamePatterns.join('|'), 'i');
			content = content.filter((e: any): boolean => commonSampleNamePattern.test(e.displayName));
		}
		return content;
	}
	private getShellExecutor(): ShellExecutor {
		return e2eContainer.get(CLASSES.ShellExecutor);
	}
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	private async getContent(url: string, headers?: object): Promise<AxiosResponse> {
		Logger.trace(`${url}`);
		if (SUPPORTED_DEVFILE_REGISTRIES.TS_GIT_API_AUTH_TOKEN.length !== 0) {
			/**
			 * if we use - https://api.github.com/repos/eclipse-che/che-devfile-registry/contents/devfiles/ URL
			 * for generating devfiles we can get a problem with rate limits to GitHub API,
			 * but we can pass auth. token for increase the limit and avoiding problems
			 */
			headers = {
				headers: { Authorization: SUPPORTED_DEVFILE_REGISTRIES.TS_GIT_API_AUTH_TOKEN }
			};
		}
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
