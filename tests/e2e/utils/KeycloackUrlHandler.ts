/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { TestConstants } from '../TestConstants';

export class KeycloackUrlHandler {
    public static getBaseKeycloakUrl(): string {
        let baseKeycloakUrl: string = TestConstants.TS_SELENIUM_BASE_URL;

        if (!TestConstants.TS_SELENIUM_SINGLE_HOST) {
            baseKeycloakUrl = baseKeycloakUrl.replace('che', 'keycloak');
        }

        return baseKeycloakUrl;
    }

    public static getTokenEndpointUrl(): string {
        return `${this.getBaseKeycloakUrl()}/auth/realms/che/protocol/openid-connect/token`;
    }

    public static getIdentityCallbackUrl(): string {
        return `${this.getBaseKeycloakUrl()}/auth/realms/che/broker/github/endpoint`;
    }

}
