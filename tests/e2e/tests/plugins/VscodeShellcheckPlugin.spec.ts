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
import { e2eContainer } from '../../inversify.config';
import { CLASSES } from '../../inversify.types';
import { Editor } from '../../pageobjects/ide/Editor';
import { ProjectTree } from '../../pageobjects/ide/ProjectTree';
import { Key } from 'selenium-webdriver';

const editor: Editor = e2eContainer.get(CLASSES.Editor);
const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);

const sampleName: string = 'nodejs-web-app';
const pathToFile: string = `${sampleName}`;
const fileName: string = 'test.sh';

suite(`The 'VscodeShellcheckPlugin' test`, async () => {
    test('Check errors highlighting', async () => {
        // the simple " character as a result. Workaround to avoiding autocomplete.
        const errorText: string = Key.chord('"""', Key.BACK_SPACE, Key.BACK_SPACE);

        await projectTree.expandPathAndOpenFile(pathToFile, fileName);
        await editor.type(fileName, errorText, 4);
        await editor.waitErrorInLine(4);
    });

    test('Check errors highlighting disappearance', async () => {
        await editor.type(fileName, Key.DELETE, 4);
        await editor.waitErrorInLineDisappearance(4);
    });

    test('Check warning highlighting', async () => {
        await editor.waitWarningInLine(5);
    });

    test('Uncomment the 4-th row', async () => {
        await editor.type(fileName, Key.DELETE, 3);
    });

    test('Check warning highlighting disappearance', async () => {
        await editor.waitWarningInLineDisappearance(5);
    });
});
