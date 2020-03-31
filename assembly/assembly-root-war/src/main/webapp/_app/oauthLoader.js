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

import { KeycloakLoader } from './keycloackLoader.js';

/**
 * Displays error message
 * @param {string} message an error message to show
 */
function error(message) {
    const container = document.getElementById("error-console");

    const element = document.createElement("pre");
    element.innerHTML = message;
    element.style.color = 'red';
    container.appendChild(element);
    return element;
}

/**
 * Returns an array of query parameter values if it is present
 * @param {string} name of the query parameter
 */
function getQueryParams(name) {
    const queryString = window.location.search;
    return new URLSearchParams(queryString).getAll(name);
}

/**
 * Fetches userId
 */
function asyncGetUserId() {
    return new Promise((resolve, reject) => {
        const request = new XMLHttpRequest();
        request.open("GET", '/api/user');
        setAuthorizationHeader(request).then((xhr) => {
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
                resolve(JSON.parse(xhr.responseText).id);
            };
        });
    });
}

/**
 * Sets authorization header for a request
 * @param {XMLHttpRequest} xhr
 */
function setAuthorizationHeader(xhr) {
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

function sendToken(token) {
    if (window.opener) {
        window.opener.postMessage('token:' + (token ? token : ''),'*');
    }
}

(async () => {
    try {
        const keycloak = await new KeycloakLoader().loadKeycloakSettings();
        const token = keycloak ? keycloak.token : undefined;
        const status = getQueryParams('status')[0]; {
            if (status && status === 'ready') {
                sendToken(token);
                return;
            }
        }
        const oauthProvider = getQueryParams('oauth_provider')[0];
        if (!oauthProvider) {
            sendToken(token);
            return;
        }
        const currentUrl = window.location.href;
        const cheUrl = currentUrl.substring(0, currentUrl.indexOf('/_app'));
        const apiUrl = cheUrl + '/api';
        const redirectUrl = currentUrl.substring(0, currentUrl.indexOf('?')) + '?status=ready';
        let url = `${apiUrl}/oauth/authenticate?oauth_provider=${oauthProvider}&userId=${await asyncGetUserId()}`;
        const scope = getQueryParams('scope'); {
            for (const s of scope) {
                url += `&scope=${s}`;
            }
        }
        url += `&redirect_after_login=${redirectUrl}`;
        if (token) {
            url += `&token=${token}`;
        }
        window.location.replace(url);
    } catch (errorMessage) {
        error(errorMessage);
        console.error(errorMessage);
    }
})
();
