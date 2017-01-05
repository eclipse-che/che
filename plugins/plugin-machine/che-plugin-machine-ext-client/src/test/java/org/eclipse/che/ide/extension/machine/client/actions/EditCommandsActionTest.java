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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class EditCommandsActionTest {

    @Mock
    private MachineLocalizationConstant localizationConstants;
    @Mock
    private MachineResources            resources;
    @Mock
    private EditCommandsPresenter       editCommandsPresenter;
    @Mock
    private ActionEvent                 event;
    @Mock
    private EventBus                    eventBus;

    @InjectMocks
    private EditCommandsAction action;

    @Test
    public void constructorShouldBeVerified() throws Exception{
        verify(localizationConstants).editCommandsControlTitle();
        verify(localizationConstants).editCommandsControlDescription();
//        verify(resources).recipe(); //Temporary commented due to new icon will be provided
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        action.actionPerformed(event);

        verify(editCommandsPresenter).show();
    }
}
