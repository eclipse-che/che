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
package org.eclipse.che.ide.ui.dropdown;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class SimpleListElementActionTest {
    static private final String ID   = "id";
    static private final String NAME = "text";

    @Mock
    private DropDownHeaderWidget header;
    @Mock
    private ActionEvent          actionEvent;
    @Mock
    private Presentation         presentation;

    private SimpleListElementAction action;

    @Before
    public void setUp() {
        when(actionEvent.getPresentation()).thenReturn(presentation);

        action = new SimpleListElementAction(ID, NAME, header);
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        action.actionPerformed(actionEvent);

        verify(header).selectElement(ID);
    }

    @Test
    public void nameShouldBeReturned() throws Exception {
        assertThat(action.getName(), equalTo(NAME));
    }

    @Test
    public void idShouldBeReturned() throws Exception {
        assertThat(action.getId(), equalTo(ID));
    }
}
