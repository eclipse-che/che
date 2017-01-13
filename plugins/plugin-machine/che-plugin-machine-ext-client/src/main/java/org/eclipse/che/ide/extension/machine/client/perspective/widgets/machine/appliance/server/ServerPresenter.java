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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * The class contains business logic which allows update server's information for current machine. The class is a tab presenter and
 * represent content of server tab.
 *
 * @author Dmitry Shnurenko
 */
public class ServerPresenter implements TabPresenter {

    private final ServerView view;
    private final EntityFactory entityFactory;

    @Inject
    public ServerPresenter(ServerView view,
                           EntityFactory entityFactory) {
        this.view = view;
        this.entityFactory = entityFactory;
    }

    /**
     * Calls special method on view which updates server's information for current machine.
     *
     * @param machine
     *         machine for which need update information
     */
    public void updateInfo(Machine machine) {
        view.setServers(getServers(machine));
    }

    private List<ServerEntity> getServers(Machine machine) {
        MachineRuntimeInfo machineRuntime = machine.getRuntime();
        if (machineRuntime == null) {
            return emptyList();
        }

        Map<String, ? extends Server> servers = machineRuntime.getServers();
        List<ServerEntity> serversList = new ArrayList<>(servers.size());
        for (Map.Entry<String, ? extends Server> entry : servers.entrySet()) {
            String exposedPort = entry.getKey();
            Server descriptor = entry.getValue();

            ServerEntity serverEntity = entityFactory.createServer(exposedPort, descriptor);
            serversList.add(serverEntity);
        }
        return serversList;
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
