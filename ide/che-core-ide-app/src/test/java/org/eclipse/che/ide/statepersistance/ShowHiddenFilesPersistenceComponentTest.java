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
package org.eclipse.che.ide.statepersistance;

import org.eclipse.che.ide.actions.ShowHiddenFilesAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.api.statepersistance.dto.ActionDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link ShowHiddenFilesPersistenceComponent} functionality.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ShowHiddenFilesPersistenceComponentTest {

    private static final String SHOW_HIDDEN_FILES_ACTION_ID = "openFile";

    @Mock
    private ActionManager actionManager;

    @Mock
    private ShowHiddenFilesAction showHiddenFilesAction;

    @Mock
    private DtoFactory dtoFactory;

    @Mock
    private ProjectExplorerPresenter projectExplorerPresenter;

    @InjectMocks
    private ShowHiddenFilesPersistenceComponent component;

    @Before
    public void setUp() {
        when(actionManager.getId(eq(showHiddenFilesAction))).thenReturn(SHOW_HIDDEN_FILES_ACTION_ID);

        ActionDescriptor actionDescriptor = mock(ActionDescriptor.class);
        when(actionDescriptor.withId(anyString())).thenReturn(actionDescriptor);
        when(actionDescriptor.withParameters(anyMapOf(String.class, String.class))).thenReturn(actionDescriptor);
        when(dtoFactory.createDto(eq(ActionDescriptor.class))).thenReturn(actionDescriptor);
    }

    @Test
    public void shouldReturnActions() {
        List<ActionDescriptor> actionDescriptors = component.getActions();

        verify(projectExplorerPresenter).isShowHiddenFiles();
        assertEquals(1, actionDescriptors.size());
    }
}
