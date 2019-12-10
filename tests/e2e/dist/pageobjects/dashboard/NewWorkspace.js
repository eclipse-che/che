"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
var NewWorkspace_1;
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
const inversify_1 = require("inversify");
const DriverHelper_1 = require("../../utils/DriverHelper");
const inversify_types_1 = require("../../inversify.types");
const TestConstants_1 = require("../../TestConstants");
const selenium_webdriver_1 = require("selenium-webdriver");
require("reflect-metadata");
const Dashboard_1 = require("./Dashboard");
const Workspaces_1 = require("./Workspaces");
const WorkspaceDetails_1 = require("./workspace-details/WorkspaceDetails");
const __1 = require("../..");
const Logger_1 = require("../../utils/Logger");
let NewWorkspace = NewWorkspace_1 = class NewWorkspace {
    constructor(driverHelper, dashboard, workspaces, testWorkspaceUtil, workspaceDetails) {
        this.driverHelper = driverHelper;
        this.dashboard = dashboard;
        this.workspaces = workspaces;
        this.testWorkspaceUtil = testWorkspaceUtil;
        this.workspaceDetails = workspaceDetails;
    }
    createAndRunWorkspace(namespace, workspaceName, dataStackId) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.createAndRunWorkspace "${namespace}/${workspaceName}" stackID: "${dataStackId}"`);
            yield this.prepareWorkspace(workspaceName, dataStackId);
            yield this.clickOnCreateAndOpenButton();
            yield this.waitPageAbsence();
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(__1.Ide.ACTIVATED_IDE_IFRAME_CSS));
            yield this.testWorkspaceUtil.waitWorkspaceStatus(namespace, workspaceName, __1.WorkspaceStatus.STARTING);
        });
    }
    waitPageAbsence(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.waitPageAbsence');
            yield this.driverHelper.waitDisappearanceWithTimeout(selenium_webdriver_1.By.css(NewWorkspace_1.NAME_FIELD_CSS), timeout);
            yield this.driverHelper.waitDisappearanceWithTimeout(selenium_webdriver_1.By.css(NewWorkspace_1.TITLE_CSS), timeout);
        });
    }
    createWorkspaceAndProceedEditing(workspaceName, dataStackId) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.createWorkspaceAndProceedEditing "${workspaceName}" stackID: "${dataStackId}"`);
            yield this.prepareWorkspace(workspaceName, dataStackId);
            yield this.selectCreateWorkspaceAndProceedEditing();
            yield this.workspaceDetails.waitPage(workspaceName);
        });
    }
    createAndOpenWorkspace(workspaceName, dataStackId) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.createAndOpenWorkspace "${workspaceName}" stackID: "${dataStackId}"`);
            yield this.prepareWorkspace(workspaceName, dataStackId);
            yield this.clickOnCreateAndOpenButton();
        });
    }
    confirmProjectAdding(sampleName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.confirmProjectAdding "${sampleName}"`);
            yield this.clickOnAddButton(timeout);
            yield this.waitProjectAdding(sampleName, timeout);
        });
    }
    waitProjectSourceForm(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.waitProjectSourceForm');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(NewWorkspace_1.PROJECT_SOURCE_FORM_CSS), timeout);
        });
    }
    selectStack(dataStackId, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.selectStack datastackID: "${dataStackId}"`);
            const stackLocator = selenium_webdriver_1.By.css(this.getStackCssLocator(dataStackId));
            yield this.driverHelper.waitAndClick(stackLocator, timeout);
            yield this.waitStackSelection(dataStackId, timeout);
        });
    }
    waitStackSelection(dataStackId, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.waitStackSelection datastackID: "${dataStackId}"`);
            const selectedStackLocator = selenium_webdriver_1.By.css(this.getSelectedStackCssLocator(dataStackId));
            yield this.driverHelper.waitAndClick(selectedStackLocator, timeout);
        });
    }
    openPageByUI() {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.openPageByUI');
            yield this.dashboard.waitPage();
            yield this.dashboard.clickWorkspacesButton();
            yield this.workspaces.clickAddWorkspaceButton();
            yield this.waitPage();
        });
    }
    waitPage(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.waitPage');
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(NewWorkspace_1.NAME_FIELD_CSS), timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(NewWorkspace_1.TITLE_CSS), timeout);
            yield this.driverHelper.waitVisibility(selenium_webdriver_1.By.css(NewWorkspace_1.PROJECT_SOURCE_FORM_CSS), timeout);
        });
    }
    waitLoaderAbsence(timeout = TestConstants_1.TestConstants.TS_SELENIUM_LOAD_PAGE_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.waitLoaderAbsence');
            yield this.driverHelper.waitPresence(selenium_webdriver_1.By.css(NewWorkspace_1.HIDDEN_LOADER_CSS), timeout);
        });
    }
    selectCreateWorkspaceAndProceedEditing(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.selectCreateWorkspaceAndProceedEditing');
            const createAndProceedEditingButtonLocator = selenium_webdriver_1.By.xpath('//span[text()=\'Create & Proceed Editing\']');
            // open drop down list
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(NewWorkspace_1.CREATE_AND_EDIT_BUTTON_CSS), timeout);
            // click on "Create & Proceed Editing" item in the drop down list
            yield this.driverHelper.waitAndClick(createAndProceedEditingButtonLocator, timeout);
        });
    }
    typeWorkspaceName(workspaceName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.typeWorkspaceName "${workspaceName}"`);
            const workspaceNameFieldLocator = selenium_webdriver_1.By.css(NewWorkspace_1.NAME_FIELD_CSS);
            yield this.driverHelper.enterValue(workspaceNameFieldLocator, workspaceName, timeout);
        });
    }
    clickOnChe7Stack(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.clickOnChe7Stack');
            const che7StackLocator = selenium_webdriver_1.By.css(NewWorkspace_1.CHE_7_STACK_CSS);
            yield this.driverHelper.waitAndClick(che7StackLocator, timeout);
            yield this.waitChe7StackSelected(timeout);
        });
    }
    waitChe7StackSelected(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.waitChe7StackSelected');
            const che7SelectedStackLocator = selenium_webdriver_1.By.css(NewWorkspace_1.SELECTED_CHE_7_STACK_CSS);
            yield this.driverHelper.waitVisibility(che7SelectedStackLocator, timeout);
        });
    }
    clickOnCreateAndOpenButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.clickOnCreateAndOpenButton');
            const ideFrameLocator = selenium_webdriver_1.By.xpath('//ide-iframe[@id=\'ide-iframe-window\' and @aria-hidden=\'false\']');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.xpath(NewWorkspace_1.CREATE_AND_OPEN_BUTTON_XPATH), timeout);
            // check that the workspace has started to boot
            yield this.driverHelper.waitVisibility(ideFrameLocator, timeout);
        });
    }
    clickOnAddOrImportProjectButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.clickOnAddOrImportProjectButton');
            const addOrImportProjectButtonLocator = selenium_webdriver_1.By.css(NewWorkspace_1.ADD_OR_IMPORT_PROJECT_BUTTON_CSS);
            yield this.driverHelper.waitAndClick(addOrImportProjectButtonLocator, timeout);
            yield this.waitProjectSourceForm(timeout);
        });
    }
    waitSampleCheckboxEnabling(sampleName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.waitSampleCheckboxEnabling "${sampleName}"`);
            const enabledSampleCheckboxLocator = selenium_webdriver_1.By.css(`#sample-${sampleName}>md-checkbox[aria-checked='true']`);
            yield this.driverHelper.waitVisibility(enabledSampleCheckboxLocator, timeout);
        });
    }
    enableSampleCheckbox(sampleName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.enableSampleCheckbox "${sampleName}"`);
            const sampleCheckboxLocator = selenium_webdriver_1.By.xpath(`(//*[@id='sample-${sampleName}']//md-checkbox//div)[1]`);
            yield this.driverHelper.waitAndClick(sampleCheckboxLocator, timeout);
            yield this.waitSampleCheckboxEnabling(sampleName, timeout);
        });
    }
    waitProjectAdding(projectName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.waitProjectAdding "${projectName}"`);
            const addedProjectLocator = selenium_webdriver_1.By.css(`#project-source-selector toggle-single-button#${projectName}`);
            yield this.driverHelper.waitVisibility(addedProjectLocator, timeout);
        });
    }
    waitProjectAbsence(projectName, timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug(`NewWorkspace.waitProjectAbsence "${projectName}"`);
            const addedProjectLocator = selenium_webdriver_1.By.css(`#project-source-selector toggle-single-button#${projectName}`);
            yield this.driverHelper.waitDisappearance(addedProjectLocator, timeout);
        });
    }
    clickOnAddButton(timeout = TestConstants_1.TestConstants.TS_SELENIUM_DEFAULT_TIMEOUT) {
        return __awaiter(this, void 0, void 0, function* () {
            Logger_1.Logger.debug('NewWorkspace.clickOnAddButton');
            yield this.driverHelper.waitAndClick(selenium_webdriver_1.By.css(NewWorkspace_1.ADD_BUTTON_CSS), timeout);
        });
    }
    getStackCssLocator(dataStackId) {
        return `span[devfile-name='${dataStackId}']`;
    }
    getSelectedStackCssLocator(dataStackId) {
        return `div.devfile-selector-item-selected[data-devfile-id='${dataStackId}']`;
    }
    prepareWorkspace(workspaceName, dataStackId) {
        return __awaiter(this, void 0, void 0, function* () {
            yield this.typeWorkspaceName(workspaceName);
            yield this.selectStack(dataStackId);
        });
    }
};
NewWorkspace.CHE_7_STACK_CSS = 'div[data-stack-id=\'che7-preview\']';
NewWorkspace.SELECTED_CHE_7_STACK_CSS = '.stack-selector-item-selected[data-stack-id=\'che7-preview\']';
NewWorkspace.CREATE_AND_OPEN_BUTTON_XPATH = '(//che-button-save-flat[@che-button-title=\'Create & Open\']/button)[1]';
NewWorkspace.CREATE_AND_EDIT_BUTTON_CSS = '#dropdown-toggle button[name=\'dropdown-toggle\']';
NewWorkspace.ADD_OR_IMPORT_PROJECT_BUTTON_CSS = '.add-import-project-toggle-button';
NewWorkspace.ADD_BUTTON_CSS = 'button[aria-disabled=\'false\'][name=\'addButton\']';
NewWorkspace.NAME_FIELD_CSS = '#workspace-name-input';
NewWorkspace.TITLE_CSS = '#New_Workspace';
NewWorkspace.HIDDEN_LOADER_CSS = 'md-progress-linear.create-workspace-progress[aria-hidden=\'true\']';
NewWorkspace.PROJECT_SOURCE_FORM_CSS = '#project-source-selector .project-source-selector-popover[aria-hidden=\'false\']';
NewWorkspace = NewWorkspace_1 = __decorate([
    inversify_1.injectable(),
    __param(0, inversify_1.inject(inversify_types_1.CLASSES.DriverHelper)),
    __param(1, inversify_1.inject(inversify_types_1.CLASSES.Dashboard)),
    __param(2, inversify_1.inject(inversify_types_1.CLASSES.Workspaces)),
    __param(3, inversify_1.inject(inversify_types_1.TYPES.WorkspaceUtil)),
    __param(4, inversify_1.inject(inversify_types_1.CLASSES.WorkspaceDetails)),
    __metadata("design:paramtypes", [DriverHelper_1.DriverHelper,
        Dashboard_1.Dashboard,
        Workspaces_1.Workspaces, Object, WorkspaceDetails_1.WorkspaceDetails])
], NewWorkspace);
exports.NewWorkspace = NewWorkspace;
//# sourceMappingURL=NewWorkspace.js.map