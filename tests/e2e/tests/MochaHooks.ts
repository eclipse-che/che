
import { IDriver } from '..';
import { TYPES, CLASSES } from '..';
import { EditorType, TestConstants } from '../TestConstants';
import { AskForConfirmationTypeTheia, PreferencesHandlerTheia, TerminalRendererTypeTheia } from '../utils/theia/PreferencesHandlerTheia';
import { CheApiRequestHandler } from '../utils/requestHandlers/CheApiRequestHandler';
import { TimeoutConstants } from '../TimeoutConstants';
import * as monacoPageObjects from 'monaco-page-objects';
import * as vscodeExtensionTesterLocators from 'vscode-extension-tester-locators';
import { e2eContainer } from '../inversify.config';

const driver: IDriver = e2eContainer.get(TYPES.Driver);
const preferencesHandler: PreferencesHandlerTheia = e2eContainer.get(CLASSES.PreferencesHandlerTheia);

exports.mochaHooks = {
    beforeAll: [
        async function enableRequestInterceptor() {
            if (TestConstants.TS_SELENIUM_REQUEST_INTERCEPTOR) {
                CheApiRequestHandler.enableRequestInteceptor();
            }
        },
        async function enableResponseInterceptor() {
            if (TestConstants.TS_SELENIUM_RESPONSE_INTERCEPTOR) {
                CheApiRequestHandler.enableResponseInterceptor();
            }
        },
        async function setTheiaSpecificPreferences() {
            if (TestConstants.TS_SELENIUM_EDITOR === EditorType.THEIA) {
                await preferencesHandler.setConfirmExit(AskForConfirmationTypeTheia.never);
                await preferencesHandler.setTerminalType(TerminalRendererTypeTheia.dom);
            }
        },
        async function initMonacoPageObjects() {
            if (TestConstants.TS_SELENIUM_EDITOR === EditorType.CHE_CODE) {
                // init vscode-extension-tester monaco-page-objects
                monacoPageObjects.initPageObjects(TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION, TestConstants.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION, vscodeExtensionTesterLocators.getLocatorsPath(), driver.get(), 'google-chrome');
            }
        },
        async function prolongTimeoutConstantsInDebugMode() {
            if (TestConstants.TS_DEBUG_MODE === true) {
                for (let [timeout, seconds] of Object.entries(TimeoutConstants)) {
                    Object.defineProperty(TimeoutConstants, timeout, {value: seconds * 100});
                }
            }
        },
    ]
};
