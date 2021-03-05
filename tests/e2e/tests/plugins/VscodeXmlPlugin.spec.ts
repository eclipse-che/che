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
import { WorkspaceNameHandler } from '../../utils/WorkspaceNameHandler';
import { Dashboard } from '../../pageobjects/dashboard/Dashboard';
import { Ide } from '../../pageobjects/ide/Ide';
import { CLASSES } from '../../inversify.types';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';
import { DriverHelper } from '../../utils/DriverHelper';
import { TestConstants } from '../../TestConstants';
import { TimeoutConstants } from '../../TimeoutConstants';

const dashboard: Dashboard = e2eContainer.get(CLASSES.Dashboard);

const ide: Ide = e2eContainer.get(CLASSES.Ide);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const driverHelper: DriverHelper = e2eContainer.get(CLASSES.DriverHelper);

let workspaceName: string = '';

const devfileUrl: string = 'https://gist.githubusercontent.com/Ohrimenko1988/806b3d27abbaa989f14ad58e89420773/raw/1299bdb17d0cece20adcec8fcb0954459cd73cfa/CamelkWithYamlAndXmlPlugins.yaml';
const factoryUrl: string = `${TestConstants.TS_SELENIUM_BASE_URL}/f?url=${devfileUrl}`;
const projectName: string = 'camel-k-examples';
const pathToFile: string = `${projectName}/examples/languages`;
const xmlFileName: string = 'hello.xml';

suite('The "VscodeXmlPlugin" userstory', async () => {
    suite('Create workspace', async () => {
        test('Create workspace using factory', async () => {
            await driverHelper.navigateToUrl(factoryUrl);
        });

        test('Wait until created workspace is started', async () => {
            await ide.waitAndSwitchToIdeFrame();
            await ide.waitIde(TimeoutConstants.TS_SELENIUM_START_WORKSPACE_TIMEOUT);

            workspaceName = await WorkspaceNameHandler.getNameFromUrl();
        });
    });

    suite('Check workspace readiness to work', async () => {
        test('Wait until project is imported', async () => {
            await projectTree.openProjectTreeContainer();
            await projectTree.waitProjectImported(projectName, 'examples');
        });
    });

    suite('Check the "vscode-xml" plugin', async () => {
        test('Check autocomplete', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);
            await editor.waitSuggestion(xmlFileName, 'rollback', 60000, 16, 4);
        });

        test('Check error appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.type(xmlFileName, '\$\%\^\#', 16);
            await editor.waitErrorInLine(16);
        });

        test('Check error disappearance', async () => {
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
            await editor.waitErrorInLineDisappearance(16);
        });

        test('Check auto-close tags', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.type(xmlFileName, '<test>', 21);
            await editor.waitText(xmlFileName, '<test></test>', 60000, 10000);

            await editor.performKeyCombination(xmlFileName, Key.chord(Key.CONTROL, 'z'));
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE, Key.BACK_SPACE));
        });

        test('To unformat the "xml" file', async () => {
            const expectedTextBeforeFormating: string = '<constant    >Hello World!!!';
            await projectTree.expandPathAndOpenFile(pathToFile, xmlFileName);

            await editor.selectTab(xmlFileName);
            await editor.moveCursorToLineAndChar(xmlFileName, 25, 22);
            await editor.performKeyCombination(xmlFileName, Key.chord(Key.SPACE, Key.SPACE, Key.SPACE, Key.SPACE));
            await editor.waitText(xmlFileName, expectedTextBeforeFormating, 60000, 10000);
        });

        test('To format the "xml" document', async () => {
            const expectedTextAfterFormating: string = '<constant>Hello World!!!';

            await editor.type(xmlFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'I'), 25);
            await editor.waitText(xmlFileName, expectedTextAfterFormating, 60000, 10000);
        });

    });

    suite('Delete workspace', async () => {
        test('Delete workspace', async () => {
            await dashboard.deleteWorkspaceByUI(workspaceName);
        });
    });
});
