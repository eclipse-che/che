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
package org.eclipse.che.ide.ui.dialogs.message;

import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * {@link MessageDialog} implementation.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public class MessageDialogPresenter implements MessageDialog, MessageDialogView.ActionDelegate {

    /** This component view. */
    private final MessageDialogView view;

    /** The callback used on OK. */
    private final ConfirmCallback confirmCallback;

    @AssistedInject
    public MessageDialogPresenter(@NotNull MessageDialogView view,
                                  @NotNull @Assisted("title") String title,
                                  @NotNull @Assisted("message") String message,
                                  @Nullable @Assisted ConfirmCallback confirmCallback) {
        this(view, title, new InlineHTML(message), confirmCallback);
    }

    @AssistedInject
    public MessageDialogPresenter(@NotNull MessageDialogView view,
                                  @NotNull @Assisted String title,
                                  @NotNull @Assisted IsWidget content,
                                  @Nullable @Assisted ConfirmCallback confirmCallback) {
        this(view, title, content, confirmCallback, null);
    }

    @AssistedInject
    public MessageDialogPresenter(@NotNull MessageDialogView view,
                                  @NotNull @Assisted("title") String title,
                                  @NotNull @Assisted IsWidget content,
                                  @Nullable @Assisted ConfirmCallback confirmCallback,
                                  @Nullable @Assisted("confirmButtonText") String confirmButtonText) {
        this.view = view;
        this.view.setContent(content);
        this.view.setTitle(title);
        this.confirmCallback = confirmCallback;
        this.view.setDelegate(this);

        if (content.asWidget() != null) {
            content.asWidget().ensureDebugId("info-window-message");
        }

        if (confirmButtonText != null) {
            view.setConfirmButtonText(confirmButtonText);
        }
    }

    @Override
    public void accepted() {
        this.view.closeDialog();
        if (this.confirmCallback != null) {
            this.confirmCallback.accepted();
        }
    }

    @Override
    public void show() {
        this.view.showDialog();
    }
}
