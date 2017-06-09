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
package org.eclipse.che.ide.command.editor.page.name;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.command.editor.page.name.NamePageView.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link NamePageViewImpl}. */
@RunWith(GwtMockitoTestRunner.class)
public class NamePageViewImplTest {

    @Mock
    private ActionDelegate actionDelegate;

    @InjectMocks
    private NamePageViewImpl view;

    @Before
    public void setUp() throws Exception {
        view.setDelegate(actionDelegate);
    }

    @Test
    public void shouldSetCommandName() throws Exception {
        String newName = "cmd 1";

        view.setCommandName(newName);

        verify(view.commandName).setValue(eq(newName));
    }

    @Test
    public void shouldCallOnNameChanged() throws Exception {
        String commandName = "cmd name";
        when(view.commandName.getValue()).thenReturn(commandName);

        view.onNameChanged(null);

        verify(actionDelegate).onNameChanged(eq(commandName));
    }

    @Test
    public void shouldCallOnCommandRun() throws Exception {
        view.handleRunButton(null);

        verify(actionDelegate).onCommandRun();
    }
}
