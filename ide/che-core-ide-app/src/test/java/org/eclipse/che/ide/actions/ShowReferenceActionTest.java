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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.reference.ShowReferencePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ShowReferenceActionTest {

    @Mock
    private ShowReferencePresenter   showReferencePresenter;
    @Mock
    private SelectionAgent           selectionAgent;
    @Mock
    private CoreLocalizationConstant locale;

    @Mock
    private ActionEvent     event;
    @Mock
    private Selection       selection;
    @Mock
    private Presentation    presentation;
    @Mock
    private HasStorablePath hasStorablePathNode;

    @InjectMocks
    private ShowReferenceAction action;

    @Before
    public void setUp() {
        //noinspection unchecked
        when(selectionAgent.getSelection()).thenReturn(selection);
        when(event.getPresentation()).thenReturn(presentation);
        when(selection.getHeadElement()).thenReturn(hasStorablePathNode);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).showReference();
    }

    @Test
    public void presentationShouldNotBeUpdatedWhenSelectionIsEmpty() {
        when(selectionAgent.getSelection()).thenReturn(null);

        action.update(event);

        verify(selection, never()).getHeadElement();
    }

    @Test
    public void presentationShouldNotBeVisibleWhenSelectedElementIsNull() {
        when(selection.getHeadElement()).thenReturn(null);

        action.update(event);

        verify(presentation).setEnabledAndVisible(false);
    }

    @Test
    public void presentationShouldBeVisibleWhenSelectedElementIsHasStorablePathNode() {
        action.update(event);

        verify(presentation).setEnabledAndVisible(true);
    }

    @Test
    public void actionShouldBePerformed() {
        action.update(event);

        verify(presentation).setEnabledAndVisible(true);

        action.actionPerformed(event);

        verify(showReferencePresenter).show(hasStorablePathNode);
    }
}