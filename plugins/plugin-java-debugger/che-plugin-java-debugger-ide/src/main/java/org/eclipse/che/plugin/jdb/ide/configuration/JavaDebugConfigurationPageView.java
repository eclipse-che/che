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
package org.eclipse.che.plugin.jdb.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.util.Pair;

import java.util.List;

/**
 * The view of {@link JavaDebugConfigurationPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaDebugConfigurationPageView extends View<JavaDebugConfigurationPageView.ActionDelegate> {

    /** Returns host. */
    String getHost();

    /** Sets host. */
    void setHost(String host);

    /** Returns port. */
    int getPort();

    /** Sets port. */
    void setPort(int port);

    void setHostEnableState(boolean enable);

    /**
     * Sets the list of ports to help user to choose an appropriate one.
     *
     * @param ports
     *         the ports list to set to the view
     */
    void setPortsList(List<Pair<String, String>> ports);

    /** Sets 'dev machine' flag state. */
    void setDevHost(boolean value);

    /** Action handler for the view's controls. */
    interface ActionDelegate {

        /** Called when 'Host' has been changed. */
        void onHostChanged();

        /** Called when 'Port' has been changed. */
        void onPortChanged();

        /** Called when 'dev machine' flag has been changed. */
        void onDevHostChanged(boolean value);
    }
}
