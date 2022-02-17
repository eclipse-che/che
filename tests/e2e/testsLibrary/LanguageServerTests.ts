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
import { Ide, LeftToolbarButton } from '../pageobjects/ide/Ide';
import { TopMenu } from '../pageobjects/ide/TopMenu';
import { DebugView } from '../pageobjects/ide/DebugView';
import { Key, error } from 'selenium-webdriver';
import { Logger } from '../utils/Logger';
import { BrowserTabsUtil } from '../utils/BrowserTabsUtil';
import { DriverHelper } from '../utils/DriverHelper';

@injectable()
export class LanguageServerTests {

    constructor(
        @inject(CLASSES.Editor) private readonly editor: Editor,
        @inject(CLASSES.Ide) private readonly ide: Ide,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu,
        @inject(CLASSES.DebugView) private readonly debugView: DebugView,
        @inject(CLASSES.BrowserTabsUtil) private readonly browserTabsUtil: BrowserTabsUtil,
        @inject(CLASSES.DriverHelper) private readonly driverHelper: DriverHelper) { }

    public errorHighlighting(openedTab: string, textToWrite: string, line: number) {
        test('Error highlighting', async () => {
            await this.editor.type(openedTab, textToWrite, line);
            try {
                await this.editor.waitErrorInLine(line, openedTab);
            } catch (err) {
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }
            }
            for (let i = 0; i < textToWrite.length; i++) {
                await this.editor.performKeyCombination(openedTab, Key.BACK_SPACE);
            }
            await this.editor.waitErrorInLineDisappearance(line, openedTab);
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
            await this.editor.waitTabWithSavedStatus(openedTab);
        });
    }

    public waitLSInitialization(startingNote: string, alternateNote: string = '', startTimeout: number = TimeoutConstants.TS_SELENIUM_LANGUAGE_SERVER_START_TIMEOUT) {
        test('LS initialization', async () => {
            try {
                await this.ide.checkLsInitializationStart(startingNote);
            } catch (err) {
                if (alternateNote.length === 0) {
                    throw err;
                }
                if (!(err instanceof error.TimeoutError)) {
                    throw err;
                }
                Logger.warn('Known flakiness has occurred https://github.com/eclipse/che/issues/17864');
                await this.ide.waitStatusBarContains(alternateNote);
                await this.ide.waitStatusBarTextAbsence(alternateNote, startTimeout);
                return;
            }
            await this.ide.waitStatusBarTextAbsence(startingNote, startTimeout);
            Logger.debug('Starting note successfully disappeared.');
            if (alternateNote.length > 0) {
                Logger.debug('AlternateNote value is set, waiting for presence and disappearance.');
                await this.ide.waitStatusBarContains(alternateNote);
                await this.ide.waitStatusBarTextAbsence(alternateNote, startTimeout);
            }
            Logger.debug('Language Server successfully initialized.');
        });
    }

    public goToDefinition(openedFile: string, line: number, char: number, codeNavigationClassName: string, timeout : number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        test('Go to Definition', async () => {
            try {
                await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F11));
                await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
            } catch (err) {
                // https://github.com/eclipse/che/issues/17411 was fixed by adding a retry
                if (err instanceof error.TimeoutError) {
                    Logger.warn('Code navigation (definition) didn\'t work. Trying again.');
                    try {
                        await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                        await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F11));
                        await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
                    } catch (err) {
                        Logger.error('Code navigation (definition) didn\'t work even after retrying.');
                        throw err;
                    }
                } else {
                    Logger.error('Code navigation (definition) failed with unexpected exception.');
                    throw err;
                }
            }
        });
    }

    public goToImplementations(openedFile: string, line: number, char: number, codeNavigationClassName: string, timeout : number = TimeoutConstants.TS_EDITOR_TAB_INTERACTION_TIMEOUT) {
        test('Go to Implementations', async () => {
            try {
                await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F12));
                await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
            } catch (err) {
                // https://github.com/eclipse/che/issues/17411 was fixed by adding a retry
                if (err instanceof error.TimeoutError) {
                    Logger.warn('Code navigation (implementations) didn\'t work. Trying again.');
                    try {
                        await this.editor.moveCursorToLineAndChar(openedFile, line, char);
                        await this.editor.performKeyCombination(openedFile, Key.chord(Key.CONTROL, Key.F12));
                        await this.editor.waitEditorAvailable(codeNavigationClassName, timeout);
                    } catch (err) {
                        Logger.error('Code navigation (implementations) didn\'t work even after retrying.');
                        throw err;
                    }
                } else {
                    Logger.error('Code navigation (implementations) failed with unexpected exception.');
                    throw err;
                }
            }
        });
    }

    public startAndAttachDebugger(openedFile: string) {
        test('Open debug panel', async () => {
            await this.editor.selectTab(openedFile);
            await this.topMenu.selectOption('View', 'Debug');
            await this.ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
        });

        test('Run debug', async () => {
            try {
                await this.debugView.clickOnRunDebugButton();
            } catch (err) {
                // debug config is probably missing, refresh IDE and try again https://github.com/eclipse/che/issues/19887
                await this.browserTabsUtil.refreshPage();
                await this.driverHelper.wait(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                await this.ide.waitAndSwitchToIdeFrame();
                await this.driverHelper.wait(TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
                await this.debugView.clickOnRunDebugButton();
            }
        });
    }

    public startAndAttachDebuggerWithConfiguration(openedFile: string, configurationName: string) {
        test('Open debug panel', async () => {
            await this.editor.selectTab(openedFile);
            await this.topMenu.selectOption('View', 'Debug');
            await this.ide.waitLeftToolbarButton(LeftToolbarButton.Debug);
        });

        test('Run debug', async () => {
            try {
                await this.debugView.clickOnDebugConfigurationDropDown();
                await this.debugView.clickOnDebugConfigurationItem(configurationName);
                await this.debugView.clickOnRunDebugButton();
            } catch (err) {
                // debug config is probably missing, refresh IDE and try again https://github.com/eclipse/che/issues/19887
                await this.browserTabsUtil.refreshPage();
                await this.driverHelper.wait(TimeoutConstants.TS_IDE_LOAD_TIMEOUT);
                await this.ide.waitAndSwitchToIdeFrame();
                await this.driverHelper.wait(TimeoutConstants.TS_SELENIUM_CLICK_ON_VISIBLE_ITEM);
                await this.debugView.clickOnDebugConfigurationDropDown();
                await this.debugView.clickOnDebugConfigurationItem(configurationName);
                await this.debugView.clickOnRunDebugButton();
            }
        });
    }

    public setBreakpoint(openedFile: string, line: number) {
        test('Activating breakpoint', async () => {
            await this.editor.activateBreakpoint(openedFile, line);
        });
    }

    public checkDebuggerStoppedAtBreakpoint(openedFile: string, line: number, timeout: number) {
        test('Check that debug stopped at the breakpoint', async () => {
            await this.editor.waitStoppedDebugBreakpoint(openedFile, line, timeout);
        });
    }
}
