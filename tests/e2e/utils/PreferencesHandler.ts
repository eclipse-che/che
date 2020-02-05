
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

    public async setTerminalType(type: string) {
        Logger.debug('PreferencesHandler.setTerminalToDom');
        const response = await this.requestHandler.processRequest(RequestType.GET, `${TestConstants.TS_SELENIUM_BASE_URL}/api/preferences`);
        let userPref = response.data;
        let theiaPref = JSON.parse(userPref['theia-user-preferences']);
        theiaPref['terminal.integrated.rendererType'] = type;
        userPref['theia-user-preferences'] = JSON.stringify(theiaPref);
        this.requestHandler.processRequest(RequestType.POST, `${TestConstants.TS_SELENIUM_BASE_URL}/api/preferences`, userPref);
    }
}
