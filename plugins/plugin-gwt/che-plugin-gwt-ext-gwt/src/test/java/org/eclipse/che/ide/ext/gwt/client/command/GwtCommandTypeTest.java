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
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GwtCommandTypeTest {

    @Mock
    private GwtResources               gwtResources;
    @Mock
    private GwtCommandPagePresenter    gwtCommandPagePresenter;
    @Mock
    private CurrentProjectPathProvider currentProjectPathProvider;
    @Mock
    private DevMachineHostNameProvider devMachineHostNameProvider;
    @Mock
    private IconRegistry iconRegistry;

    @InjectMocks
    private GwtCommandType gwtCommandType;

    @Test
    public void shouldReturnIcon() throws Exception {
        gwtCommandType.getIcon();

        verify(gwtResources, times(2)).gwtCommandType();
    }

    @Test
    public void shouldReturnPages() throws Exception {
        final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages = gwtCommandType.getConfigurationPages();

        assertTrue(pages.contains(gwtCommandPagePresenter));
    }

    @Test
    public void shouldReturnCommandTemplate() throws Exception {
        gwtCommandType.getCommandTemplate();

        verify(currentProjectPathProvider).getKey();
        verify(devMachineHostNameProvider).getKey();
    }
}
