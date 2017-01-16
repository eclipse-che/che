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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.MachineResources.Css;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class TabHeaderImplTest {

    private static final String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private MachineResources resources;

    //additional mocks
    @Mock
    private ClickEvent     event;
    @Mock
    private ActionDelegate delegate;
    @Mock
    private Css            css;

    private TabHeaderImpl header;

    @Before
    public void setUp() {
        when(resources.getCss()).thenReturn(css);

        header = new TabHeaderImpl(resources, SOME_TEXT);
        header.setDelegate(delegate);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(header.tabName).setText(SOME_TEXT);
    }

    @Test
    public void onTabShouldBeClicked() {
        when(header.tabName.getText()).thenReturn(SOME_TEXT);

        header.onClick(event);

        verify(header.tabName).getText();
        verify(delegate).onTabClicked(SOME_TEXT);
    }

    @Test
    public void nameShouldBeReturned() {
        String name = header.getName();

        assertThat(name, equalTo(SOME_TEXT));
    }

    @Test
    public void tabShouldBeEnabled() {
        header.setEnable();

        verify(css).disableTab();
        verify(css).activeTab();
        verify(css).activeTabText();
    }

    @Test
    public void tabShouldBeDisable() {
        header.setDisable();

        verify(css).disableTab();
        verify(css).activeTab();
        verify(css).activeTabText();
    }
}