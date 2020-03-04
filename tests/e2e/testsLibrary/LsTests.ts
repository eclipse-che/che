/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { e2eContainer } from '../inversify.config';
import { Editor, CLASSES, Ide } from '..';
import { Key, error } from 'selenium-webdriver';
import { Logger } from '../utils/Logger';

const editor: Editor = e2eContainer.get(CLASSES.Editor);
const ide: Ide = e2eContainer.get(CLASSES.Ide);

 export function errorHighlighting(openedTab: string, textToWrite: string, line: number) {
    test('Error highlighting', async () => {
        await editor.type(openedTab, textToWrite, line);
        try {
            await editor.waitErrorInLine(line);
        } catch (err) {
            if (!(err instanceof error.TimeoutError)) {
                throw err;
            }
            // try again
            Logger.debug(`Waiting for squiggly-error failed for the first time (issue #16166)`);
            await editor.waitErrorInLine(line);
        }
        for (let i = 0; i < textToWrite.length; i++) {
            await editor.performKeyCombination(openedTab, Key.BACK_SPACE);
        }
        await editor.waitErrorInLineDisappearance(line);
    });
 }

 export function suggestionInvoking(openedTab: string, line: number, char: number, suggestionText: string) {
    test('Suggestion invoking', async () => {
        await ide.closeAllNotifications();
        await editor.waitEditorAvailable(openedTab);
        await editor.clickOnTab(openedTab);
        await editor.waitEditorAvailable(openedTab);
        await editor.waitTabFocused(openedTab);
        await editor.moveCursorToLineAndChar(openedTab, line, char);
        await editor.pressControlSpaceCombination(openedTab);
        await editor.waitSuggestionWithScrolling(openedTab, suggestionText);
    });
 }

 export function autocomplete(openedTab: string, line: number, char: number, expectedText: string) {
    test('Autocomplete', async () => {
        await editor.moveCursorToLineAndChar(openedTab, line, char);
        await editor.pressControlSpaceCombination(openedTab);
        await editor.waitSuggestionContainer();
        await editor.waitSuggestionWithScrolling(openedTab, expectedText);
    });
 }

 export function waitLSInitialization(startingNote: string, startTimeout: number, buildWorkspaceTimeout: number) {
    test('LS initialization', async () => {
        await ide.checkLsInitializationStart(startingNote);
        await ide.waitStatusBarTextAbsence(startingNote, startTimeout);
        await ide.waitStatusBarTextAbsence('Building workspace', buildWorkspaceTimeout);
    });
 }

 export function codeNavigation(openedFile: string, line: number, char: number, codeNavigationClassName: string) {
    test('Codenavigation', async () => {
        await editor.moveCursorToLineAndChar(openedFile, line, char);
        await editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F12));
        await editor.waitEditorAvailable(codeNavigationClassName);
    });
 }
