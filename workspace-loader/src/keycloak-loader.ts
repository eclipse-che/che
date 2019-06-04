/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

// tslint:disable:no-any

declare const Keycloak: Function;

export class KeycloakLoader {
    /**
     * Load keycloak settings
     */
    public loadKeycloakSettings(): Promise<any> {
        const msg = 'Cannot load keycloak settings. This is normal for single-user mode.';

        return new Promise((resolve, reject) => {
            if (window.parent && window.parent['_keycloak']) {
                window['_keycloak'] = window.parent['_keycloak'];
                resolve(window['_keycloak']);
                return;
            }

            const request = new XMLHttpRequest();

            request.onerror = request.onabort = function () {
                reject(new Error(msg));
            };

            request.onload = () => {
                if (request.status === 200) {
                    resolve(this.injectKeycloakScript(JSON.parse(request.responseText)));
                } else {
                    reject(new Error(msg + ' Cannot load keycloak script'));
                }
            };

            const url = '/api/keycloak/settings';
            request.open('GET', url, true);
            request.send();
        });
    }

    /**
     * Injects keycloak javascript
     */
    private injectKeycloakScript(keycloakSettings: any): Promise<any> {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.type = 'text/javascript';
            script.async = true;
            script.src = keycloakSettings['che.keycloak.js_adapter_url'];

            script.onload = () => {
                resolve(this.initKeycloak(keycloakSettings));
            };

            script.onerror = script.onabort = () => {
                reject('Cannot load ' + script.src);
            };

            document.head.appendChild(script);
        });
    }

    /**
     * Initialize keycloak and load the IDE
     */
    private initKeycloak(keycloakSettings: any): Promise<any> {
        function keycloakConfig() {
            const theOidcProvider = keycloakSettings['che.keycloak.oidc_provider'];
            if (!theOidcProvider) {
                return {
                    url: keycloakSettings['che.keycloak.auth_server_url'],
                    realm: keycloakSettings['che.keycloak.realm'],
                    clientId: keycloakSettings['che.keycloak.client_id']
                };
            } else {
                return {
                    oidcProvider: theOidcProvider,
                    clientId: keycloakSettings['che.keycloak.client_id']
                };
            }
        }
        const keycloak = Keycloak(keycloakConfig());

        window['_keycloak'] = keycloak;

        let useNonce: boolean;
        if (typeof keycloakSettings['che.keycloak.use_nonce'] === 'string') {
            useNonce = keycloakSettings['che.keycloak.use_nonce'].toLowerCase() === 'true';
        }
        window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);

        return new Promise((resolve, reject) => {
            keycloak
                .init({
                    onLoad: 'login-required',
                    checkLoginIframe: false,
                    useNonce: useNonce,
                    scope: 'email profile',
                    redirectUri: keycloakSettings['che.keycloak.redirect_url.ide']
                })
                .success(() => {
                    resolve(keycloak);
                })
                .error(() => {
                    reject('[Keycloak] Failed to initialize Keycloak');
                });
        });
    }

}
