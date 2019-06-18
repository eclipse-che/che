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

class Loader {

    constructor() {
        document.getElementById('workspace-loader-reload').onclick = () => this.onclickReload();
    }

    /**
     * Hides progress bar and displays reloading prompt
     */
    hideLoader() {
        document.getElementById('workspace-loader-label').style.display = 'none';
        document.getElementById('workspace-loader-progress').style.display = 'none';

        document.getElementById('workspace-loader-reload').style.display = 'block';
    }

    /**
     * Displays error message
     * @param {string} message an error message to show
     */
    error(message) {
        const container = document.getElementById("workspace-console-container");
        if (container.childElementCount > 500) {
            container.removeChild(container.firstChild)
        }

        const element = document.createElement("pre");
        element.innerHTML = message;
        container.appendChild(element);
        if (element.scrollIntoView) {
            element.scrollIntoView();
        }
        element.className = "error";
        return element;
    }

    /**
     * Reloads the page
     */
    onclickReload() {
        window.location.reload();
        return false;
    }

    /**
     * Returns query parameter value if it is present
     * @param {string} name a query parameter name
     */
    getQueryParam(name) {
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

    /**
     * Fetches workspace details by ID
     * @param {string} workspaceId a workspace ID
     */
    asyncGetWorkspace(workspaceId) {
        return new Promise((resolve, reject) => {
            const request = new XMLHttpRequest();
            request.open("GET", '/api/workspace/' + workspaceId);
            this.setAuthorizationHeader(request).then((xhr) => {
                xhr.send();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== 4) {
                        return;
                    }
                    if (xhr.status !== 200) {
                        const errorMessage = 'Failed to get the workspace: "' + this.getRequestErrorMessage(xhr) + '"';
                        reject(new Error(errorMessage));
                        return;
                    }
                    resolve(JSON.parse(xhr.responseText));
                };
            });
        });
    }

    /**
     * Sets authorization header for a request
     * @param {XMLHttpRequest} xhr
     */
    setAuthorizationHeader(xhr) {
        return new Promise((resolve, reject) => {
            if (window._keycloak && window._keycloak.token) {
                window._keycloak.updateToken(5).success(() => {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + window._keycloak.token);
                    resolve(xhr);
                }).error(() => {
                    window.sessionStorage.setItem('oidcIdeRedirectUrl', location.href);
                    window._keycloak.login();
                    reject(new Error('Failed to refresh token'));
                });
            }

            resolve(xhr);
        });
    }

    /**
     * Returns `true` if any machine in workspace contains a server which matches with `redirectUrl`
     * @param {*} workspace a workspace
     * @param {string} redirectUrl a redirect URL
     */
    asyncCheckServiceLink(workspace, redirectUrl) {
        return new Promise((resolve, reject) => {
            if (!workspace.runtime) {
                reject(new Error("Can't check service link: Workspace isn't RUNNING at the moment."));
                return;
            }

            var machines = Object.values(workspace.runtime.machines);

            var servers = machines.filter(machines => machines.servers)
              .map(machine => Object.values(machine.servers))
              .reduce((servers, machineServers) => servers.concat(...machineServers), []);

            var server = servers.find(_server => _server.url && redirectUrl.startsWith(_server.url));

            if (server) {
                resolve(server);
            } else {
                reject(new Error("Workspace doesn't have a server which matches with URL: " + redirectUrl));
            }
        });
    }

    /**
     * Returns resolved promise if `workspace` has the `runtime` property
     * @param {*} workspace a workspace
     */
    asyncGetWsToken(workspace) {
        return new Promise((resolve, reject) => {
            if (workspace.runtime) {
                resolve(workspace.runtime.machineToken);
            } else {
                reject(new Error("Can't get ws-token: Workspace isn't RUNNING at the moment."));
            }
        });
    }

    /**
     * @param {string} redirectUrl a redirect URL
     * @param {string} token
     */
    asyncAuthenticate(redirectUrl, token) {
        const re = new RegExp(/(https?:\/\/[^\/]+?)(?:$|\/).*/),
            //                  \    /     \     /
            //                  scheme    host:port
            url = redirectUrl.replace(re, "$1" + "/jwt/auth");
        return new Promise((resolve, reject) => {
            const request = new XMLHttpRequest();
            request.open('GET', url);
            request.setRequestHeader('Authorization', 'Bearer ' + token);
            request.withCredentials = true;
            request.send();
            request.onreadystatechange = () => {
                if (request.readyState !== 4) {
                    return;
                }
                if (request.status !== 204) {
                    const errorMessage = 'Failed to authenticate: "' + this.getRequestErrorMessage(xhr) + '"';
                    reject(new Error(errorMessage));
                    return;
                }
                resolve();
            };
        });
    }

    getRequestErrorMessage(xhr) {
        let errorMessage;
        try {
            const response = JSON.parse(xhr.responseText);
            errorMessage = response.message;
        } catch (e) { }

        if (errorMessage) {
            return errorMessage;
        }

        if (xhr.statusText) {
            return xhr.statusText;
        }

        return "Unknown error";
    }

}

(async function() {
    const loader = new Loader();

    const workspaceId = loader.getQueryParam('workspaceId');
    const redirectUrl = loader.getQueryParam('redirectUrl');

    try {
        if (!workspaceId) {
            throw new Error("Workspace ID isn't found in query parameters.");
        }
        if (!redirectUrl) {
            throw new Error("Redirect URL isn't found in query parameters.");
        }

        await new KeycloakLoader().loadKeycloakSettings();
        const workspace = await loader.asyncGetWorkspace(workspaceId);
        await loader.asyncCheckServiceLink(workspace, redirectUrl);
        const token = await loader.asyncGetWsToken(workspace);
        await loader.asyncAuthenticate(redirectUrl, token);

        window.location.replace(redirectUrl);
    } catch (errorMessage) {
        console.error(errorMessage);
        loader.hideLoader();
        loader.error(errorMessage);
    };
})();
