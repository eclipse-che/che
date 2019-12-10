"use strict";
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const inversify_config_1 = require("../../inversify.config");
const inversify_types_1 = require("../../inversify.types");
const NameGenerator_1 = require("../../utils/NameGenerator");
const workspaceName = NameGenerator_1.NameGenerator.generate('wksp-test-', 5);
const namespace = 'che';
const sampleName = 'console-java-simple';
const fileFolderPath = `${sampleName}/src/main/java/org/eclipse/che/examples`;
const tabTitle = 'HelloWorld.java';
const loginPage = inversify_config_1.e2eContainer.get(inversify_types_1.TYPES.CheLogin);
const dashboard = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Dashboard);
const newWorkspace = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.NewWorkspace);
const ide = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Ide);
const projectTree = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.ProjectTree);
const editor = inversify_config_1.e2eContainer.get(inversify_types_1.CLASSES.Editor);
suite('E2E', () => __awaiter(this, void 0, void 0, function* () {
    suite('Login and wait dashboard', () => __awaiter(this, void 0, void 0, function* () {
        test('Login', () => __awaiter(this, void 0, void 0, function* () {
            yield loginPage.login();
        }));
    }));
    suite('Create workspace and add plugin', () => __awaiter(this, void 0, void 0, function* () {
        test('Open \'New Workspace\' page', () => __awaiter(this, void 0, void 0, function* () {
            yield newWorkspace.openPageByUI();
        }));
        test('Create and open workspace', () => __awaiter(this, void 0, void 0, function* () {
            yield newWorkspace.createAndOpenWorkspace(workspaceName, 'Java Maven');
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
        test('Expand project and open file in editor', () => __awaiter(this, void 0, void 0, function* () {
            yield projectTree.expandPathAndOpenFile(fileFolderPath, tabTitle);
        }));
        test('Check "Java Language Server" initialization by statusbar', () => __awaiter(this, void 0, void 0, function* () {
            yield ide.waitStatusBarContains('Starting Java Language Server');
            yield ide.waitStatusBarTextAbsence('Starting Java Language Server', 60000);
        }));
        test('Check "Java Language Server" initialization by suggestion invoking', () => __awaiter(this, void 0, void 0, function* () {
            yield ide.closeAllNotifications();
            yield editor.waitEditorAvailable(tabTitle);
            yield editor.clickOnTab(tabTitle);
            yield editor.waitEditorAvailable(tabTitle);
            yield editor.waitTabFocused(tabTitle);
            yield editor.moveCursorToLineAndChar(tabTitle, 6, 20);
            yield editor.pressControlSpaceCombination(tabTitle);
            yield editor.waitSuggestionWithScrolling(tabTitle, 'append(CharSequence csq, int start, int end) : PrintStream');
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
}));
//# sourceMappingURL=WorkspaceCreationAndLsInitialization.spec.js.map