/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.context;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.ProductInfoDataProvider;

/**
 * The class contains business logic which allows get or set workspace name to query field in browser.
 *
 * @author Dmitry Shnurenko
 * @author Alexander Andrienko
 */
@Singleton
public class BrowserQueryFieldRenderer {

    private static final int WORKSPACE_ORDER_IN_URL = 2;

    //Used in the JSNI methods follow
    private final ProductInfoDataProvider productInfoDataProvider;

    @Inject
    public BrowserQueryFieldRenderer(ProductInfoDataProvider productInfoDataProvider) {
        this.productInfoDataProvider = productInfoDataProvider;
    }

    /**
     * Returns value of parameter by name from browser query string. If parameter not found empty string will be returned.
     *
     * @param name
     *         name of value parameter
     * @return string representation of value parameter
     */
    public native String getParameterFromURLByName(String name) /*-{
        var urlParameterString = window.location.search;

        if (!urlParameterString) {
            return "";
        }

        // remove leading question marks
        while (urlParameterString.indexOf("?") == 0) {
            urlParameterString = urlParameterString.substring(1);
        }

        var pairs = urlParameterString.split("&");

        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i].split('=');

            if (pair.length == 2 && decodeURIComponent(pair[0]) == name) {
                return decodeURIComponent(pair[1]);
            }
        }

        return "";
    }-*/;

    /**
     * Sets workspace name to query field in browser.
     *
     * @param workspaceName
     *         name which will be set
     */
    public native void setWorkspaceName(String workspaceName) /*-{
        try {
            var window = $wnd;
            var document = $doc;

            if (!window["_history_relocation_id"]) {
                window["_history_relocation_id"] = 0;
            }

            var browserUrl = window.location.pathname;

            var urlParts = browserUrl.split("/");
            urlParts[2] = workspaceName;

            browserUrl = urlParts.join("/");

            window.top.document.title = this.@org.eclipse.che.ide.context.BrowserQueryFieldRenderer::
                productInfoDataProvider.@org.eclipse.che.ide.api.ProductInfoDataProvider::getDocumentTitle()();
            window.history.pushState(window["_history_relocation_id"], window.top.document.title, browserUrl);
            window["_history_relocation_id"]++;
        } catch (e) {
            console.log(e.message);
        }
    }-*/;

    /**
     * Sets project name to query field in browser.
     *
     * @param projectName
     *         name which will be set
     */
    public native void setProjectName(String projectName) /*-{
        try {
            var window = $wnd;
            var document = $doc;

            if (!window["_history_relocation_id"]) {
                window["_history_relocation_id"] = 0;
            }

            var browserUrl = window.location.pathname;

            var urlParts = browserUrl.split("/");
            urlParts[3] = "";

            browserUrl = urlParts.join("/") + projectName;

            var titleWithoutSelectedProject = this.@org.eclipse.che.ide.context.BrowserQueryFieldRenderer::
                productInfoDataProvider.@org.eclipse.che.ide.api.ProductInfoDataProvider::getDocumentTitle()();
            var titleWithSelectedProject = this.@org.eclipse.che.ide.context.BrowserQueryFieldRenderer::
                productInfoDataProvider.@org.eclipse.che.ide.api.ProductInfoDataProvider::getDocumentTitle(Ljava/lang/String;)(projectName);
            window.top.document.title = projectName.length == 0 ? titleWithoutSelectedProject : titleWithSelectedProject;

            window.history.pushState(window["_history_relocation_id"], window.top.document.title, browserUrl);
            window["_history_relocation_id"]++;
        } catch (e) {
            console.log(e.message);
        }
    }-*/;

    /** Returns workspace name from browser query fields. */
    public String getWorkspaceName() {
        String browserUrl = Window.Location.getPath();

        String[] urlParts = browserUrl.split("/");

        return urlParts.length < 3 ? "" : urlParts[WORKSPACE_ORDER_IN_URL];
    }
}
