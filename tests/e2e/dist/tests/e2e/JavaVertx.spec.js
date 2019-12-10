"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const NameGenerator_1 = require("../../utils/NameGenerator");
const TestConstants_1 = require("../../TestConstants");
const inversify_config_1 = require("../../inversify.config");
const inversify_types_1 = require("../../inversify.types");
require("reflect-metadata");
const selenium_webdriver_1 = require("selenium-webdriver");
const workspaceName = NameGenerator_1.NameGenerator.generate('wksp-test-', 5);
const namespace = TestConstants_1.TestConstants.TS_SELENIUM_USERNAME;
const sampleName = 'java-web-vertx';
const fileFolderPath = `${sampleName}/src/main/java/io/vertx/examples/spring`;
const tabTitle = 'SpringExampleRunner.java';
const codeNavigationClassName = 'ApplicationContext.class';
const driverHelper = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.DriverHelper);
const loginPage = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.CheLogin);
const dashboard = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Dashboard);
const newWorkspace = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.NewWorkspace);
const ide = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Ide);
const projectTree = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.ProjectTree);
const editor = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Editor);
const topMenu = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.TopMenu);
const quickOpenContainer = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.QuickOpenContainer);
const terminal = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Terminal);
const contextMenu = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.ContextMenu);
suite('Java Vert.x test', () => __awaiter(this, void 0, void 0, function* () {
    suite('Login and wait dashboard', () => __awaiter(this, void 0, void 0, function* () {
        test('Login', () => __awaiter(this, void 0, void 0, function* () {
            yield driverHelper.navigateToUrl(TestConstants_1.TestConstants.TS_SELENIUM_BASE_URL);
            yield loginPage.login();
        }));
    }));
    suite('Create Java Vert.x workspace ' + workspaceName, () => __awaiter(this, void 0, void 0, function* () {
        test('Open \'New Workspace\' page', () => __awaiter(this, void 0, void 0, function* () {
            yield newWorkspace.openPageByUI();
        }));
        test('Create and open workspace', () => __awaiter(this, void 0, void 0, function* () {
            yield newWorkspace.createAndOpenWorkspace(workspaceName, 'Java Vert.x');
        }));
    }));
    suite('Work with IDE', () => __awaiter(this, void 0, void 0, function* () {
        test('Wait IDE availability', () => __awaiter(this, void 0, void 0, function* () {
            yield ide.waitWorkspaceAndIde(namespace, workspaceName);
        }));
        test('Open project tree container', () => __awaiter(this, void 0, void 0, function* () {
            yield projectTree.openProjectTreeContainer();
        }));
        test('Wait project imported', () => __awaiter(this, void 0, void 0, function* () {
            yield projectTree.waitProjectImported(sampleName, 'src');
        }));
    }));
    suite('Language server validation', () => __awaiter(this, void 0, void 0, function* () {
        test('Expand project and open file in editor', () => __awaiter(this, void 0, void 0, function* () {
            yield projectTree.expandPathAndOpenFileInAssociatedWorkspace(fileFolderPath, tabTitle);
            yield editor.selectTab(tabTitle);
        }));
        test('Java LS initialization', () => __awaiter(this, void 0, void 0, function* () {
            yield ide.checkLsInitializationStart('Starting Java Language Server');
            yield ide.waitStatusBarTextAbsence('Starting Java Language Server', 1800000);
            yield ide.waitStatusBarTextAbsence('Building workspace', 360000);
        }));
        test('Suggestion invoking', () => __awaiter(this, void 0, void 0, function* () {
            yield ide.closeAllNotifications();
            yield editor.waitEditorAvailable(tabTitle);
            yield editor.clickOnTab(tabTitle);
            yield editor.waitEditorAvailable(tabTitle);
            yield editor.waitTabFocused(tabTitle);
            yield editor.moveCursorToLineAndChar(tabTitle, 19, 11);
            yield editor.pressControlSpaceCombination(tabTitle);
            yield editor.waitSuggestionWithScrolling(tabTitle, 'cancelTimer(long arg0) : boolean');
        }));
        test('Error highlighting', () => __awaiter(this, void 0, void 0, function* () {
            yield editor.type(tabTitle, 'error', 21);
            yield editor.waitErrorInLine(21);
            yield editor.performKeyCombination(tabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE, selenium_webdriver_1.Key.BACK_SPACE));
            yield editor.waitErrorInLineDisappearance(21);
        }));
        test('Autocomplete', () => __awaiter(this, void 0, void 0, function* () {
            yield editor.moveCursorToLineAndChar(tabTitle, 18, 15);
            yield editor.pressControlSpaceCombination(tabTitle);
            yield editor.waitSuggestionContainer();
            yield editor.waitSuggestionWithScrolling(tabTitle, 'Vertx - io.vertx.core');
        }));
        test('Codenavigation', () => __awaiter(this, void 0, void 0, function* () {
            yield editor.moveCursorToLineAndChar(tabTitle, 17, 15);
            try {
                yield editor.performKeyCombination(tabTitle, selenium_webdriver_1.Key.chord(selenium_webdriver_1.Key.CONTROL, selenium_webdriver_1.Key.F12));
                yield editor.waitEditorAvailable(codeNavigationClassName);
            }
            catch (err) {
                // workaround for issue: https://github.com/eclipse/che/issues/14520
                if (err instanceof selenium_webdriver_1.error.TimeoutError) {
                    checkCodeNavigationWithContextMenu();
                }
            }
        }));
    }));
    suite('Validation of project build', () => __awaiter(this, void 0, void 0, function* () {
        test('Build application', () => __awaiter(this, void 0, void 0, function* () {
            let taskName = 'maven build';
            yield runTask(taskName);
            yield quickOpenContainer.clickOnContainerItem('Continue without scanning the task output');
            yield ide.waitNotification('Task ' + taskName + ' has exited with code 0.', 60000);
        }));
        test('Close the terminal tasks', () => __awaiter(this, void 0, void 0, function* () {
            yield terminal.closeTerminalTab('maven build');
        }));
    }));
    suite('Stop and remove workspace', () => __awaiter(this, void 0, void 0, function* () {
        test('Stop workspace', () => __awaiter(this, void 0, void 0, function* () {
            yield dashboard.stopWorkspaceByUI(workspaceName);
        }));
        test('Delete workspace', () => __awaiter(this, void 0, void 0, function* () {
            yield dashboard.deleteWorkspaceByUI(workspaceName);
        }));
    }));
    function runTask(task) {
        return __awaiter(this, void 0, void 0, function* () {
            yield topMenu.selectOption('Terminal', 'Run Task...');
            try {
                yield quickOpenContainer.waitContainer();
            }
            catch (err) {
                if (err instanceof selenium_webdriver_1.error.TimeoutError) {
                    console.warn(`After clicking to the "Terminal" -> "Run Task ..." the "Quick Open Container" has not been displayed, one more try`);
                    yield topMenu.selectOption('Terminal', 'Run Task...');
                    yield quickOpenContainer.waitContainer();
                }
            }
            yield quickOpenContainer.clickOnContainerItem(task);
        });
    }
    function checkCodeNavigationWithContextMenu() {
        return __awaiter(this, void 0, void 0, function* () {
            yield contextMenu.invokeContextMenuOnActiveElementWithKeys();
            yield contextMenu.waitContextMenuAndClickOnItem('Go to Definition');
            console.log('Known isuue https://github.com/eclipse/che/issues/14520.');
        });
    }
}));
//# sourceMappingURL=JavaVertx.spec.js.map