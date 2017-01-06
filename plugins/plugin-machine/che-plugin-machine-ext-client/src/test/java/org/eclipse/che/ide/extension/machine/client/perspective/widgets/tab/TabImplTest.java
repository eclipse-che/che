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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView.TabSelectHandler;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class TabImplTest {

    @Mock
    private TabHeader        header;
    @Mock
    private TabPresenter     content;
    @Mock
    private TabSelectHandler handler;

    private TabImpl tab;

    @Before
    public void setUp() {
        tab = new TabImpl(header, content, handler);
    }

    @Test
    public void headerShouldBeReturned() {
        TabHeader testHeader = tab.getHeader();

        assertThat(testHeader, sameInstance(header));
    }

    @Test
    public void contentShouldBeReturned() {
        TabPresenter testContent = tab.getContent();

        assertThat(testContent, sameInstance(content));
    }

    @Test
    public void handlerShouldBePerformed() {
        tab.performHandler();

        verify(handler).onTabSelected();
    }

    @Test
    public void handlerShouldNotBeCalledWhenItIsNull() {
        Tab tab = new TabImpl(header, content, null);

        tab.performHandler();

        verify(handler, never()).onTabSelected();
    }

}