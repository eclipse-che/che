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
const xmlFileName: string = 'hello.xml';

suite('The "VscodeXmlPlugin" userstory', async () => {
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
