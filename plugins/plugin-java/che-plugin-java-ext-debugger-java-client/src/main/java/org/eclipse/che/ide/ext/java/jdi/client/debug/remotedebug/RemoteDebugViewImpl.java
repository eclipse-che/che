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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * @author Dmitry Shnurenko
 */
public class RemoteDebugViewImpl extends Composite implements RemoteDebugView {
    interface RemoteDebugImplUiBinder extends UiBinder<Widget, RemoteDebugViewImpl> {
    }

    private static final RemoteDebugImplUiBinder UI_BINDER = GWT.create(RemoteDebugImplUiBinder.class);

    @UiField
    DockLayoutPanel mainPanel;
    @UiField
    TextBox         host;
    @UiField
    TextBox         port;

    @UiField(provided = true)
    final JavaRuntimeLocalizationConstant locale;
    @UiField(provided = true)
    final JavaRuntimeResources            resources;

    private ActionDelegate delegate;

    private final ConfirmDialog dialog;

    @Inject
    public RemoteDebugViewImpl(final JavaRuntimeLocalizationConstant locale,
                               JavaRuntimeResources resources,
                               DialogFactory dialogFactory,
                               final NotificationManager notificationManager) {
        this.locale = locale;
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                try {
                    delegate.onConfirmClicked(host.getText(), Integer.parseInt(port.getText()));
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

        this.dialog = dialogFactory.createConfirmDialog(locale.connectToRemote(), this, confirmCallback, cancelCallback);
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
    public void setFocusInHostField() {
        host.setFocus(true);
    }

}
