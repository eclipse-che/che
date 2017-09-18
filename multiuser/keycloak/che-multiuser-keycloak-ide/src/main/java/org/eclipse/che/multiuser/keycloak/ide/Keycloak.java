/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.ide;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.api.promises.client.Promise;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public final class Keycloak extends JavaScriptObject {

  protected Keycloak() {
    super();
  }

  public static native Promise<Keycloak> init(
      String theUrl, String theRealm, String theClientId) /*-{
        return new Promise(function (resolve, reject) {
            try {
                console.log('[Keycloak] Initializing');
                var keycloak = $wnd.Keycloak({
                    url: theUrl,
                    realm: theRealm,
                    clientId: theClientId
                });
                keycloak.init({onLoad: 'login-required', checkLoginIframe: false})
                    .success(function (authenticated) {
                        resolve(keycloak);
                    })
                    .error(function () {
                        console.log('[Keycloak] Failed to initialize Keycloak');
                        reject();
                    });
                console.log('[Keycloak] Initializing complete');
            } catch (ex) {
                console.log('[Keycloak] Failed to initialize Keycloak with exception: ', ex);
                reject();
            }
        });
    }-*/;

  public native Promise<Boolean> updateToken(int minValidity) /*-{
        var theKeycloak = this;
        return new Promise(function (resolve, reject) {
            try {
                theKeycloak.updateToken(minValidity)
                    .success(function (refreshed) {
                        resolve(refreshed);
                    })
                    .error(function () {
                        console.log('[Keycloak] Failed updating Keycloak token');
                        reject();
                        theKeycloak.login();
                    });
            } catch (ex) {
                console.log('[Keycloak] Failed updating Keycloak token with exception: ', ex);
                reject();
                theKeycloak.login();
            }
        });


        return updatePromise;
    }-*/;

  public native String getToken() /*-{
        return this.token;
    }-*/;
}
