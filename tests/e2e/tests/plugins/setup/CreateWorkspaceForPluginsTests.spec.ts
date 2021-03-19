/*********************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'reflect-metadata';
import { DriverHelper } from '../../../utils/DriverHelper';
import { e2eContainer } from '../../../inversify.config';
import { CLASSES, TYPES } from '../../../inversify.types';
import { Ide } from '../../../pageobjects/ide/Ide';
import { PreferencesHandler } from '../../../utils/PreferencesHandler';
import { ProjectTree } from '../../../pageobjects/ide/ProjectTree';
import { TestConstants } from '../../../TestConstants';
import { TimeoutConstants } from '../../../TimeoutConstants';
import { ITestWorkspaceUtil } from '../../../utils/workspace/ITestWorkspaceUtil';
import { COMMON_PLUGIN_TESTS_DEVFILE, COMMON_PLUGIN_TESTS_WORKSPACE_NAME } from './CommonPluginTestsDevfile';
import { WorkspaceStatus } from '../../../utils/workspace/WorkspaceStatus';


const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);
const ide: Ide = e2eContainer.get(CLASSES.Ide);
const preferencesHandler: PreferencesHandler = e2eContainer.get(CLASSES.PreferencesHandler);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const testWorkspaceUtil: ITestWorkspaceUtil = e2eContainer.get(TYPES.WorkspaceUtil);

const vsKubernetesConfig = { 'vs-kubernetes.kubeconfig': '/projects/nodejs-web-app/config' };
const sampleName: string = 'nodejs-web-app';
const subRootFolder: string = 'app';


let workspaceId: string = '';

suite(`The 'CreateWorkspaceForPluginsTests' test`, async () => {
    suite('Set required preferences', async () => {
        test('Set kubeconfig path', async () => {
            await preferencesHandler.setVscodeKubernetesPluginConfig(vsKubernetesConfig);
        });

        test('', async () => {

        });
    });

    suite('Create workspace', async () => {
        test('Create workspace using API', async () => {
            await testWorkspaceUtil.createWsFromDevFile(COMMON_PLUGIN_TESTS_DEVFILE);
        });

        test('Get created workspace ID', async () => {
            workspaceId = await testWorkspaceUtil.getIdOfRunningWorkspace(COMMON_PLUGIN_TESTS_WORKSPACE_NAME)
        });

        test('Start workspace', async () => {
            await testWorkspaceUtil.startWorkspace(workspaceId);
        });

        test('Wait until created workspace is started', async () => {
            await testWorkspaceUtil.waitWorkspaceStatus(TestConstants.TS_SELENIUM_USERNAME, COMMON_PLUGIN_TESTS_WORKSPACE_NAME, WorkspaceStatus.RUNNING);
        });

        test('Open workspace', async () => {
            const workspaceUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/dashboard/#/ide/admin/${COMMON_PLUGIN_TESTS_WORKSPACE_NAME}`;

            await driverHelper.navigateToUrl(workspaceUrl);
        });

        test('Wait workspace is ready to work', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(sampleName, subRootFolder);
        });
    });

});
