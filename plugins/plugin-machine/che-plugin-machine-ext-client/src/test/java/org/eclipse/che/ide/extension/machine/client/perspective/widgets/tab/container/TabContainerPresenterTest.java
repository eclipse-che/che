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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class TabContainerPresenterTest {

    private final static String SOME_TEXT = "someText";
    //constructor mocks
    @Mock
    private TabContainerView view;

    //additional mocks
    @Mock
    private Tab          tab;
    @Mock
    private TabHeader    header;
    @Mock
    private TabPresenter content;
    @Mock
    private Tab          tab1;
    @Mock
    private TabHeader    header1;
    @Mock
    private TabPresenter content1;

    @InjectMocks
    private TabContainerPresenter presenter;

    @Before
    public void setUp() {
        when(tab.getHeader()).thenReturn(header);
        when(tab.getContent()).thenReturn(content);

        when(tab1.getHeader()).thenReturn(header1);
        when(tab1.getContent()).thenReturn(content1);
    }

    @Test
    public void viewShouldBeReturned() {
        TabContainerView testView = presenter.getView();

        assertThat(testView, sameInstance(view));
    }

    @Test
    public void tabShouldBeAdded() {
        presenter.addTab(tab);

        verify(tab).getHeader();
        verify(header).setDelegate(presenter);

        verify(tab).getContent();

        verify(view).addHeader(header);
        verify(view).addContent(content);
    }

    @Test
    public void onTabShouldBeClicked() {
        when(header.getName()).thenReturn(SOME_TEXT);

        presenter.addTab(tab);
        presenter.addTab(tab1);

        presenter.onTabClicked(SOME_TEXT);

        verifyDisplayingTab();
    }

    private void verifyDisplayingTab() {
        verify(header).setEnable();
        verify(header1).setDisable();

        verify(content).setVisible(true);
        verify(content1).setVisible(false);
    }

    @Test
    public void tabShouldBeShown() {
        when(header.getName()).thenReturn(SOME_TEXT);

        presenter.addTab(tab);
        presenter.addTab(tab1);

        presenter.showTab(SOME_TEXT);

        verifyDisplayingTab();
    }

}