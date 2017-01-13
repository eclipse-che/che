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
package org.eclipse.che.ide.ui.dialogs.confirm;

import org.eclipse.che.ide.ui.dialogs.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialogView.ActionDelegate;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link ConfirmDialogViewImpl} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class ConfirmDialogViewTest extends BaseTest {
    @Mock
    private ActionDelegate        actionDelegate;
    @Mock
    private ConfirmDialogFooter   footer;
    private ConfirmDialogViewImpl view;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        view = new ConfirmDialogViewImpl(footer);
    }

    @Test
    public void shouldSetDelegateOnFooter() throws Exception {
        view.setDelegate(actionDelegate);

        verify(footer).setDelegate(eq(actionDelegate));
    }

}
