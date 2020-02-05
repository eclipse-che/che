import { TestConstants } from '..';
import axios from 'axios';
import querystring from 'querystring';
import { injectable } from 'inversify';
import { IRequestHandler } from './IRequestHandler';
import { AbstractRequestHandler } from './AbstractRequestHandler';

@injectable()
export class MultiUserRequestHandler extends AbstractRequestHandler implements IRequestHandler {

    async setHeaders() {
        let authorization = 'Authorization';
        axios.defaults.headers.common[authorization] = 'Bearer ' + await this.getCheBearerToken();
    }

    async getCheBearerToken(): Promise<string> {
        let params = {};

        let keycloakUrl = TestConstants.TS_SELENIUM_BASE_URL;
        const keycloakAuthSuffix = '/auth/realms/che/protocol/openid-connect/token';
        keycloakUrl = keycloakUrl.replace('che', 'keycloak') + keycloakAuthSuffix;
        params = {
            client_id: 'che-public',
            username: TestConstants.TS_SELENIUM_USERNAME,
            password: TestConstants.TS_SELENIUM_PASSWORD,
            grant_type: 'password'
        };

        try {
            const responseToObtainBearerToken = await axios.post(keycloakUrl, querystring.stringify(params));
            return responseToObtainBearerToken.data.access_token;
        } catch (err) {
            console.log(`Can not get bearer token. URL used: ${keycloakUrl}`);
            throw err;
        }

    }
}
