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
import { Terminal } from '../../pageobjects/ide/Terminal';

const projectTree: ProjectTree = e2eContainer.get(CLASSES.ProjectTree);
const editor: Editor = e2eContainer.get(CLASSES.Editor);
const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);

const projectName: string = 'nodejs-web-app';
const pathToFile: string = `${projectName}`;
const docFileName: string = 'assembly_authenticating-users.adoc';

// skipped until issue: https://github.com/eclipse/che/issues/19289 resolved
suite.skip('The "VscodeValePlugin" userstory', async () => {
    suite('Check the "vale" plugin', async () => {
        test('Check warning in the editor appearance', async () => {
            await projectTree.expandPathAndOpenFile(pathToFile, docFileName);
            await editor.waitWarningInLine(16);
        });

        test('Open the "Problems" terminal tab', async () => {
            await editor.type(docFileName, Key.chord(Key.CONTROL, Key.SHIFT, 'm'), 3);
            await terminal.waitTab('Problems', 60_000);
        });

        test('Check the vale plugin output in the "Problems" tab', async () => {
            await terminal.waitTextInProblemsTab('Keep sentences short and to the point', 60_000);
        });

    });
});
