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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.TableResources;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The class displays server's information for current machine.
 *
 * @author Dmitry Shnurenko
 */
public class ServerViewImpl extends Composite implements ServerView {
    interface ServerWidgetImplUiBinder extends UiBinder<Widget, ServerViewImpl> {
    }

    private final static ServerWidgetImplUiBinder UI_BINDER = GWT.create(ServerWidgetImplUiBinder.class);

    @UiField(provided = true)
    final MachineLocalizationConstant locale;
    @UiField(provided = true)
    final CellTable<Server>           servers;

    @Inject
    public ServerViewImpl(MachineLocalizationConstant locale, TableResources tableResources) {
        this.locale = locale;
        this.servers = createTable(tableResources);

        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @NotNull
    private CellTable<Server> createTable(@NotNull TableResources tableResources) {
        CellTable<Server> table = new CellTable<>(0, tableResources);
        table.setLoadingIndicator(null);

        TextColumn<Server> ref = new TextColumn<Server>() {
            @Override
            public String getValue(Server server) {
                return server.getRef();
            }
        };

        TextColumn<Server> exposedPort = new TextColumn<Server>() {
            @Override
            public String getValue(Server server) {
                return server.getPort();
            }
        };

        TextColumn<Server> address = new TextColumn<Server>() {
            @Override
            public String getValue(Server server) {
                return server.getAddress();
            }
        };

        TextColumn<Server> url = new TextColumn<Server>() {
            @Override
            public String getValue(Server server) {
                return server.getUrl();
            }
        };

        table.addColumn(ref, locale.infoServerRef());
        table.addColumn(exposedPort, locale.infoServerPort());
        table.addColumn(address, locale.infoServerAddress());
        table.addColumn(url, locale.infoServerUrl());

        return table;
    }

    /** {@inheritDoc} */
    @Override
    public void setServers(@NotNull List<Server> servers) {
        List<Server> list = new ArrayList<>();
        for (Server server : servers) {
           if (!Constants.TERMINAL_REFERENCE.equals(server.getRef())){//TODO: temporary hide terminal URL  
               list.add(server);
           }
        }
        this.servers.setRowData(list);
    }

}
