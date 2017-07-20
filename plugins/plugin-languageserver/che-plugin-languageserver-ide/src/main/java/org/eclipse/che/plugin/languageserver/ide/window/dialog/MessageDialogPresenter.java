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
package org.eclipse.che.plugin.languageserver.ide.window.dialog;

import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.lsp4j.MessageActionItem;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the message dialog UI
 *
 */
public class MessageDialogPresenter implements MessageDialogView.ActionDelegate {

    /** This component view. */
    private final MessageDialogView view;
    private Consumer<MessageActionItem> callback;


    @Inject
    public MessageDialogPresenter(final @NotNull MessageDialogView view) {
        this.view = view;

        this.view.setDelegate(this);
    }

//    @Override
//    public void cancelled() {
//        this.view.closeDialog();
//        if (this.cancelCallback != null) {
//            this.cancelCallback.cancelled();
//        }
//    }
//
//    @Override
//    public void accepted() {
//        this.view.closeDialog();
//        if (this.confirmCallback != null) {
//            this.confirmCallback.accepted();
//        }
//    }

    public void show(String content, String title, List<MessageActionItem> actions, Consumer<MessageActionItem> callback) {
        this.callback = callback;
        view.setContent(content);
        view.setTitle(title);
        view.setActions(actions);
        view.showDialog();
    }

    @Override
    public void onAction(MessageActionItem actionItem) {
        view.closeDialog();
        callback.accept(actionItem);
    }

    @Override
    public void onEnterClicked() {
//        if (view.isOkButtonInFocus()) {
//            accepted();
//            return;
//        }
//
//        if (view.isCancelButtonInFocus()) {
//            cancelled();
//        }
    }
}
