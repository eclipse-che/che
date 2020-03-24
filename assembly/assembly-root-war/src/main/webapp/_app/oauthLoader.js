/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
class KeycloakLoader {
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

                request.onerror = request.onabort = function () {
                    reject(new Error(msg));
                };

                request.onload = () => {
                    if (request.status === 200) {
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

/**
 * Returns query parameter value if it is present
 * @param {string} name a query parameter name
 */
function getQueryParam(name) {
    const params = window.location.search.substr(1),
        paramEntries = params.split('&');
    const entry = paramEntries.find(_entry => {
        return _entry.startsWith(name + '=');
    });
    if (!entry) {
        return;
    }
    const [_, value] = entry.split('=');
    return decodeURIComponent(value);
}

(async function () {

    // const machineToken = this.getQueryParam('token');
    // const parcedToken = window.atob(machineToken);
    // const tokenObject = JSON.parse(parcedToken);

    const apiUrl = 'http://che-che.192.168.99.254.nip.io/api';
    const oauthProvider = 'github';
    const userId = this.getQueryParam('userId');

    try {
        await new KeycloakLoader().loadKeycloakSettings();
        const token = window._keycloak.token;

        const redirectUrl = window.location.href;
        let url = `${apiUrl}/oauth/authenticate?oauth_provider=${oauthProvider}&userId=${userId}`;
        // if (scope) {
        //     for (const s of scope) {
        //         url += `&scope=${s}`;
        //     }
        // }
        if (token) {
            url += `&token=${token}`;
        }
        url += `&redirect_after_login=${redirectUrl}`;
        const popupWindow = window.open(url, 'popup');
        const popup_close_handler = async () => {
            if (!popupWindow || popupWindow.closed) {
                if (popupCloseHandlerIntervalId) {
                    window.clearInterval(popupCloseHandlerIntervalId);
                }
                window.close();
            } else {
                try {
                    if (redirectUrl === popupWindow.location.href) {
                        if (popupCloseHandlerIntervalId) {
                            window.clearInterval(popupCloseHandlerIntervalId);
                        }
                        popupWindow.close();
                        window.close();
                    }
                } catch (error) {
                }
            }
        };

        const popupCloseHandlerIntervalId = window.setInterval(popup_close_handler, 80);
    } catch (errorMessage) {
        console.error(errorMessage);
        loader.hideLoader();
        loader.error(errorMessage);
    }
})
();
