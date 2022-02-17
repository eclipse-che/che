/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable, inject } from 'inversify';
import { Logger } from './Logger';
import { CLASSES } from '../inversify.types';
import { CheApiRequestHandler } from './requestHandlers/CheApiRequestHandler';
import { Editor } from '../pageobjects/ide/Editor';
import { QuickOpenContainer } from '../pageobjects/ide/QuickOpenContainer';
import { TopMenu } from '../pageobjects/ide/TopMenu';
import { Key } from 'selenium-webdriver';

export enum TerminalRendererType {
    canvas = 'canvas',
    dom = 'dom',
}

export enum AskForConfirmationType {
    never = 'never',
    ifRquired = 'ifRequired',
    always = 'always',
}

@injectable()
export class PreferencesHandler {

    constructor(@inject(CLASSES.CheApiRequestHandler) private readonly requestHandler: CheApiRequestHandler,
        @inject(CLASSES.Editor) private readonly editor: Editor,
        @inject(CLASSES.QuickOpenContainer) private readonly quickOpenContainer: QuickOpenContainer,
        @inject(CLASSES.TopMenu) private readonly topMenu: TopMenu) {
    }

    /**
     * Works properly only for the running workspace.
     */
    public async setPreferenceUsingUI(property: string, value: any) {
        const tabTitle: string = 'settings.json';

        await this.topMenu.selectOption('View', 'Find Command...');
        await this.quickOpenContainer.typeAndSelectSuggestion('Preferences:', 'Preferences: Open Preferences (JSON)');

        let editorText: string = await this.editor.getEditorVisibleText(tabTitle);
        if (!editorText) {
            editorText = '{}';
        }
        let preferences = JSON.parse(editorText);
        preferences[property] = value;

        await this.editor.deleteAllText(tabTitle);
        await this.editor.type(tabTitle, JSON.stringify(preferences), 1);
        await this.editor.type(tabTitle, Key.chord(Key.CONTROL, Key.SHIFT, 'i'), 1);
    }

    /**
     * Works properly only if set before workspace startup.
     */
    public async setTerminalType(type: TerminalRendererType) {
        Logger.debug('PreferencesHandler.setTerminalToDom');
        await this.setPreference('terminal.integrated.rendererType', type);
    }

    /**
     *
     * @param askForConfirmation possible values are "never", "ifRequired" and "always"
     */
    public async setConfirmExit(askForConfirmation: AskForConfirmationType) {
        Logger.debug(`PreferencesHandler.setConfirmExit to ${askForConfirmation}`);
        await this.setPreference(`application.confirmExit`, askForConfirmation);
    }

    /**
     * Works properly only if set before workspace startup.
     */
    public async setUseGoLanaguageServer() {
        Logger.debug(`PreferencesHandler.setUseGoLanguageServer to true.`);
        await this.setPreference('go.useLanguageServer', 'true');
    }

    /**
     * Works properly only if set before workspace startup.
     */
    public async setVscodeKubernetesPluginConfig(vsKubernetesConfig: any) {
        Logger.debug(`PreferencesHandler.setVscodeKubernetesPluginConfig`);
        await this.setPreference('vs-kubernetes', vsKubernetesConfig);
    }

    public async setPreference(attribute: string, value: any) {
        Logger.trace(`PreferencesHandler.setPreferences ${attribute} to ${value}`);
        let response;
        try {
            response = await this.requestHandler.get('api/preferences');
        } catch (e) {
            Logger.error(`PreferencesHandler.setPreferences failed to get user preferences: ${e}`);
            return;
        }
        let userPref = response.data;
        try {
            let theiaPref = JSON.parse(userPref['theia-user-preferences']);
            theiaPref[attribute] = value;
            userPref['theia-user-preferences'] = JSON.stringify(theiaPref);
            await this.requestHandler.post('api/preferences', userPref);
        } catch (e) {
            // setting terminal before running a workspace, so no theia preferences are set
            Logger.warn(`PreferencesHandler.setPreference could not set theia-user-preferences from api/preferences response, forcing manually.`);
            let theiaPref = `{ "${attribute}":"${value}" }`;
            userPref['theia-user-preferences'] = JSON.stringify(JSON.parse(theiaPref));
            try {
                await this.requestHandler.post('api/preferences', userPref);
            } catch (e) {
                Logger.error(`PreferencesHandler.setPreference failed to manually set preferences value: ${e}`);
                return;
            }
        }
        Logger.trace(`PreferencesHandler.setPreferences ${attribute} to ${value} done.`);
    }
}
