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
package org.eclipse.che.plugin.gdb.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

import java.util.Map;

/**
 * The view of {@link GdbConfigurationPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface GdbConfigurationPageView extends View<GdbConfigurationPageView.ActionDelegate> {

    /** Returns host. */
    String getHost();

    /** Sets host. */
    void setHost(String host);

    /** Returns port. */
    int getPort();

    /** Sets port. */
    void setPort(int port);

    /** Returns path to the binary. */
    String getBinaryPath();

    /** Sets path to the binary. */
    void setBinaryPath(String path);

    /** Sets 'dev machine' flag state. */
    void setDevHost(boolean value);

    /**
     * Sets the list of hosts to help user to choose an appropriate one.
     *
     * @param hosts
     *         the hosts list to set into the view
     */
    void setHostsList(Map<String, String> hosts);

    /** Sets enable state for host text box. */
    void setHostEnableState(boolean enable);

    /** Sets enable state for port text box. */
    void setPortEnableState(boolean enable);

    /** Action handler for the view's controls. */
    interface ActionDelegate {

        /** Called when 'Host' has been changed. */
        void onHostChanged();

        /** Called when 'Port' has been changed. */
        void onPortChanged();

        /** Called when 'Binary Path' has been changed. */
        void onBinaryPathChanged();

        /** Called when 'dev machine' flag has been changed. */
        void onDevHostChanged(boolean value);
    }
}
