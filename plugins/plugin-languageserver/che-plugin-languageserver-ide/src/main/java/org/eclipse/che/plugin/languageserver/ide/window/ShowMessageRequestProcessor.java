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

package org.eclipse.che.plugin.languageserver.ide.window;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.plugin.languageserver.ide.window.dialog.MessageDialogPresenter;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 ** A processor for incoming <code>window/showMessageRequest</code> requests sent
 *  by a language server.
 */
@Singleton
public class ShowMessageRequestProcessor {

    private final Provider<MessageDialogPresenter> provider;

    @Inject
    public ShowMessageRequestProcessor(Provider<MessageDialogPresenter> provider) {
        this.provider = provider;
    }

    public JsonRpcPromise<MessageActionItem> processNotificationRequest(ShowMessageRequestParams params) {
        JsonRpcPromise<MessageActionItem> result = new JsonRpcPromise<>();

        MessageDialogPresenter dialogPresenter = provider.get();
        dialogPresenter.show(params.getMessage(), params.getType().toString(), params.getActions(), actionItem -> {
            result.getSuccessConsumer().ifPresent(consumer -> consumer.accept("ws-agent", actionItem));
        });

        return result;
    }
}
