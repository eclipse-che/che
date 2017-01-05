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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button.EditorButtonWidgetImpl.Background.GREY;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class EditorButtonWidgetImplTest {
    private static final String TITLE = "Title";
    private static final String TEXT  = "text";

    @Mock
    private MachineLocalizationConstant       locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private MachineResources                  resources;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private MachineResources.Css              css;
    @Mock
    private ClickEvent                        event;
    @Mock
    private EditorButtonWidget.ActionDelegate delegate;

    EditorButtonWidgetImpl button;

    @Before
    public void setUp() throws Exception {
        when(resources.getCss()).thenReturn(css);
        when(css.opacityButton()).thenReturn(TEXT);
        button = new EditorButtonWidgetImpl(locale, resources, TITLE, GREY);

        button.setDelegate(delegate);
    }

    @Test
    public void buttonShouldBeInitialized() throws Exception {
        verify(button.button).setText(TITLE);
        verify(button.button).removeStyleName(TEXT);
    }

    @Test
    public void buttonShouldBeClickIfItIsEnable() throws Exception {
        button.onClick(event);

        verify(delegate).onButtonClicked();
    }

    @Test
    public void buttonShouldNotBeClickIfItIsDisable() throws Exception {
        button.setEnable(false);

        verify(button.button).addStyleName(TEXT);

        button.onClick(event);

        verify(delegate, never()).onButtonClicked();
    }
}
