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
package org.eclipse.che.ide.ui.dialogs.message;

import org.eclipse.che.ide.ui.dialogs.BaseTest;
import com.google.gwt.user.client.Element;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link MessageDialogViewImpl} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class MessageDialogViewTest extends BaseTest {
    @Mock
    private MessageDialogView.ActionDelegate actionDelegate;
    @Mock
    private MessageDialogFooter              footer;
    private MessageDialogViewImpl            view;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        when(footer.getElement()).thenReturn(mock(Element.class));
        view = new MessageDialogViewImpl(footer);
    }

    @Test
    public void shouldSetConfirmButtonText() throws Exception {
        view.setConfirmButtonText(CONFIRM_BUTTON_TEXT);

        verify(footer).setConfirmButtonText(eq(CONFIRM_BUTTON_TEXT));
    }

    @Test
    public void shouldSetDelegateOnFooter() throws Exception {
        view.setDelegate(actionDelegate);

        verify(footer).setDelegate(eq(actionDelegate));
    }

    @Test
    public void shouldCallAcceptedOnEnterClicked() throws Exception {
        view.setDelegate(actionDelegate);
        view.onEnterClicked();

        verify(actionDelegate).accepted();
    }
}
