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

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.CLIENT_ID_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;

/** KeycloakProvider */
@Singleton
public class KeycloakProvider {
  private AppContext appContext;
  private boolean keycloakDisabled = false;
  private Promise<Keycloak> keycloak;

  @Inject
  public KeycloakProvider(AppContext appContext, PromiseProvider promiseProvider) {
    this.appContext = appContext;
    String keycloakSettings =
        getKeycloakSettings(KeycloakConstants.getEndpoint(appContext.getMasterEndpoint()));
    Map<String, String> settings = JsonHelper.toMap(keycloakSettings);
    Log.info(getClass(), "Keycloak settings: ", settings);

    keycloak =
        CallbackPromiseHelper.createFromCallback(
                new CallbackPromiseHelper.Call<Void, Throwable>() {
                  @Override
                  public void makeCall(final Callback<Void, Throwable> callback) {
                    ScriptInjector.fromUrl(
                            settings.get(AUTH_SERVER_URL_SETTING) + "/js/keycloak.js")
                        .setCallback(
                            new Callback<Void, Exception>() {
                              @Override
                              public void onSuccess(Void result) {
                                callback.onSuccess(null);
                              }

                              @Override
                              public void onFailure(Exception reason) {
                                callback.onFailure(reason);
                              }
                            })
                        .setWindow(getWindow())
                        .inject();
                  }
                })
            .thenPromise(
                (v) ->
                    Keycloak.init(
                        settings.get(AUTH_SERVER_URL_SETTING),
                        settings.get(REALM_SETTING),
                        settings.get(CLIENT_ID_SETTING)));
    Log.info(getClass(), "Keycloak init complete: ", this);
  }

  public static native String getKeycloakSettings(String keycloakSettingsEndpoint) /*-{
      var myReq = new XMLHttpRequest();
      myReq.open('GET', '' + keycloakSettingsEndpoint, false);
      myReq.send(null);
      return myReq.responseText;
    }-*/;

  public static native JavaScriptObject getWindow() /*-{
      return $wnd;
    }-*/;

  public Promise<Keycloak> getKeycloak() {
    return keycloak;
  }

  public Promise<String> getUpdatedToken(int minValidity) {
    return keycloak.thenPromise(
        new Function<Keycloak, Promise<String>>() {
          @Override
          public Promise<String> apply(Keycloak keycloak) {
            Log.debug(getClass(), "Keycloak initialized with token: ", keycloak.getToken());
            try {
              return keycloak
                  .updateToken(minValidity)
                  .then(
                      new Function<Boolean, String>() {
                        @Override
                        public String apply(Boolean refreshed) {
                          if (refreshed) {
                            Log.debug(
                                getClass(),
                                "Keycloak updated token. New token is : ",
                                keycloak.getToken());
                          } else {
                            Log.debug(getClass(), "Keycloak didn't need to update token.");
                          }
                          return keycloak.getToken();
                        }
                      });
            } catch (Throwable t) {
              Log.error(getClass(), t);
              throw t;
            }
          }
        });
  }
}
