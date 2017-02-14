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
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.reference.ShowReferencePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
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
    private ActionEvent  event;
    @Mock
    private Presentation presentation;
    @Mock
    private AppContext   appContext;
    @Mock
    private Resource     resource;

    @InjectMocks
    private ShowReferenceAction action;

    @Before
    public void setUp() {
        when(event.getPresentation()).thenReturn(presentation);
        when(appContext.getResource()).thenReturn(resource);
        when(appContext.getResources()).thenReturn(new Resource[]{resource});
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).showReference();
    }

    @Test
    public void presentationShouldNotBeVisibleWhenSelectedElementIsNull() {
        when(appContext.getResource()).thenReturn(null);
        when(appContext.getResources()).thenReturn(null);

        action.update(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(false);
    }

    @Test
    public void presentationShouldBeVisibleWhenSelectedElementIsHasStorablePathNode() {
        action.update(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(true);
    }

    @Test
    public void actionShouldBePerformed() {
        action.update(event);

        verify(presentation).setVisible(true);
        verify(presentation).setEnabled(true);

        action.actionPerformed(event);

        verify(showReferencePresenter).show(resource);
    }
}
