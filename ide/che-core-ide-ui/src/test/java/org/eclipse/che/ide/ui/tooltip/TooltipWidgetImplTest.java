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
package org.eclipse.che.ide.ui.tooltip;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class TooltipWidgetImplTest {

    private static final String SOME_TEXT = "some text";

    @InjectMocks
    private TooltipWidgetImpl tooltip;

    @Test
    public void descriptionShouldBeSet() throws Exception {
        tooltip.setDescription(SOME_TEXT);

        verify(tooltip.description).setText(SOME_TEXT);
    }
}