/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar.previews;

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/** Represents an item for displaying in the 'Previews' list. */
class PreviewUrl {

    private final AppContext appContext;

    private final String url;
    private final String displayName;

    PreviewUrl(String url, AppContext appContext) {
        this.url = url;
        this.appContext = appContext;
        this.displayName = getDisplayNameForPreviewUrl(url);
    }

    /** Returns actual preview URL. */
    String getUrl() {
        return url;
    }

    /**
     * Returns preview URL in user-friendly form.
     * E.g. {@code dev-machine:8080} instead of {@code http://172.19.21.35:32801}.
     */
    String getDisplayName() {
        return displayName;
    }

    private String getDisplayNameForPreviewUrl(String previewUrl) {
        final DevMachine devMachine = appContext.getDevMachine();
// FIXME: spi
//        final MachineRuntimeInfo devMachineRuntime = devMachine.getRuntime();

//        if (devMachineRuntime == null) {
//            return previewUrl;
//        }

        for (Entry<String, ? extends Server> entry : devMachine.getServers().entrySet()) {
            Server server = entry.getValue();
            String serverUrl = server.getUrl();

            if (serverUrl == null) {
                continue;
            }

            if (previewUrl.startsWith(serverUrl)) {
                String port = entry.getKey();

                // server's port may be in form of '8080/tcp' so need to cut protocol name
                final int slashIndex = port.lastIndexOf('/');
                if (slashIndex > -1) {
                    port = port.substring(0, slashIndex);
                }

                return previewUrl.replace(serverUrl, devMachine.getDisplayName() + ':' + port);
            }
        }

        return previewUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviewUrl that = (PreviewUrl)o;
        return Objects.equals(url, that.url) &&
               Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, displayName);
    }
}
