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
package org.eclipse.che.plugin.gdb.ide.configuration;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;

import java.util.Map;

/**
 * Page allows to edit GDB debug configuration.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GdbConfigurationPagePresenter implements GdbConfigurationPageView.ActionDelegate, DebugConfigurationPage<DebugConfiguration> {

    public static final String BIN_PATH_CONNECTION_PROPERTY = "BINARY";

    private final GdbConfigurationPageView view;

    private DebugConfiguration editedConfiguration;
    private String             originHost;
    private int                originPort;
    private String             originBinaryPath;
    private DirtyStateListener listener;

    @Inject
    public GdbConfigurationPagePresenter(GdbConfigurationPageView view) {
        this.view = view;
        view.setDelegate(this);
    }

    private static String getBinaryPath(DebugConfiguration editedConfiguration) {
        Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();
        Optional<String> binPathOptional = Optional.fromNullable(connectionProperties.get(BIN_PATH_CONNECTION_PROPERTY));
        return binPathOptional.or("");
    }

    @Override
    public void resetFrom(DebugConfiguration configuration) {
        editedConfiguration = configuration;

        originHost = configuration.getHost();
        originPort = configuration.getPort();
        originBinaryPath = getBinaryPath(configuration);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);

        view.setHost(editedConfiguration.getHost());
        view.setPort(editedConfiguration.getPort());
        view.setBinaryPath(getBinaryPath(editedConfiguration));
    }

    @Override
    public boolean isDirty() {
        return !originHost.equals(editedConfiguration.getHost())
               || originPort != editedConfiguration.getPort()
               || !originBinaryPath.equals(getBinaryPath(editedConfiguration));
    }

    @Override
    public void setDirtyStateListener(DirtyStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onHostChanged() {
        editedConfiguration.setHost(view.getHost());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onPortChanged() {
        editedConfiguration.setPort(view.getPort());
        listener.onDirtyStateChanged();
    }

    @Override
    public void onBinaryPathChanged() {
        final Map<String, String> connectionProperties = editedConfiguration.getConnectionProperties();
        connectionProperties.put(BIN_PATH_CONNECTION_PROPERTY, view.getBinaryPath());

        editedConfiguration.setConnectionProperties(connectionProperties);
        listener.onDirtyStateChanged();
    }
}
