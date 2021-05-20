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
import { KeycloakLoader } from './keycloackLoader.js';

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
     * Finds and returns appropriate server which user request belongs to, or throw error
     * if none is found.
     * @param {*} workspace a workspace
     * @param {string} redirectUrl a redirect URL
     */
    asyncGetMatchedServer(workspace, redirectUrl) {
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
     * Calls auth endpoint in order to accept authentication cookie.
     * @param {string} redirectUrl a redirect URL
     * @param {string} token
     */
    asyncAuthenticate(redirectUrl, endpointOrigin, token) {
        redirectUrl = new URL(redirectUrl);
        // if endpointOrigin is just "/", we'd end up with "///jwt/auth". So we replace two or more consecutive / with a single /.
        const url = redirectUrl.protocol + "//" + redirectUrl.host + ("/" + endpointOrigin + "/jwt/auth").replace(/\/{2,}/g, "/");
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
                    const errorMessage = 'Failed to authenticate: "' + this.getRequestErrorMessage(request) + '"';
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
        const server = await loader.asyncGetMatchedServer(workspace, redirectUrl);
        const token = await loader.asyncGetWsToken(workspace);
        await loader.asyncAuthenticate(server.url, server.attributes.endpointOrigin, token);

        window.location.replace(redirectUrl);
    } catch (errorMessage) {
        console.error(errorMessage);
        loader.hideLoader();
        loader.error(errorMessage);
    };
})();
