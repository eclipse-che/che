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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Provides methods which allow change view representation of server tab.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(ServerViewImpl.class)
public interface ServerView extends IsWidget {

    /**
     * Sets servers in special place on view.
     *
     * @param servers
     *         servers which need save
     */
    void setServers(@NotNull List<ServerEntity> servers);

    /**
     * Change visibility of server tab panel.
     *
     * @param visible
     *         <code>true</code> panel is visible, <code>false</code> panel isn't visible
     */
    void setVisible(boolean visible);
}
