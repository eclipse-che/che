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
var ActivityTracker = new function () {

    var url;
    var timeoutInterval = 30000;
    var maxErrors = 10;
    var errors = 0;
    var active;

    this.init = function (restContext, workspaceId) {
        this.url = restContext + "/activity/" + workspaceId;
        document.addEventListener("mousemove",  ActivityTracker.setActive);
        document.addEventListener("keypress", ActivityTracker.setActive);
        setInterval(ActivityTracker.sendRequest, timeoutInterval);

    };

    this.setActive = function() {
        if (!active && errors < maxErrors) {
            active = true;
        }
    }


    this.sendRequest = function () {
        if (!active) {
            return;
        }
        active = false;

        var request;
        if (window.XMLHttpRequest) {
            request = new XMLHttpRequest();
        } else {
            request = new ActiveXObject("Microsoft.XMLHTTP");
        }

        request.onreadystatechange = function () {
            if (request.readyState == 4) {
                if (request.status == 204) {
                    errors = 0;
                } else {
                    errors++;
                }

            }
        };


        request.open("PUT", ActivityTracker.url, true);

        var keycloak = window['_keycloak'];
        if (keycloak) {
            keycloak.updateToken(5)
                .success(function (refreshed) {
                    var token = "Bearer " + keycloak.token;
                    request.setRequestHeader("Authorization", token);
                    request.send();
                });
        } else {
            request.send();
        }
    };
};
