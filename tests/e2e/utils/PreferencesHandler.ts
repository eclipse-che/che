
import { injectable, inject } from 'inversify';
import { Logger } from './Logger';
import { CLASSES } from '../inversify.types';
import { CheApiRequestHandler } from './requestHandlers/CheApiRequestHandler';

export enum TerminalRendererType {
    canvas = 'canvas',
    dom = 'dom'
}

export enum AskForConfirmationType {
    never = 'never',
    ifRquired = 'ifRequired',
    always = 'always'
}

@injectable()
export class PreferencesHandler {

    constructor(@inject(CLASSES.CheApiRequestHandler) private readonly requestHandler: CheApiRequestHandler) {
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

    private async setPreference(attribute: string, value: string) {
        Logger.debug(`PreferencesHandler.setPreferences ${attribute} to ${value}`);
        const response = await this.requestHandler.get('api/preferences');
        const userPref = response.data;
        try {
            const theiaPref = JSON.parse(userPref['theia-user-preferences']);
            theiaPref[attribute] = value;
            userPref['theia-user-preferences'] = JSON.stringify(theiaPref);
            this.requestHandler.post('api/preferences', userPref);
        } catch (e) {
            // setting terminal before running a workspace, so no theia preferences are set
            const theiaPref = `{ "${attribute}":"${value}" }`;
            userPref['theia-user-preferences'] = JSON.stringify(JSON.parse(theiaPref));
            this.requestHandler.post('api/preferences', userPref);
        }
    }
}
