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
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.search.FullTextSearchPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link FullTextSearchAction}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FullTextSearchActionTest {
    @Mock
    private ActionEvent             actionEvent;
    @Mock
    private FullTextSearchPresenter fullTextSearchPresenter;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private CoreLocalizationConstant locale;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private Resources                resources;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private AppContext               appContext;

    @InjectMocks
    FullTextSearchAction fullTextSearchAction;

    @Test
    public void actionShouldBePerformed() {
        fullTextSearchAction.actionPerformed(actionEvent);

        verify(fullTextSearchPresenter).showDialog();
    }

    @Test
    public void actionShouldBeEnabled() {
        Presentation presentation = Mockito.mock(Presentation.class);
        when(actionEvent.getPresentation()).thenReturn(presentation);

        fullTextSearchAction.updateInPerspective(actionEvent);

        verify(presentation).setEnabled(true);
    }

    @Test
    public void actionShouldBeDisabled() {
        Presentation presentation = Mockito.mock(Presentation.class);
        when(actionEvent.getPresentation()).thenReturn(presentation);
        when(appContext.getCurrentProject()).thenReturn(null);

        fullTextSearchAction.updateInPerspective(actionEvent);

        verify(presentation).setEnabled(false);
    }

}
