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
package org.eclipse.che.ide.ui.dialogs.input;

import org.eclipse.che.ide.ui.UILocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Testing {@link InputDialogViewImpl} functionality.
 *
 * @author Artem Zatsarynnyi
 */
public class InputDialogViewTest extends BaseTest {
    @Mock
    private InputDialogView.ActionDelegate actionDelegate;
    @Mock
    private UILocalizationConstant         uiLocalizationConstant;
    @Mock
    private InputDialogFooter              footer;
    private InputDialogViewImpl            view;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        view = new InputDialogViewImpl(footer);
    }

    @Test
    public void shouldSetDelegateOnFooter() throws Exception {
        view.setDelegate(actionDelegate);

        verify(footer).setDelegate(eq(actionDelegate));
    }
}
