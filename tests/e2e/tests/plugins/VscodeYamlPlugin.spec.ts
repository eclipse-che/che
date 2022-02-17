/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { Key } from 'selenium-webdriver';
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Ide } from '../../pageobjects/ide/Ide';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';
import { TestConstants } from '../../TestConstants';
import { TimeoutConstants } from '../../TimeoutConstants';
import { Logger } from '../../utils/Logger';
import { WorkspaceHandlingTests } from '../../testsLibrary/WorkspaceHandlingTests';
import CheReporter from '../../driver/CheReporter';
import { BrowserTabsUtil } from '../../utils/BrowserTabsUtil';

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const browserTabsUtil: BrowserTabsUtil = e2eContainer.get(CLASSES.BrowserTabsUtil);
const workspaceHandlingTests: WorkspaceHandlingTests = e2eContainer.get(CLASSES.WorkspaceHandlingTests);

const devFileUrl: string = 'https://github.com/che-samples/web-nodejs-sample/tree/yaml-plugin';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devFileUrl}`;
const projectName: string = 'web-nodejs-sample';
const pathToFile: string = `${projectName}`;
const yamlFileName: string = 'routes.yaml';
const yamlSchema = { 'yaml-schema.json': '*.yaml' };

let workspaceName: string = 'yaml-plugin';

suite('The "VscodeYamlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await browserTabsUtil.navigateTo(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            CheReporter.registerRunningWorkspace(workspaceName);

            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);
        });
    });

    suite('Check workspace readiness to work', async () => {
        test('Wait until project is imported', async () => {
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'app');
        });
    });

    suite('Check the "vscode-yaml" plugin', async () => {
        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);
            await editor.waitSuggestion(yamlFileName, 'for', 60000, 18, 5);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

            await editor.type(yamlFileName, Key.SPACE, 20);
            await editor.waitErrorInLine(20, yamlFileName);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(yamlFileName, Key.BACK_SPACE);
            await editor.waitErrorInLineDisappearance(20, yamlFileName);
        });

        test('To unformat the "yaml" file', async () => {
            const expectedTextBeforeFormating: string = 'uri:     "timer:tick"';
            await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

            await editor.selectTab(yamlFileName);
            await editor.moveCursorToLineAndChar(yamlFileName, 19, 10);
            await editor.performKeyCombination(yamlFileName, Key.chord(Key.SPACE, Key.SPACE, Key.SPACE, Key.SPACE));
            await editor.waitText(yamlFileName, expectedTextBeforeFormating, 60000, 10000);
        });

        test('To format the "yaml" document', async () => {
            const expectedTextAfterFormating: string = 'uri: "timer:tick"';

            await editor.type(yamlFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'I'), 19);
            await editor.waitText(yamlFileName, expectedTextAfterFormating, 60000, 10000);
        });

    });

    suite('Delete workspace', async () => {
        test('Stop and remove workspace', async () => {
            if (TestConstants.TS_DELETE_PLUGINS_TEST_WORKSPACE === 'true') {
                await workspaceHandlingTests.stopAndRemoveWorkspace(workspaceName);
                return;
            }

            Logger.info(`As far as the "TS_DELETE_PLUGINS_TEST_WORKSPACE" value is "false the workspace deletion is skipped"`);
        });
    });
});
