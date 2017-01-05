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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class TabContainerViewImplTest {

    //additional mocks
    @Mock
    private TabHeader    header;
    @Mock
    private TabPresenter content;
    @Mock
    private IsWidget     widget;

    @InjectMocks
    private TabContainerViewImpl view;

    @Test
    public void headerShouldBeAdded() {
        view.addHeader(header);

        verify(view.tabs).add(header);
    }

    @Test
    public void contentShouldBeAdded() {
        when(content.getView()).thenReturn(widget);

        view.addContent(content);

        verify(view.content).add(widget);
    }
}