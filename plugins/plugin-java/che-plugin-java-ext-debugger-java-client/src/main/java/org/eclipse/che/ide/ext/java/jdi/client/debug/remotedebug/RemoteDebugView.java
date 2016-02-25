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

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Provides methods which allow control of remote debugging.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(RemoteDebugViewImpl.class)
public interface RemoteDebugView extends View<RemoteDebugView.ActionDelegate> {

    /** Shows dialog window to connect debugger to remote server */
    void show();

    interface ActionDelegate {
        /**
         * Performs some actions when user clicks on confirm button and input host and port.
         *
         * @param host
         *         host via which we connect to remote server
         * @param port
         *         port via which we connect to remote server
         */
        void onConfirmClicked(@NotNull String host, @Min(1) int port);
    }

    /** Give focus to host field. */
    void setFocusInHostField();

}
