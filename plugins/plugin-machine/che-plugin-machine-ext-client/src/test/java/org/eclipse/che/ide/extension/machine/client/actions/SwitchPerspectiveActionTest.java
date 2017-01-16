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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.radiobuttongroup.RadioButtonGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class SwitchPerspectiveActionTest {

    @Mock
    private PerspectiveManager          perspectiveManager;
    @Mock
    private MachineResources            resources;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private RadioButtonGroup            radioButtonGroup;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private ActionEvent actionEvent;

    @InjectMocks
    private SwitchPerspectiveAction action;

    @Test
    public void buttonsShouldBeCreatedInGroup() {
        verify(radioButtonGroup).addButton(anyString(), anyString(), any(SVGResource.class), any(ClickHandler.class));
        verify(radioButtonGroup).selectButton(eq(0));
    }
}
