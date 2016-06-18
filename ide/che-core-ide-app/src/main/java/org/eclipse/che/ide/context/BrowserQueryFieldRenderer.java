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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.collections.Jso;

/**
 * The class contains business logic which allows get or set workspace name to query field in browser.
 *
 * @author Dmitry Shnurenko
 * @author Alexander Andrienko
 */
@Singleton
public class BrowserQueryFieldRenderer {

    private static final int WORKSPACE_ORDER_IN_URL = 2;
    private static final int NAMESPACE_ORDER_IN_URL = 1;
    //Used in the JSNI methods follow
    private static final int AMOUNT_URL_PARTS = 4;

    //Used in the JSNI methods follow
    private final ProductInfoDataProvider productInfoDataProvider;
    private final Provider<AppContext>    appContextProvider;

    @Inject
    public BrowserQueryFieldRenderer(ProductInfoDataProvider productInfoDataProvider,
                                     Provider<AppContext> appContextProvider) {
        this.productInfoDataProvider = productInfoDataProvider;
        this.appContextProvider = appContextProvider;
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
     * Returns map containing key and value of parameters. If there are no parameters, empty map will be returned.
     *
     * @return string/string map of parameters
     */
    public native Jso getParameters() /*-{
        var urlParameterString = window.location.search;

        if (!urlParameterString) {
            return {};
        }

        // remove leading question marks
        while (urlParameterString.indexOf("?") == 0) {
            urlParameterString = urlParameterString.substring(1);
        }

        var map = {};
        var pairs = urlParameterString.split("&");

        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i].split('=');

            if (pair.length == 2) {
                var key = decodeURIComponent(pair[0]);
                var value = decodeURIComponent(pair[1]);
                map[key] = value;
            }
        }

        return map;
    }-*/;

    /**
     * Sets {@code projectName} to query field in browser.
     *
     * @param projectName
     *         name which will be set. Can be null or empty if workspace does not contain any projects
     */
    public void setProjectName(String projectName) {
        String workspaceName = "";
        String namespace = "";
        WorkspaceDto workspaceDto = appContextProvider.get().getWorkspace();
        if (workspaceDto != null) {
            workspaceName = workspaceDto.getConfig().getName();
            namespace  = workspaceDto.getNamespace();
        }
        setQueryField(namespace, workspaceName, projectName);
    }

    /**
     * Sets {@code projectName} to query field in browser and set tab title with current running {@code workspaceName}
     *
     * @param workspaceName
     *         name of the current running workspace. Can be null or empty if workspace was stopped.
     * @param projectName
     *         name which will be set. Can be null or empty if workspace does not contain any projects
     */
    public native void setQueryField(String namespace, String workspaceName, String projectName) /*-{
        try {
            var window = $wnd;
            var document = $doc;

            if (!window["_history_relocation_id"]) {
                window["_history_relocation_id"] = 0;
            }

            var browserUrl = window.location.pathname;
            var urlParts = browserUrl.split('/');

            urlParts[1] = namespace;
            urlParts[2] = workspaceName;
            urlParts[3] = projectName;

            var sliceIndex = @org.eclipse.che.ide.context.BrowserQueryFieldRenderer::AMOUNT_URL_PARTS;
            if (namespace == null || namespace.length == 0) {
                sliceIndex--;
            }
            if (projectName == null || projectName.length == 0) {
                sliceIndex--;
            }
            if (workspaceName == null || workspaceName.length == 0) {
                sliceIndex--;
            }
            browserUrl = urlParts.slice(0, sliceIndex).join('/');

            var title = this.@org.eclipse.che.ide.context.BrowserQueryFieldRenderer::
            productInfoDataProvider.@org.eclipse.che.ide.api.ProductInfoDataProvider::getDocumentTitle()();

            var titleWithWorkspaceName = this.@org.eclipse.che.ide.context.BrowserQueryFieldRenderer::
            productInfoDataProvider.@org.eclipse.che.ide.api.ProductInfoDataProvider::getDocumentTitle(Ljava/lang/String;)(workspaceName);

            window.top.document.title = (workspaceName == null || workspaceName.length == 0) ? title : titleWithWorkspaceName;

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

    /** Returns namespace name from browser query fields. */
    public String getNamespace() {
        String browserUrl = Window.Location.getPath();

        String[] urlParts = browserUrl.split("/");

        return urlParts.length < 3 ? "" : urlParts[NAMESPACE_ORDER_IN_URL];
    }
}
