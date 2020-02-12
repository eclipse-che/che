
import { injectable, inject } from 'inversify';
import { Logger } from './Logger';
import { CLASSES } from '../inversify.types';
import { CheApiRequestHandler } from './requestHandlers/CheApiRequestHandler';

@injectable()
export class PreferencesHandler {

    constructor(@inject(CLASSES.CheApiRequestHandler) private readonly requestHandler: CheApiRequestHandler) {
    }

    public async setTerminalType(type: string) {
        Logger.debug('PreferencesHandler.setTerminalToDom');
        const response = await this.requestHandler.get('api/preferences');
        let userPref = response.data;
        try {
            let theiaPref = JSON.parse(userPref['theia-user-preferences']);
            theiaPref['terminal.integrated.rendererType'] = type;
            userPref['theia-user-preferences'] = JSON.stringify(theiaPref);
            this.requestHandler.post('api/preferences', userPref);
        } catch (e) {
            // setting terminal before running a workspace, so no theia preferences are set
            let theiaPref = `{ "terminal.integrated.rendererType":"${type}" }`;
            userPref['theia-user-preferences'] = JSON.stringify(JSON.parse(theiaPref));
            this.requestHandler.post('api/preferences', userPref);
        }

    }
}
