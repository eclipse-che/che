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
package org.eclipse.che.ide.ext.java.testing.core.client.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.java.testing.core.client.TestLocalizationConstant;
import org.eclipse.che.ide.ext.java.testing.core.client.TestResources;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Mirage Abeysekara
 */
@RunWith(GwtMockitoTestRunner.class)
public class TestResultPresenterTest {


    @Mock
    private TestResultView view;
    @Mock
    private WorkspaceAgent workspaceAgent;
    @Mock
    private TestResources resources;
    @Mock
    private EventBus eventBus;
    @Mock
    private TestLocalizationConstant localizationConstant;
    @Mock
    private TestResult testResult;

    @InjectMocks
    TestResultPresenter testResultPresenter;

    @Test
    public void titleShouldBeReturned() {
        testResultPresenter.getTitle();

        verify(localizationConstant).titleTestResultPresenter();
    }

    @Test
    public void titleToolTipShouldBeReturned() {
        testResultPresenter.getTitleToolTip();

        verify(localizationConstant).titleTestResultPresenterToolTip();
    }

    @Test
    public void viewShouldBeReturned() {
        assertEquals(testResultPresenter.getView(), view);
    }

    @Test
    public void imageShouldBeReturned() {
        testResultPresenter.getTitleImage();

        verify(resources).testIcon();
    }

    @Test
    public void methodGoShouldBePerformed() {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);
        testResultPresenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void responseShouldBeHandled() throws Exception {
        testResultPresenter.handleResponse(testResult);

        verify(workspaceAgent).openPart(testResultPresenter, PartStackType.INFORMATION);
        verify(workspaceAgent).setActivePart(testResultPresenter);
        verify(view).showResults(testResult);
    }
}
