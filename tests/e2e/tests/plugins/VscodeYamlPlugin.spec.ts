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
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Editor } from '../../pageobjects/ide/Editor';

const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);

const projectName: string = 'nodejs-web-app';
const pathToFile: string = `${projectName}`;
const yamlFileName: string = 'routes.yaml';

suite.skip('The "VscodeYamlPlugin" userstory', async () => {
    test('Check autocomplete', async () => {
        await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);
        await editor.waitSuggestion(yamlFileName, 'from', 60000, 18, 5);
    });

    test('Check error appearance', async () => {
        await projectTree.expandPathAndOpenFile(pathToFile, yamlFileName);

        await editor.type(yamlFileName, Key.SPACE, 19);
        await editor.waitErrorInLine(19);
    });

    test('Check error disappearance', async () => {
        await editor.performKeyCombination(yamlFileName, Key.BACK_SPACE);
        await editor.waitErrorInLineDisappearance(19);
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
