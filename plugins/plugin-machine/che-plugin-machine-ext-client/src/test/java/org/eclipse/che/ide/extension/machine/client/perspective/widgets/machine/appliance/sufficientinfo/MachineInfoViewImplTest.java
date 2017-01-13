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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class MachineInfoViewImplTest {

    private final static String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private MachineLocalizationConstant locale;

    //additional mocks
    @Mock
    private MachineEntity machine;

    @InjectMocks
    private MachineInfoViewImpl view;

    @Test
    public void infoShouldBeUpdated() {
        when(machine.getDisplayName()).thenReturn(SOME_TEXT);
        when(machine.getId()).thenReturn(SOME_TEXT);
        when(machine.getStatus()).thenReturn(MachineStatus.CREATING);
        when(machine.getType()).thenReturn(SOME_TEXT);
        when(machine.isDev()).thenReturn(true);

        view.updateInfo(machine);

        verify(machine).getDisplayName();
        verify(machine).getId();
        verify(machine).getStatus();
        verify(machine).getType();
        verify(machine).isDev();

        verify(view.name).setText(SOME_TEXT);
        verify(view.machineId).setText(SOME_TEXT);
        verify(view.status).setText("CREATING");
        verify(view.type).setText(SOME_TEXT);
        verify(view.dev).setText("true");
    }

    @Test
    public void ownerShouldBeSet() {
        view.setOwner(SOME_TEXT);

        verify(view.owner).setText(SOME_TEXT);
    }

    @Test
    public void workspaceShouldBeSet() {
        view.setWorkspaceName(SOME_TEXT);

        verify(view.workspaceId).setText(SOME_TEXT);
    }
}
