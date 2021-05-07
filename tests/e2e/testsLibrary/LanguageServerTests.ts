/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import 'reflect-metadata';
import { inject, injectable } from 'inversify';
import { CLASSES } from '../inversify.types';
import { TimeoutConstants } from '../TimeoutConstants';
import { Editor } from '../pageobjects/ide/Editor';
import { Ide} from '../pageobjects/ide/Ide';
import { Key, error } from 'selenium-webdriver';
import { Logger } from '../utils/Logger';

@injectable()
export class LanguageServerTests {

    constructor(@inject(CLASSES.Editor) private readonly editor: Editor, @inject(CLASSES.Ide) private readonly ide: Ide) { }

    public errorHighlighting(openedTab: string, textToWrite: string, line: number) {
        test('Error highlighting', async () => {
            await this.editor.type(openedTab, textToWrite, line);
            try {
                await this.editor.waitErrorInLine(line);
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }
            }
            for (let i = 0; i < textToWrite.length; i++) {
                await this.editor.performKeyCombination(openedTab, Key.BACK_SPACE);
            }
            await this.editor.waitErrorInLineDisappearance(line);
        });
    }

    public suggestionInvoking(openedTab: string, line: number, char: number, suggestionText: string) {
        test('Suggestion invoking', async () => {
            await this.ide.closeAllNotifications();
            await this.editor.waitEditorAvailable(openedTab);
            await this.editor.clickOnTab(openedTab);
            await this.editor.waitEditorAvailable(openedTab);
            await this.editor.waitTabFocused(openedTab);
            await this.editor.moveCursorToLineAndChar(openedTab, line, char);
            await this.editor.pressControlSpaceCombination(openedTab);
            await this.editor.waitSuggestion(openedTab, suggestionText);
        });
    }

    public autocomplete(openedTab: string, line: number, char: number, expectedText: string) {
        test('Autocomplete', async () => {
            await this.editor.moveCursorToLineAndChar(openedTab, line, char);
            await this.editor.pressControlSpaceCombination(openedTab);
            await this.editor.waitSuggestionContainer();
            await this.editor.waitSuggestionWithScrolling(openedTab, expectedText);
        });
    }

    public waitLSInitialization(startingNote: string, startTimeout: number, buildWorkspaceTimeout: number) {
        test('LS initialization', async () => {
            await this.ide.checkLsInitializationStart(startingNote);
            await this.ide.waitStatusBarTextAbsence(startingNote, startTimeout);
            await this.ide.waitStatusBarTextAbsence('Building workspace', buildWorkspaceTimeout);
        });
    }

    public codeNavigation(openedFile: string, line: number, char: number, codeNavigationClassName: string, timeout : number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        test('Codenavigation', async () => {
            // adding retry to fix https://github.com/eclipse/che/issues/17411
            try {
                await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F12));
                await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
            } catch (err) {
                if (err instanceof error.TimeoutError) {
                    Logger.warn('Code navigation didn\'t work. Trying again.');
                    await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                    await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F12));
                    await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
                } else {
                    Logger.error('Code navigation didn\'t work even after retrying.');
                    throw err;
                }
            }
        });
    }
}
