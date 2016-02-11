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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class CustomCommandTypeTest {

    @Mock
    private MachineResources    machineResources;
    @Mock
    private CustomPagePresenter arbitraryPagePresenter;

    @InjectMocks
    private CustomCommandType arbitraryCommandType;

    @Test
    public void shouldReturnIcon() throws Exception {
        arbitraryCommandType.getIcon();

        verify(machineResources).customCommandTypeSubElementIcon();
    }

    @Test
    public void shouldReturnPages() throws Exception {
        final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages = arbitraryCommandType.getConfigurationPages();

        assertTrue(pages.contains(arbitraryPagePresenter));
    }
}
