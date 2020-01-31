
import { injectable, inject } from 'inversify';
import { Logger } from './Logger';
import { CLASSES } from '../inversify.types';
import { RequestHandler } from './RequestHandler';
import { TestConstants } from '../TestConstants';
import { RequestType } from './RequestType';

@injectable()
export class PreferencesHandler {

    constructor(@inject(CLASSES.RequestHandler) private readonly requestHandler: RequestHandler) {
    }

    public async setTerminalToDom() {
        Logger.debug('PreferencesHandler.setTerminalToDom');
        const response = await this.requestHandler.processRequest(RequestType.GET, `${TestConstants.TS_SELENIUM_BASE_URL}/api/preferences`);
        let userPref = response.data;
        try {
            let theiaPref = JSON.parse(userPref['theia-user-preferences']);
            let terminal = theiaPref['terminal.integrated.rendererType'];
            if ( terminal === 'dom' ) {
                Logger.trace('Terminal renderer type already set to dom.');
            } else {
                Logger.trace('Setting terminal renderer type to dom.');
                theiaPref['terminal.integrated.rendererType'] = 'dom';
                userPref['theia-user-preferences'] = JSON.stringify(theiaPref);
                this.requestHandler.processRequest(RequestType.POST, `${TestConstants.TS_SELENIUM_BASE_URL}/api/preferences`, userPref);
            }
        } catch (e) {
            Logger.trace('Can not set terminal renderer type.');
            throw e;
        }
    }
}
