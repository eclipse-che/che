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
package org.eclipse.che.ide.context;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.ProductInfoDataProvider;

/**
 * Helps to update address and extract information from current address.
 *
 * @author Alexander Andrienko
 * @author Sergii Leschenko
 */
@Singleton
public class BrowserAddress {
  private static final int NAMESPACE_START_SEGMENT = 1;

  private final ProductInfoDataProvider productInfoDataProvider;

  @Inject
  public BrowserAddress(ProductInfoDataProvider productInfoDataProvider) {
    this.productInfoDataProvider = productInfoDataProvider;
  }

  /**
   * Updates browser's address along with tab title in accordance to current running workspace.
   *
   * @param namespace namespace of workspace
   * @param workspaceName name of the current running workspace. Can be null or empty if workspace
   *     was stopped.
   */
  public void setAddress(String namespace, String workspaceName) {
    String browserUrl = Window.Location.getPath();
    int currentSlashIndex = 0;
    int currentSlashNumber = 0;
    while (currentSlashNumber < NAMESPACE_START_SEGMENT) {
      currentSlashIndex = browserUrl.indexOf('/', currentSlashIndex);
      currentSlashNumber++;
    }

    String baseUrl =
        browserUrl.substring(0, currentSlashIndex) + "/" + namespace + "/" + workspaceName;

    String title;
    if (workspaceName == null || workspaceName.isEmpty()) {
      title = productInfoDataProvider.getDocumentTitle();
    } else {
      title = productInfoDataProvider.getDocumentTitle(workspaceName);
    }
    doSetAddress(baseUrl, title);
  }

  /** Returns workspace name from current address or empty string when it is undefined. */
  public String getWorkspaceName() {
    String workspaceKey = getWorkspaceKey();
    String[] split = workspaceKey.split("/", 2);
    if (split.length != 2) {
      return "";
    }
    return workspaceKey.substring(workspaceKey.lastIndexOf("/") + 1);
  }

  /** Returns workspace key from current address or empty string when it is undefined. */
  public String getWorkspaceKey() {
    String browserUrl = Window.Location.getPath();

    // TODO temporary to make it work with not "path obliged" URL
    String wsParam = Window.Location.getParameter("ws");
    if (wsParam != null) return wsParam;
    //

    String[] urlParts = browserUrl.split("/", NAMESPACE_START_SEGMENT + 1);
    if (urlParts.length < NAMESPACE_START_SEGMENT) {
      return "";
    } else {
      return urlParts[NAMESPACE_START_SEGMENT];
    }
  }

  private native void doSetAddress(String url, String title) /*-{
        try {
            var window = $wnd;

            if (!window["_history_relocation_id"]) {
                window["_history_relocation_id"] = 0;
            }

            window.top.document.title = title;

            window.history.pushState(window["_history_relocation_id"], window.top.document.title, url);
            window["_history_relocation_id"]++;
        } catch (e) {
            console.log(e.message);
        }
    }-*/;
}
