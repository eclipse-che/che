/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.statepersistance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.json.JsonObject;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.statepersistance.AppStateServiceClient;

/**
 * Provides ability to save IDE state synchronously. It is strongly encouraged to use {@link
 * AppStateServiceClient} instead.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class AppStateSyncWriter {
  private static final String UPDATE_STATE = "/app/state/update";

  private final AppContext appContext;

  @Inject
  public AppStateSyncWriter(AppContext appContext) {
    this.appContext = appContext;
  }

  /**
   * Save IDE state synchronously. Note: Consider using {@link
   * AppStateServiceClient#saveState(String)} instead.
   *
   * @param appState IDE state to save
   */
  void saveState(JsonObject appState) {
    String userId = appContext.getCurrentUser().getId();
    String url = appContext.getWsAgentServerApiEndpoint() + UPDATE_STATE + "?userId=" + userId;
    String machineToken = appContext.getWorkspace().getRuntime().getMachineToken();

    sendSyncRequest(url, machineToken, appState.toJson());
  }

  private native void sendSyncRequest(String url, String machineToken, String json) /*-{
              try {
                    var request = new XMLHttpRequest();
                    request.open("POST", url, false);
                    request.setRequestHeader("Content-Type", "application/json");
                    request.setRequestHeader("Authorization", "Bearer " + machineToken);
                    request.send(json);
                } catch (e) {
                    console.error(e);
                }
    }-*/;
}
