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
package org.eclipse.che.ide.search.presentation;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link FindResultPresenter}.
 *
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class FindResultPresenterTest {
    @Mock
    private CoreLocalizationConstant localizationConstant;
    @Mock
    private FindResultView           view;
    @Mock
    private WorkspaceAgent           workspaceAgent;
    @Mock
    private Resources                resources;
    @Mock
    private EventBus                 eventBus;

    @InjectMocks
    FindResultPresenter findResultPresenter;

    @Test
    public void titleShouldBeReturned() {
        findResultPresenter.getTitle();

        verify(localizationConstant).actionFullTextSearch();
    }

    @Test
    public void viewShouldBeReturned() {
        assertEquals(findResultPresenter.getView(), view);
    }

    @Test
    public void imageShouldBeReturned() {
        findResultPresenter.getTitleImage();

        verify(resources).find();
    }

    @Test
    public void methodGoShouldBePerformed() {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        findResultPresenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void responseShouldBeHandled() throws Exception {
        findResultPresenter.handleResponse(Matchers.<Resource[]>any(), anyString());

        verify(workspaceAgent).openPart(findResultPresenter, PartStackType.INFORMATION);
        verify(workspaceAgent).setActivePart(findResultPresenter);
        verify(view).showResults(Matchers.<Resource[]>any(), anyString());
    }
}
