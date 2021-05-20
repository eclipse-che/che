/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
export class KeycloakLoader {
    /**
     * Load keycloak settings
     */
    loadKeycloakSettings() {
        const msg = "Cannot load keycloak settings. This is normal for single-user mode.";

        return new Promise((resolve, reject) => {
            try {
                if (window.parent && window.parent['_keycloak']) {
                    window['_keycloak'] = window.parent['_keycloak'];
                    resolve(window['_keycloak']);
                    return;
                }
            } catch (e) {
                // parent frame has different origin, so access to parent frame is forbidden
                console.error(msg, e);
            }

            try {
                const request = new XMLHttpRequest();

                request.onerror = request.onabort = function() {
                    reject(new Error(msg));
                };

                request.onload = () => {
                    if (request.status == 200) {
                        resolve(this.injectKeycloakScript(JSON.parse(request.responseText)));
                    } else {
                        reject(new Error(msg));
                    }
                };

                const url = "/api/keycloak/settings";
                request.open("GET", url, true);
                request.send();
            } catch (e) {
                reject(new Error(msg + e.message));
            }
        });
    }

    /**
     * Injects keycloak javascript
     */
    injectKeycloakScript(keycloakSettings) {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.type = 'text/javascript';
            script.language = 'javascript';
            script.async = true;
            script.src = keycloakSettings['che.keycloak.js_adapter_url'];

            script.onload = () => {
                resolve(this.initKeycloak(keycloakSettings));
            };

            script.onerror = script.onabort = () => {
                reject(new Error('cannot load ' + script.src));
            };

            document.head.appendChild(script);
        });
    }

    /**
     * Initialize keycloak
     */
    initKeycloak(keycloakSettings) {
        return new Promise((resolve, reject) => {

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

            var useNonce;
            if (typeof keycloakSettings['che.keycloak.use_nonce'] === 'string') {
                useNonce = keycloakSettings['che.keycloak.use_nonce'].toLowerCase() === 'true';
            }
            window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
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
                    reject(new Error('[Keycloak] Failed to initialize Keycloak'));
                });
        });
    }

}
