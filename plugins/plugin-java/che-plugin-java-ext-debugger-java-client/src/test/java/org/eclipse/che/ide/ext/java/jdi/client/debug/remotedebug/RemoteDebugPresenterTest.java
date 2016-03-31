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

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteDebugPresenterTest {

    @Mock
    private RemoteDebugView                 view;
    @Mock
    private DebuggerManager                 debuggerManager;
    @Mock
    private AppContext                      appContext;
    @Mock
    private MachineServiceClient            machineServiceClient;
    @Mock
    private EntityFactory                   entityFactory;
    @Mock
    private DtoFactory                      dtoFactory;
    @Mock
    private DialogFactory                   dialogFactory;
    @Mock
    private JavaRuntimeLocalizationConstant localizationConstant;

    @InjectMocks
    private RemoteDebugPresenter presenter;

    @Test
    public void connectToRemoteDebuggerShouldBeDone() throws Exception {
        presenter.onConfirmClicked("host", 8000);
    }
}
