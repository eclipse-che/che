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
package org.eclipse.che.ide.extension.machine.client.actions;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.machine.create.CreateMachinePresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateMachineActionTest {

    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private CreateMachinePresenter      createMachinePresenter;
    @Mock
    private AnalyticsEventLogger        eventLogger;
    @Mock
    private ActionEvent                 event;

    @InjectMocks
    private CreateMachineAction action;

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).machineCreateTitle();
        verify(locale).machineCreateDescription();
    }

    @Test
    public void actionShouldBePerformed() {
        action.actionPerformed(event);

        verify(createMachinePresenter).showDialog();
    }
}