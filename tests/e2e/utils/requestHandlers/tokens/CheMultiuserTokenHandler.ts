/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import axios from 'axios';
import querystring from 'querystring';
import { injectable } from 'inversify';
import { TestConstants } from '../../../TestConstants';
import { ITokenHandler } from './ITokenHandler';

@injectable()
export class CheMultiuserTokenHandler implements ITokenHandler {
    async get(): Promise<string> {
        let params = {};

        let keycloakUrl = this.handleTrailingSlash(TestConstants.TS_SELENIUM_BASE_URL);
        const keycloakAuthSuffix = 'auth/realms/che/protocol/openid-connect/token';

        if (TestConstants.TS_SELENIUM_SINGLE_HOST) {
            keycloakUrl = keycloakUrl + keycloakAuthSuffix;
        } else {
            keycloakUrl = keycloakUrl.replace('che', 'keycloak') + keycloakAuthSuffix;
        }

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

    /**
     * Append `/` if it's not in the URL yet. Keycloak doesn't handle double `//` in URL well.
     *
     * @param keycloakUrl
     */
    private handleTrailingSlash(keycloakUrl: string) {
        if (!(new RegExp('/$').test(keycloakUrl))) {
            keycloakUrl += '/';
        }
        return keycloakUrl;
    }
}
