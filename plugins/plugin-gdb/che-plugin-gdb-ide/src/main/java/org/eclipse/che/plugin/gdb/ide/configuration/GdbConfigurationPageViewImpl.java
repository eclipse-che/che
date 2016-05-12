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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The implementation of {@link GdbConfigurationPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GdbConfigurationPageViewImpl implements GdbConfigurationPageView {

    private static final GDBConfigurationPageViewImplUiBinder UI_BINDER = GWT.create(GDBConfigurationPageViewImplUiBinder.class);

    private final FlowPanel rootElement;

    @UiField
    TextBox host;
    @UiField
    TextBox port;
    @UiField
    TextBox binaryPath;

    private ActionDelegate delegate;

    public GdbConfigurationPageViewImpl() {
        rootElement = UI_BINDER.createAndBindUi(this);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public String getHost() {
        return host.getValue();
    }

    @Override
    public void setHost(String host) {
        this.host.setValue(host);
    }

    @Override
    public int getPort() {
        return Integer.valueOf(port.getValue());
    }

    @Override
    public void setPort(int port) {
        this.port.setValue(String.valueOf(port));
    }

    @Override
    public String getBinaryPath() {
        return binaryPath.getValue();
    }

    @Override
    public void setBinaryPath(String path) {
        this.binaryPath.setValue(path);
    }

    @UiHandler({"host"})
    void onHostKeyUp(KeyUpEvent event) {
        delegate.onHostChanged();
    }

    @UiHandler({"port"})
    void onPortKeyUp(KeyUpEvent event) {
        delegate.onPortChanged();
    }

    @UiHandler({"binaryPath"})
    void onBinaryPathKeyUp(KeyUpEvent event) {
        delegate.onBinaryPathChanged();
    }

    interface GDBConfigurationPageViewImplUiBinder extends UiBinder<FlowPanel, GdbConfigurationPageViewImpl> {
    }
}
