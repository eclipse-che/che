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
package org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.listbox.CustomComboBox;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * @author Dmitry Shnurenko
 * @author Anatoliy Bazko
 */
public class RemoteDebugViewImpl extends Composite implements RemoteDebugView {
    interface RemoteDebugImplUiBinder extends UiBinder<Widget, RemoteDebugViewImpl> {
    }

    private static final RemoteDebugImplUiBinder UI_BINDER = GWT.create(RemoteDebugImplUiBinder.class);

    @UiField
    CheckBox        devHost;
    @UiField
    DockLayoutPanel mainPanel;
    @UiField
    TextBox         host;
    @UiField
    CustomComboBox  port;

    @UiField(provided = true)
    final JavaRuntimeLocalizationConstant locale;
    @UiField(provided = true)
    final JavaRuntimeResources            resources;

    private ActionDelegate             delegate;
    private List<Pair<String, String>> ports;

    private final ConfirmDialog dialog;

    @Inject
    public RemoteDebugViewImpl(final JavaRuntimeLocalizationConstant locale,
                               final JavaRuntimeResources resources,
                               final DialogFactory dialogFactory,
                               final NotificationManager notificationManager) {
        this.locale = locale;
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                try {
                    delegate.onConfirmClicked(host.getValue(), Integer.parseInt(port.getValue()));
                } catch (NumberFormatException exception) {
                    dialog.show();
                    notificationManager.notify(locale.failedToConnectToRemoteDebuggerTitle(),
                                               locale.failedToConnectToRemoteDebuggerWrongPort(port.getValue()), FAIL, true);
                }
            }
        };

        CancelCallback cancelCallback = new CancelCallback() {
            @Override
            public void cancelled() {
                //do nothing
            }
        };

        bind();
        this.dialog = dialogFactory.createConfirmDialog(locale.connectToRemote(), this, confirmCallback, cancelCallback);
        this.devHost.setValue(true);
        this.ports = new ArrayList<Pair<String, String>>();
        updateDialog();
    }

    /** Bind handlers. */
    private void bind() {
        devHost.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateDialog();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(@NotNull ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void setPortsList(@NotNull List<Pair<String, String>> ports) {
        this.ports = ports;
        updatePortsList();
    }

    private void updatePortsList() {
        port.clear();
        for (Pair<String, String> entry : ports) {
            port.addItem(entry.first, entry.second);
        }
    }

    private void updateDialog() {
        boolean connectToDevMachine = devHost.getValue();
        host.setEnabled(!connectToDevMachine);

        if (connectToDevMachine) {
            host.setValue("localhost");
            updatePortsList();
            port.setFocus(true);
        } else {
            host.selectAll();
            host.setFocus(true);
            port.clear();
        }
    }

    @Override
    public void focus() {
        if (host.isEnabled()) {
            host.setFocus(true);
        } else {
            port.setFocus(true);
        }
    }

}
