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
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.eclipse.che.ide.actions.LinkWithEditorAction.LINK_WITH_EDITOR;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class LinkWithEditorActionTest {
    private final static String TEXT = "to be or not to be";

    @Mock
    private CoreLocalizationConstant localizationConstant;
    @Mock
    private Provider<EditorAgent>    editorAgentProvider;
    @Mock
    private EventBus                 eventBus;
    @Mock
    private PreferencesManager       preferencesManager;
    @Mock
    private EditorAgent              editorAgent;

    @Mock
    private EditorPartPresenter editorPartPresenter;
    @Mock
    private EditorInput         editorInput;
    @Mock
    private VirtualFile         virtualFile;

    @InjectMocks
    private LinkWithEditorAction action;

    @Before
    public void setUp() throws Exception {
        when(localizationConstant.actionLinkWithEditor()).thenReturn(TEXT);
        when(editorAgentProvider.get()).thenReturn(editorAgent);

        when(editorAgent.getActiveEditor()).thenReturn(editorPartPresenter);
        when(editorPartPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getLocation()).thenReturn(Path.valueOf(TEXT));
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        when(preferencesManager.getValue(eq(LINK_WITH_EDITOR))).thenReturn(null);

        action.actionPerformed(null);

        verify(preferencesManager).setValue("linkWithEditor", Boolean.toString(true));
        verify(eventBus).fireEvent((Event<?>)anyObject());
    }

    @Test
    public void actionShouldNotBePerformedIfActiveEditorIsNull() throws Exception {
        when(preferencesManager.getValue(eq(LINK_WITH_EDITOR))).thenReturn(null);
        when(editorAgent.getActiveEditor()).thenReturn(null);

        action.actionPerformed(null);

        verify(eventBus, never()).fireEvent((Event<?>)anyObject());
    }

    @Test
    public void actionShouldNotBePerformedIfEditorInputIsNull() throws Exception {
        when(preferencesManager.getValue(eq(LINK_WITH_EDITOR))).thenReturn(null);
        when(editorPartPresenter.getEditorInput()).thenReturn(null);

        action.actionPerformed(null);

        verify(eventBus, never()).fireEvent((Event<?>)anyObject());
    }

    @Test
    public void revealEventShouldNotBeFiredIfPreferenceValueIsFalse() throws Exception {
        when(preferencesManager.getValue(eq(LINK_WITH_EDITOR))).thenReturn(Boolean.toString(true));

        action.actionPerformed(null);

        verify(eventBus, never()).fireEvent((Event<?>)anyObject());
    }

    @Test
    public void revealEventShouldBeFiredIfPreferenceValueIsTrue() throws Exception {
        when(preferencesManager.getValue(eq(LINK_WITH_EDITOR))).thenReturn(Boolean.toString(false));

        action.actionPerformed(null);

        verify(eventBus).fireEvent((Event<?>)anyObject());
    }
}
