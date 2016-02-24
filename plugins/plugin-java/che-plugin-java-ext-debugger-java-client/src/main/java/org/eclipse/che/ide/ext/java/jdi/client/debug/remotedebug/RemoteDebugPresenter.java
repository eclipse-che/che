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

import com.google.inject.Inject;

import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import com.google.gwt.user.client.Timer;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Contains methods which allows control of remote debugging.
 *
 * @author Dmitry Shnurenko
 */
public class RemoteDebugPresenter implements RemoteDebugView.ActionDelegate {

    private final RemoteDebugView   view;
    private final DebuggerPresenter debuggerPresenter;

    @Inject
    public RemoteDebugPresenter(RemoteDebugView view, DebuggerPresenter debuggerPresenter) {
        this.view = view;
        this.view.setDelegate(this);

        this.debuggerPresenter = debuggerPresenter;
    }

    /** Calls special method on view which shows dialog window. */
    public void showDialog() {
        view.show();
        // TODO fix behaviour. Because of animation/render we cannot set focus without delay
        new Timer() {
            @Override
            public void run() {
                view.setFocusInHostField();
            }
        }.schedule(300);
    }

    /** {@inheritDoc} */
    @Override
    public void onConfirmClicked(@NotNull String host, @Min(1) int port) {
        debuggerPresenter.attachDebugger(host, port);
    }
}
