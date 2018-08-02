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
package org.eclipse.che.multiuser.keycloak.ide;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.api.promises.client.Promise;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public final class Keycloak extends JavaScriptObject {

  protected Keycloak() {
    super();
  }

  public static native boolean isConfigured() /*-{
    if ($wnd['_keycloak']) {
      return true;
    }

    return false;
  }-*/;

  public static native Promise<Keycloak> get() /*-{
    return new Promise(function (resolve, reject) {
      if ($wnd['_keycloak']) {
        resolve($wnd['_keycloak']);
      } else {
        reject();
      }
    });
  }-*/;

  public static native Promise<Keycloak> init(
      String theUrl,
      String theRealm,
      String theClientId,
      String theOidcProvider,
      boolean theUseNonce) /*-{
    return new Promise(function (resolve, reject) {
      try {
        console.log('[Keycloak] Initializing');
        var config;
        if(!theOidcProvider) {
          config = {
            url: theUrl,
            realm: theRealm,
            clientId: theClientId
          };
        } else {
          config = {
            oidcProvider: theOidcProvider,
            clientId: theClientId
          };
        }
        var keycloak = $wnd.Keycloak(config);
        $wnd['_keycloak'] = keycloak;
        keycloak.init({onLoad: 'login-required', checkLoginIframe: false, useNonce: theUseNonce})
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
