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
package org.eclipse.che.ide.ext.java.jdi.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug.RemoteDebugPresenter;

import javax.validation.constraints.NotNull;

/**
 * Action which allows connect debugger to remote server.
 *
 * @author Dmitry Shnurenko
 * @author Anatoliy Bazko
 */
@Singleton
public class RemoteDebugAction extends Action {

    private final RemoteDebugPresenter presenter;

    @Inject
    public RemoteDebugAction(RemoteDebugPresenter presenter,
                             JavaRuntimeLocalizationConstant locale,
                             JavaRuntimeResources resources) {
        super(locale.connectToRemote(), locale.connectToRemoteDescription(), null, resources.debug());

        this.presenter = presenter;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@NotNull ActionEvent actionEvent) {
        presenter.showDialog();
    }
}
