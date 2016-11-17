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

import com.google.inject.Provider;

import org.eclipse.che.ide.actions.OpenFileAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.api.statepersistance.dto.ActionDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link OpenedFilesPersistenceComponent} functionality.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenedFilesPersistenceComponentTest {

    private static final String OPEN_FILE_ACTION_ID = "openFile";
    private static final String FILE1_PATH          = "/project/file1";
    private static final String FILE2_PATH          = "/project/file2";

    @Mock
    private Provider<EditorAgent> editorAgentProvider;

    @Mock
    private EditorAgent editorAgent;

    @Mock
    private ActionManager actionManager;

    @Mock
    private OpenFileAction openFileAction;

    @Mock
    private DtoFactory dtoFactory;

    @Mock
    private EditorPartPresenter editorPartPresenter1;

    @Mock
    private EditorPartPresenter editorPartPresenter2;

    @Mock
    private EditorInput editorInput1;

    @Mock
    private EditorInput editorInput2;

    @Mock
    private File virtualFile1;

    @Mock
    private File virtualFile2;

    @InjectMocks
    private OpenedFilesPersistenceComponent component;

    @Before
    public void setUp() {
        when(editorAgentProvider.get()).thenReturn(editorAgent);
        when(actionManager.getId(eq(openFileAction))).thenReturn(OPEN_FILE_ACTION_ID);

        ActionDescriptor actionDescriptor = mock(ActionDescriptor.class);
        when(actionDescriptor.withId(anyString())).thenReturn(actionDescriptor);
        when(actionDescriptor.withParameters(anyMapOf(String.class, String.class))).thenReturn(actionDescriptor);
        when(dtoFactory.createDto(eq(ActionDescriptor.class))).thenReturn(actionDescriptor);
    }

    @Test
    public void shouldReturnActionsForReopeningFiles() {
        configureOpenedEditors();

        List<ActionDescriptor> actionDescriptors = component.getActions();

        assertEquals(2, actionDescriptors.size());
    }

    private void configureOpenedEditors() {
        when(virtualFile1.getLocation()).thenReturn(Path.valueOf(FILE1_PATH));
        when(virtualFile2.getLocation()).thenReturn(Path.valueOf(FILE2_PATH));
        when(editorInput1.getFile()).thenReturn(virtualFile1);
        when(editorInput2.getFile()).thenReturn(virtualFile2);
        when(editorPartPresenter1.getEditorInput()).thenReturn(editorInput1);
        when(editorPartPresenter2.getEditorInput()).thenReturn(editorInput2);

        List<EditorPartPresenter> openedEditors = new ArrayList<>();
        openedEditors.add(editorPartPresenter1);
        openedEditors.add(editorPartPresenter1);

        when(editorAgent.getOpenedEditors()).thenReturn(openedEditors);
    }
}
