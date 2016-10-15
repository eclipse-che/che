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
package org.eclipse.che.plugin.maven.client.command;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class MavenCommandTypeTest {

    @Mock
    private MavenResources            mavenResources;
    @Mock
    private MavenCommandPagePresenter mavenCommandPagePresenter;
    @Mock
    private CurrentProjectPathMacro   currentProjectPathMacro;
    @Mock
    private IconRegistry              iconRegistry;

    @InjectMocks
    private MavenCommandType mavenCommandType;

    @Test
    public void shouldReturnPages() throws Exception {
        final Collection<CommandPage> pages = mavenCommandType.getPages();

        assertTrue(pages.contains(mavenCommandPagePresenter));
    }

    @Test
    public void testGettingCommandTemplate() throws Exception {
        mavenCommandType.getCommandLineTemplate();

        verify(currentProjectPathMacro).getName();
    }
}
